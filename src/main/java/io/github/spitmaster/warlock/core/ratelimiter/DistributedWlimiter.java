package io.github.spitmaster.warlock.core.ratelimiter;

import io.github.spitmaster.warlock.core.Waround;
import org.aopalliance.intercept.MethodInvocation;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

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

    protected RRateLimiter getRateLimiter() {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(this.rateLimiterInfo.getRateLimiterKey());
        rateLimiter.trySetRate(
                RateType.OVERALL, //所有实例共享限流器
                rateLimiterInfo.getPermitsPerSecond(), //单位时间生成令牌数
                1, //生成令牌数的间隔
                RateIntervalUnit.SECONDS //时间单位
        );
        return rateLimiter;
    }

}
