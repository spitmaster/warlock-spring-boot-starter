package io.github.spitmaster.warlock.core.ratelimiter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import io.github.spitmaster.warlock.core.Waround;
import io.github.spitmaster.warlock.exceptions.WarlockException;
import org.aopalliance.intercept.MethodInvocation;

import java.time.Duration;
import java.util.concurrent.ExecutionException;


/**
 * 单机的限流器
 * 依赖guava实现
 *
 * @author zhouyijin
 */
public class StandaloneWlimiter implements Waround {

    /**
     * 多个切点可以使用同一个key
     * 如果使用同一个key, 那么它们使用的就是同一个限流器
     * --
     * 不使用引用计数 + ConcurrentHashMap 的方案 : RateLimiter 是有状态的令牌桶,
     * 如果串行调用都移除再重建, 会丢失令牌桶状态, 使限流完全失效.
     * 改用 Guava Cache + maximumSize + 长 TTL : 跨调用保留 RateLimiter 状态,
     * 只有长时间空闲或超出容量才被淘汰(10分钟空闲已足够令牌桶自然恢复满载, 丢弃无害).
     */
    private static final Cache<String, RateLimiter> RATE_LIMITER_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(4096)
            .build();

    private final RateLimiterInfo rateLimiterInfo;

    public StandaloneWlimiter(RateLimiterInfo rateLimiterInfo) {
        this.rateLimiterInfo = rateLimiterInfo;
    }


    @Override
    public Object doAround(MethodInvocation methodInvocation) throws Throwable {
        if (this.getRateLimiter().tryAcquire(this.rateLimiterInfo.getWaitTime())) {
            //通过限流器, 正常执行业务代码
            return methodInvocation.proceed();
        } else {
            //超时的处理
            return rateLimiterInfo.getWaitTimeoutHandler().handleWaitTimeout(methodInvocation);
        }
    }

    protected RateLimiter getRateLimiter() {
        try {
            return RATE_LIMITER_CACHE.get(
                    rateLimiterInfo.getRateLimiterKey(),
                    () -> RateLimiter.create(rateLimiterInfo.getPermitsPerSecond())
            );
        } catch (ExecutionException e) {
            throw new WarlockException("failed to create RateLimiter", e);
        }
    }

}
