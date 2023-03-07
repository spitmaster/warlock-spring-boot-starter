package com.zyj.warlock.core.lock;

import com.zyj.warlock.core.LockInfo;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 基于JUC实现的读锁
 *
 * @author zhouyijin
 * @see WriteWarlock 与 WriteWarlock 共同使用一个Map管理锁
 */
public class ReadWarlock extends AbstractStandaloneWarlock {

    private final LockInfo lockInfo;

    public ReadWarlock(LockInfo lockInfo) {
        this.lockInfo = lockInfo;
    }

    @Override
    protected LockInfo getLockInfo() {
        return this.lockInfo;
    }

    @Override
    protected ReentrantReadWriteLock.ReadLock getLock() {
        Pair<ReentrantReadWriteLock, AtomicInteger> lockPair = WriteWarlock.READ_WRITE_LOCK_MAP.compute(lockInfo.getLockKey(), (s, pair) -> {
            if (pair == null) {
                //没有就初始化
                pair = Pair.of(new ReentrantReadWriteLock(), new AtomicInteger(0));
            }
            pair.getRight().incrementAndGet();
            return pair;
        });
        //2. 上锁
        return lockPair.getLeft().readLock();
    }

    @Override
    protected void returnLock() {
        WriteWarlock.READ_WRITE_LOCK_MAP.computeIfPresent(lockInfo.getLockKey(), (s, pair) -> {
            int holdCount = pair.getRight().decrementAndGet();
            if (holdCount <= 0) {
                //返回null,相当于把这个value给移除了
                return null;
            }
            return pair;
        });
    }
}
