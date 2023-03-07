package com.zyj.warlock.core.lock.dist;

import com.zyj.warlock.core.lock.LockInfo;
import org.redisson.Redisson;
import org.redisson.api.RLock;


/**
 * 基于redisson实现的读锁
 *
 * @author zhouyijin
 */
public class DistributedReadWlock extends AbstractDistributedWlock {

    private Redisson redisson;
    private LockInfo lockInfo;

    public DistributedReadWlock(Redisson redisson, LockInfo lockInfo) {
        this.redisson = redisson;
        this.lockInfo = lockInfo;
    }

    @Override
    public LockInfo getLockInfo() {
        return lockInfo;
    }

    @Override
    protected RLock getRLock() {
        return redisson.getReadWriteLock(lockInfo.getLockKey()).readLock();
    }
}
