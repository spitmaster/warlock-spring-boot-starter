package com.zyj.warlock.core;

import org.aspectj.lang.ProceedingJoinPoint;

public class WarlockProcessor {

    /**
     * 在使用warlock的前提下, 调用函数
     *
     * @param lockInfo 锁的信息
     * @param pjp      切点
     * @return 函数调用结果
     */
    public Object invokeWithWarlock(LockInfo lockInfo, ProceedingJoinPoint pjp) throws Throwable{
        // TODO: 2023/3/6 在这加锁
        return pjp.proceed();
    }
}
