package io.github.spitmaster.warlock.core.factory.lock;

import io.github.spitmaster.warlock.core.lock.Wlock;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 构造Wlock实例的工厂
 * 这里提供一个默认实现 DefaultWlockFactory,
 * 如果不服气, 你可以自己实现一个替代我的 DefaultWlockFactory
 *
 * @author zhouyijin
 */
public interface WlockFactory {

    /**
     * 构造一个Warlock对象
     *
     * @param methodInvocation 切点
     * @return 构造好的warlock
     */
    Wlock build(MethodInvocation methodInvocation);

}
