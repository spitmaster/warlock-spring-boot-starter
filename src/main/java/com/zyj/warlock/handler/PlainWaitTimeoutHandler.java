package com.zyj.warlock.handler;

import com.zyj.warlock.core.LockInfo;
import com.zyj.warlock.exceptions.WarlockException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;


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
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        throw new WarlockException("warlock wait timeout; timeout from " + method.getName());
    }
}