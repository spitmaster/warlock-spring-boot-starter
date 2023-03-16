package io.github.spitmaster.warlock.core.ratelimiter;

import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 单机的限流器
 * 依赖guava实现
 *
 * @author zhouyijin
 */
public class StandaloneWlimiter implements Wlimiter {

    /**
     * 多个切点可以使用同一个key
     * 如果使用同一个key, 那么它们使用的就是同一个限流器
     * 在使用信号量的时候会被添加到这个MAP中, 全局使用同一个MAP
     */
    private static final ConcurrentHashMap<String, Pair<RateLimiter, AtomicInteger>> RATE_LIMITER_MAP = new ConcurrentHashMap<>();

    private final RateLimiterInfo rateLimiterInfo;

    public StandaloneWlimiter(RateLimiterInfo rateLimiterInfo) {
        this.rateLimiterInfo = rateLimiterInfo;
    }


    @Override
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        if (this.getRateLimiter().tryAcquire(this.rateLimiterInfo.getWaitTime())) {
            //通过限流器, 正常执行业务代码
            return pjp.proceed();
        } else {
            //超时的处理
            return rateLimiterInfo.getWaitTimeoutHandler().handleWaitTimeout(pjp);
        }
    }


    protected RateLimiter getRateLimiter() {
        Pair<RateLimiter, AtomicInteger> lockPair = RATE_LIMITER_MAP.compute(rateLimiterInfo.getRateLimiterKey(), (s, pair) -> {
            if (pair == null) {
                //没有就初始化
                RateLimiter rateLimiter = RateLimiter.create(rateLimiterInfo.getPermitsPerSecond());
                pair = Pair.of(rateLimiter, new AtomicInteger(0));
            }
            pair.getRight().incrementAndGet();
            return pair;
        });
        return lockPair.getLeft();
    }

    protected void returnRateLimiter() {
        RATE_LIMITER_MAP.computeIfPresent(rateLimiterInfo.getRateLimiterKey(), (s, pair) -> {
            int holdCount = pair.getRight().decrementAndGet();
            if (holdCount <= 0) {
                //返回null,相当于把这个信号量删除了
                return null;
            }
            return pair;
        });
    }


    @Override
    public RateLimiterInfo getRateLimiterInfo() {
        return this.rateLimiterInfo;
    }
}
