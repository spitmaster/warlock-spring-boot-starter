package io.github.spitmaster.warlock.core.lock.factory;

import io.github.spitmaster.warlock.annotation.Warlock;
import io.github.spitmaster.warlock.core.lock.LockInfo;
import io.github.spitmaster.warlock.core.lock.PlainWarlock;
import io.github.spitmaster.warlock.core.lock.Wlock;
import io.github.spitmaster.warlock.core.lock.dist.DistributedReadWlock;
import io.github.spitmaster.warlock.core.lock.dist.DistributedReentrantWlock;
import io.github.spitmaster.warlock.core.lock.dist.DistributedWriteWlock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.BeanFactory;

/**
 * 生成分布式Warlock
 * 依赖于Redisson
 *
 * @author zhouyijin
 */
public class DistributedWlockFactory extends AbstractWarlockFactory implements WlockFactory {

    private final BeanFactory beanFactory;
    private final RedissonClient redissonClient;

    public DistributedWlockFactory(BeanFactory beanFactory, RedissonClient redissonClient) {
        this.beanFactory = beanFactory;
        this.redissonClient = redissonClient;
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
                wlock = new DistributedReentrantWlock(redissonClient, lockInfo);
                break;
            case READ:
                wlock = new DistributedReadWlock(redissonClient, lockInfo);
                break;
            case WRITE:
                wlock = new DistributedWriteWlock(redissonClient, lockInfo);
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