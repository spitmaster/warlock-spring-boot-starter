package com.zyj.warlock.redisson;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

@Configuration
public class RedissonConfig {
    @Bean
    RedisProperties redisProperties() {
        return new RedisProperties();
    }

    @Bean
    public RedissonClient redisson(RedisProperties redisProperties) throws IOException {
        ResourceLoader loader = new DefaultResourceLoader();
        Resource resource = loader.getResource("application.yaml");
        Config config = Config.fromYAML(resource.getInputStream());
        return Redisson.create(config);
    }
}
