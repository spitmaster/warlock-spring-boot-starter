package com.zyj.warlock.core;

import com.zyj.warlock.annotation.Leasing;
import com.zyj.warlock.annotation.Waiting;
import com.zyj.warlock.annotation.Wlock;
import com.zyj.warlock.core.lock.PlainWarlock;
import com.zyj.warlock.core.lock.standalone.ReadWarlock;
import com.zyj.warlock.core.lock.standalone.ReentrantWarlock;
import com.zyj.warlock.core.lock.standalone.WriteWarlock;
import com.zyj.warlock.handler.LeaseTimeoutHandler;
import com.zyj.warlock.handler.PlainLeaseTimeoutHandler;
import com.zyj.warlock.handler.PlainWaitTimeoutHandler;
import com.zyj.warlock.handler.WaitTimeoutHandler;
import com.zyj.warlock.util.SpelExpressionUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * WarlockFactory的简易实现
 *
 * @author zhouyijin
 */
public class DefaultWarlockFactory implements WarlockFactory, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Warlock build(ProceedingJoinPoint pjp, Wlock wlock) {
        //1. 构造锁
        LockInfo lockInfo = buildLock(pjp, wlock);

        //2. 根据锁类型选择合适的锁
        //According lock type decide what warlock should be used
        Warlock warlock;
        switch (lockInfo.getLockType()) {
            case REENTRANT:
                warlock = new ReentrantWarlock(lockInfo);
                break;
            case READ:
                warlock = new ReadWarlock(lockInfo);
                break;
            case WRITE:
                warlock = new WriteWarlock(lockInfo);
                break;
            default:
                warlock = PlainWarlock.INSTANCE;
                break;
        }
        return warlock;
    }

    private LockInfo buildLock(ProceedingJoinPoint pjp, Wlock wlock) {
        LockInfo lockInfo = new LockInfo();
        //1. 构造lockKey
        //收集锁的信息
        // 获取方法参数值
        Object[] arguments = pjp.getArgs();
        // 获取method
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();

        String lockName = wlock.name();
        // 获取spel表达式
        String keySpEL = wlock.key();
        String key = SpelExpressionUtil.parseSpel(method, arguments, keySpEL, String.class);

        /*
         * construct a lockkey that indicate a unique lock
         * this lock would be used in Warlock.beforeBiz and Warlock.afterBiz and Warlock.except
         */
        String lockKey = lockName + key;
        lockInfo.setLockKey(lockKey);
        //2. 拿到lockType
        lockInfo.setLockType(wlock.lockType());
        //3. 获取等待时间
        Waiting waiting = wlock.waiting();
        Duration waitTime = Duration.of(waiting.waitTime(), waiting.timeUnit().toChronoUnit());
        lockInfo.setWaitTime(waitTime);
        lockInfo.setWaitTimeoutHandler(getWaitTimeoutHandler(waiting));
        //4. 获取等待时间
        Leasing leasing = wlock.leasing();
        Duration leaseTime = Duration.of(leasing.leaseTime(), leasing.timeUnit().toChronoUnit());
        lockInfo.setLeaseTime(leaseTime);
        lockInfo.setLeaseTimeoutHandler(getLeaseTimeoutHandler(leasing));
        //5. 返回锁信息
        return lockInfo;
    }

    private WaitTimeoutHandler getWaitTimeoutHandler(Waiting waiting) {
        Class<? extends WaitTimeoutHandler> waitTimeoutHandlerClass = waiting.waitTimeoutHandler();
        if (waitTimeoutHandlerClass != null && waitTimeoutHandlerClass != PlainWaitTimeoutHandler.class) {
            ObjectProvider<? extends WaitTimeoutHandler> beanProvider = applicationContext.getBeanProvider(waitTimeoutHandlerClass);
            WaitTimeoutHandler handler = beanProvider.getIfAvailable();
            if (handler != null) {
                return handler;
            }
        }
        return PlainWaitTimeoutHandler.INSTANCE;
    }

    private LeaseTimeoutHandler getLeaseTimeoutHandler(Leasing leasing) {
        Class<? extends LeaseTimeoutHandler> leaseTimeoutHandlerClass = leasing.leaseTimeoutHandler();
        if (leaseTimeoutHandlerClass != null && leaseTimeoutHandlerClass != PlainLeaseTimeoutHandler.class) {
            ObjectProvider<? extends LeaseTimeoutHandler> beanProvider = applicationContext.getBeanProvider(leaseTimeoutHandlerClass);
            LeaseTimeoutHandler handler = beanProvider.getIfAvailable();
            if (handler != null) {
                return handler;
            }
        }
        return PlainLeaseTimeoutHandler.INSTANCE;
    }
}