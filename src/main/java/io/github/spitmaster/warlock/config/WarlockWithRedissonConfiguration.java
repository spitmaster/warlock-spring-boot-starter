package io.github.spitmaster.warlock.config;

import io.github.spitmaster.warlock.core.factory.RedissonProvider;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 在有Redisson的情况下的配置
 * warlock的默认配置
 *
 * @author zhouyijin
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnProperty(name = "warlock.enabled", matchIfMissing = true)
public class WarlockWithRedissonConfiguration {

    private static final Logger WARLOCK_WITHREDISSON_CONFIGURATION_LOGGER = LoggerFactory.getLogger(WarlockWithRedissonConfiguration.class);

    @Bean
    public RedissonProvider redissonProvider(BeanFactory beanFactory) {
        WARLOCK_WITHREDISSON_CONFIGURATION_LOGGER.info("WarlockWithRedissonAutoConfiguration init redissonProvider");
        return () -> beanFactory.getBeanProvider(RedissonClient.class).getIfAvailable();
    }
}