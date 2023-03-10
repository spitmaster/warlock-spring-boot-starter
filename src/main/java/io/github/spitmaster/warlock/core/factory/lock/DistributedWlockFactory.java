package io.github.spitmaster.warlock.core.factory.lock;

import io.github.spitmaster.warlock.annotation.Warlock;
import io.github.spitmaster.warlock.core.lock.LockInfo;
import io.github.spitmaster.warlock.core.lock.Wlock;
import io.github.spitmaster.warlock.core.lock.dist.DistributedReadWlock;
import io.github.spitmaster.warlock.core.lock.dist.DistributedReentrantWlock;
import io.github.spitmaster.warlock.core.lock.dist.DistributedWriteWlock;
import io.github.spitmaster.warlock.exceptions.WarlockException;
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

    private final RedissonClient redissonClient;

    public DistributedWlockFactory(BeanFactory beanFactory, RedissonClient redissonClient) {
        super(beanFactory);
        this.redissonClient = redissonClient;
    }

    @Override
    public Wlock build(ProceedingJoinPoint pjp, Warlock warlock) {
        //1. 构造锁信息
        LockInfo lockInfo = buildLockInfo(pjp, warlock);
        //2. 根据锁类型选择合适的锁
        //According lock type decide what wlock should be used
        switch (lockInfo.getLockType()) {
            case REENTRANT:
                return new DistributedReentrantWlock(redissonClient, lockInfo);
            case READ:
                return new DistributedReadWlock(redissonClient, lockInfo);
            case WRITE:
                return new DistributedWriteWlock(redissonClient, lockInfo);
            default:
                throw new WarlockException("Unsupported lock type; type = " + lockInfo.getLockType());
        }
    }
}