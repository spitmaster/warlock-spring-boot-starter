package io.github.spitmaster.warlock.handler;

import io.github.spitmaster.warlock.exceptions.WarlockException;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 超时抛出异常
 *
 * @author zhouyijin
 */
public enum FastFailLeaseTimeoutHandler implements LeaseTimeoutHandler {
    //单例
    INSTANCE;

    @Override
    public Object handleLeaseTimeout(MethodInvocation methodInvocation, Object result) throws Throwable {
        throw new WarlockException("lease timeout; timeout from " + methodInvocation.getMethod().getName());
    }
}
