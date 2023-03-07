package com.zyj.warlock.core;

import com.zyj.warlock.annotation.Wlock;
import com.zyj.warlock.core.lock.ReadWarlock;
import com.zyj.warlock.core.lock.ReentrantWarlock;
import com.zyj.warlock.core.lock.WriteWarlock;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * WarlockFactory的简易实现
 *
 * @author zhouyijin
 */
public class DefaultWarlockFactory implements WarlockFactory {

    private static final Warlock BLANK_WARLOCK = new BlankWarlock();

    @Override
    public Warlock build(ProceedingJoinPoint pjp, Wlock wlock) {
        //1. 构造锁
        LockInfo lockInfo = LockInfo.from(pjp, wlock);

        //2. 根据锁类型选择合适的锁
        //According lock type decide what warlock should be used
        Warlock warlock;
        switch (lockInfo.getLockType()) {
            case REENTRANT:
                warlock = new ReentrantWarlock(lockInfo);
                break;
            case READ:
                warlock = new ReadWarlock(lockInfo);
                break;
            case WRITE:
                warlock = new WriteWarlock(lockInfo);
                break;
            default:
                warlock = BLANK_WARLOCK;
                break;
        }
        return warlock;
    }


    /**
     * 空实现
     */
    private static class BlankWarlock implements Warlock {
        @Override
        public Object doWithLock(BizFunction bizFunc) throws Throwable {
            return bizFunc.doBiz();
        }
    }
}
