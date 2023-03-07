package com.zyj.warlock.core.lock.dist;

import com.zyj.warlock.core.lock.LockInfo;
import com.zyj.warlock.core.lock.Wlock;
import com.zyj.warlock.core.lock.DefaultWlock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.redisson.api.RLock;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁的基本操作
 *
 * @author zhouyijin
 */
abstract class AbstractDistributedWlock implements Wlock, DefaultWlock {

    @Override
    public Object doWithLock(ProceedingJoinPoint pjp) throws Throwable {
        Duration waitTime = getLockInfo().getWaitTime();
        Duration leaseTime = getLockInfo().getLeaseTime();
        if (waitTime.isNegative() && leaseTime.isNegative()) {
            return doWithLock0(pjp);
        } else {
            return doWithTryLock(pjp);
        }
    }

    private Object doWithLock0(ProceedingJoinPoint pjp) throws Throwable {
        //1. 拿锁
        RLock lock = getRLock();
        //2. 上锁
        lock.lock();
        try {
            //3. 执行业务代码
            return pjp.proceed();
        } finally {
            //4. 解锁
            lock.unlock();
        }
    }


    private Object doWithTryLock(ProceedingJoinPoint pjp) throws Throwable {
        //1. 拿锁
        RLock lock = getRLock();
        //是否成功获取到锁
        boolean acquired = false;
        Object result = null;
        try {
            LockInfo lockInfo = getLockInfo();
            Duration waitTime = lockInfo.getWaitTime();
            Duration leaseTime = lockInfo.getLeaseTime();
            //2. 上锁
            acquired = lock.tryLock(waitTime.toMillis(), leaseTime.toMillis(), TimeUnit.MILLISECONDS);//NOSONAR
            if (acquired) {
                //3. 执行业务代码
                result = pjp.proceed();
            } else {
                result = getLockInfo().getWaitTimeoutHandler().handle(pjp, this.getLockInfo());
            }
        } finally {
            //4. 解锁
            if (acquired) {
                if (lock.isHeldByCurrentThread()) {
                    //获取锁成功, 并且自己还持有锁, 则需要释放锁
                    lock.unlock();
                } else {
                    //获取锁成功, 但是现在已经不持有锁了, 说明锁超时了, 或者超时之后被其他线程获取到
                    //此时不需要解锁
                    //但是需要回调超时的handler
                    Object leaseTimeoutHandleResult = getLockInfo().getLockLeaseTimeoutHandler().handle(pjp, getLockInfo());
                    if (leaseTimeoutHandleResult != null) {
                        result = leaseTimeoutHandleResult;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 获取Redisson实现的锁
     *
     * @return 锁
     */
    protected abstract RLock getRLock();
}
