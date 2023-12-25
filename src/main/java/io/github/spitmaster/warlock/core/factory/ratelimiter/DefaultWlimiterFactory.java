package io.github.spitmaster.warlock.core.factory.ratelimiter;

import com.google.common.base.Joiner;
import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.annotation.WrateLimiter;
import io.github.spitmaster.warlock.core.Waround;
import io.github.spitmaster.warlock.core.factory.RedissonProvider;
import io.github.spitmaster.warlock.core.factory.TimeoutHandlerProvider;
import io.github.spitmaster.warlock.core.factory.WaroundFactory;
import io.github.spitmaster.warlock.core.ratelimiter.DistributedWlimiter;
import io.github.spitmaster.warlock.core.ratelimiter.RateLimiterInfo;
import io.github.spitmaster.warlock.core.ratelimiter.StandaloneWlimiter;
import io.github.spitmaster.warlock.enums.Scope;
import io.github.spitmaster.warlock.exceptions.WarlockException;
import io.github.spitmaster.warlock.util.SpelExpressionUtil;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;

/**
 * 限流器工厂的默认实现
 *
 * @author zhouyijin
 */
public class DefaultWlimiterFactory implements WaroundFactory {

    private final RedissonProvider redissonProvider;
    private final TimeoutHandlerProvider timeoutHandlerProvider;

    public DefaultWlimiterFactory(RedissonProvider redissonProvider, TimeoutHandlerProvider timeoutHandlerProvider) {
        this.redissonProvider = redissonProvider;
        this.timeoutHandlerProvider = timeoutHandlerProvider;
    }


    @Override
    public Waround build(MethodInvocation methodInvocation) {
        Method method = methodInvocation.getMethod();
        WrateLimiter wrateLimiter = AnnotatedElementUtils.findMergedAnnotation(method, WrateLimiter.class);
        if (wrateLimiter == null) {
            throw new WarlockException("invoke WrateLimiter interceptor on non warlock method, method = " + method.getName());
        }
        Scope scope = wrateLimiter.scope();
        switch (scope) {
            case STANDALONE:
                return new StandaloneWlimiter(this.buildRateLimiterInfo(methodInvocation, wrateLimiter));
            case DISTRIBUTED:
                if (redissonProvider == null || redissonProvider.getRedisson() == null) {
                    //如果项目没有使用Redisson,则不支持使用分布式限流器
                    throw new WarlockException("Not supported RateLimiter scope: DISTRIBUTED ; please use redisson to active this function; method: " + method.getName());
                }
                /*
                 * 如果要使用分布式版本的限流器, 强烈建议使用alibaba的sentinel
                 * 这里暂时只提供一个简陋的分布式限流器实现
                 */
                return new DistributedWlimiter(this.buildRateLimiterInfo(methodInvocation, wrateLimiter), redissonProvider.getRedisson());
        }
        throw new WarlockException("Wrong rateLimiter scope; scope = " + scope);
    }

    private RateLimiterInfo buildRateLimiterInfo(MethodInvocation methodInvocation, WrateLimiter wrateLimiter) {
        Method method = methodInvocation.getMethod();
        RateLimiterInfo rateLimiterInfo = new RateLimiterInfo();
        //1. key
        String rateLimiterKey = Joiner
                .on(':')
                .skipNulls()
                .join(Arrays.asList(
                        "wrateLimiter",
                        wrateLimiter.name(),
                        SpelExpressionUtil.parseSpel(method, methodInvocation.getArguments(), wrateLimiter.key(), String.class)
                ));
        rateLimiterInfo.setRateLimiterKey(rateLimiterKey);
        //2. 限流器每秒允许通过的数量
        rateLimiterInfo.setPermitsPerSecond(wrateLimiter.permitsPerSecond());
        //3. 等待策略信息
        Waiting waiting = wrateLimiter.waiting();
        Duration waitTime = Duration.of(waiting.waitTime(), waiting.timeUnit());
        if (waitTime.isNegative() || waitTime.isZero()) {
            throw new WarlockException("WaitTime cannot Less than or equal to 0; method = " + method.getName());
        }
        rateLimiterInfo.setWaitTime(waitTime);
        rateLimiterInfo.setWaitTimeoutHandler(timeoutHandlerProvider.getWaitTimeoutHandler(waiting));
        return rateLimiterInfo;
    }
}
