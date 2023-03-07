package com.zyj.warlock.core.lock.factory;

import com.zyj.warlock.annotation.Leasing;
import com.zyj.warlock.annotation.Waiting;
import com.zyj.warlock.annotation.Warlock;
import com.zyj.warlock.core.lock.LockInfo;
import com.zyj.warlock.handler.lock.LockLeaseTimeoutHandler;
import com.zyj.warlock.handler.lock.PlainLockLeaseTimeoutHandler;
import com.zyj.warlock.handler.lock.PlainLockWaitTimeoutHandler;
import com.zyj.warlock.handler.lock.LockWaitTimeoutHandler;
import com.zyj.warlock.util.JoinPointUtil;
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
    protected LockWaitTimeoutHandler getWaitTimeoutHandler(Waiting waiting) {
        Class<? extends LockWaitTimeoutHandler> waitTimeoutHandlerClass = waiting.waitTimeoutHandler();
        if (waitTimeoutHandlerClass != null && waitTimeoutHandlerClass != PlainLockWaitTimeoutHandler.class) {
            ObjectProvider<? extends LockWaitTimeoutHandler> beanProvider = getBeanFactory().getBeanProvider(waitTimeoutHandlerClass);
            LockWaitTimeoutHandler handler = beanProvider.getIfAvailable();
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
    protected LockLeaseTimeoutHandler getLeaseTimeoutHandler(Leasing leasing) {
        Class<? extends LockLeaseTimeoutHandler> leaseTimeoutHandlerClass = leasing.leaseTimeoutHandler();
        if (leaseTimeoutHandlerClass != null && leaseTimeoutHandlerClass != PlainLockLeaseTimeoutHandler.class) {
            ObjectProvider<? extends LockLeaseTimeoutHandler> beanProvider = getBeanFactory().getBeanProvider(leaseTimeoutHandlerClass);
            LockLeaseTimeoutHandler handler = beanProvider.getIfAvailable();
            if (handler != null) {
                return handler;
            }
        }
        return PlainLockLeaseTimeoutHandler.INSTANCE;
    }

}
