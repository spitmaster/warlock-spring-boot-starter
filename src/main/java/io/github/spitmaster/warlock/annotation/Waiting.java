package io.github.spitmaster.warlock.annotation;

import io.github.spitmaster.warlock.handler.FastFailWaitTimeoutHandler;
import io.github.spitmaster.warlock.handler.WaitTimeoutHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

/**
 * 定义锁等待的规则
 *
 * @author zhouyijin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Waiting {

    /**
     * 尝试获取锁的时间
     * 超过该时间还未获得锁, 则调用自定义的接口处理, 如果未指定自定义处理的Handler处理, 如果没有指定handler则直接抛异常
     * 不能为负
     * --
     * 默认值为 1年 , 相当于一直等待
     *
     * @return waitTime
     */
    long waitTime() default 1* 365 * 24 * 60 * 60;

    /**
     * 时间配置相关的单位
     *
     * @return 时间单位
     */
    ChronoUnit timeUnit() default ChronoUnit.SECONDS;

    /**
     * 当锁等待超时的处理策略
     * 如果不使用, 则默认抛出异常
     *
     * @return WaitTimeoutHandler接口的实现类
     */
    Class<? extends WaitTimeoutHandler> waitTimeoutHandler() default FastFailWaitTimeoutHandler.class;
}
