package io.github.spitmaster.warlock.core.lock.dist;

import io.github.spitmaster.warlock.core.lock.LockInfo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;


/**
 * 基于redisson实现的写锁
 *
 * @author zhouyijin
 */
public class DistributedWriteWlock extends AbstractDistributedWlock {

    private RedissonClient redissonClient;
    private LockInfo lockInfo;

    public DistributedWriteWlock(RedissonClient redissonClient, LockInfo lockInfo) {
        this.redissonClient = redissonClient;
        this.lockInfo = lockInfo;
    }

    @Override
    public LockInfo getLockInfo() {
        return lockInfo;
    }

    @Override
    protected RLock getRLock() {
        return redissonClient.getReadWriteLock(lockInfo.getLockKey()).writeLock();
    }
}
