package com.zyj.warlock.core.factory;

import com.zyj.warlock.annotation.Leasing;
import com.zyj.warlock.annotation.Waiting;
import com.zyj.warlock.annotation.Warlock;
import com.zyj.warlock.core.LockInfo;
import com.zyj.warlock.core.Wlock;
import com.zyj.warlock.core.lock.PlainWarlock;
import com.zyj.warlock.core.lock.standalone.ReadWarlock;
import com.zyj.warlock.core.lock.standalone.ReentrantWarlock;
import com.zyj.warlock.core.lock.standalone.WriteWarlock;
import com.zyj.warlock.util.SpelExpressionUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.BeanFactory;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * 生产单机使用的Warlock的Factory实现
 *
 * @author zhouyijin
 */
public class StandaloneWlockFactory extends AbstractWarlockFactory implements WlockFactory {

    private BeanFactory beanFactory;

    public StandaloneWlockFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public Wlock build(ProceedingJoinPoint pjp, Warlock warlock) {
        //1. 构造锁
        LockInfo lockInfo = buildLock(pjp, warlock);

        //2. 根据锁类型选择合适的锁
        //According lock type decide what warlock should be used
        Wlock wlock;
        switch (lockInfo.getLockType()) {
            case REENTRANT:
                wlock = new ReentrantWarlock(lockInfo);
                break;
            case READ:
                wlock = new ReadWarlock(lockInfo);
                break;
            case WRITE:
                wlock = new WriteWarlock(lockInfo);
                break;
            default:
                wlock = PlainWarlock.INSTANCE;
                break;
        }
        return wlock;
    }

    @Override
    protected BeanFactory getBeanFactory() {
        return beanFactory;
    }

}