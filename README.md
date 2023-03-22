# warlock-spring-boot-starter

![Warlock](warlock_icon_2.png)

Lock for spring!

warlock-spring-boot-starter is an annotation-driven concurrency tools library

It is easy to use in Spring application

Just using annotation on your method , the concurrency problem would be solved

> base on Spring AOP
> 
> https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#aop-api

# Quick Start

---

## Requirements

* JDK11 or higher
* Spring ....
* Redisson (Optional, if you want to use distributed lock etc.)

### Dependency
```xml
<dependency>
    <groupId>io.github.spitmaster</groupId>
    <artifactId>warlock-spring-boot-starter</artifactId>
    <version>0.0.3</version>
</dependency>
```
---

---

## Usage

---

### 1. Lock

How to use ...

1. Add `@Warlock()` Annotation on your method
2. Done

```java
@Component
public class WarlockSample {
    @Warlock(name = "uniquename1", key = "#dto.userId")
    public void doBiz(StudentDto dto) {
        //your business code
    }
}
```

And `@Warlock` support various condition

such as `Scope.STANDALONE` and `Scope.DISTRIBUTED`

and support three lock type for using `LockType.REENTRANT` , `LockType.READ` , `LockType.WRITE`


---


### 2. RateLimiter

WrateLimiter provide 2 scope RateLimiter
1. `Scope.STANDALONE` base on guava
2. `Scope.DISTRIBUTED`  base on redisson

```java
@Component
public class WrateLimiterSample {
    @WrateLimiter(name = "testRateLimiter",
            key = "#id",
            permitsPerSecond = 2, //每秒允许两个请求
            scope = Scope.DISTRIBUTED,
            waiting = @Waiting(waitTime = 1, timeUnit = TimeUnit.SECONDS, waitTimeoutHandler = StandaloneRateLimiterTestService.class) //超时处理策略
    )
    public void doBiz(int id) {
        //your business code
    }
}
```

---

### 3. CyclicBarrier

WcyclicBarrier only support standalone scope
Cause redisson not implement CyclicBarrier
and i dont have sufficient ability to creating a distributed CyclicBarrier

```java
@Component
public class WcyclicBarrierSample {

    @WcyclicBarrier(
            parties = 2,
            name = "add1",
            key = "#id",
            waiting = @Waiting(waitTime = 1, timeUnit = TimeUnit.SECONDS)
    )
    public void doBiz(int id) {
        //your business code
    }
}
```

---

### 4. Semaphore

```java
@Component
public class WsemaphoreSample {

    @Wsemaphore(name = "mys1",
            key = "#id",
            permits = 7,
            waiting = @Waiting(waitTime = 1, timeUnit = TimeUnit.SECONDS, waitTimeoutHandler = SemaphoreAspectTestService.class),
            scope = Scope.STANDALONE)
    public void doBiz(String id) {
        //only allow 7 threads running at the same time
        //the 7 threads share a same key (name + key(this is a SpEL))
        //do your biz ....
    }
}
```

---

