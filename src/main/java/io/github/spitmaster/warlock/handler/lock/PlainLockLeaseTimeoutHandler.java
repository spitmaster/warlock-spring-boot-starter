package io.github.spitmaster.warlock.handler.lock;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 默认的空实现, 什么都不处理
 *
 * @author zhouyijin
 */
public enum PlainLockLeaseTimeoutHandler implements LeaseTimeoutHandler {
    //单例
    INSTANCE;

    @Override
    public Object handleLeaseTimeout(ProceedingJoinPoint pjp) throws Throwable {
        //空实现
        return null;
    }
}
