package io.github.spitmaster.warlock.aspect;

import io.github.spitmaster.warlock.annotation.WcyclicBarrier;
import io.github.spitmaster.warlock.core.barrier.Wbarrier;
import io.github.spitmaster.warlock.core.factory.barrier.WbarrierFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * WcyclicBarrier 注解的切面
 *
 * @author zhouyijin
 */
@Aspect
public class WcyclicBarrierAspect {

    private final WbarrierFactory wbarrierFactory;

    public WcyclicBarrierAspect(WbarrierFactory wbarrierFactory) {
        this.wbarrierFactory = wbarrierFactory;
    }

    /**
     * WcyclicBarrier 的切点处理
     *
     * @param pjp            切点
     * @param wcyclicBarrier CyclicBarrier的规则
     * @return 正常业务返回的值
     * @throws Throwable 透传异常
     */
    @Around(value = "@annotation(io.github.spitmaster.warlock.annotation.WcyclicBarrier) && @annotation(wcyclicBarrier)")
    public Object wcyclicBarrierPointcut(final ProceedingJoinPoint pjp, WcyclicBarrier wcyclicBarrier) throws Throwable {
        Wbarrier wbarrier = wbarrierFactory.build(pjp, wcyclicBarrier);
        return wbarrier.doWithBarrier(pjp);
    }

}
