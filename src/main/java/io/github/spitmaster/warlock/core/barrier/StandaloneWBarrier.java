package io.github.spitmaster.warlock.core.barrier;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 基于JUC的CyclicBarrier实现
 *
 * @author zhouyijin
 */
public class StandaloneWBarrier implements WBarrier {

    private final BarrierInfo barrierInfo;

    public StandaloneWBarrier(BarrierInfo barrierInfo) {
        this.barrierInfo = barrierInfo;
    }

    @Override
    public Object doWithBarrier(ProceedingJoinPoint pjp) throws Throwable {
        return null;
    }

    @Override
    public BarrierInfo getBarrierInfo() {
        return this.barrierInfo;
    }
}
