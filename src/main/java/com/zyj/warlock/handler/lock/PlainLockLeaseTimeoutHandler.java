package com.zyj.warlock.handler.lock;

import com.zyj.warlock.core.lock.LockInfo;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 默认的空实现, 什么都不处理
 *
 * @author zhouyijin
 */
public enum PlainLockLeaseTimeoutHandler implements LockLeaseTimeoutHandler {
    //单例
    INSTANCE;

    @Override
    public Object handle(ProceedingJoinPoint pjp, LockInfo lockInfo) throws Throwable {
        //空实现
        return null;
    }
}
