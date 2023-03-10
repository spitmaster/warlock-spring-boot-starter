package io.github.spitmaster.warlock.annotation;

import io.github.spitmaster.warlock.enums.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 一个围栏, 凑齐了数量一起放行
 *
 * @author zhouyijin
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WCyclicBarrier {

    /**
     * the number of threads that must invoke await before the barrier is tripped
     *
     * @return the number of threads that must invoke await before the barrier is tripped
     */
    int parties();

    /**
     * 围栏的名字
     *
     * @return 锁的名字
     */
    String name();

    /**
     * Spring Expression Language (SpEL) expression
     * 可以通过el表达式从参数中获取内容
     * 围栏的唯一key的一部分
     * 完整的key是 name + spel的计算结果
     *
     * @return key
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
     * 如果使用 Scope.STANDALONE 的作用域, 则leasing不可用
     *
     * @return 加锁超时策略
     */
    Leasing leasing() default @Leasing();

}
