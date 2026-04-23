# 03 · 关键时序

本文用文字 + ASCII 时序图说明几个核心场景，便于排查问题时快速定位每一步的逻辑。

## 1. 启动期：Spring Boot 自动装配

```
Spring Boot 启动
  │
  ├─ 扫描 META-INF/spring/…AutoConfiguration.imports
  │   └─ 发现 WarlockAutoConfiguration
  │
  ├─ 评估 @ConditionalOnProperty(warlock.enabled)   ── 默认 true
  │   └─ 通过
  │
  ├─ 实例化 WarlockAutoConfiguration
  │   ├─ @Import WarlockWithRedissonConfiguration
  │   │   └─ 评估 @ConditionalOnClass(RedissonClient)
  │   │       ├─ 存在 → 注册 RedissonProvider bean
  │   │       └─ 不存在 → 跳过
  │   │
  │   ├─ 注册 timeoutHandlerProvider bean
  │   │   └─ 构造: new TimeoutHandlerProvider(beanFactory)   (只持引用)
  │   │
  │   └─ 注册 4 个 Advisor bean
  │       • warlockAnnotationAdvisor          (@Warlock)
  │       • wcyclicBarrierAnnotationAdvisor   (@WcyclicBarrier)
  │       • wsemaphoreAdvisor                 (@Wsemaphore)
  │       • wrateLimiterAdvisor               (@WrateLimiter)
  │
  │     每个 Advisor:
  │       - Pointcut: AnnotationMatchingPointcut(注解类, checkInherited=true)
  │       - Advice:   new WaroundMethodInterceptor(new DefaultXxxFactory(...))
  │
  └─ Spring AOP 扫描所有 Bean 方法
      └─ 凡方法上带对应注解的 → 包一层 CGLIB/JDK 代理
```

**注意**：4 个工厂是 `new` 出来的，**不是 Spring Bean**。它们只依赖注入进去的 Provider，用户通常不需要替换它们。

## 2. 请求期（最经典的 @Warlock STANDALONE REENTRANT）

```
caller.doBiz(dto)
  │
  ▼
[Spring AOP 代理]
  │
  ▼ invoke(MethodInvocation)
WaroundMethodInterceptor
  │
  ▼ factory.build(mi)
DefaultWlockFactory
  ├─ 反射读 @Warlock
  ├─ SpEL 求值 key: #dto.userId → "u-123"
  ├─ 拼 lockKey: "warlock:uniquename1:u-123"
  ├─ TimeoutHandlerProvider.getWaitTimeoutHandler(waiting)
  │    └─ beanFactory.getBean(XXService.class)   (运行时 Spring 查找)
  ├─ TimeoutHandlerProvider.getLeaseTimeoutHandler(leasing)
  └─ 根据 scope=STANDALONE + type=REENTRANT → new ReentrantWlock(lockInfo)
  │
  ▼ waround.doAround(mi)
AbstractStandaloneWlock
  │
  ▼ lock = getLock()
ReentrantWlock.getLock()
  ├─ REENTRANT_LOCK_MAP.compute(key, ...)
  │    ├─ pair == null → new ReentrantLock() + counter(0)
  │    └─ counter.incrementAndGet()
  └─ return pair.left
  │
  ▼ lock.tryLock(waitTime)
   ├─ acquired == true
   │   │
   │   ▼ mi.proceed()     ── 执行业务代码
   │   │
   │   └─ return result
   │
   └─ acquired == false
       │
       ▼ waitTimeoutHandler.handleWaitTimeout(mi)
       │   (FastFail → 抛异常; 或 Ignore → 返回 null; 或自定义)
       │
       └─ result (替代返回值)
  │
  ▼ finally
  ├─ if acquired: lock.unlock()
  └─ returnLock()
      └─ computeIfPresent(key, ...)
          └─ counter.decrementAndGet()
              ├─ > 0 → keep pair
              └─ <= 0 → return null (从 Map 移除)
```

## 3. 分布式锁的差异点

只画与上面不同的部分：

```
lock.tryLock(waitTime, leaseTime, MILLISECONDS)   ← 多了 leaseTime
│
▼ 业务执行中 ... (leaseTime 为 60s)
│
▼ (假设业务跑了 61s)
│
▼ finally
  if (acquired) {
    if (lock.isHeldByCurrentThread()) {
      lock.unlock()                 ← 正常路径
    } else {
      // Redisson 已经自动放锁, 其他线程可能已抢到
      result = leaseTimeoutHandler.handleLeaseTimeout(mi, result)
      // ⚠️ FastFail 会把正常 return 的 result 换成抛异常
    }
  }
```

## 4. CyclicBarrier 的 Broken 恢复流程

```
Thread A: 等待 barrier.await(1s)
Thread B: 等待 barrier.await(1s)
Thread C: 1.5s 后才到
  │
  ▼ Thread A 和 B 都 TimeoutException
  │
 Thread A                       Thread B
  │                              │
  ▼ processBrokenBarrier         ▼ processBrokenBarrier
  │                              │
  ▼ synchronized(cyclicBarrier)  │  synchronized(cyclicBarrier)  (阻塞)
  │
  ├─ getCyclicBarrier()
  │    └─ 此时 Map 里还是旧的那把 Broken 的 barrier
  │    └─ counter++, 返回同一把
  │
  ├─ cyclicBarrier == newCyclicBarrier?  → true
  │
  ├─ returnCyclicBarrier()
  │    └─ counter 递减; 到 0 则移除
  │
  ▼ 退出 synchronized
                                 ▼ 进入 synchronized (Thread A 已退出)
                                 │
                                 ├─ getCyclicBarrier()
                                 │    └─ 此时 Map 可能空; 初始化新 barrier
                                 │    └─ counter=1, 返回新 barrier
                                 │
                                 ├─ 与 B 原来持有的 cyclicBarrier 不相等?
                                 │    ├─ 相等 → returnCyclicBarrier()
                                 │    └─ 不相等 → 🔴 什么也不做，counter 永远偏高

Thread A + B 最终都调 waitTimeoutHandler.handleWaitTimeout(mi)
```

⚠️ 这段代码本身的设计意图是对的（让第一个进入的线程负责清理），但：
1. 正常 doAround 根本没调 `returnCyclicBarrier`（见 [02-components.md](02-components.md) 的 bug 列表）
2. 恢复分支"别人已经重置"的 case 少 return 一次

## 5. 请求期 @WrateLimiter DISTRIBUTED

```
invoke(mi)
  │
  ▼ DefaultWlimiterFactory.build(mi) → new DistributedWlimiter(info, redisson)
  │
  ▼ waround.doAround(mi)
  │
  ▼ getRateLimiter()
  │   └─ RATE_LIMITER_CACHE.get(key, () -> {
  │        RRateLimiter rl = redisson.getRateLimiter(key);
  │        rl.trySetRate(OVERALL, permitsPerSecond, 1, SECONDS);  ← 会打一次 Redis
  │        return rl;
  │     })
  │   (⚠️ weakKeys() + String key 使得几乎每次都进 supplier)
  │
  ▼ rl.tryAcquire(waitTime, MILLISECONDS)
  ├─ true  → mi.proceed()
  └─ false → waitTimeoutHandler.handleWaitTimeout(mi)
```

## 6. 切面顺序问题（与 @Transactional 联用时）

假设用户写：

```java
@Transactional
@Warlock(name = "u", key = "#id")
public void updateUser(Long id) { ... }
```

两个 Advisor 都匹配，Spring 按 `getOrder()` 排序。两者都默认 `LOWEST_PRECEDENCE`，顺序**不稳定**。

**坏情况**（事务在外层）：

```
enter TX
  enter Warlock
    tryLock success
      proceed (UPDATE ...)      ← 数据改在本事务内, 其他节点看不到
    unlock                      ← 🔴 锁先释放!
  exit Warlock
  (commit TX)                   ← 事务才提交
exit TX

其他节点: tryLock success → SELECT → 读到旧数据 (事务未提交前)
```

**好情况**（Warlock 在外层）：

```
enter Warlock
  tryLock success
    enter TX
      proceed (UPDATE ...)
    commit TX                   ← 先提交
  unlock                        ← 再释放锁
exit Warlock
```

→ **必须让 Warlock Advisor 的 order 比事务 Advisor 低**（低 = 外层）。当前代码未处理，属于待修复项。

## 7. 超时 handler 查找时序

```
factory.build(mi)
  │
  ▼ timeoutHandlerProvider.getWaitTimeoutHandler(waiting)
  ├─ Class<? extends WaitTimeoutHandler> cls = waiting.waitTimeoutHandler()
  ├─ if cls == FastFailTimeoutHandler.class → return INSTANCE   (无 Spring 查找)
  ├─ if cls == IgnoreTimeoutHandler.class   → return INSTANCE
  └─ else → beanFactory.getBean(cls)                             (Spring 查找)
           │
           ├─ 找到 → 返回
           └─ 找不到 → 抛 NoSuchBeanDefinitionException (向外传播)
```

关键：**每次 build 都会查一次 Bean**。Spring 的 `DefaultListableBeanFactory.getBean(Class)` 内部有缓存，但高频调用下仍有小开销。可以考虑在工厂里按 `Class` 缓存 handler 实例。

---

## 总结：每次方法调用的开销

按 Benchmark 结果（README）逆向拆解，~65μs/op（JDK 11）的耗时大致分布：

| 环节 | 占比 | 原因 |
|---|---|---|
| SpEL 求值 | ~40% | `parseExpression` 每次重新解析 + 参数反射 |
| AOP 代理 | ~15% | Spring 代理调度（不可避免） |
| 锁原语本身 | ~25% | `ReentrantLock.tryLock` |
| Map 操作 | ~10% | `compute` / `computeIfPresent` |
| 其他 | ~10% | 反射读注解、handler 查找等 |

**优化优先级**：缓存 SpEL `Expression` > 缓存 handler 实例 > 缓存注解元信息 > Map 结构优化。
