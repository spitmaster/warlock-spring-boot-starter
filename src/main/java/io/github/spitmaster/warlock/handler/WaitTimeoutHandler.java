package io.github.spitmaster.warlock.handler;

import org.aopalliance.intercept.MethodInvocation;

/**
 * 当等待超时的时候触发的回调对象
 *
 * @author zhouyijin
 */
public interface WaitTimeoutHandler {

    /**
     * 当锁等待超时的时候触发的回调方法
     *
     * @param methodInvocation 方法切点
     * @return 替代原来的业务方法的返回值
     * @throws Throwable pjp操作可能会抛出的异常
     */
    Object handleWaitTimeout(MethodInvocation methodInvocation) throws Throwable;

}
