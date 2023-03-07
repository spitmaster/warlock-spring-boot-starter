package com.zyj.warlock.core.lock;

import com.zyj.warlock.core.LockInfo;
import com.zyj.warlock.core.Warlock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.lang.NonNull;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * 单机上使用的warlock基类
 * 规定了如何拿锁上锁解锁还锁
 * 以及在有超时策略下的行为
 *
 * @author zhouyijin
 */
abstract class AbstractStandaloneWarlock implements Warlock {

    @Override
    public Object doWithLock(ProceedingJoinPoint pjp) throws Throwable {
        Duration waitTime = getLockInfo().getWaitTime();
        if (waitTime.isNegative()) {
            return doWithLock0(pjp);
        } else {
            return doWithTryLock(pjp, waitTime);
        }
    }


    private Object doWithLock0(ProceedingJoinPoint pjp) throws Throwable {
        //1. 拿锁
        Lock lock = getLock();
        //2. 上锁
        lock.lock();
        try {
            //3. 执行业务代码
            return pjp.proceed();
        } finally {
            //4. 解锁
            lock.unlock();
            //5. 还锁
            returnLock();
        }
    }

    private Object doWithTryLock(ProceedingJoinPoint pjp, Duration waitTime) throws Throwable {
        //1. 拿锁
        Lock lock = getLock();
        //是否成功获取到锁
        boolean acquired = false;
        try {
            //2. 上锁
            acquired = lock.tryLock(waitTime.toMillis(), TimeUnit.MILLISECONDS);
            if (acquired) {
                //3. 执行业务代码
                return pjp.proceed();
            } else {
                return getLockInfo().getWaitTimeoutHandler().handle(pjp, this.getLockInfo());
            }
        } finally {
            //4. 解锁
            if (acquired) {
                lock.unlock();
            }
            //5. 还锁
            returnLock();
        }
    }


    /**
     * 锁的信息
     *
     * @return Duration对象, 不能为空
     */
    @NonNull
    protected abstract LockInfo getLockInfo();


    /**
     * 子类实现获取锁的规则
     *
     * @return 获取一个锁
     */
    protected abstract Lock getLock();

    /**
     * 将锁返还
     * 抽象方法中保证, getLock() 必然对应一个 returnLock()
     */
    protected abstract void returnLock();


}
