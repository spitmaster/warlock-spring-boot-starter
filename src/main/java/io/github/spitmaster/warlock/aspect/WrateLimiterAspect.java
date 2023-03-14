package io.github.spitmaster.warlock.aspect;

import io.github.spitmaster.warlock.annotation.WrateLimiter;
import io.github.spitmaster.warlock.core.factory.ratelimiter.WlimiterFactory;
import io.github.spitmaster.warlock.core.ratelimiter.Wlimiter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;


/**
 * 处理@WrateLimiter注解的切面
 *
 * @author zhouyijin
 */
@Aspect
public class WrateLimiterAspect {

    private final WlimiterFactory wlimiterFactory;

    public WrateLimiterAspect(WlimiterFactory wlimiterFactory) {
        this.wlimiterFactory = wlimiterFactory;
    }

    @Around(value = "@annotation(io.github.spitmaster.warlock.annotation.WrateLimiter) && @annotation(wrateLimiter)")
    public Object wrateLimiterPointcut(final ProceedingJoinPoint pjp, WrateLimiter wrateLimiter) throws Throwable {
        //1. 构建 Wlimiter
        Wlimiter wlimiter = wlimiterFactory.build(pjp, wrateLimiter);
        //2. 在Wlimiter的环境下执行业务代码
        return wlimiter.doBizWithRateLimiter(pjp);
    }

}
