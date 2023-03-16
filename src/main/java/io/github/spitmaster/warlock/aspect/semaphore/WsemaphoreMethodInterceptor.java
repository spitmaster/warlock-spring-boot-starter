package io.github.spitmaster.warlock.aspect.semaphore;

import io.github.spitmaster.warlock.core.factory.semaphore.WmutexFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 处理@Wsemaphore注解的切面
 * 让你比较方便的使用锁
 *
 * @author zhouyijin
 */
public class WsemaphoreMethodInterceptor implements MethodInterceptor {

    private final WmutexFactory wmutexFactory;

    public WsemaphoreMethodInterceptor(WmutexFactory wmutexFactory) {
        this.wmutexFactory = wmutexFactory;
    }


    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation methodInvocation) throws Throwable {
        return wmutexFactory.build(methodInvocation)
                .doAround(methodInvocation);
    }
}
