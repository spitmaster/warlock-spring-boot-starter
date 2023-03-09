package io.github.spitmaster.warlock.core.semaphore;

import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
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

    private final SemaphoreInfo semaphoreInfo;

    public StandaloneWmutex(SemaphoreInfo semaphoreInfo) {
        this.semaphoreInfo = semaphoreInfo;
    }

    @Override
    public Object doBizWithSemaphore(ProceedingJoinPoint pjp) throws Throwable {
        //1. 懒加载信号量对象
        Semaphore semaphore = getSemaphore();
        boolean acquired = false;
        try {
            //2. 尝试获取permit
            acquired = semaphore.tryAcquire(semaphoreInfo.getWaitTime().toMillis(), TimeUnit.MILLISECONDS);
            if (acquired) {
                //3. 执行业务代码
                return pjp.proceed();
            } else {
                return semaphoreInfo.getWaitTimeoutHandler().handleWaitTimeout(pjp);
            }
        } finally {
            //4. 归还permit
            if (acquired) {
                semaphore.release();
            }
            //5. 归还信号量
            returnSemaphore();
        }
    }

    protected Semaphore getSemaphore() {
        Pair<Semaphore, AtomicInteger> lockPair = SEMAPHORE_MAP.compute(semaphoreInfo.getSemaphoreKey(), (s, pair) -> {
            if (pair == null) {
                //没有就初始化
                pair = Pair.of(new Semaphore(semaphoreInfo.getPermits()), new AtomicInteger(0));
            }
            pair.getRight().incrementAndGet();
            return pair;
        });
        return lockPair.getLeft();
    }

    protected void returnSemaphore() {
        SEMAPHORE_MAP.computeIfPresent(semaphoreInfo.getSemaphoreKey(), (s, pair) -> {
            int holdCount = pair.getRight().decrementAndGet();
            if (holdCount <= 0) {
                //返回null,相当于把这个信号量删除了
                return null;
            }
            return pair;
        });
    }


    @Override
    public SemaphoreInfo getSemaphoreInfo() {
        return this.semaphoreInfo;
    }
}