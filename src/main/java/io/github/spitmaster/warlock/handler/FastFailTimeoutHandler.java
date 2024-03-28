package io.github.spitmaster.warlock.handler;

import io.github.spitmaster.warlock.exceptions.WarlockException;
import io.github.spitmaster.warlock.util.MethodNameUtil;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;

import java.lang.reflect.Method;

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

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(FastFailTimeoutHandler.class);

    @Override
    public Object handleWaitTimeout(MethodInvocation methodInvocation) throws Throwable {
        Method method = methodInvocation.getMethod();
        String methodReference = MethodNameUtil.methodName(method);
        LOGGER.warn("FastFailTimeoutHandler wait timeout; timeout from {}", methodReference);
        throw new WarlockException("wait timeout; timeout from " + methodReference);
    }

    @Override
    public Object handleLeaseTimeout(MethodInvocation methodInvocation, Object result) throws Throwable {
        Method method = methodInvocation.getMethod();
        String methodReference = MethodNameUtil.methodName(method);
        LOGGER.warn("IgnoreTimeoutHandler lease timeout; timeout from {}", methodReference);
        throw new WarlockException("lease timeout; timeout from " + methodReference);
    }

}
