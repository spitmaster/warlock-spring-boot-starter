package io.github.spitmaster.warlock.annotation;

import io.github.spitmaster.warlock.enums.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

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
     * 信号量的唯一key的一部分
     * 完整的key是 name + spel的计算结果
     *
     * @return key
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
     * 不允许小于1
     *
     * @return 允许并发数
     */
    int permits();

    /**
     * 等待信号量的策略
     *
     * @return 等待超时策略
     */
    Waiting waiting() default @Waiting();

    /**
     * 信号量permit归还的时候超时的处理策略
     * 如果使用 Scope.STANDALONE 的作用域, 则leasing不可用
     *
     * @return 加锁超时策略
     */
    Leasing leasing() default @Leasing();
}
