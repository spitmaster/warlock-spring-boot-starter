# Changelog

所有显著变更都会记录在此文件中。

格式参考 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

---

## [1.2.0] - 2026-04-23

首个系统性 bug 修复版本。所有修复都是**行为与代码注释/设计意图不一致**的遗漏或错误，没有引入新特性。

### Fixed

#### 1. `StandaloneWlimiter` — 限流器内存泄漏 & 状态丢失

**问题**：`doAround` 每次调用都 `getRateLimiter()` 使计数 +1，但从不调用已定义好的 `returnRateLimiter()` 方法，`RATE_LIMITER_MAP` 条目永不回收。朴素地补上 `returnRateLimiter()` 又会让串行调用每次都销毁并重建 `RateLimiter`，**令牌桶状态丢失、限流完全失效**。

**修复**：移除 `ConcurrentHashMap` + 引用计数方案，改用 Guava `Cache`（`expireAfterAccess(10min)` + `maximumSize(4096)`）存放 `RateLimiter`。
- 跨调用保留令牌桶状态，限流语义正确；
- 长时间空闲或超出容量由 Cache 自动淘汰，Map 有界；
- 对齐 `DistributedWlimiter` 的缓存模式，行为一致。

涉及文件：[StandaloneWlimiter.java](src/main/java/io/github/spitmaster/warlock/core/ratelimiter/StandaloneWlimiter.java)

#### 2. `StandaloneWbarrier` — 屏障 Map 内存泄漏 & Broken 恢复逻辑错误

**问题**：
- `doAround` 调了 `getCyclicBarrier()` 但没有配对 `returnCyclicBarrier()`，`CYCLIC_BARRIER_MAP` 单调增长；
- 原 `processBrokenBarrier` 在 `synchronized` 里额外 `get` 一次但只在"自己是第一个"分支 return，另一分支净多占一次计数，且 `returnCyclicBarrier()` 只做计数 -1，**并没有真正将 Broken barrier 从 Map 中移除**，后续线程仍会拿到同一把废 barrier。

**修复**：在 `doAround` 外层加 `try/finally + returnCyclicBarrier()`，简化 Broken 分支为 "catch → 记日志 → 走 `waitTimeoutHandler`"。依靠天然的"所有持有者陆续归还 → 引用计数归零 → Map 移除 → 下个线程拿到全新 barrier"流程完成自愈，删除不必要的 `processBrokenBarrier` 方法。

涉及文件：[StandaloneWbarrier.java](src/main/java/io/github/spitmaster/warlock/core/barrier/StandaloneWbarrier.java)

#### 3. `SpelExpressionUtil.parseSpel` — 无参方法上的 SpEL 不生效

**问题**：无参方法 `paramNames.length == 0` 时直接 `return null`，即使 SpEL 表达式是 `'constantKey'` 或 `T(Thread).currentThread().getName()` 这类不依赖参数的表达式，也会被跳过，导致无参方法无法使用 SpEL 作为锁 key。

**修复**：把 `isBlank(spel)` 的早退判断前移；`paramNames` 为空时只跳过变量绑定循环，仍然执行 `parseExpression + getValue`。同时加上 `args.length` 与 `paramNames.length` 的 min 保护，避免参数数量不一致时越界。

涉及文件：[SpelExpressionUtil.java](src/main/java/io/github/spitmaster/warlock/util/SpelExpressionUtil.java)

#### 4. `DistributedWlimiter` / `DistributedWmutex` — Guava Cache `.weakKeys()` 让缓存失效

**问题**：两个类的缓存都用了 `.weakKeys().weakValues()`，但 key 是 `Joiner.join(...)` 方法内新建的 `String`，方法返回即无强引用，GC 一次缓存即清空。注释说"防止频繁初始化"，**实际几乎每次都 miss**，`trySetRate` / `trySetPermits` / `expire` 等 Redis 操作并没有被真正防住。

**修复**：去掉 `.weakKeys().weakValues()`，改用 `.maximumSize(1024)` + 原有 10s TTL 做有界缓存。
- 保持缓存命中率；
- 最坏情况下条目数被 `maximumSize` 限死，不会因为用户 key 基数大而内存爆炸；
- 10s TTL 保证只要这段时间没用，下次自然重新拉 Redis 状态。

涉及文件：
- [DistributedWlimiter.java](src/main/java/io/github/spitmaster/warlock/core/ratelimiter/DistributedWlimiter.java)
- [DistributedWmutex.java](src/main/java/io/github/spitmaster/warlock/core/semaphore/DistributedWmutex.java)

#### 5. `AbstractDistributedWlock` — 业务异常被 `LeaseTimeoutHandler` 吞掉

**问题**：业务 `methodInvocation.proceed()` 抛异常 + 锁同时因 `leaseTime` 被 Redisson 自动释放时，`finally` 中会调 `leaseTimeoutHandler.handleLeaseTimeout(...)`，默认 `FastFail` 会抛出 `WarlockException`——按 Java 语义，`finally` 里抛出的异常会**压制 try 里的原始业务异常**。调用方看到 `WarlockException("lease timeout")`，原异常丢失且不在 cause 链里。

**修复**：引入 `bizCompletedNormally` 标志位，仅在业务正常返回（或等待超时走 waitHandler）时才调用 `leaseTimeoutHandler`；业务已抛异常则跳过，把原异常传播出去。

涉及文件：[AbstractDistributedWlock.java](src/main/java/io/github/spitmaster/warlock/core/lock/dist/AbstractDistributedWlock.java)

#### 6. `DefaultWbarrierFactory` — 缺少注解 null 检查

**问题**：`findMergedAnnotation(method, WcyclicBarrier.class)` 返回 null 时，后续直接 `wcyclicBarrier.name()` 会 `NullPointerException`，错误信息不友好。其他三个工厂（`DefaultWlockFactory` / `DefaultWmutexFactory` / `DefaultWlimiterFactory`）都有对应的 null 校验 + `WarlockException`，此处漏写。

**修复**：对齐其他工厂的写法，加上：

```java
if (wcyclicBarrier == null) {
    throw new WarlockException("invoke WcyclicBarrier interceptor on non WcyclicBarrier method, method = " + ...);
}
```

涉及文件：[DefaultWbarrierFactory.java](src/main/java/io/github/spitmaster/warlock/core/factory/barrier/DefaultWbarrierFactory.java)

### Changed

- `pom.xml` 版本：`1.0.2-SNAPSHOT` → `1.2.0`
- `README.md` 示例依赖版本：`1.1.0` → `1.2.0`
- 新增 `design/` 目录下系统性设计文档（README + 4 个子文档，详见 [design/README.md](design/README.md)）

### Removed

- `StandaloneWbarrier.processBrokenBarrier(...)` 私有方法（逻辑并入 `doAround`）
- `StandaloneWlimiter.returnRateLimiter(...)` 方法及关联的 `RATE_LIMITER_MAP` 静态字段
- `SpelExpressionUtil` 中不再需要的 `java.lang.reflect.Array` 引用

### 兼容性说明

- 对外 API（注解、`WaroundFactory` 接口、`WaitTimeoutHandler` / `LeaseTimeoutHandler` 接口）**均未变更**，业务代码无需修改。
- 分布式锁在"业务抛异常 + lease 超时"双重触发下，调用方现在看到的是**原始业务异常**而不再是 `WarlockException("lease timeout")`。依赖旧行为（例如用 `catch (WarlockException)` 拦截所有异常）的代码需要调整。
- `StandaloneWlimiter` 内部实现从 `ConcurrentHashMap` 换成 Guava `Cache`。如果有代码通过反射访问 `RATE_LIMITER_MAP` 字段做监控（非公开 API），需改为访问新的 `RATE_LIMITER_CACHE`。

---

## [1.1.0] - 历史版本

README 示例中提到的版本。

---

## [1.0.x] - 历史版本

初始发布版本，包含 `@Warlock` / `@WrateLimiter` / `@WcyclicBarrier` / `@Wsemaphore` 4 个注解的基础实现。
