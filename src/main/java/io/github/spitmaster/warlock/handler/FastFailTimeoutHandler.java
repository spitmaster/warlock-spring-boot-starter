package io.github.spitmaster.warlock.handler;

import io.github.spitmaster.warlock.exceptions.WarlockException;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 超时抛出异常
 * 等待超时: 抛出异常, 方法不执行
 * 执行超时: 抛出异常, 方法虽然执行完毕, 但丢弃返回值
 *
 * @author zhouyijin
 */
public enum FastFailTimeoutHandler implements WaitTimeoutHandler, LeaseTimeoutHandler {
    //单例
    INSTANCE;

    @Override
    public Object handleWaitTimeout(MethodInvocation methodInvocation) throws Throwable {
        throw new WarlockException("wait timeout; timeout from " + methodInvocation.getMethod().getName());
    }

    @Override
    public Object handleLeaseTimeout(MethodInvocation methodInvocation, Object result) throws Throwable {
        throw new WarlockException("lease timeout; timeout from " + methodInvocation.getMethod().getName());
    }

}
