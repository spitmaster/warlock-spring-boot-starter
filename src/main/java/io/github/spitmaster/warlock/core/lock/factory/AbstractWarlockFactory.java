package io.github.spitmaster.warlock.core.lock.factory;

import io.github.spitmaster.warlock.annotation.Leasing;
import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.annotation.Warlock;
import io.github.spitmaster.warlock.core.lock.LockInfo;
import io.github.spitmaster.warlock.handler.lock.LeaseTimeoutHandler;
import io.github.spitmaster.warlock.handler.lock.PlainLockLeaseTimeoutHandler;
import io.github.spitmaster.warlock.handler.lock.PlainLockWaitTimeoutHandler;
import io.github.spitmaster.warlock.handler.lock.WaitTimeoutHandler;
import io.github.spitmaster.warlock.util.JoinPointUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Duration;

/**
 * 一些公用方法的抽象类
 *
 * @author zhouyijin
 */
abstract class AbstractWarlockFactory {

    /**
     * 子类实现获取BeanFactory
     *
     * @return spring的BeanFactory
     */
    protected abstract BeanFactory getBeanFactory();


    protected LockInfo buildLock(ProceedingJoinPoint pjp, Warlock warlock) {
        LockInfo lockInfo = new LockInfo();
        //1. 构造lockKey
        //收集锁的信息

        /*
         * construct a lockkey that indicate a unique lock
         * this lock would be used in Warlock.beforeBiz and Warlock.afterBiz and Warlock.except
         */
        String lockKey = warlock.name() + JoinPointUtil.parseSpEL(pjp, warlock.key());

        lockInfo.setLockKey(lockKey);
        //2. 拿到lockType
        lockInfo.setLockType(warlock.lockType());
        //3. 获取等待时间
        Waiting waiting = warlock.waiting();
        Duration waitTime = Duration.of(waiting.waitTime(), waiting.timeUnit().toChronoUnit());
        lockInfo.setWaitTime(waitTime);
        lockInfo.setWaitTimeoutHandler(getWaitTimeoutHandler(waiting));
        //4. 获取等待时间
        Leasing leasing = warlock.leasing();
        Duration leaseTime = Duration.of(leasing.leaseTime(), leasing.timeUnit().toChronoUnit());
        lockInfo.setLeaseTime(leaseTime);
        lockInfo.setLockLeaseTimeoutHandler(getLeaseTimeoutHandler(leasing));
        //5. 返回锁信息
        return lockInfo;
    }

    /**
     * 根据注解获取处理等待超时的handler
     *
     * @param waiting 切面上的注解
     * @return Spring环境中的handler
     */
    protected WaitTimeoutHandler getWaitTimeoutHandler(Waiting waiting) {
        Class<? extends WaitTimeoutHandler> waitTimeoutHandlerClass = waiting.waitTimeoutHandler();
        if (waitTimeoutHandlerClass != null && waitTimeoutHandlerClass != PlainLockWaitTimeoutHandler.class) {
            ObjectProvider<? extends WaitTimeoutHandler> beanProvider = getBeanFactory().getBeanProvider(waitTimeoutHandlerClass);
            WaitTimeoutHandler handler = beanProvider.getIfAvailable();
            if (handler != null) {
                return handler;
            }
        }
        return PlainLockWaitTimeoutHandler.INSTANCE;
    }

    /**
     * 根据注解获取处理业务方法超时的handler
     *
     * @param leasing 切面上的注解
     * @return Spring环境中的handler
     */
    protected LeaseTimeoutHandler getLeaseTimeoutHandler(Leasing leasing) {
        Class<? extends LeaseTimeoutHandler> leaseTimeoutHandlerClass = leasing.leaseTimeoutHandler();
        if (leaseTimeoutHandlerClass != null && leaseTimeoutHandlerClass != PlainLockLeaseTimeoutHandler.class) {
            ObjectProvider<? extends LeaseTimeoutHandler> beanProvider = getBeanFactory().getBeanProvider(leaseTimeoutHandlerClass);
            LeaseTimeoutHandler handler = beanProvider.getIfAvailable();
            if (handler != null) {
                return handler;
            }
        }
        return PlainLockLeaseTimeoutHandler.INSTANCE;
    }

}
