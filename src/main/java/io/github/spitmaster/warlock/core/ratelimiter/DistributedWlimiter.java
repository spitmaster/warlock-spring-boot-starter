package io.github.spitmaster.warlock.core.ratelimiter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.spitmaster.warlock.core.Waround;
import org.aopalliance.intercept.MethodInvocation;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


/**
 * 分布式的限流器
 * 依赖redisson实现
 * 如果要使用分布式版本的限流器, 强烈建议使用alibaba的sentinel
 * 这里暂时只提供一个简陋的分布式限流器实现
 *
 * @author zhouyijin
 */
public class DistributedWlimiter implements Waround {

    /**
     * 缓存RRateLimiter对象来防止 RRateLimiter 被频繁的初始化, 浪费资源, 尤其是频繁的setRate
     */
    private static final Cache<String, RRateLimiter> RATE_LIMITER_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(10))
            .expireAfterAccess(Duration.ofSeconds(10))
            .weakKeys()
            .weakValues()
            .build();
    private final RateLimiterInfo rateLimiterInfo;
    private final RedissonClient redissonClient;

    public DistributedWlimiter(RateLimiterInfo rateLimiterInfo, RedissonClient redissonClient) {
        this.rateLimiterInfo = rateLimiterInfo;
        this.redissonClient = redissonClient;
    }

    @Override
    public Object doAround(MethodInvocation methodInvocation) throws Throwable {
        if (this.getRateLimiter().tryAcquire(this.rateLimiterInfo.getWaitTime().toMillis(), TimeUnit.MILLISECONDS)) {
            //通过限流器, 正常执行业务代码
            return methodInvocation.proceed();
        } else {
            //超时的处理
            return rateLimiterInfo.getWaitTimeoutHandler().handleWaitTimeout(methodInvocation);
        }
    }

    protected RRateLimiter getRateLimiter() throws ExecutionException {
        return RATE_LIMITER_CACHE.get(this.rateLimiterInfo.getRateLimiterKey(), () -> {
            RRateLimiter rateLimiter = redissonClient.getRateLimiter(this.rateLimiterInfo.getRateLimiterKey());
            rateLimiter.trySetRate(
                    RateType.OVERALL, //所有实例共享限流器
                    rateLimiterInfo.getPermitsPerSecond(), //单位时间生成令牌数
                    1, //生成令牌数的间隔
                    RateIntervalUnit.SECONDS //时间单位
            );
            return rateLimiter;
        });
    }

}
