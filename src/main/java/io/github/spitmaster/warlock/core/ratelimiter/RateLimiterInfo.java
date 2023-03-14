package io.github.spitmaster.warlock.core.ratelimiter;

import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.annotation.Wsemaphore;
import io.github.spitmaster.warlock.handler.WaitTimeoutHandler;

import java.time.Duration;

/**
 * @author zhouyijin
 */
public class RateLimiterInfo {

    /**
     * 每秒能通过的请求数
     * 不得小于1
     */
    private long permitsPerSecond;

    /**
     * 此限流器的唯一标识
     */
    private String rateLimiterKey;

    /**
     * 等待执行的时间
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

    public long getPermitsPerSecond() {
        return permitsPerSecond;
    }

    public void setPermitsPerSecond(long permitsPerSecond) {
        this.permitsPerSecond = permitsPerSecond;
    }

    public String getRateLimiterKey() {
        return rateLimiterKey;
    }

    public void setRateLimiterKey(String rateLimiterKey) {
        this.rateLimiterKey = rateLimiterKey;
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
}
