package io.github.spitmaster.warlock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 方法级的限流器
 *
 * @author zhouyijin
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WrateLimiter {

    /**
     * 每单位时间能通过的请求数
     *
     * @return 不得小于1
     */
    int permitsPerUnit();

    /**
     * 时间单位
     * 与permitsPerUnit搭配使用
     * 用于permitsPerUnit的单位时间
     *
     * @return 时间单位
     */
    TimeUnit timeUnit();

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
     * 等待空位的策略
     *
     * @return 等待超时策略
     */
    Waiting waiting() default @Waiting();

}
