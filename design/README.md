# Warlock 设计文档

> 版本: 1.2.0
> 目标读者: 贡献者、需要二次开发或审阅的工程师
> 更新日期: 2026-04-23

`warlock-spring-boot-starter` 是一个**注解驱动的并发工具库**，将"锁 / 限流 / 屏障 / 信号量"4 种并发原语统一抽象成"在业务方法周围环绕执行的切面"，并同时覆盖**单机 (JUC)** 和**分布式 (Redisson)** 两种作用域。

## 文档索引

| 文档 | 内容 |
|---|---|
| [01-architecture.md](01-architecture.md) | 整体架构、核心抽象（`Waround`、`WaroundFactory`）、模块划分、自动装配 |
| [02-components.md](02-components.md) | 4 大能力的内部实现细节（锁 / 限流 / 屏障 / 信号量），单机 + 分布式共 8 种实现 |
| [03-sequences.md](03-sequences.md) | 关键调用时序：注解 → 切面 → 工厂 → Waround → 业务 → 超时回调 |
| [04-extension.md](04-extension.md) | 扩展点说明：自定义超时 Handler、自定义 RedissonProvider、开关配置 |

## 核心理念一览

```
 ┌──────────────────────┐
 │  @Warlock / @Wrate…  │  用户只加注解
 └───────────┬──────────┘
             │
             ▼
 ┌──────────────────────┐
 │ AnnotationMatching   │  Spring AOP: 匹配注解的 Pointcut
 │      Pointcut        │
 └───────────┬──────────┘
             │
             ▼
 ┌──────────────────────┐
 │WaroundMethod         │  唯一的 MethodInterceptor
 │Interceptor           │  (4 个 Advisor 共享同一个类)
 └───────────┬──────────┘
             │  build(invocation)
             ▼
 ┌──────────────────────┐
 │  WaroundFactory      │  工厂: 根据注解 + Scope + LockType
 │ (Lock/Limit/Barrier/ │     挑选具体实现
 │       Semaphore)     │
 └───────────┬──────────┘
             │  doAround(invocation)
             ▼
 ┌──────────────────────┐
 │      Waround         │  核心抽象:
 │ (具体实现, 共 8 种)    │  "在某种环境下执行业务方法"
 └──────────────────────┘
```

**一句话**：`Waround = Around Advice 的 OOP 化`，每种并发原语都是一种"不同的环绕方式"。

## 4 种注解与 8 种实现矩阵

| 注解 | STANDALONE 实现 | DISTRIBUTED 实现 | 说明 |
|---|---|---|---|
| `@Warlock` (REENTRANT) | `ReentrantWlock` | `DistributedReentrantWlock` | JUC `ReentrantLock` / Redisson `RLock` |
| `@Warlock` (READ) | `ReadWlock` | `DistributedReadWlock` | JUC `RRWL.readLock` / Redisson `RReadWriteLock.readLock` |
| `@Warlock` (WRITE) | `WriteWlock` | `DistributedWriteWlock` | JUC `RRWL.writeLock` / Redisson `RReadWriteLock.writeLock` |
| `@WrateLimiter` | `StandaloneWlimiter` | `DistributedWlimiter` | Guava `RateLimiter` / Redisson `RRateLimiter` |
| `@WcyclicBarrier` | `StandaloneWbarrier` | ❌ 不支持 | JUC `CyclicBarrier`（Redisson 无对应实现） |
| `@Wsemaphore` | `StandaloneWmutex` | `DistributedWmutex` | JUC `Semaphore` / Redisson `RPermitExpirableSemaphore` |

## 关键设计取舍

1. **用 MethodInterceptor 而不是 `@Aspect`**：减少一层 AspectJ 表达式的运行时开销，直接由 Spring AOP 原生调度。
2. **Pointcut 按注解匹配（`AnnotationMatchingPointcut`）**：精准、无额外扫描。
3. **Redisson 可选**：`@ConditionalOnClass(RedissonClient.class)` + `@ConditionalOnProperty(warlock.enabled)`，没有 Redisson 也能只用单机能力。
4. **超时策略可插拔**：`WaitTimeoutHandler` / `LeaseTimeoutHandler` 接口 + 运行时 `BeanFactory` 懒查找，避免循环依赖。
5. **SpEL 支持动态 key**：锁 key = `name + ":" + SpEL(key, 方法参数)`，灵活按参数切分粒度。
6. **单机资源引用计数 + 懒清理**：以 `ConcurrentHashMap<key, Pair<Resource, AtomicInteger>>` 模拟"最后一个用户离开时清场"，避免 Map 无限增长（⚠️ 部分实现有 bug，详见 [02-components.md](02-components.md) 的"已知问题"一节）。

## 已知隐患速览

> 详见各文档，此处只列最严重的 3 条

1. `StandaloneWlimiter` / `StandaloneWbarrier` 的 `doAround` **未配对 `returnXxx()`** → Map 单调增长的内存泄漏。
2. 4 个 `AbstractPointcutAdvisor` 未设 `order` → 与 `@Transactional` 叠加可能"**锁先于事务释放**"。
3. 分布式 `DistributedWlimiter` / `DistributedWmutex` 的 Guava Cache 用了 `.weakKeys()`，String key 没有强引用立即被 GC → **缓存几乎必 miss**。

## 版本

- 当前: 1.2.0（开发中）
- 历史: 1.1.0, 1.0.x
- 依赖: Spring Boot 2.4.5 / Redisson 3.11.6 / Guava 30.1.1-jre / JDK 8+

