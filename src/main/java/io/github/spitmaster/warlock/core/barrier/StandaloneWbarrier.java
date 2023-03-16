package io.github.spitmaster.warlock.core.barrier;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于JUC的CyclicBarrier实现
 *
 * @author zhouyijin
 */
public class StandaloneWbarrier implements Wbarrier {

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
            //2. 等待超时, 或者其他同时在等的线程超时, 那么这个 cyclicBarrier 就会 Broken
            cyclicBarrier.await(barrierInfo.getWaitTime().toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
            //3. 修复 cyclicBarrier, 并进行超时处理
            //一旦Broken这个CyclicBarrier就相当于废了, 要立刻恢复,
            // 但是原来的 cyclicBarrier 实际上已经不能继续用了, 因为还有许多其他线程在不停的处理Exception,
            // 那么这时候就需要将Map中的换掉, 重新初始化
            return processBrokenBarrier(methodInvocation, cyclicBarrier, e);
        }
        return methodInvocation.proceed();
    }

    private Object processBrokenBarrier(MethodInvocation methodInvocation, CyclicBarrier cyclicBarrier, Exception e) throws Throwable {
        Method method = methodInvocation.getMethod();
        LOGGER.error("processBrokenBarrier; method={}", method, e);
        //使用同一个Barrier的需要一起处理, 不能各处理各的
        synchronized (cyclicBarrier) {
            CyclicBarrier newCyclicBarrier = this.getCyclicBarrier();
            if (cyclicBarrier == newCyclicBarrier) {
                //说明本线程是第一个进入 synchronized 代码块的线程
                //需要将已有的 barrier废弃掉, 其他线程再进来, 他们会自己初始化
                this.returnCyclicBarrier();
            }
        }
        //超时处理
        return this.barrierInfo.getWaitTimeoutHandler().handleWaitTimeout(methodInvocation);
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

    @Override
    public BarrierInfo getBarrierInfo() {
        return this.barrierInfo;
    }
}
