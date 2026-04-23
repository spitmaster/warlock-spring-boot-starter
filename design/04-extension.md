# 04 · 扩展点与使用建议

## 1. 开关

```properties
# application.properties / application.yml
warlock.enabled=true    # 默认 true；设为 false 关闭所有注解切面
```

**作用**：控制 4 个 `AbstractPointcutAdvisor` 是否注册。关闭时注解不会生效，方法退化为直接调用。

> ⚠️ 没有单独关闭"锁"或"限流"的细粒度开关。如果只想关闭其中一种，只能不在业务代码里加对应注解。

## 2. 分布式能力的前置条件

分布式功能需要：

```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson</artifactId>
    <!-- 或 redisson-spring-boot-starter -->
</dependency>
```

并且 Spring 容器里要有 `RedissonClient` bean。本项目不会帮你创建 `RedissonClient`，它只是 `@Conditional` 地**观察**容器里是否存在一个，并通过 `RedissonProvider` 懒取。

如果 `RedissonClient` 不存在而注解上写了 `Scope.DISTRIBUTED`，运行期会抛：
> `WarlockException: Not supported lock scope: DISTRIBUTED ; please use redisson to active this function`

## 3. 自定义 `WaitTimeoutHandler` / `LeaseTimeoutHandler`

### 3.1 写一个 handler

```java
@Component
public class MyWaitHandler implements WaitTimeoutHandler {
    @Override
    public Object handleWaitTimeout(MethodInvocation mi) {
        log.warn("wait timeout, method={}", mi.getMethod());
        return null;   // 或返回 fallback 值
    }
}
```

**必须是 Spring Bean**（除了框架内置两个 `enum INSTANCE` 单例）。`TimeoutHandlerProvider` 会用 `beanFactory.getBean(Class)` 查找。

### 3.2 在注解里引用

```java
@Warlock(
    name = "u",
    key = "#id",
    waiting = @Waiting(
        waitTime = 5, timeUnit = ChronoUnit.SECONDS,
        waitTimeoutHandler = MyWaitHandler.class
    )
)
public void doBiz(long id) { ... }
```

### 3.3 注意事项

- **不要在 handler 内部调用同一把 @Warlock 保护的方法**，容易死锁（单机可重入无碍，但易逻辑混乱）。
- handler 的 `handleWaitTimeout` **允许 throw**，异常会透传给调用方。
- handler 方法里可以通过 `methodInvocation.proceed()` **强制执行业务**（无锁执行），但这等于放弃了锁的保护，请谨慎。
- Lease handler 的参数 `result` 是业务 `proceed()` 返回的值，**可以原样返回**（等价于 `IgnoreTimeoutHandler`），也可以替换。

### 3.4 默认策略对照

|  | 默认 `waitTimeoutHandler` | 默认 `leaseTimeoutHandler` |
|---|---|---|
| 配置 | `FastFailTimeoutHandler` | `FastFailTimeoutHandler` |
| wait 超时表现 | 抛 `WarlockException` | — |
| lease 超时表现 | — | **抛 `WarlockException`，替换掉正常 result** |

> 🔴 对分布式锁特别提醒：**默认 leaseTimeoutHandler = FastFail** 会在业务代码跑超过 `leaseTime` 时把已经成功 return 的值换成异常。生产环境通常应该显式改为 `IgnoreTimeoutHandler` 或自定义 handler，见 [02-components.md](02-components.md)。

## 4. 自定义 SpEL key

`key` 是**可选**的，留空时同一把锁覆盖这个 `name` 下所有方法调用。写了 SpEL 时按表达式结果切分。

```java
@Warlock(name = "u", key = "#dto.userId")        // 按 userId 分锁
@Warlock(name = "u", key = "#a + '-' + #b")      // 组合参数
@Warlock(name = "u", key = "T(String).valueOf(#id)")  // 使用类型方法
```

**注意**：
- 参数名解析依赖编译产物里有 `LocalVariableTable`（Maven 编译默认开）。若项目用了不标准的构建（例如某些 Kotlin/Scala 或关闭了 debug info），会**拿不到参数名**，SpEL 求值失败返回 null。
- 返回 null 时 `Joiner.skipNulls()` 会悄悄吞掉，最终 lockKey 变成 `warlock:name`，退化为**全局锁**。没有警告日志。建议自己在构造 key 时检查。
- SpEL 表达式里可以调 Spring Bean 方法：`@beanName.method(#arg)`，但要注意 `StandardEvaluationContext` 没注入 BeanResolver，**当前版本不支持 `@bean` 引用**。如需扩展，需要修改 `SpelExpressionUtil`。

## 5. 替换 `RedissonProvider`

默认实现是 `WarlockWithRedissonConfiguration` 注册的一个 lambda，从 `BeanFactory` 懒取 `RedissonClient`。如果你有多个 `RedissonClient`（例如一个给业务、一个专门给锁），可以自己注册一个同名 bean 覆盖：

```java
@Configuration
public class MyConfig {
    @Bean
    public RedissonProvider redissonProvider(
        @Qualifier("lockRedisson") RedissonClient lockClient
    ) {
        return () -> lockClient;
    }
}
```

框架里 `RedissonProvider` 是接口，**运行时**才从中拿 `RedissonClient`，因此可以实现动态路由（例如按租户）。

## 6. 加一种新的并发原语

（仅作为"可以这么做"的参考，实战前建议先讨论）

1. **定义注解**：
   ```java
   @Target(METHOD) @Retention(RUNTIME)
   public @interface WcountDownLatch { ... }
   ```

2. **实现 Waround**：在 `core/countdownlatch/` 下写 `StandaloneWlatch implements Waround`，按引用计数 Map 模式。

3. **实现 Factory**：在 `core/factory/countdownlatch/` 下写 `DefaultWlatchFactory implements WaroundFactory`。

4. **注册 Advisor**：在 `WarlockAutoConfiguration` 里加一个 `@Bean` 方法，仿照其他 4 个：
   ```java
   @Bean("wcountDownLatchAdvisor")
   public AbstractPointcutAdvisor wcountDownLatchAdvisor(TimeoutHandlerProvider p) {
       AnnotationMatchingPointcut pc = new AnnotationMatchingPointcut(null, WcountDownLatch.class, true);
       WaroundMethodInterceptor mi = new WaroundMethodInterceptor(new DefaultWlatchFactory(p));
       return new AbstractPointcutAdvisor() {
           public Pointcut getPointcut() { return pc; }
           public Advice getAdvice()     { return mi; }
       };
   }
   ```

## 7. 与其他切面（特别是 `@Transactional`）联用

> 🔴 **必读**：当前版本 Warlock 的 4 个 Advisor **没有** 设置 `order`，默认 `LOWEST_PRECEDENCE`。

**推荐做法**：直到上游修复之前，用户自行加 `@Order` 或通过自定义 BPP 强制让 `@Transactional` 在内层：

```java
@Configuration
public class MyConfig {
    @Bean
    public BeanPostProcessor ensureWarlockOrder(ApplicationContext ctx) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String name) {
                if (bean instanceof AbstractPointcutAdvisor
                    && name.contains("warlock")) {
                    ((AbstractPointcutAdvisor) bean).setOrder(Ordered.HIGHEST_PRECEDENCE);
                }
                return bean;
            }
        };
    }
}
```

**推荐编排**：锁在外层，事务在内层。这样：
1. 先拿锁 → 2. 开事务 → 3. 业务 → 4. 提交事务 → 5. 释放锁
"释放锁"发生在"事务已提交"之后，其他节点拿到锁时读到的是最新数据。

## 8. 生产环境 Check-list

启用前至少确认以下几点：

- [ ] `RedissonClient` 是否健康（含 Redis 连接、认证）
- [ ] 单机 vs 分布式的选择是否明确（不要混用相同 name）
- [ ] `leaseTime` 设置是否 > 最长业务耗时 × 1.5（留足余量）
- [ ] 是否显式设置了 `leaseTimeoutHandler`（默认 FastFail 会覆盖成功返回值）
- [ ] 是否与 `@Transactional` 同时使用；若是，是否控制了 order
- [ ] SpEL key 是否能正常取到参数值（单元测试能发现）
- [ ] 业务代码是否幂等（锁不是万能，Redisson 的网络分区下存在极端情况）
- [ ] JVM 参数是否保留了 debug 信息（对 SpEL 参数名必需）

## 9. 调试小技巧

- `WarlockAutoConfiguration` 和 `WarlockWithRedissonConfiguration` 都带了 `info` 日志，启动时能确认哪个分支生效。
- Redisson 的锁在 Redis 里是 Hash：`KEYS warlock:*` 能看到当前 active lock。
- 单机 Map 可通过反射 dump：`ReentrantWlock.REENTRANT_LOCK_MAP` 是 package-private 静态字段（`WriteWlock.READ_WRITE_LOCK_MAP` 同），排查 Map 爆炸时可以直接 JMX / 远程 attach 查看。

---

## 相关文档

- [README](README.md) —— 文档入口
- [01-architecture.md](01-architecture.md) —— 架构总览
- [02-components.md](02-components.md) —— 实现细节
- [03-sequences.md](03-sequences.md) —— 关键时序
