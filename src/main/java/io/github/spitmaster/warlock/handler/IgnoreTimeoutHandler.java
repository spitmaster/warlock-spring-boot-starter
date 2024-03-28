package io.github.spitmaster.warlock.handler;

import io.github.spitmaster.warlock.util.MethodNameUtil;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;

/**
 * 忽略超时的情况
 * 等待超时: 继续执行
 * 执行超时: 任然返回
 *
 * @author zhouyijin
 */
public enum IgnoreTimeoutHandler implements WaitTimeoutHandler, LeaseTimeoutHandler {

    //单例
    INSTANCE;

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(IgnoreTimeoutHandler.class);

    @Override
    public Object handleWaitTimeout(MethodInvocation methodInvocation) throws Throwable {
        String methodReference = MethodNameUtil.methodName(methodInvocation.getMethod());
        LOGGER.warn("IgnoreTimeoutHandler wait timeout; timeout from {}", methodReference);
        return methodInvocation.proceed();
    }

    @Override
    public Object handleLeaseTimeout(MethodInvocation methodInvocation, Object result) throws Throwable {
        String methodReference = MethodNameUtil.methodName(methodInvocation.getMethod());
        LOGGER.warn("IgnoreTimeoutHandler lease timeout; timeout from {}", methodReference);
        return result;
    }

}