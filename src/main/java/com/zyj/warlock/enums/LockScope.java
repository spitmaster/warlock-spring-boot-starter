package com.zyj.warlock.enums;

/**
 * 锁的范围
 *
 * @author zhouyijin
 */
public enum LockScope {

    /**
     * 单机锁
     * 作用域仅在单JVM实例上
     */
    STANDALONE,

    /**
     * 分布式锁
     * 默认是基于Redis的分布式锁, 需要Redisson的依赖支持
     */
    DISTRIBUTED,

    //未来考虑是否支持ZK等其他中间件
    ;
}
