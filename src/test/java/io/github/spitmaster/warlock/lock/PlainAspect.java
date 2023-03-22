package io.github.spitmaster.warlock.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 什么都不干的切面, 用于基准测试的比较
 *
 * @author zhouyijin
 */
@Aspect
@Component
public class PlainAspect {

    @Around(value = "@annotation(io.github.spitmaster.warlock.lock.PlainAspect.WBenchmark) && @annotation(w)")
    public Object warlockPointcut(final ProceedingJoinPoint pjp, WBenchmark w) throws Throwable {
        //什么额外的操作都不做
        return pjp.proceed();
    }


    @Target(value = {ElementType.METHOD})
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface WBenchmark {
    }
}