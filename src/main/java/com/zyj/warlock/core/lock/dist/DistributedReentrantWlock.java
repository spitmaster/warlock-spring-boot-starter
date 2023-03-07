package com.zyj.warlock.core.lock.dist;

import com.zyj.warlock.core.lock.LockInfo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;


/**
 * 基于redisson实现的可重入锁
 *
 * @author zhouyijin
 */
public class DistributedReentrantWlock extends AbstractDistributedWlock {

    private RedissonClient redissonClient;
    private LockInfo lockInfo;

    public DistributedReentrantWlock(RedissonClient redissonClient, LockInfo lockInfo) {
        this.redissonClient = redissonClient;
        this.lockInfo = lockInfo;
    }

    @Override
    public LockInfo getLockInfo() {
        return lockInfo;
    }

    @Override
    protected RLock getRLock() {
        return redissonClient.getLock(lockInfo.getLockKey());
    }
}
