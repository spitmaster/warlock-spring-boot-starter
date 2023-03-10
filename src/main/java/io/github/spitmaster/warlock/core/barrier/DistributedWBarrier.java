package io.github.spitmaster.warlock.core.barrier;

import org.aspectj.lang.ProceedingJoinPoint;
import org.redisson.api.RedissonClient;

/**
 *
 */
public class DistributedWBarrier implements WBarrier{

    private final BarrierInfo barrierInfo;
    private final RedissonClient redissonClient;

    public DistributedWBarrier(BarrierInfo barrierInfo, RedissonClient redissonClient) {
        this.barrierInfo = barrierInfo;
        this.redissonClient = redissonClient;
    }

    @Override
    public Object doWithBarrier(ProceedingJoinPoint pjp) throws Throwable {
        return null;
    }

    @Override
    public BarrierInfo getBarrierInfo() {
        return barrierInfo;
    }
}
