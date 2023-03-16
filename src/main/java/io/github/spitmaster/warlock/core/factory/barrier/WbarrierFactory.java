package io.github.spitmaster.warlock.core.factory.barrier;

import io.github.spitmaster.warlock.core.barrier.Wbarrier;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 生成 WBarrier 对象
 *
 * @author zhouyijin
 */
public interface WbarrierFactory {

    /**
     * 构造一个 WBarrier 对象
     *
     * @param methodInvocation 切点
     * @return 构造好的 WBarrier
     */
    Wbarrier build(MethodInvocation methodInvocation);
}
