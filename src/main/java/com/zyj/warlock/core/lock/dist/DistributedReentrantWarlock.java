package com.zyj.warlock.core.lock.dist;

import com.zyj.warlock.core.LockInfo;
import org.redisson.Redisson;
import org.redisson.api.RLock;


/**
 * 基于redisson实现的可重入锁
 *
 * @author zhouyijin
 */
public class DistributedReentrantWarlock extends AbstractDistributedWarlock {

    private Redisson redisson;
    private LockInfo lockInfo;

    public DistributedReentrantWarlock(Redisson redisson, LockInfo lockInfo) {
        this.redisson = redisson;
        this.lockInfo = lockInfo;
    }

    @Override
    public LockInfo getLockInfo() {
        return lockInfo;
    }

    @Override
    protected RLock getRLock() {
        return redisson.getLock(lockInfo.getLockKey());
    }
}
