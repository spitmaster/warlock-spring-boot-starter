package io.github.spitmaster.warlock.core.lock;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 空实现
 *
 * @author zhouyijin
 */
public enum PlainWarlock implements Wlock {
    //单例
    INSTANCE;

    @Override
    public Object doWithLock(ProceedingJoinPoint pjp) throws Throwable {
        return pjp.proceed();
    }
}