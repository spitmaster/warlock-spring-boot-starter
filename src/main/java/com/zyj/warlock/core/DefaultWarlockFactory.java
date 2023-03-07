package com.zyj.warlock.core;

import com.zyj.warlock.annotation.Wlock;
import com.zyj.warlock.core.lock.ReadWarlock;
import com.zyj.warlock.core.lock.ReentrantWarlock;
import com.zyj.warlock.core.lock.WriteWarlock;
import com.zyj.warlock.util.SpelExpressionUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * WarlockFactory的简易实现
 *
 * @author zhouyijin
 */
public class DefaultWarlockFactory implements WarlockFactory {

    private static final Warlock BLANK_WARLOCK = new BlankWarlock();

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
                warlock = BLANK_WARLOCK;
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


    /**
     * 空实现
     */
    private static class BlankWarlock implements Warlock {
        @Override
        public Object doWithLock(ProceedingJoinPoint pjp) throws Throwable {
            return pjp.proceed();
        }
    }
}
