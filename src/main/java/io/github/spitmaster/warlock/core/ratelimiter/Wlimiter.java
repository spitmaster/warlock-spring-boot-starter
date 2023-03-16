package io.github.spitmaster.warlock.core.ratelimiter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.lang.NonNull;

/**
 * 在使用限流器的情况下执行业务函数
 *
 * @author zhouyijin
 */
public interface Wlimiter {

    /**
     * 限流RateLimiter控制的方法
     *
     * @param pjp 切点
     * @return 业务代码返回值
     * @throws Throwable 透传异常
     */
    Object doAround(final ProceedingJoinPoint pjp) throws Throwable;

    /**
     * 获取限流器的信息
     * 在执行被限流器控制的方法的切点的时候(doBizWithRateLimiter中), 需要应用限流器的规则
     *
     * @return 限流器的信息, 不允许为空
     */
    @NonNull
    RateLimiterInfo getRateLimiterInfo();

}
