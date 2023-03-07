package com.zyj.warlock.core.factory;

import com.zyj.warlock.annotation.Leasing;
import com.zyj.warlock.annotation.Waiting;
import com.zyj.warlock.annotation.Warlock;
import com.zyj.warlock.core.LockInfo;
import com.zyj.warlock.handler.LeaseTimeoutHandler;
import com.zyj.warlock.handler.PlainLeaseTimeoutHandler;
import com.zyj.warlock.handler.PlainWaitTimeoutHandler;
import com.zyj.warlock.handler.WaitTimeoutHandler;
import com.zyj.warlock.util.JoinPointUtil;
import com.zyj.warlock.util.SpelExpressionUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;

import java.lang.reflect.Method;
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
        // 获取方法参数值
        Object[] arguments = pjp.getArgs();
        // 获取method
        Method method = JoinPointUtil.method(pjp);

        String lockName = warlock.name();
        // 获取spel表达式
        String keySpEL = warlock.key();
        String key = SpelExpressionUtil.parseSpel(method, arguments, keySpEL, String.class);

        /*
         * construct a lockkey that indicate a unique lock
         * this lock would be used in Warlock.beforeBiz and Warlock.afterBiz and Warlock.except
         */
        String lockKey = lockName + key;
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
        lockInfo.setLeaseTimeoutHandler(getLeaseTimeoutHandler(leasing));
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
        if (waitTimeoutHandlerClass != null && waitTimeoutHandlerClass != PlainWaitTimeoutHandler.class) {
            ObjectProvider<? extends WaitTimeoutHandler> beanProvider = getBeanFactory().getBeanProvider(waitTimeoutHandlerClass);
            WaitTimeoutHandler handler = beanProvider.getIfAvailable();
            if (handler != null) {
                return handler;
            }
        }
        return PlainWaitTimeoutHandler.INSTANCE;
    }

    /**
     * 根据注解获取处理业务方法超时的handler
     *
     * @param leasing 切面上的注解
     * @return Spring环境中的handler
     */
    protected LeaseTimeoutHandler getLeaseTimeoutHandler(Leasing leasing) {
        Class<? extends LeaseTimeoutHandler> leaseTimeoutHandlerClass = leasing.leaseTimeoutHandler();
        if (leaseTimeoutHandlerClass != null && leaseTimeoutHandlerClass != PlainLeaseTimeoutHandler.class) {
            ObjectProvider<? extends LeaseTimeoutHandler> beanProvider = getBeanFactory().getBeanProvider(leaseTimeoutHandlerClass);
            LeaseTimeoutHandler handler = beanProvider.getIfAvailable();
            if (handler != null) {
                return handler;
            }
        }
        return PlainLeaseTimeoutHandler.INSTANCE;
    }

}
