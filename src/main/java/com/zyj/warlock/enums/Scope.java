package com.zyj.warlock.enums;

/**
 * 同步工具的范围
 *
 * @author zhouyijin
 */
public enum Scope {

    /**
     * 作用范围仅本JVM
     * 作用域仅在单JVM实例上
     */
    STANDALONE,

    /**
     * 作用范围是分布式集群
     * 默认是基于Redis, 需要Redisson的依赖支持
     */
    DISTRIBUTED,

    //未来考虑是否支持ZK等其他中间件
    ;
}
