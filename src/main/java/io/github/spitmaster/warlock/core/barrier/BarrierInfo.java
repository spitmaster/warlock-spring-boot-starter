package io.github.spitmaster.warlock.core.barrier;

import io.github.spitmaster.warlock.annotation.Leasing;
import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.annotation.Wsemaphore;
import io.github.spitmaster.warlock.handler.lock.LeaseTimeoutHandler;
import io.github.spitmaster.warlock.handler.lock.WaitTimeoutHandler;

import java.time.Duration;

/**
 * WCyclicBarrier 所需要的基础信息
 */
public class BarrierInfo {

    /**
     * 唯一标识
     */
    private String barrierKey;

    /**
     * 围栏大小
     */
    private int parties;

    /**
     * 尝试获取锁的时间
     * 超过该时间还未获得锁, 则调用自定义的接口处理, 如果未指定自定义处理的Handler处理, 如果没有指定handler则直接抛异常
     *
     * @see Wsemaphore
     * @see Waiting
     */
    private Duration waitTime;

    /**
     * 等待超时之后的处理策略
     *
     * @see Wsemaphore
     * @see Waiting
     */
    private WaitTimeoutHandler waitTimeoutHandler;

    /**
     * 锁超过租期时间的处理方式
     * Standalone的锁, 没有锁过租期的情况
     *
     * @see Wsemaphore
     * @see Leasing
     */
    private Duration leaseTime;

    /**
     * 执行完业务代码之后, 检查发现锁的租期已经过了, 就会回调这个handler
     *
     * @see Wsemaphore
     * @see Leasing
     */
    private LeaseTimeoutHandler leaseTimeoutHandler;

    public String getBarrierKey() {
        return barrierKey;
    }

    public void setBarrierKey(String barrierKey) {
        this.barrierKey = barrierKey;
    }

    public int getParties() {
        return parties;
    }

    public void setParties(int parties) {
        this.parties = parties;
    }

    public Duration getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(Duration waitTime) {
        this.waitTime = waitTime;
    }

    public WaitTimeoutHandler getWaitTimeoutHandler() {
        return waitTimeoutHandler;
    }

    public void setWaitTimeoutHandler(WaitTimeoutHandler waitTimeoutHandler) {
        this.waitTimeoutHandler = waitTimeoutHandler;
    }

    public Duration getLeaseTime() {
        return leaseTime;
    }

    public void setLeaseTime(Duration leaseTime) {
        this.leaseTime = leaseTime;
    }

    public LeaseTimeoutHandler getLeaseTimeoutHandler() {
        return leaseTimeoutHandler;
    }

    public void setLeaseTimeoutHandler(LeaseTimeoutHandler leaseTimeoutHandler) {
        this.leaseTimeoutHandler = leaseTimeoutHandler;
    }
}
