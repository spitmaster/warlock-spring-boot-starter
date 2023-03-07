package com.zyj.warlock.core.semaphore;

import com.zyj.warlock.annotation.Wsemaphore;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 生成Wmutex对象, 用以在信号量环境下执行业务代码
 *
 * @author zhouyijin
 */
public interface WmutexFactory {

    /**
     * 构造一个Wmutex对象
     *
     * @param pjp        切点
     * @param wsemaphore 信号量的元信息
     * @return 构造好的Wlock
     */
    Wmutex build(ProceedingJoinPoint pjp, Wsemaphore wsemaphore);
}
