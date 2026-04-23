package io.github.spitmaster.warlock.core.barrier;

import io.github.spitmaster.warlock.core.Waround;
import io.github.spitmaster.warlock.util.Pair;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于JUC的CyclicBarrier实现
 *
 * @author zhouyijin
 */
public class StandaloneWbarrier implements Waround {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandaloneWbarrier.class);

    private static final ConcurrentHashMap<String, Pair<CyclicBarrier, AtomicInteger>> CYCLIC_BARRIER_MAP = new ConcurrentHashMap<>();

    private final BarrierInfo barrierInfo;

    public StandaloneWbarrier(BarrierInfo barrierInfo) {
        this.barrierInfo = barrierInfo;
    }

    @Override
    public Object doAround(MethodInvocation methodInvocation) throws Throwable {
        //1. 获取这个key对应的 cyclicBarrier
        CyclicBarrier cyclicBarrier = this.getCyclicBarrier();
        try {
            try {
                //2. 等待超时, 或者其他同时在等的线程超时, 那么这个 cyclicBarrier 就会 Broken
                cyclicBarrier.await(barrierInfo.getWaitTime().toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
                //一旦Broken, 所有持有此barrier的线程都会走到这里并走 waitTimeoutHandler
                //各线程各自在 finally 中 returnCyclicBarrier, 当引用计数归零时从Map中移除
                //下一个进入的线程自然会初始化一个全新的barrier
                LOGGER.error("barrier broken; method={}", methodInvocation.getMethod(), e);
                return this.barrierInfo.getWaitTimeoutHandler().handleWaitTimeout(methodInvocation);
            }
            return methodInvocation.proceed();
        } finally {
            //3. 归还barrier引用, 引用计数归零则移除Map条目
            returnCyclicBarrier();
        }
    }

    /**
     * 根据key获取CyclicBarrier, 懒加载
     *
     * @return CyclicBarrier
     */
    protected CyclicBarrier getCyclicBarrier() {
        Pair<CyclicBarrier, AtomicInteger> barrierPair = CYCLIC_BARRIER_MAP.compute(barrierInfo.getBarrierKey(), (s, pair) -> {
            if (pair == null) {
                //没有就初始化
                pair = Pair.of(new CyclicBarrier(barrierInfo.getParties()), new AtomicInteger(0));
            }
            pair.getRight().incrementAndGet();
            return pair;
        });
        return barrierPair.getLeft();
    }

    /**
     * 销毁map中的 CyclicBarrier
     */
    protected void returnCyclicBarrier() {
        CYCLIC_BARRIER_MAP.computeIfPresent(barrierInfo.getBarrierKey(), (s, pair) -> {
            int holdCount = pair.getRight().decrementAndGet();
            if (holdCount <= 0) {
                //返回null,相当于把这个信号量删除了
                return null;
            }
            return pair;
        });
    }

}
