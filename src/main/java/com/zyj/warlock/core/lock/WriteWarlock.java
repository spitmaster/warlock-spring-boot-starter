package com.zyj.warlock.core.lock;

import com.zyj.warlock.core.LockInfo;
import com.zyj.warlock.core.Warlock;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 基于JUC实现的写锁
 *
 * @author zhouyijin
 */
public class WriteWarlock implements Warlock {

    /**
     * ReadWarlock 与 WriteWarlock 共用同一个Map进行锁的管理
     *
     * @see ReadWarlock 与 ReadWarlock 共用一个Map管理锁
     */
    static final ConcurrentHashMap<String, Pair<ReentrantReadWriteLock, AtomicInteger>> READ_WRITE_LOCK_MAP = new ConcurrentHashMap<>();

    private final LockInfo lockInfo;

    public WriteWarlock(LockInfo lockInfo) {
        this.lockInfo = lockInfo;
    }

    @Override
    public Object doWithLock(BizFunction bizFunc) throws Throwable {
        //1. 拿锁
        Pair<ReentrantReadWriteLock, AtomicInteger> lockPair = READ_WRITE_LOCK_MAP.compute(lockInfo.getLockKey(), (s, pair) -> {
            if (pair == null) {
                //没有就初始化
                pair = new ImmutablePair<>(new ReentrantReadWriteLock(), new AtomicInteger(0));
            }
            pair.getRight().incrementAndGet();
            return pair;
        });
        //2. 上锁
        lockPair.getLeft().writeLock().lock();
        try {
            //3. 执行业务代码
            return bizFunc.doBiz();
        } finally {
            //4. 解锁
            lockPair.getLeft().writeLock().lock();
            //5. 还锁
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
}
