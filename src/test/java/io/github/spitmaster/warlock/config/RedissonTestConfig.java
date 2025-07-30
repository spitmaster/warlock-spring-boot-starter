package io.github.spitmaster.warlock.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import java.io.IOException;

/**
 * 仅在单元测试使用的配置
 */
@Configuration
public class RedissonTestConfig implements DisposableBean {

    private RedisServer redisServer;

    @Bean
    public RedissonClient redissonClient(RedisProperties redisProperties) throws IOException {
        startEmbeddedRedis();
        Config config = new Config();
        //使用json序列化方式
        config.setCodec(new JsonJacksonCodec())
                .useSingleServer()
                .setAddress("redis://127.0.0.1:6399");
        return Redisson.create(config);
    }

    private void startEmbeddedRedis() {
        redisServer = RedisServer.builder()
                .port(6399)
                .setting("maxheap 20m")
                .setting("bind localhost")
                .build();
        redisServer.start();
    }

    @Override
    public void destroy() throws Exception {
        redisServer.stop();
    }
}
