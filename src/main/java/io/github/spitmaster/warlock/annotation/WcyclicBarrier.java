package io.github.spitmaster.warlock.annotation;

import com.google.common.annotations.Beta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 一个围栏, 凑齐了数量一起放行
 * 暂时不支持分布式版CyclicBarrier, 因为没有Redisson实现, 我不敢妄图自己实现, 以后有缘有时间,可以自己实现分布式版
 *
 * @author zhouyijin
 */
@Beta
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WcyclicBarrier {

    /**
     * the number of threads that must invoke await before the barrier is tripped
     * 不允许小于1
     * cannot below 1
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
     * 等待空位的策略
     *
     * @return 等待超时策略
     */
    Waiting waiting() default @Waiting();

}
