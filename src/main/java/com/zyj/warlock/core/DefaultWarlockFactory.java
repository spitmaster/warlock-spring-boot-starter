package com.zyj.warlock.core;

import com.zyj.warlock.annotation.Waiting;
import com.zyj.warlock.annotation.Wlock;
import com.zyj.warlock.core.lock.standalone.ReadWarlock;
import com.zyj.warlock.core.lock.standalone.ReentrantWarlock;
import com.zyj.warlock.core.lock.standalone.WriteWarlock;
import com.zyj.warlock.exceptions.WarlockException;
import com.zyj.warlock.handler.WaitTimeoutHandler;
import com.zyj.warlock.util.SpelExpressionUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
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

    private static final Warlock PLAIN_WARLOCK = new PlainWarlock();
    private static final PlainWaitTimeoutHandler PLAIN_WAIT_TIMEOUT_HANDLER = new PlainWaitTimeoutHandler();

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
                warlock = PLAIN_WARLOCK;
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
        Duration waitTime = Duration.of(wlock.waiting().waitTime(), wlock.waiting().timeUnit().toChronoUnit());
        lockInfo.setWaitTime(waitTime);
        //4. 获取等待时间
        Duration leaseTime = Duration.of(wlock.leasing().leaseTime(), wlock.leasing().timeUnit().toChronoUnit());
        lockInfo.setLeaseTime(leaseTime);
        //5. 返回锁信息
        return lockInfo;
    }

    private WaitTimeoutHandler getWaitTimeoutHandler(Waiting waiting) {
        Class<? extends WaitTimeoutHandler> waitTimeoutHandlerClass = waiting.waitTimeoutHandler();
        if (waitTimeoutHandlerClass == null) {
            return PLAIN_WAIT_TIMEOUT_HANDLER;
        } else {
            return applicationContext.getBean(waitTimeoutHandlerClass);
        }
    }


    /**
     * 空实现
     */
    private static class PlainWarlock implements Warlock {
        @Override
        public Object doWithLock(ProceedingJoinPoint pjp) throws Throwable {
            return pjp.proceed();
        }
    }

    private static class PlainWaitTimeoutHandler implements WaitTimeoutHandler {
        @Override
        public Object handle(ProceedingJoinPoint pjp, LockInfo lockInfo) throws Throwable {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            Method method = signature.getMethod();
            throw new WarlockException("warlock wait timeout; timeout from " + method.getName());
        }
    }
}
