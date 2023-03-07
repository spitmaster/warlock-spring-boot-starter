package com.zyj.warlock.core.lock.dist;

import com.zyj.warlock.core.LockInfo;
import org.redisson.Redisson;
import org.redisson.api.RLock;


/**
 * 基于redisson实现的写锁
 *
 * @author zhouyijin
 */
public class DistributedWriteWlock extends AbstractDistributedWlock {

    private Redisson redisson;
    private LockInfo lockInfo;

    public DistributedWriteWlock(Redisson redisson, LockInfo lockInfo) {
        this.redisson = redisson;
        this.lockInfo = lockInfo;
    }

    @Override
    public LockInfo getLockInfo() {
        return lockInfo;
    }

    @Override
    protected RLock getRLock() {
        return redisson.getReadWriteLock(lockInfo.getLockKey()).writeLock();
    }
}
