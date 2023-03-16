package io.github.spitmaster.warlock.aspect.ratelimiter;

import io.github.spitmaster.warlock.core.factory.ratelimiter.WlimiterFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 处理@WrateLimiter注解的切面
 * 让你比较方便的使用锁
 *
 * @author zhouyijin
 */
public class WrateLimiterMethodInterceptor implements MethodInterceptor {

    private final WlimiterFactory wlimiterFactory;

    public WrateLimiterMethodInterceptor(WlimiterFactory wlimiterFactory) {
        this.wlimiterFactory = wlimiterFactory;
    }

    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation methodInvocation) throws Throwable {
        return wlimiterFactory.build(methodInvocation)
                .doAround(methodInvocation);
    }
}
