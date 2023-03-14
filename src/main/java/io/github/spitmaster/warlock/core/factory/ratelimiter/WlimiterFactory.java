package io.github.spitmaster.warlock.core.factory.ratelimiter;

import io.github.spitmaster.warlock.annotation.WrateLimiter;
import io.github.spitmaster.warlock.core.ratelimiter.Wlimiter;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author zhouyijin
 */
public interface WlimiterFactory {

    /**
     * 构造一个 Wlimiter 对象
     *
     * @param pjp          切点
     * @param wrateLimiter 限流器信息
     * @return 构造好的Wlock
     */
    Wlimiter build(ProceedingJoinPoint pjp, WrateLimiter wrateLimiter);
}
