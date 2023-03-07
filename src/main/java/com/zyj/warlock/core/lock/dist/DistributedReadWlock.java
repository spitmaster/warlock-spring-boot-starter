package com.zyj.warlock.core.lock.dist;

import com.zyj.warlock.core.lock.LockInfo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;


/**
 * 基于redisson实现的读锁
 *
 * @author zhouyijin
 */
public class DistributedReadWlock extends AbstractDistributedWlock {

    private RedissonClient redissonClient;
    private LockInfo lockInfo;

    public DistributedReadWlock(RedissonClient redissonClient, LockInfo lockInfo) {
        this.redissonClient = redissonClient;
        this.lockInfo = lockInfo;
    }

    @Override
    public LockInfo getLockInfo() {
        return lockInfo;
    }

    @Override
    protected RLock getRLock() {
        return redissonClient.getReadWriteLock(lockInfo.getLockKey()).readLock();
    }
}
