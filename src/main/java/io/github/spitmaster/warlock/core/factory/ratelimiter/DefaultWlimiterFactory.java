package io.github.spitmaster.warlock.core.factory.ratelimiter;

import com.google.common.base.Joiner;
import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.annotation.WrateLimiter;
import io.github.spitmaster.warlock.core.factory.AbstractFactory;
import io.github.spitmaster.warlock.core.ratelimiter.DistributedWlimiter;
import io.github.spitmaster.warlock.core.ratelimiter.RateLimiterInfo;
import io.github.spitmaster.warlock.core.ratelimiter.StandaloneWlimiter;
import io.github.spitmaster.warlock.core.ratelimiter.Wlimiter;
import io.github.spitmaster.warlock.enums.Scope;
import io.github.spitmaster.warlock.exceptions.WarlockException;
import io.github.spitmaster.warlock.util.JoinPointUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.BeanFactory;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Arrays;

/**
 * 限流器工厂的默认实现
 *
 * @author zhouyijin
 */
public class DefaultWlimiterFactory extends AbstractFactory implements WlimiterFactory {

    private final RedissonClient redissonClient;

    /**
     * 必须搭配BeanFactory才能使用
     *
     * @param beanFactory    Spring的Bean工厂对象, 一般来说是applicationContext
     * @param redissonClient redisson依赖可以为空
     */
    public DefaultWlimiterFactory(BeanFactory beanFactory, @Nullable RedissonClient redissonClient) {
        super(beanFactory);
        this.redissonClient = redissonClient;
    }

    @Override
    public Wlimiter build(ProceedingJoinPoint pjp, WrateLimiter wrateLimiter) {
        Scope scope = wrateLimiter.scope();
        switch (scope) {
            case STANDALONE:
                return new StandaloneWlimiter(this.buildRateLimiterInfo(pjp, wrateLimiter));
            case DISTRIBUTED:
                if (redissonClient == null) {
                    //如果项目没有使用Redisson,则不支持使用分布式限流器
                    throw new WarlockException("Not supported RateLimiter scope: DISTRIBUTED ; please use redisson client to active this function; method: " + JoinPointUtil.methodName(pjp));
                }
                return new DistributedWlimiter(this.buildRateLimiterInfo(pjp, wrateLimiter), redissonClient);
        }
        throw new WarlockException("Wrong rateLimiter scope; scope = " + scope);
    }

    private RateLimiterInfo buildRateLimiterInfo(ProceedingJoinPoint pjp, WrateLimiter wrateLimiter) {
        RateLimiterInfo rateLimiterInfo = new RateLimiterInfo();
        //1. key
        String rateLimiterKey = Joiner
                .on(':')
                .skipNulls()
                .join(Arrays.asList(
                        "wrateLimiter",
                        wrateLimiter.name(),
                        JoinPointUtil.parseSpEL(pjp, wrateLimiter.key())
                ));
        rateLimiterInfo.setRateLimiterKey(rateLimiterKey);
        //2. 限流器每秒允许通过的数量
        rateLimiterInfo.setPermitsPerSecond(wrateLimiter.permitsPerSecond());
        //3. 等待策略信息
        Waiting waiting = wrateLimiter.waiting();
        Duration waitTime = Duration.of(waiting.waitTime(), waiting.timeUnit().toChronoUnit());
        if (waitTime.isNegative() || waitTime.isZero()) {
            throw new WarlockException("WaitTime cannot Less than or equal to 0; method = " + JoinPointUtil.methodName(pjp));
        }
        rateLimiterInfo.setWaitTime(waitTime);
        rateLimiterInfo.setWaitTimeoutHandler(this.getWaitTimeoutHandler(waiting));
        return rateLimiterInfo;
    }
}
