package com.zyj.warlock.handler;

import com.zyj.warlock.core.lock.LockInfo;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 默认的空实现, 什么都不处理
 *
 * @author zhouyijin
 */
public enum PlainLeaseTimeoutHandler implements LeaseTimeoutHandler {
    //单例
    INSTANCE;

    @Override
    public Object handle(ProceedingJoinPoint pjp, LockInfo lockInfo) throws Throwable {
        //空实现
        return null;
    }
}
