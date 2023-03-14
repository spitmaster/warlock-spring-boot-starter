package io.github.spitmaster.warlock.config;

import io.github.spitmaster.warlock.aspect.WrateLimiterAspect;
import io.github.spitmaster.warlock.core.factory.ratelimiter.DefaultWlimiterFactory;
import io.github.spitmaster.warlock.core.factory.ratelimiter.WlimiterFactory;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WrateLimiter的默认配置
 * 觉得不满足你的要求, 可以换成你自己实现的Bean
 *
 * @author zhouyijin
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "warlock.enabled", matchIfMissing = true)
public class WrateLimiterAutoConfiguration {

    /**
     * Wlimiter 对象的工厂
     *
     * @param beanFactory    spring的BeanFactory
     * @param redissonClient 分布式的限流器,需要依赖redisson(非必须,如果没有redisson,则不支持分布式的限流器)
     * @return 生成一个我写的默认的工厂对象, 你可以自己写一个替代我的, 用来实现想要的其他能力
     */
    @Bean
    @ConditionalOnMissingBean
    public WlimiterFactory wlimiterFactory(
            BeanFactory beanFactory,
            @Autowired(required = false) RedissonClient redissonClient) {
        return new DefaultWlimiterFactory(beanFactory, redissonClient);
    }

    /**
     * 限流器切面
     *
     * @param wlimiterFactory 限流器对象的工厂
     * @return 切面对象
     */
    @Bean
    @ConditionalOnMissingBean
    public WrateLimiterAspect wrateLimiterAspect(WlimiterFactory wlimiterFactory) {
        return new WrateLimiterAspect(wlimiterFactory);
    }

}