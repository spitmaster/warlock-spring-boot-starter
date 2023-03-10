package io.github.spitmaster.warlock.aspect;

import io.github.spitmaster.warlock.annotation.Wsemaphore;
import io.github.spitmaster.warlock.core.factory.semaphore.WmutexFactory;
import io.github.spitmaster.warlock.core.semaphore.Wmutex;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * 信号量注解的切面
 *
 * @author zhouyijin
 */
@Aspect
public class WsemaphoreAspect {

    private final WmutexFactory wmutexFactory;

    public WsemaphoreAspect(WmutexFactory wmutexFactory) {
        this.wmutexFactory = wmutexFactory;
    }

    /**
     * 信号量的切点处理
     *
     * @param pjp        切点
     * @param wsemaphore 信号量的规则
     * @return 正常业务返回的值
     * @throws Throwable 透传异常
     */
    @Around(value = "@annotation(io.github.spitmaster.warlock.annotation.Wsemaphore) && @annotation(wsemaphore)")
    public Object wsemaphorePointcut(final ProceedingJoinPoint pjp, Wsemaphore wsemaphore) throws Throwable {
        Wmutex wmutex = wmutexFactory.build(pjp, wsemaphore);
        return wmutex.doBizWithSemaphore(pjp);
    }

}
