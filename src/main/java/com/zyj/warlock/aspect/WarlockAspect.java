package com.zyj.warlock.aspect;

import com.zyj.warlock.annotation.Wlock;
import com.zyj.warlock.core.Warlock;
import com.zyj.warlock.core.WarlockFactory;
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

    private final WarlockFactory warlockFactory;

    public WarlockAspect(WarlockFactory warlockFactory) {
        this.warlockFactory = warlockFactory;
    }

    @Around(value = "@annotation(com.zyj.warlock.annotation.Wlock) && @annotation(wlock)")
    public Object warlockPointcut(final ProceedingJoinPoint pjp, Wlock wlock) throws Throwable {
        //1. 构建warlock
        Warlock warlock = warlockFactory.build(pjp, wlock);
        //2. 在锁的环境下执行业务代码
        return warlock.doWithLock(pjp);
    }

}
