package io.github.spitmaster.warlock.core.factory;

import org.redisson.api.RedissonClient;

/**
 * warlock的分布式锁等功能依赖于Redisson
 * 如果能够提供RedissonClient, 即可方便的使用分布式锁,分布式限流器等功能
 */
public interface RedissonProvider {

    /**
     * 获取RedissonClient对象
     *
     * @return RedissonClient对象
     */
    RedissonClient getRedisson();
}
