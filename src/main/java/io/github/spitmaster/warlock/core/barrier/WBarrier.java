package io.github.spitmaster.warlock.core.barrier;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.lang.NonNull;

/**
 * 我是傻逼
 *
 * @author zhouyijin
 */
public interface WBarrier {

    /**
     * 在WCyclicBarrier的环境下执行业务函数
     *
     * @param pjp 切点
     * @return 业务函数的返回值
     * @throws Throwable 业务方法可能抛出的异常
     */
    Object doWithBarrier(ProceedingJoinPoint pjp) throws Throwable;

    /**
     * WCyclicBarrier的信息
     *
     * @return BarrierInfo 对象, 不能为空
     */
    @NonNull
    BarrierInfo getBarrierInfo();
}
