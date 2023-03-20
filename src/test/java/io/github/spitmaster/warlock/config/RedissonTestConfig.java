package io.github.spitmaster.warlock.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 仅在单元测试使用的配置
 */
@Configuration
public class RedissonTestConfig {

    @Bean
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        Config config = new Config();
        //使用json序列化方式
        config.setCodec(new JsonJacksonCodec())
                .useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setPassword("123");
        return Redisson.create(config);
    }

}
