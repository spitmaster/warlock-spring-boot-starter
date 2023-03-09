package io.github.spitmaster.warlock.core.semaphore;

import io.github.spitmaster.warlock.annotation.Leasing;
import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.annotation.Wsemaphore;
import io.github.spitmaster.warlock.core.factory.semaphore.DefaultWmutexFactory;
import io.github.spitmaster.warlock.handler.lock.LeaseTimeoutHandler;
import io.github.spitmaster.warlock.handler.lock.WaitTimeoutHandler;
import lombok.Data;
import org.aspectj.lang.ProceedingJoinPoint;

import java.time.Duration;

/**
 * 信号量的基本信息
 *
 * @author zhouyijin
 */
@Data
public class SemaphoreInfo {

    /**
     * 信号量的唯一标识
     *
     * @see io.github.spitmaster.warlock.annotation.Wsemaphore
     * @see io.github.spitmaster.warlock.util.SpelExpressionUtil
     * @see io.github.spitmaster.warlock.aspect.WsemaphoreAspect
     * @see DefaultWmutexFactory#buildLockInfo(ProceedingJoinPoint, Wsemaphore)
     */
    private String semaphoreKey;

    /**
     * 允许同时并发的线程数量,
     * 若小于1, 则不起作用
     */
    private int permits;

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
}
