package com.zyj.warlock.core.lock.standalone;

import com.zyj.warlock.core.lock.LockInfo;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 基于JUC实现的写锁
 *
 * @author zhouyijin
 */
public class WriteWlock extends AbstractStandaloneWlock {

    /**
     * ReadWlock 与 WriteWlock 共用同一个Map进行锁的管理
     *
     * @see ReadWlock 与 ReadWlock 共用一个Map管理锁
     */
    static final ConcurrentHashMap<String, Pair<ReentrantReadWriteLock, AtomicInteger>> READ_WRITE_LOCK_MAP = new ConcurrentHashMap<>();

    private final LockInfo lockInfo;

    public WriteWlock(LockInfo lockInfo) {
        this.lockInfo = lockInfo;
    }

    @Override
    public LockInfo getLockInfo() {
        return this.lockInfo;
    }

    @Override
    protected Lock getLock() {
        Pair<ReentrantReadWriteLock, AtomicInteger> lockPair = READ_WRITE_LOCK_MAP.compute(lockInfo.getLockKey(), (s, pair) -> {
            if (pair == null) {
                //没有锁就初始化锁
                pair = Pair.of(new ReentrantReadWriteLock(), new AtomicInteger(0));
            }
            pair.getRight().incrementAndGet();
            return pair;
        });
        return lockPair.getLeft().writeLock();
    }

    @Override
    protected void returnLock() {
        READ_WRITE_LOCK_MAP.computeIfPresent(lockInfo.getLockKey(), (s, pair) -> {
            int holdCount = pair.getRight().decrementAndGet();
            if (holdCount <= 0) {
                //返回null,相当于把这个value给移除了
                return null;
            }
            return pair;
        });
    }
}
