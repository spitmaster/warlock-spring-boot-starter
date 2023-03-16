package io.github.spitmaster.warlock.core.factory.ratelimiter;

import io.github.spitmaster.warlock.core.ratelimiter.Wlimiter;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author zhouyijin
 */
public interface WlimiterFactory {

    /**
     * 构造一个 Wlimiter 对象
     *
     * @param methodInvocation 切点
     * @return 构造好的Wlock
     */
    Wlimiter build(MethodInvocation methodInvocation);
}
