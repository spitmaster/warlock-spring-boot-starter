package com.zyj.warlock.annotation;

import com.zyj.warlock.handler.PlainWaitTimeoutHandler;
import com.zyj.warlock.handler.WaitTimeoutHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

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
     * waitTime < 0 表示一直等待
     * waitTime = 0 表示获取不到锁, 直接失败
     * waitTime > 0 表示尝试等待一段时间, 获取不到锁再失败
     *
     * @return waitTime
     */
    long waitTime() default Long.MIN_VALUE;

    /**
     * 时间配置相关的单位
     *
     * @return 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 当锁等待超时的处理策略
     * 如果不使用, 则默认抛出异常
     *
     * @return WaitTimeoutHandler接口的实现类
     */
    Class<? extends WaitTimeoutHandler> waitTimeoutHandler() default PlainWaitTimeoutHandler.class;
}
