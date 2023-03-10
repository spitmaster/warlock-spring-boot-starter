package io.github.spitmaster.warlock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * (暂未实现)
 * 配合 WCountDown 一起使用
 *
 * @author zhouyijin
 * @see WCountDown
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WLatch {

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
     * 锁的唯一key一部分
     * 完整的key是 name + spel的计算结果
     *
     * @return key
     */
    String key() default "";

}
