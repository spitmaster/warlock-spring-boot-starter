package com.zyj.warlock.aspect;

import com.zyj.warlock.annotation.Wsemaphore;
import com.zyj.warlock.exceptions.WarlockException;
import com.zyj.warlock.util.JoinPointUtil;
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

    /**
     * 信号量的切点处理
     *
     * @param pjp        切点
     * @param wsemaphore 信号量的规则
     * @return 正常业务返回的值
     * @throws Throwable 透传异常
     */
    @Around(value = "@annotation(com.zyj.warlock.annotation.Wsemaphore) && @annotation(wsemaphore)")
    public Object wsemaphorePointcut(final ProceedingJoinPoint pjp, Wsemaphore wsemaphore) throws Throwable {
        if (wsemaphore.permits() < 1) {
            //允许同时并发的数量小于1, 等于没吊用
            //不需要信号量控制, 直接执行业务代码
            return pjp.proceed();
        }
        //信号量的唯一key
        String semaphoreKey = wsemaphore.name() + JoinPointUtil.parseSpEL(pjp, wsemaphore.key());
        switch (wsemaphore.scope()) {
            case STANDALONE:
                return doWithStandaloneSemaphore(pjp, semaphoreKey, wsemaphore.permits());
            case DISTRIBUTED:
                return doWithDistributedSemaphore(pjp, semaphoreKey, wsemaphore.permits());
            default:
                break;
        }
        throw new WarlockException("Wrong semaphore scope; scope=" + wsemaphore.scope());
    }

    private Object doWithStandaloneSemaphore(ProceedingJoinPoint pjp, String semaphoreKey, int permits) throws Throwable {
        return null;
    }

    private Object doWithDistributedSemaphore(ProceedingJoinPoint pjp, String semaphoreKey, int permits) throws Throwable {
        return null;
    }

}
