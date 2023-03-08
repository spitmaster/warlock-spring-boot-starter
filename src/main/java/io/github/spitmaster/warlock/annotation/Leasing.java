package io.github.spitmaster.warlock.annotation;

import io.github.spitmaster.warlock.handler.lock.LockLeaseTimeoutHandler;
import io.github.spitmaster.warlock.handler.lock.PlainLockLeaseTimeoutHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * //暂不支持
 * 锁的租期 信息
 * 也就是获取锁之后的自动释放时间
 *
 * @author zhouyijin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Leasing {

    /**
     * 获得锁后，自动释放锁的时间
     *
     * @return leaseTime
     */
    long leaseTime() default Long.MIN_VALUE;

    /**
     * 时间配置相关的单位
     *
     * @return 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 当你的业务代码耗时太长, 以至于最后释放锁的时候, 发现锁已经过期了, 这时候的处理handler
     * 如果不指定, 则不会做任务操作
     *
     * @return LeaseTimeoutHandler接口的实现类
     */
    Class<? extends LockLeaseTimeoutHandler> leaseTimeoutHandler() default PlainLockLeaseTimeoutHandler.class;
}
