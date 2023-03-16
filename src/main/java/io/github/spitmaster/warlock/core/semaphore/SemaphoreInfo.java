package io.github.spitmaster.warlock.core.semaphore;

import io.github.spitmaster.warlock.annotation.Leasing;
import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.annotation.Wsemaphore;
import io.github.spitmaster.warlock.core.factory.semaphore.DefaultWmutexFactory;
import io.github.spitmaster.warlock.enums.Scope;
import io.github.spitmaster.warlock.handler.LeaseTimeoutHandler;
import io.github.spitmaster.warlock.handler.WaitTimeoutHandler;

import java.time.Duration;

/**
 * 信号量的基本信息
 *
 * @author zhouyijin
 */
public class SemaphoreInfo {

    /**
     * 信号量的唯一标识
     *
     * @see io.github.spitmaster.warlock.annotation.Wsemaphore
     * @see io.github.spitmaster.warlock.util.SpelExpressionUtil
     * @see DefaultWmutexFactory#buildLockInfo()
     */
    private String semaphoreKey;

    /**
     * 允许同时并发的线程数量,
     * 若小于1, 则不起作用
     */
    private int permits;

    /**
     * 信号量的作用域
     */
    private Scope scope;

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

    public String getSemaphoreKey() {
        return semaphoreKey;
    }

    public void setSemaphoreKey(String semaphoreKey) {
        this.semaphoreKey = semaphoreKey;
    }

    public int getPermits() {
        return permits;
    }

    public void setPermits(int permits) {
        this.permits = permits;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
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
