package io.github.spitmaster.warlock.aspect.warlock;

import io.github.spitmaster.warlock.core.factory.lock.WlockFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 处理@Warlock注解的切面
 * 让你比较方便的使用锁
 *
 * @author zhouyijin
 */
public class WarlockMethodInterceptor implements MethodInterceptor {

    private final WlockFactory wlockFactory;

    public WarlockMethodInterceptor(WlockFactory wlockFactory) {
        this.wlockFactory = wlockFactory;
    }


    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation methodInvocation) throws Throwable {
        //1. 构建warlock
        return wlockFactory.build(methodInvocation)
                //2. 在锁的环境下执行业务代码
                .doAround(methodInvocation);
    }
}
