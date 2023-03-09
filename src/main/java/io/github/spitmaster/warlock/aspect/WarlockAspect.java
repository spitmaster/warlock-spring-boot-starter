package io.github.spitmaster.warlock.aspect;

import io.github.spitmaster.warlock.annotation.Warlock;
import io.github.spitmaster.warlock.core.lock.Wlock;
import io.github.spitmaster.warlock.core.factory.lock.WlockFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;


/**
 * 处理@Warlock注解的切面
 * 让你比较方便的使用锁
 *
 * @author zhouyijin
 */
@Aspect
public class WarlockAspect {

    private final WlockFactory wlockFactory;

    public WarlockAspect(WlockFactory wlockFactory) {
        this.wlockFactory = wlockFactory;
    }

    @Around(value = "@annotation(io.github.spitmaster.warlock.annotation.Warlock) && @annotation(warlock)")
    public Object warlockPointcut(final ProceedingJoinPoint pjp, Warlock warlock) throws Throwable {
        //1. 构建warlock
        Wlock wlock = wlockFactory.build(pjp, warlock);
        //2. 在锁的环境下执行业务代码
        return wlock.doWithLock(pjp);
    }

}
