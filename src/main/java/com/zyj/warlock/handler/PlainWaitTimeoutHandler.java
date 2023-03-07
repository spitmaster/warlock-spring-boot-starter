package com.zyj.warlock.handler;

import com.zyj.warlock.core.lock.LockInfo;
import com.zyj.warlock.exceptions.WarlockException;
import com.zyj.warlock.util.JoinPointUtil;
import org.aspectj.lang.ProceedingJoinPoint;


/**
 * 单例空实现
 *
 * @author zhouyijin
 */
public enum PlainWaitTimeoutHandler implements WaitTimeoutHandler {
    //单例
    INSTANCE;

    @Override
    public Object handle(ProceedingJoinPoint pjp, LockInfo lockInfo) throws Throwable {
        throw new WarlockException("warlock wait timeout; timeout from " + JoinPointUtil.methodName(pjp));
    }
}