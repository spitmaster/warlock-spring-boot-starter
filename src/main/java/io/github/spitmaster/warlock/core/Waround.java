package io.github.spitmaster.warlock.core;

import org.aopalliance.intercept.MethodInvocation;

public interface Waround {

    /**
     * 在自定义的环境下执行业务函数
     *
     * @param methodInvocation 切点
     * @return 业务函数的返回值
     * @throws Throwable 业务方法可能抛出的异常
     */
    Object doAround(MethodInvocation methodInvocation) throws Throwable;
}
