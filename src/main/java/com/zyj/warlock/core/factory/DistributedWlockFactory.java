package com.zyj.warlock.core.factory;

import com.zyj.warlock.annotation.Warlock;
import com.zyj.warlock.core.LockInfo;
import com.zyj.warlock.core.Wlock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.redisson.Redisson;
import org.springframework.beans.factory.BeanFactory;

/**
 * 生成分布式Warlock
 * 依赖于Redisson
 *
 * @author zhouyijin
 */
public class DistributedWlockFactory extends AbstractWarlockFactory implements WlockFactory {

    private final BeanFactory beanFactory;
    private final Redisson redisson;

    public DistributedWlockFactory(BeanFactory beanFactory, Redisson redisson) {
        this.beanFactory = beanFactory;
        this.redisson = redisson;
    }

    @Override
    public Wlock build(ProceedingJoinPoint pjp, Warlock warlock) {
        //1. 构造锁
        LockInfo lockInfo = buildLock(pjp, warlock);

        // TODO: 2023/3/7
        return null;
    }

    

    @Override
    protected BeanFactory getBeanFactory() {
        return this.beanFactory;
    }
}