package io.github.spitmaster.warlock.annotation;

import io.github.spitmaster.warlock.handler.FastFailTimeoutHandler;
import io.github.spitmaster.warlock.handler.IgnoreTimeoutHandler;
import io.github.spitmaster.warlock.handler.LeaseTimeoutHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

/**
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
     * 分布式锁一定要指定租期, 如果租期<=0则会抛异常
     *
     * @return leaseTime
     */
    long leaseTime() default 60;

    /**
     * 时间配置相关的单位
     *
     * @return 时间单位
     */
    ChronoUnit timeUnit() default ChronoUnit.SECONDS;

    /**
     * 当你的业务代码耗时太长, 以至于最后释放锁的时候, 发现锁已经过期了, 这时候的处理handler
     * 如果不指定, 则不会做任务操作
     *
     * @return LeaseTimeoutHandler接口的实现类
     * @see FastFailTimeoutHandler 默认的策略, 快速失败, 抛出异常
     * @see IgnoreTimeoutHandler 框架提供了一个实现, 无视超时
     */
    Class<? extends LeaseTimeoutHandler> leaseTimeoutHandler() default FastFailTimeoutHandler.class;
}
