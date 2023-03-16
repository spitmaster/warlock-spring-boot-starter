package io.github.spitmaster.warlock.core.barrier;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.lang.NonNull;

/**
 * 我是傻逼
 *
 * @author zhouyijin
 */
public interface Wbarrier {

    /**
     * 在WCyclicBarrier的环境下执行业务函数
     *
     * @param methodInvocation 切点
     * @return 业务函数的返回值
     * @throws Throwable 业务方法可能抛出的异常
     */
    Object doWithBarrier(MethodInvocation methodInvocation) throws Throwable;

    /**
     * WCyclicBarrier的信息
     *
     * @return BarrierInfo 对象, 不能为空
     */
    @NonNull
    BarrierInfo getBarrierInfo();
}
