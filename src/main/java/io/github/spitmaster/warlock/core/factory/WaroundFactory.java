package io.github.spitmaster.warlock.core.factory;

import io.github.spitmaster.warlock.core.Waround;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 构造 Waround 实例的工厂
 *
 * @author zhouyijin
 */
public interface WaroundFactory {

    /**
     * 构造一个 Waround 对象
     *
     * @param methodInvocation 切点
     * @return 构造好的warlock
     */
    Waround build(MethodInvocation methodInvocation);

}
