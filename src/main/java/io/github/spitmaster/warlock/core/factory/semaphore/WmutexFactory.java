package io.github.spitmaster.warlock.core.factory.semaphore;

import io.github.spitmaster.warlock.core.semaphore.Wmutex;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 生成Wmutex对象, 用以在信号量环境下执行业务代码
 *
 * @author zhouyijin
 */
public interface WmutexFactory {

    /**
     * 构造一个Wmutex对象
     *
     * @param methodInvocation 切点
     * @return 构造好的Wlock
     */
    Wmutex build(MethodInvocation methodInvocation);
}
