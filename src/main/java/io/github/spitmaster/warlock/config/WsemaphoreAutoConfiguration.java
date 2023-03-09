package io.github.spitmaster.warlock.config;

import io.github.spitmaster.warlock.aspect.WsemaphoreAspect;
import io.github.spitmaster.warlock.core.factory.semaphore.DefaultWmutexFactory;
import io.github.spitmaster.warlock.core.factory.semaphore.WmutexFactory;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wsemaphore的默认配置
 * 觉得不满足你的要求, 可以换成你自己实现的Bean
 *
 * @author zhouyijin
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "warlock.enabled", matchIfMissing = true)
public class WsemaphoreAutoConfiguration {


    /**
     * Wsemaphore 注解的切面
     *
     * @param wmutexFactory 信号量对象的工厂
     * @return 切面对象
     */
    @Bean
    @ConditionalOnMissingBean
    public WsemaphoreAspect wsemaphoreAspect(WmutexFactory wmutexFactory) {
        return new WsemaphoreAspect(wmutexFactory);
    }

    /**
     * Wmutex对象的工厂
     *
     * @param beanFactory    spring的BeanFactory
     * @param redissonClient 分布式的信号量,需要依赖redisson(非必须,如果没有redisson,则不支持分布式信号量)
     * @return 生成一个我写的默认的工厂对象, 你可以自己写一个替代我的, 用来实现想要的其他能力
     */
    @Bean
    @ConditionalOnMissingBean
    public WmutexFactory wmutexFactory(
            BeanFactory beanFactory,
            @Autowired(required = false) RedissonClient redissonClient) {
        return new DefaultWmutexFactory(beanFactory, redissonClient);
    }
}