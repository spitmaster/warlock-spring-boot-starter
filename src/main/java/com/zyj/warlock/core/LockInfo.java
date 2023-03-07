package com.zyj.warlock.core;

import com.zyj.warlock.enums.LockType;
import com.zyj.warlock.handler.LeaseTimeoutHandler;
import com.zyj.warlock.handler.WaitTimeoutHandler;
import lombok.Data;

import java.time.Duration;

/**
 * 锁的基本信息
 *
 * @author zhouyijin
 */
@Data
public class LockInfo {
    /**
     * 锁的唯一标识
     * 会在WarlockAspect中被组装
     * 由 com.zyj.warlock.annotation.Wlock#name() + SpEL表达式计算结果得到
     *
     * @see com.zyj.warlock.annotation.Wlock
     * @see com.zyj.warlock.util.SpelExpressionUtil
     * @see com.zyj.warlock.aspect.WarlockAspect
     */
    private String lockKey;

    /**
     * Wlock 中指定的锁类型
     *
     * @see com.zyj.warlock.annotation.Wlock
     */
    private LockType lockType;

    /**
     * 尝试获取锁的时间
     * 超过该时间还未获得锁, 则调用自定义的接口处理, 如果未指定自定义处理的Handler处理, 如果没有指定handler则直接抛异常
     *
     * @see com.zyj.warlock.annotation.Wlock
     * @see com.zyj.warlock.annotation.Waiting
     */
    private Duration waitTime;

    /**
     * 等待超时之后的处理策略
     */
    private WaitTimeoutHandler waitTimeoutHandler;

    /**
     * 锁超过租期时间的处理方式
     * Standalone的锁, 没有锁过租期的情况
     *
     * @see com.zyj.warlock.annotation.Wlock
     * @see com.zyj.warlock.annotation.Leasing
     */
    private Duration leaseTime;

    /**
     * 执行完业务代码之后, 检查发现锁的租期已经过了, 就会回调这个handler
     */
    private LeaseTimeoutHandler leaseTimeoutHandler;
}
