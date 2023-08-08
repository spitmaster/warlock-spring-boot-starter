package io.github.spitmaster.warlock.handler;

import io.github.spitmaster.warlock.exceptions.WarlockException;
import org.aopalliance.intercept.MethodInvocation;


/**
 * 单例空实现
 *
 * @author zhouyijin
 */
public enum FastFailWaitTimeoutHandler implements WaitTimeoutHandler {
    //单例
    INSTANCE;

    @Override
    public Object handleWaitTimeout(MethodInvocation methodInvocation) throws Throwable {
        throw new WarlockException("wait timeout; timeout from " + methodInvocation.getMethod().getName());
    }
}