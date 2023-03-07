package com.zyj.warlock.annotation;

import com.zyj.warlock.enums.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 依赖于Spring框架
 * 信号量注解
 * ---
 * 方法上使用该注解, 可以提供相当于线程限流排队的功能
 *
 * @author zhouyijin
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Wsemaphore {

    /**
     * 该信号量的名字
     * 相同的名字共享同一个信号量
     *
     * @return 信号量的名字
     */
    String name();

    /**
     * Spring Expression Language (SpEL) expression
     * 可以通过el表达式从参数中获取内容
     * 信号量的唯一key
     *
     * @return keys
     */
    String key() default "";

    /**
     * 信号量的作用域范围
     *
     * @return Scope, 目前支持两种JVM单机 和 基于Redis的分布式锁
     */
    Scope scope() default Scope.STANDALONE;


    /**
     * 允许同时并发的线程数量,
     * 若小于1, 则不起作用
     *
     * @return 允许并发数
     */
    int permits() default 0;
}
