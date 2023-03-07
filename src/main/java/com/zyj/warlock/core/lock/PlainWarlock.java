package com.zyj.warlock.core.lock;

import com.zyj.warlock.core.Warlock;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 空实现
 *
 * @author zhouyijin
 */
public enum PlainWarlock implements Warlock {
    //单例
    INSTANCE;

    @Override
    public Object doWithLock(ProceedingJoinPoint pjp) throws Throwable {
        return pjp.proceed();
    }
}