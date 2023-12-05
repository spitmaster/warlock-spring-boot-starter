package io.github.spitmaster.warlock.core.factory.lock;

import io.github.spitmaster.warlock.annotation.Warlock;
import io.github.spitmaster.warlock.core.Waround;
import io.github.spitmaster.warlock.core.factory.WaroundFactory;
import io.github.spitmaster.warlock.enums.Scope;
import io.github.spitmaster.warlock.exceptions.WarlockException;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;

/**
 * WarlockFactory的简易实现
 *
 * @author zhouyijin
 */
public class DefaultWlockFactory implements WaroundFactory {

    private StandaloneWlockFactory standaloneWlockFactory;
    private DistributedWlockFactory distributedWlockFactory;

    public DefaultWlockFactory(StandaloneWlockFactory standaloneWlockFactory, DistributedWlockFactory distributedWlockFactory) {
        this.standaloneWlockFactory = standaloneWlockFactory;
        this.distributedWlockFactory = distributedWlockFactory;
    }

    @Override
    public Waround build(MethodInvocation methodInvocation) {
        //根据锁的范围选择合适的锁factory
        Method method = methodInvocation.getMethod();
        Warlock warlock = AnnotatedElementUtils.findMergedAnnotation(method, Warlock.class);
        if (warlock == null) {
            throw new WarlockException("invoke warlock interceptor on non warlock method, method = " + method.getName());
        }
        Scope scope = warlock.lockScope();
        switch (scope) {
            case STANDALONE:
                //单机锁
                return standaloneWlockFactory.build(methodInvocation);
            case DISTRIBUTED:
                //分布式锁
                return distributedWlockFactory.build(methodInvocation);
            default:
                break;
        }
        //没有选择合适的锁范围, 抛异常,  代码应该跑不到这里
        throw new WarlockException("There is no suitable Warlock for this method: " + method);
    }

}