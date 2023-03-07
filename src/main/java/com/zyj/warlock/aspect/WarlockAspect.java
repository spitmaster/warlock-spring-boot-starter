package com.zyj.warlock.aspect;

import com.zyj.warlock.annotation.Warlock;
import com.zyj.warlock.core.Wlock;
import com.zyj.warlock.core.factory.WlockFactory;
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

    @Around(value = "@annotation(com.zyj.warlock.annotation.Warlock) && @annotation(warlock)")
    public Object warlockPointcut(final ProceedingJoinPoint pjp, Warlock warlock) throws Throwable {
        //1. 构建warlock
        Wlock wlock = wlockFactory.build(pjp, warlock);
        //2. 在锁的环境下执行业务代码
        return wlock.doWithLock(pjp);
    }

}
