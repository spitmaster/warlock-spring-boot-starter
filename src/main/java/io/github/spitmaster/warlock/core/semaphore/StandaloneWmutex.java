package io.github.spitmaster.warlock.core.semaphore;

import io.github.spitmaster.warlock.annotation.Wsemaphore;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 单机使用的信号量
 *
 * @author zhouyijin
 */
public class StandaloneWmutex implements Wmutex {

    /**
     * 多个方法可以使用同一个key
     * 如果使用同一个key, 那么它们使用的就是同一个信号量
     * 在使用信号量的时候会被添加到这个MAP中, 全局使用同一个MAP
     */
    private static final ConcurrentHashMap<String, Pair<Semaphore, AtomicInteger>> SEMAPHORE_MAP = new ConcurrentHashMap<>();

    private final String semaphoreKey;
    private final Wsemaphore wsemaphore;

    public StandaloneWmutex(String semaphoreKey, Wsemaphore wsemaphore) {
        this.semaphoreKey = semaphoreKey;
        this.wsemaphore = wsemaphore;
    }

    @Override
    public Object doBizWithSemaphore(ProceedingJoinPoint pjp, Wsemaphore wsemaphore) throws Throwable {
        //1. 懒加载信号量对象
        Semaphore semaphore = getSemaphore();
        //2. 尝试获取1个permit
        semaphore.acquire();
        try {
            //3. 执行业务代码
            return pjp.proceed();
        } finally {
            //4. 释放信号量
            semaphore.release();
            //5. holdCount-1, 如果没人用这个信号量就会被释放
            returnSemaphore();
        }
    }

    protected Semaphore getSemaphore() {
        Pair<Semaphore, AtomicInteger> lockPair = SEMAPHORE_MAP.compute(semaphoreKey, (s, pair) -> {
            if (pair == null) {
                //没有就初始化
                pair = Pair.of(new Semaphore(wsemaphore.permits()), new AtomicInteger(0));
            }
            pair.getRight().incrementAndGet();
            return pair;
        });
        return lockPair.getLeft();
    }

    protected void returnSemaphore() {
        SEMAPHORE_MAP.computeIfPresent(semaphoreKey, (s, pair) -> {
            int holdCount = pair.getRight().decrementAndGet();
            if (holdCount <= 0) {
                //返回null,相当于把这个信号量删除了
                return null;
            }
            return pair;
        });
    }
}