package io.github.spitmaster.warlock.core.lock.dist;

import io.github.spitmaster.warlock.core.Waround;
import io.github.spitmaster.warlock.core.lock.AbstractWlock;
import io.github.spitmaster.warlock.core.lock.LockInfo;
import org.aopalliance.intercept.MethodInvocation;
import org.redisson.api.RLock;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁的基本操作
 *
 * @author zhouyijin
 */
abstract class AbstractDistributedWlock extends AbstractWlock implements Waround {

    @Override
    public Object doAround(MethodInvocation methodInvocation) throws Throwable {
        //1. 拿锁
        RLock lock = getRLock();
        //是否成功获取到锁
        boolean acquired = false;
        //业务是否正常返回 (未抛异常)
        boolean bizCompletedNormally = false;
        Object result = null;
        try {
            LockInfo lockInfo = getLockInfo();
            Duration waitTime = lockInfo.getWaitTime();
            Duration leaseTime = lockInfo.getLeaseTime();
            //2. 上锁
            acquired = lock.tryLock(waitTime.toMillis(), leaseTime.toMillis(), TimeUnit.MILLISECONDS);//NOSONAR
            if (acquired) {
                //3. 执行业务代码
                result = methodInvocation.proceed();
                bizCompletedNormally = true;
            } else {
                result = getLockInfo().getWaitTimeoutHandler().handleWaitTimeout(methodInvocation);
                bizCompletedNormally = true;
            }
        } finally {
            //4. 解锁
            if (acquired) {
                if (lock.isHeldByCurrentThread()) {
                    //获取锁成功, 并且自己还持有锁, 则需要释放锁
                    lock.unlock();
                } else if (bizCompletedNormally) {
                    //获取锁成功, 但是现在已经不持有锁了, 说明锁超时了, 或者超时之后被其他线程获取到
                    //此时不需要解锁
                    //但是需要回调超时的handler, handler执行的结果替换掉原来执行的返回值
                    //若业务已抛异常则不调用handler, 避免 finally 覆盖原异常
                    result = getLockInfo().getLockLeaseTimeoutHandler().handleLeaseTimeout(methodInvocation, result);
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
