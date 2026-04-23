# 02 · 各能力实现细节

本文档按"单机 / 分布式"两类展开，说明每种 `Waround` 的实现要点。

## 1. 锁 `@Warlock`

### 1.1 单机锁

3 个实现共用同一套模板（`AbstractStandaloneWlock.doAround`）：

| 类 | 底层原语 | Map 存储 |
|---|---|---|
| `ReentrantWlock` | JDK `ReentrantLock` | `ConcurrentHashMap<String, Pair<ReentrantLock, AtomicInteger>>`（类私有） |
| `ReadWlock` | `ReentrantReadWriteLock.readLock()` | 与 `WriteWlock` **共用同一个 Map** |
| `WriteWlock` | `ReentrantReadWriteLock.writeLock()` | 同上（`static final` in `WriteWlock`） |

**核心机制：懒加载 + 引用计数清理**

```java
// getLock
REENTRANT_LOCK_MAP.compute(key, (k, pair) -> {
    if (pair == null) pair = Pair.of(new ReentrantLock(), new AtomicInteger(0));
    pair.getRight().incrementAndGet();
    return pair;
});

// returnLock（在 finally 中）
REENTRANT_LOCK_MAP.computeIfPresent(key, (k, pair) -> {
    if (pair.getRight().decrementAndGet() <= 0) return null;  // 移除 entry
    return pair;
});
```

**为什么不一直保留 Map entry？**
→ 避免因业务 key 多变而导致 Map 无限增长（例如 key 用了 `#orderId`）。

**为什么用 `compute` 而不是 `putIfAbsent`？**
→ `compute` 对同一 key 的多个线程是**原子串行**的，保证初始化和计数 +1 不可分。

### 1.2 分布式锁

| 类 | 底层 | 说明 |
|---|---|---|
| `DistributedReentrantWlock` | `RedissonClient.getLock(key)` | 可重入 RLock |
| `DistributedReadWlock` | `getReadWriteLock(key).readLock()` | |
| `DistributedWriteWlock` | `getReadWriteLock(key).writeLock()` | |

**不需要引用计数**：每次 `getLock` 是从 Redisson 客户端取一个轻量代理对象，不持有真正的锁资源。

**与单机的关键差异**：
```java
acquired = lock.tryLock(waitTime.toMillis(), leaseTime.toMillis(), TimeUnit.MILLISECONDS);
```
同时传 `leaseTime`，**这会关闭 Redisson 的 watchdog 自动续租**。业务超时后锁会被强制释放，需要 `leaseTimeoutHandler` 兜底。

### 1.3 LockInfo 携带的信息

- `lockKey`（由 `name + ":" + SpEL` 拼成）
- `lockType` / `lockScope`
- `waitTime` / `leaseTime`（`Duration`）
- `waitTimeoutHandler` / `lockLeaseTimeoutHandler`（已解析为实例）

---

## 2. 限流 `@WrateLimiter`

### 2.1 单机：`StandaloneWlimiter`

底层：Guava `com.google.common.util.concurrent.RateLimiter`

```java
public Object doAround(...) throws Throwable {
    if (this.getRateLimiter().tryAcquire(waitTime)) {
        return methodInvocation.proceed();
    } else {
        return rateLimiterInfo.getWaitTimeoutHandler().handleWaitTimeout(...);
    }
}
```

仍然使用 `ConcurrentHashMap<String, Pair<RateLimiter, AtomicInteger>>` + `getRateLimiter` 计数。

> 🔴 **已知 bug**：`doAround()` 没有配对调用 `returnRateLimiter()`，counter 只增不减，Map 永不清理 → 长期运行造成内存泄漏。

### 2.2 分布式：`DistributedWlimiter`

底层：Redisson `RRateLimiter`

```java
RATE_LIMITER_CACHE.get(key, () -> {
    RRateLimiter rl = redissonClient.getRateLimiter(key);
    rl.trySetRate(RateType.OVERALL, permitsPerSecond, 1, RateIntervalUnit.SECONDS);
    return rl;
});
```

用 Guava `Cache` 缓存 `RRateLimiter`（10s 过期），避免重复 `trySetRate` 调用。

> 🔴 **已知 bug**：`.weakKeys()` + 每次都是新拼的 String key，GC 一到 entry 就被回收 → 缓存几乎必 miss，失去意义。

### 2.3 注释中特别说明

源码注释：
> 如果要使用分布式版本的限流器, 强烈建议使用 alibaba 的 sentinel
> 这里暂时只提供一个简陋的分布式限流器实现

—— 作者自己都认为这是"够用但不专业"的实现。

---

## 3. 屏障 `@WcyclicBarrier`

### 3.1 单机：`StandaloneWbarrier`

底层：JDK `CyclicBarrier`

```java
CyclicBarrier cyclicBarrier = this.getCyclicBarrier();
try {
    cyclicBarrier.await(waitTime, MILLISECONDS);
} catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
    return processBrokenBarrier(methodInvocation, cyclicBarrier, e);
}
return methodInvocation.proceed();
```

**Broken 修复**：`CyclicBarrier` 一旦有线程超时，就会变成 Broken，整个 barrier 作废。这里用 `synchronized(cyclicBarrier)` 保证**只有一个线程**重置 Map，其他线程进入时已经看到新 barrier。

### 3.2 分布式

**不支持**。Redisson 没有 `CyclicBarrier` 实现（有 `CountDownLatch` 但语义不同），作者选择暂不自研。

### 3.3 已知问题

> 🔴 **bug 1**：`doAround` 没有配对 `returnCyclicBarrier()`，正常流程下 counter 只增不减（同限流器）。
> 🟡 **bug 2**：`processBrokenBarrier` 在"已经被别人重置"分支少归还一次 counter，加剧上述泄漏。

---

## 4. 信号量 `@Wsemaphore`

### 4.1 单机：`StandaloneWmutex`

底层：JDK `Semaphore`

```java
boolean acquired = false;
try {
    acquired = semaphore.tryAcquire(waitTime, MILLISECONDS);
    if (acquired) return methodInvocation.proceed();
    else return waitTimeoutHandler.handleWaitTimeout(...);
} finally {
    if (acquired) semaphore.release();
    returnSemaphore();  // ← 这里唯一一处"单机"里正确配对的实现
}
```

同样的 Map + 引用计数。**是 4 种单机实现里唯一正确使用 finally + return 的**。

### 4.2 分布式：`DistributedWmutex`

底层：Redisson `RPermitExpirableSemaphore`

```java
permitId = semaphore.tryAcquire(waitTime, leaseTime, MILLISECONDS);
if (permitId != null) {
    result = methodInvocation.proceed();
} else {
    result = waitTimeoutHandler.handleWaitTimeout(...);
}
// finally:
if (permitId != null) {
    boolean released = semaphore.tryRelease(permitId);
    if (!released) {
        // permitId 已自动过期 → 调 leaseTimeoutHandler
        result = leaseTimeoutHandler.handleLeaseTimeout(...);
    }
}
```

与分布式锁类似，`leaseTime` 到期 permit 自动释放。`tryRelease` 返回 false 表示 permit 已经过期（可能已被别人占走）。

缓存同样用了 Guava `Cache` + `weakKeys()`，**同样的 miss 问题**。

---

## 5. 超时处理（handler 包）

### 5.1 接口

```java
interface WaitTimeoutHandler {
    Object handleWaitTimeout(MethodInvocation mi) throws Throwable;
}

interface LeaseTimeoutHandler {
    Object handleLeaseTimeout(MethodInvocation mi, Object result) throws Throwable;
}
```

### 5.2 内置实现

| 类 | 类型 | 行为 |
|---|---|---|
| `FastFailTimeoutHandler` | `enum INSTANCE` | **直接抛 `WarlockException`**，无论 wait/lease |
| `IgnoreTimeoutHandler` | `enum INSTANCE` | wait 超时 → 返回 null；lease 超时 → 返回原 result |

`enum` 单例保证全局共享，无需 Spring 管理。

### 5.3 `TimeoutHandlerProvider`

```java
public WaitTimeoutHandler getWaitTimeoutHandler(Waiting waiting) {
    Class<? extends WaitTimeoutHandler> cls = waiting.waitTimeoutHandler();
    if (cls == FastFailTimeoutHandler.class) return FastFailTimeoutHandler.INSTANCE;
    if (cls == IgnoreTimeoutHandler.class)   return IgnoreTimeoutHandler.INSTANCE;
    return beanFactory.getBean(cls);  // ← 运行时才查找，避免切面启动期的循环依赖
}
```

**为什么不在 `@Bean` 方法里直接注入所有 handler？**
源码注释：
> 某些情况下, 会造成循环依赖, 导致切面失效

因为用户的 handler 可能自己也标了 `@Warlock` 注解所属的 Bean，构造时强依赖全集合会出问题。

---

## 6. SpEL `SpelExpressionUtil`

- 用 `SpelExpressionParser` 单例解析 key 表达式（`#dto.userId`、`#id` 等）
- 参数名由 `LocalVariableTableParameterNameDiscoverer` 获取
  - ⚠️ 依赖编译期 `-g` 保留 LocalVariableTable；没开的话拿不到参数名，直接返回 null，**Joiner.skipNulls 悄悄吞掉**，结果一个 name 下所有调用共用一把锁
  - ⚠️ Spring 6+ 已 deprecated，推荐 `DefaultParameterNameDiscoverer`
- 每次调用都 `parser.parseExpression(spel)`，未缓存（benchmark 显示是当前热点）

---

## 7. 已知问题合集

（也见 `design/README.md` 概览）

| 严重度 | 位置 | 问题 |
|:-:|---|---|
| 🔴 | `StandaloneWlimiter.doAround` | 未配对 `returnRateLimiter`，Map 泄漏 |
| 🔴 | `StandaloneWbarrier.doAround` | 未配对 `returnCyclicBarrier`，Map 泄漏 |
| 🔴 | `WarlockAutoConfiguration` 4 个 Advisor | 无 `order`，与 `@Transactional` 顺序不可控 |
| 🔴 | `Leasing` 默认 `FastFail` | 业务成功但 lease 过期时抛异常，覆盖正常返回值 |
| 🔴 | `DistributedWlimiter` / `DistributedWmutex` | Cache `.weakKeys()` 对 String key 失效，缓存命中率极低 |
| 🟡 | `StandaloneWbarrier.processBrokenBarrier` | "已被别人重置"分支少一次 `returnCyclicBarrier` |
| 🟡 | `SpelExpressionUtil` | 依赖 `-g` debug 信息；Spring 6 deprecated API |
| 🟡 | `DefaultWlockFactory.buildLockInfo` | 单机锁也校验 leaseTime > 0，但运行时不使用 |
| 🟡 | `AbstractDistributedWlock` | 主动传 leaseTime 关闭了 Redisson watchdog，文档未说明 |
| 🟢 | `SpelExpressionUtil` | 表达式未缓存 |
| 🟢 | 依赖版本 | Redisson 3.11.6 / Spring Boot 2.4.5 偏旧，存在 CVE |
