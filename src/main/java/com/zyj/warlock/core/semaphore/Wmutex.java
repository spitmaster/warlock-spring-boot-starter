package com.zyj.warlock.core.semaphore;

import com.zyj.warlock.annotation.Wsemaphore;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 信号量的使用接口
 *
 * @author zhouyijin
 */
public interface Wmutex {

    /**
     * 使用信号量执行业务代码
     *
     * @param pjp        切点
     * @param wsemaphore 信号量的设置信息
     * @return 业务代码返回值
     * @throws Throwable 透传异常
     */
    Object doBizWithSemaphore(final ProceedingJoinPoint pjp, Wsemaphore wsemaphore) throws Throwable;
}
