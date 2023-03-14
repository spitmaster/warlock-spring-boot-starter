package io.github.spitmaster.warlock.annotation;

import io.github.spitmaster.warlock.enums.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法级的限流器
 *
 * @author zhouyijin
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WrateLimiter {

    /**
     * 每秒能通过的请求数
     *
     * @return 不得小于1
     */
    long permitsPerSecond();

    /**
     * 围栏的名字
     *
     * @return key的前缀
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
     * 作用域范围
     *
     * @return Scope, 目前支持两种JVM单机 和 基于Redis的分布式
     */
    Scope scope() default Scope.STANDALONE;

    /**
     * 等待空位的策略
     *
     * @return 等待超时策略
     */
    Waiting waiting() default @Waiting();

}
