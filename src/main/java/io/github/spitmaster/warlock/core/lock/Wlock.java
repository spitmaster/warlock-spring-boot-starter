package io.github.spitmaster.warlock.core.lock;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.lang.NonNull;

/**
 * 构造一个Wlock
 * 用于在业务方法上下文中, 根据需求, 进行锁的操作
 *
 * @author zhouyijin
 */
public interface Wlock {

    /**
     * 在锁的环境下执行业务函数
     *
     * @param methodInvocation 切点
     * @return 业务函数的返回值
     * @throws Throwable 业务方法可能抛出的异常
     */
    Object doWithLock(MethodInvocation methodInvocation) throws Throwable;

    /**
     * 锁的信息
     *
     * @return LockInfo对象, 不能为空
     */
    @NonNull
    LockInfo getLockInfo();
}
