package com.zyj.warlock.core.lock;

import com.zyj.warlock.core.LockInfo;
import com.zyj.warlock.core.Warlock;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantWarlock implements Warlock {

    private static final ConcurrentHashMap<String, Pair<ReentrantLock, AtomicInteger>> REENTRANT_LOCK_MAP = new ConcurrentHashMap<>();

    private final LockInfo lockInfo;

    public ReentrantWarlock(LockInfo lockInfo) {
        this.lockInfo = lockInfo;
    }

    @Override
    public Object doWithLock(BizFunction bizFunc) {
        Pair<ReentrantLock, AtomicInteger> lockPair = REENTRANT_LOCK_MAP.compute(lockInfo.getLockKey(), (s, pair) -> {
            if (pair == null) {
                //没有就初始化
                pair = new ImmutablePair<>(new ReentrantLock(), new AtomicInteger(0));
            }
            pair.getRight().incrementAndGet();
            return pair;
        });
        //加锁
        lockPair.getLeft().lock();
        try {
            return bizFunc.doBiz();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            //解锁
            lockPair.getLeft().unlock();
            REENTRANT_LOCK_MAP.computeIfPresent(lockInfo.getLockKey(), (s, pair) -> {
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
