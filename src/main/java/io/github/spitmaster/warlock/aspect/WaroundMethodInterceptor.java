package io.github.spitmaster.warlock.aspect;

import io.github.spitmaster.warlock.core.factory.WaroundFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 处理注解的切面
 *
 * @author zhouyijin
 */
public class WaroundMethodInterceptor implements MethodInterceptor {

    private final WaroundFactory factory;

    public WaroundMethodInterceptor(WaroundFactory factory) {
        this.factory = factory;
    }


    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation methodInvocation) throws Throwable {
        //1. 工厂构建Waround
        return factory.build(methodInvocation)
                //2. 在锁的环境下执行业务代码
                .doAround(methodInvocation);
    }
}
