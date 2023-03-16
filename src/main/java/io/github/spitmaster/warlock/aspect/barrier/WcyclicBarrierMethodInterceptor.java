package io.github.spitmaster.warlock.aspect.barrier;

import io.github.spitmaster.warlock.core.barrier.Wbarrier;
import io.github.spitmaster.warlock.core.factory.barrier.WbarrierFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 处理@WcyclicBarrier注解的切面
 * 让你比较方便的使用锁
 *
 * @author zhouyijin
 */
public class WcyclicBarrierMethodInterceptor implements MethodInterceptor {

    private final WbarrierFactory wbarrierFactory;

    public WcyclicBarrierMethodInterceptor(WbarrierFactory wbarrierFactory) {
        this.wbarrierFactory = wbarrierFactory;
    }


    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation methodInvocation) throws Throwable {
        Wbarrier wbarrier = wbarrierFactory.build(methodInvocation);
        return wbarrier.doWithBarrier(methodInvocation);
    }
}
