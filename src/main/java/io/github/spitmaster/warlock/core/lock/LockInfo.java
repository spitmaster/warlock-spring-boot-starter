package io.github.spitmaster.warlock.core.lock;

import io.github.spitmaster.warlock.annotation.Leasing;
import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.annotation.Warlock;
import io.github.spitmaster.warlock.enums.LockType;
import io.github.spitmaster.warlock.enums.Scope;
import io.github.spitmaster.warlock.handler.LeaseTimeoutHandler;
import io.github.spitmaster.warlock.handler.WaitTimeoutHandler;
import io.github.spitmaster.warlock.util.SpelExpressionUtil;

import java.time.Duration;

/**
 * 锁的基本信息
 *
 * @author zhouyijin
 */
public class LockInfo {
    /**
     * 锁的唯一标识
     * 会在 io.github.spitmaster.warlock.core.lock.factory.AbstractWarlockFactory#buildLock(org.aspectj.lang.ProceedingJoinPoint, io.github.spitmaster.warlock.annotation.Warlock)中被组装
     * 由 io.github.spitmaster.warlock.annotation.Warlock#name() + SpEL表达式计算结果得到
     *
     * @see Warlock
     * @see SpelExpressionUtil
     */
    private String lockKey;

    /**
     * Wlock 中指定的锁类型
     *
     * @see Warlock
     */
    private LockType lockType;

    /**
     * 锁的作用域
     *
     * @see Scope
     */
    private Scope lockScope;

    /**
     * 尝试获取锁的时间
     * 超过该时间还未获得锁, 则调用自定义的接口处理, 如果未指定自定义处理的Handler处理, 如果没有指定handler则直接抛异常
     *
     * @see Warlock
     * @see Waiting
     */
    private Duration waitTime;

    /**
     * 等待超时之后的处理策略
     *
     * @see Warlock
     * @see Waiting
     */
    private WaitTimeoutHandler waitTimeoutHandler;

    /**
     * 锁超过租期时间的处理方式
     * Standalone的锁, 没有锁过租期的情况
     *
     * @see Warlock
     * @see Leasing
     */
    private Duration leaseTime;

    /**
     * 执行完业务代码之后, 检查发现锁的租期已经过了, 就会回调这个handler
     *
     * @see Warlock
     * @see Leasing
     */
    private LeaseTimeoutHandler lockLeaseTimeoutHandler;

    public String getLockKey() {
        return lockKey;
    }

    public void setLockKey(String lockKey) {
        this.lockKey = lockKey;
    }

    public LockType getLockType() {
        return lockType;
    }

    public void setLockType(LockType lockType) {
        this.lockType = lockType;
    }

    public Scope getLockScope() {
        return lockScope;
    }

    public void setLockScope(Scope lockScope) {
        this.lockScope = lockScope;
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

    public LeaseTimeoutHandler getLockLeaseTimeoutHandler() {
        return lockLeaseTimeoutHandler;
    }

    public void setLockLeaseTimeoutHandler(LeaseTimeoutHandler lockLeaseTimeoutHandler) {
        this.lockLeaseTimeoutHandler = lockLeaseTimeoutHandler;
    }
}
