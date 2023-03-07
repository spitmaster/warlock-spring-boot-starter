package com.zyj.warlock.core.lock;

import com.zyj.warlock.core.LockInfo;
import com.zyj.warlock.core.Warlock;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于JUC实现的可重入Warlock
 *
 * @author zhouyijin
 */
public class ReentrantWarlock implements Warlock {

    /**
     * 一个锁的池子
     * 第一次拿, 会初始化这个锁
     * 每次拿都会在计数器上+1, pair.right += 1
     * 每次归还都会在计数器上-1, pair.left -= 1
     * 当计数器归零, 则从内存中移除这个锁
     */
    private static final ConcurrentHashMap<String, Pair<ReentrantLock, AtomicInteger>> REENTRANT_LOCK_MAP = new ConcurrentHashMap<>();

    /**
     * 锁的具体信息
     */
    private final LockInfo lockInfo;

    public ReentrantWarlock(LockInfo lockInfo) {
        this.lockInfo = lockInfo;
    }

    @Override
    public Object doWithLock(BizFunction bizFunc) throws Throwable {
        //1. 拿锁
        ReentrantLock lock = getLock();
        //2. 上锁
        lock.lock();
        try {
            //3. 执行业务代码
            return bizFunc.doBiz();
        } finally {
            //4. 解锁
            lock.unlock();
            //5. 还锁
            returnLock();
        }
    }

    //根据lockInfo 拿锁
    private ReentrantLock getLock() {
        Pair<ReentrantLock, AtomicInteger> lockPair = REENTRANT_LOCK_MAP.compute(lockInfo.getLockKey(), (s, pair) -> {
            if (pair == null) {
                //没有就初始化
                pair = new ImmutablePair<>(new ReentrantLock(), new AtomicInteger(0));
            }
            pair.getRight().incrementAndGet();
            return pair;
        });
        return lockPair.getLeft();
    }

    //根据lockInfo 归还锁
    private void returnLock() {
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
