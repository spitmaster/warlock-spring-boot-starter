package io.github.spitmaster.warlock.annotation;

import io.github.spitmaster.warlock.enums.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * (暂未实现)
 * 配合 WLatch 一起使用
 *
 * @author zhouyijin
 * @see WLatch
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WCountDown {

    /**
     * CountDownLatch 的名字
     * 相同的名字共享同一个锁
     *
     * @return 锁的名字
     */
    String name();

    /**
     * Spring Expression Language (SpEL) expression
     * 可以通过el表达式从参数中获取内容
     * 锁的唯一key
     *
     * @return keys
     */
    String key() default "";

    /**
     * 锁的作用域范围
     *
     * @return Scope, 目前支持两种JVM单机 和 基于Redis的分布式锁
     */
    Scope scope() default Scope.STANDALONE;

    /**
     * 等待加锁的策略
     *
     * @return 等待超时策略
     */
    Waiting waiting() default @Waiting();

    /**
     * 加锁超时的处理策略
     *
     * @return 加锁超时策略
     */
    Leasing leasing() default @Leasing();
}
