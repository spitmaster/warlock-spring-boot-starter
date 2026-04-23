# 01 · 整体架构

## 1. 分层

```
 ┌─────────────────────────────────────────────────┐
 │ 注解层 (annotation)                              │
 │   @Warlock  @WrateLimiter  @WcyclicBarrier      │
 │   @Wsemaphore  @Waiting  @Leasing               │
 └───────────────────┬─────────────────────────────┘
                     │ 注解作为 Pointcut 输入
 ┌───────────────────▼─────────────────────────────┐
 │ 切面层 (aspect + config)                         │
 │   WarlockAutoConfiguration                      │
 │     └ 4 个 AbstractPointcutAdvisor               │
 │        └ WaroundMethodInterceptor (单一拦截器)    │
 └───────────────────┬─────────────────────────────┘
                     │ invoke(MethodInvocation)
 ┌───────────────────▼─────────────────────────────┐
 │ 工厂层 (core.factory)                            │
 │   WaroundFactory                                │
 │     ├ DefaultWlockFactory                       │
 │     ├ DefaultWbarrierFactory                    │
 │     ├ DefaultWmutexFactory                      │
 │     └ DefaultWlimiterFactory                    │
 └───────────────────┬─────────────────────────────┘
                     │ build → Waround
 ┌───────────────────▼─────────────────────────────┐
 │ 实现层 (core.lock / ratelimiter / barrier /     │
 │         semaphore)                               │
 │   Waround 的 8 种具体实现                         │
 └───────────────────┬─────────────────────────────┘
                     │ 依赖
 ┌───────────────────▼─────────────────────────────┐
 │ 基础设施 (handler + util + enums + exceptions)    │
 │   TimeoutHandlerProvider / RedissonProvider     │
 │   SpelExpressionUtil / Pair / MethodNameUtil    │
 └─────────────────────────────────────────────────┘
```

## 2. 核心抽象

### 2.1 `Waround` —— "环绕执行"

```java
public interface Waround {
    Object doAround(MethodInvocation methodInvocation) throws Throwable;
}
```

只有一个方法，语义上等价于 AspectJ 的 `@Around` Advice，但是**把"环绕逻辑"变成了可实例化的对象**。每次方法调用构造一个新 `Waround`，持有该次调用所需的锁信息 / 限流信息等，执行完即抛弃。

> 类似 Command 模式 + 策略模式的混合。

### 2.2 `WaroundFactory` —— "工厂 + 策略选择"

```java
public interface WaroundFactory {
    Waround build(MethodInvocation methodInvocation);
}
```

一个注解一个工厂，负责：
1. 从方法上读取对应注解（`@Warlock` / `@Wsemaphore` / ...）；
2. 校验注解参数（waitTime > 0 等）；
3. 根据 `Scope` + `LockType` 等条件挑选具体的 `Waround` 实现类；
4. 构造 `XxxInfo`（`LockInfo` / `SemaphoreInfo` / ...）作为 `Waround` 的参数。

### 2.3 `AbstractWlock` / `AbstractStandaloneWlock` / `AbstractDistributedWlock`

锁体系用了 **模板方法模式** 抽三层：

```
         AbstractWlock (只声明 getLockInfo)
            /          \
AbstractStandaloneWlock   AbstractDistributedWlock
  │ doAround 模板:           │ doAround 模板:
  │  getLock → tryLock →     │  getRLock → tryLock(wait, lease) →
  │  proceed → unlock →      │  proceed → unlock（如未过期） →
  │  returnLock              │  否则调 leaseTimeoutHandler
  ▼                          ▼
ReentrantWlock / ReadWlock / WriteWlock   /   Distributed* 的三个对应类
```

**`AbstractStandaloneWlock.doAround`** 固化流程：
1. `getLock()` — 从静态 Map 里懒加载锁（计数 +1）
2. `tryLock(waitTime)` — 阻塞等待
3. 成功 → `methodInvocation.proceed()`
4. 失败 → `waitTimeoutHandler.handleWaitTimeout(...)`
5. finally: `unlock()` + `returnLock()`（计数 -1，归零则移除）

**`AbstractDistributedWlock.doAround`** 额外多了：
- `tryLock(waitTime, leaseTime, unit)` 一次传两个时间
- 业务完成后检查 `lock.isHeldByCurrentThread()`；若为 false 说明 leaseTime 到了 Redisson 自动放锁，调 `leaseTimeoutHandler` 替换返回值

> ⚠️ 限流器、屏障、信号量都没走这套抽象，是各自平行的 `Waround` 实现。结构上有一定重复，未来可以考虑再抽一层 `AbstractStandaloneWaround` 把"引用计数 + Map"通用化。

## 3. 模块包结构

```
io.github.spitmaster.warlock
├── annotation/       # 6 个注解
├── aspect/           # WaroundMethodInterceptor (唯一切面)
├── config/           # 2 个 AutoConfiguration
├── core/
│   ├── Waround.java
│   ├── factory/      # 工厂 + 2 个 Provider
│   │   ├── barrier/
│   │   ├── lock/
│   │   ├── ratelimiter/
│   │   └── semaphore/
│   ├── lock/         # 锁的实现 (standalone / dist)
│   ├── ratelimiter/
│   ├── barrier/
│   └── semaphore/
├── enums/            # LockType, Scope
├── exceptions/       # WarlockException
├── handler/          # 2 接口 + 2 默认枚举实现 (FastFail / Ignore)
└── util/             # Pair, SpelExpressionUtil, MethodNameUtil
```

## 4. 自动装配

### 4.1 触发方式

```
resources/META-INF/spring.factories                          (兼容 SB 2.6 及以前)
resources/META-INF/spring/
    org.springframework.boot.autoconfigure.AutoConfiguration.imports   (SB 2.7+)
```

两套都注册 `WarlockAutoConfiguration`。**两者可共存**——Spring Boot 会去重同名 bean。

### 4.2 配置类分工

- `WarlockAutoConfiguration`（默认）
  - 4 个 `AbstractPointcutAdvisor` bean（锁 / 屏障 / 信号量 / 限流）
  - `TimeoutHandlerProvider` bean
  - 通过 `@Import(WarlockWithRedissonConfiguration.class)` 级联加载
- `WarlockWithRedissonConfiguration`
  - `@ConditionalOnClass(RedissonClient.class)` —— 无 Redisson 不加载
  - 只注册 `RedissonProvider`（一个 lambda，懒取用户自己配的 `RedissonClient`）

### 4.3 开关

```properties
warlock.enabled=true   # 默认开启 (matchIfMissing=true)
```

设为 `false` 时**所有 Advisor 都不会注册** —— 注解不生效，方法退化为裸调用。

## 5. 依赖关系

```
annotation  ←─────────────┐
    ↑                     │
aspect ──→ core.factory ──→ core.lock/ratelimiter/barrier/semaphore
    ↑          ↑                      ↑
    │          │                      │
config ────────┴──── handler ─────────┘
                        ↑
                     util, enums, exceptions
```

- 注解层是**叶子**（被所有人依赖，但不依赖别人）
- `core.*` 内部的 `factory` 包只依赖相应的实现包
- `handler` 是横切依赖

## 6. 运行时概览

**每次命中加了注解的方法**：

1. Spring AOP 代理识别 Pointcut（`AnnotationMatchingPointcut`）
2. 调用 `WaroundMethodInterceptor.invoke(methodInvocation)`
3. 内部调 `factory.build(methodInvocation)` → 产出一个 `Waround`
   - 这里会做一次 SpEL 求值、一次反射读注解、一次超时 handler 查找
4. `waround.doAround(methodInvocation)` → 进入具体锁逻辑
5. 成功/失败/超时，返回结果给调用方

每次调用都 **new 一个 `Waround` 对象**，是短生命周期的堆对象。性能瓶颈主要在 SpEL 求值与注解反射（见 README 的 benchmark）。

## 7. 扩展切入点

- **加一种新的并发原语**：
  1. 定义新注解
  2. 实现 `Waround`
  3. 实现 `WaroundFactory`
  4. 在 `WarlockAutoConfiguration` 注册一个 `AbstractPointcutAdvisor`

- **换一种分布式实现**（例如 ZK 或自研中间件）：
  - `Scope` enum 新增枚举值
  - 在对应的 `DefaultXxxFactory.build()` 分支里处理新的 scope
  - 可以提供新的 `XxxProvider` 接口并 `@ConditionalOnClass` 注入

- **自定义超时处理**：
  - 实现 `WaitTimeoutHandler` / `LeaseTimeoutHandler`，声明为 Spring Bean
  - 在注解 `waitTimeoutHandler = XxService.class` 中引用
  - `TimeoutHandlerProvider` 会在运行时从 `BeanFactory` 懒取
