package io.github.spitmaster.warlock.config;

import io.github.spitmaster.warlock.core.factory.RedissonProvider;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
public class WarlockWithRedissonAutoConfiguration {

    @Bean
    @ConditionalOnBean(RedissonClient.class) //没有使用redisson的情况下不加载
    @ConditionalOnClass(RedissonClient.class)
    public RedissonProvider redissonProvider(@Autowired(required = false) RedissonClient redissonClient) {
        return () -> redissonClient;
    }
}