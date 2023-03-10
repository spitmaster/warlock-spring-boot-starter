package io.github.spitmaster.warlock.core.factory.barrier;

import io.github.spitmaster.warlock.annotation.WcyclicBarrier;
import io.github.spitmaster.warlock.core.barrier.WBarrier;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 生成 WBarrier 对象
 *
 * @author zhouyijin
 */
public interface WBarrierFactory {

    /**
     * 构造一个 WBarrier 对象
     *
     * @param pjp            切点
     * @param wcyclicBarrier 注解
     * @return 构造好的 WBarrier
     */
    WBarrier build(ProceedingJoinPoint pjp, WcyclicBarrier wcyclicBarrier);
}
