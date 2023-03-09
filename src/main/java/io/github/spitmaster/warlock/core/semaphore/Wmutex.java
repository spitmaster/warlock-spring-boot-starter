package io.github.spitmaster.warlock.core.semaphore;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.lang.NonNull;

/**
 * 信号量的使用接口
 *
 * @author zhouyijin
 */
public interface Wmutex {

    /**
     * 使用信号量执行业务代码
     *
     * @param pjp 切点
     * @return 业务代码返回值
     * @throws Throwable 透传异常
     */
    Object doBizWithSemaphore(final ProceedingJoinPoint pjp) throws Throwable;

    /**
     * 锁的信息
     *
     * @return SemaphoreInfo 对象, 不能为空
     */
    @NonNull
    SemaphoreInfo getSemaphoreInfo();
}
