package io.github.spitmaster.warlock.handler;

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
    public Object handleLeaseTimeout(ProceedingJoinPoint pjp, Object result) throws Throwable {
        //空实现, 原样返回函数执行结果
        return result;
    }
}
