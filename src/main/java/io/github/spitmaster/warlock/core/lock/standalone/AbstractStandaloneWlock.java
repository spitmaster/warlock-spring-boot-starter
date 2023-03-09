package io.github.spitmaster.warlock.core.lock.standalone;

import io.github.spitmaster.warlock.core.lock.Wlock;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * 单机上使用的warlock基类
 * 规定了如何拿锁上锁解锁还锁
 * 以及在有超时策略下的行为
 *
 * @author zhouyijin
 */
abstract class AbstractStandaloneWlock implements Wlock {

    @Override
    public Object doWithLock(ProceedingJoinPoint pjp) throws Throwable {
        //1. 拿锁
        Lock lock = getLock();
        //是否成功获取到锁
        boolean acquired = false;
        try {
            //2. 上锁
            acquired = lock.tryLock(getLockInfo().getWaitTime().toMillis(), TimeUnit.MILLISECONDS);
            if (acquired) {
                //3. 执行业务代码
                return pjp.proceed();
            } else {
                return getLockInfo().getWaitTimeoutHandler().handleWaitTimeout(pjp);
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
