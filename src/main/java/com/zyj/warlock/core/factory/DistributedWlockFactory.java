package com.zyj.warlock.core.factory;

import com.zyj.warlock.annotation.Warlock;
import com.zyj.warlock.core.LockInfo;
import com.zyj.warlock.core.Wlock;
import com.zyj.warlock.core.lock.PlainWarlock;
import com.zyj.warlock.core.lock.dist.DistributedReadWlock;
import com.zyj.warlock.core.lock.dist.DistributedReentrantWlock;
import com.zyj.warlock.core.lock.dist.DistributedWriteWlock;
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
        //1. 构造锁信息
        LockInfo lockInfo = buildLock(pjp, warlock);
        //2. 根据锁类型选择合适的锁
        //According lock type decide what wlock should be used
        Wlock wlock;
        switch (lockInfo.getLockType()) {
            case REENTRANT:
                wlock = new DistributedReentrantWlock(redisson, lockInfo);
                break;
            case READ:
                wlock = new DistributedReadWlock(redisson, lockInfo);
                break;
            case WRITE:
                wlock = new DistributedWriteWlock(redisson, lockInfo);
                break;
            default:
                wlock = PlainWarlock.INSTANCE;
                break;
        }
        return wlock;
    }


    @Override
    protected BeanFactory getBeanFactory() {
        return this.beanFactory;
    }
}