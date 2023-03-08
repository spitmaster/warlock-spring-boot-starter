package io.github.spitmaster.warlock.core.semaphore;

import io.github.spitmaster.warlock.annotation.Wsemaphore;
import org.aspectj.lang.ProceedingJoinPoint;
import org.redisson.Redisson;
import org.redisson.api.RPermitExpirableSemaphore;

public class DistributedWmutex implements Wmutex {

    private final String semaphoreKey;
    private final Wsemaphore wsemaphore;
    private final Redisson redisson;

    public DistributedWmutex(String semaphoreKey, Wsemaphore wsemaphore, Redisson redisson) {
        this.semaphoreKey = semaphoreKey;
        this.wsemaphore = wsemaphore;
        this.redisson = redisson;
    }

    @Override
    public Object doBizWithSemaphore(ProceedingJoinPoint pjp, Wsemaphore wsemaphore) throws Throwable {
        //1. 通过redisson获取信号量
        RPermitExpirableSemaphore semaphore = redisson.getPermitExpirableSemaphore(semaphoreKey);
        //2. 如果初次使用该信号量, 则会设置该信号量的permit数量
        semaphore.trySetPermits(wsemaphore.permits());
        //2. 尝试获取1个permit
        String permitId = semaphore.acquire();
        try {
            //3. 执行业务代码
            return pjp.proceed();
        } finally {
            //4. 释放信号量
            semaphore.release(permitId);
        }
    }
}
