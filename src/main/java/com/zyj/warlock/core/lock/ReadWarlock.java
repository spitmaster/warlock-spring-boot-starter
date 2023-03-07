package com.zyj.warlock.core.lock;

import com.zyj.warlock.core.LockInfo;
import com.zyj.warlock.core.Warlock;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 基于JUC实现的读锁
 *
 * @author zhouyijin
 * @see WriteWarlock 与 WriteWarlock 共同使用一个Map管理锁
 */
public class ReadWarlock implements Warlock {

    private final LockInfo lockInfo;

    public ReadWarlock(LockInfo lockInfo) {
        this.lockInfo = lockInfo;
    }

    @Override
    public Object doWithLock(ProceedingJoinPoint pjp) throws Throwable {
        //1. 拿锁
        Pair<ReentrantReadWriteLock, AtomicInteger> lockPair = WriteWarlock.READ_WRITE_LOCK_MAP.compute(lockInfo.getLockKey(), (s, pair) -> {
            if (pair == null) {
                //没有就初始化
                pair = new ImmutablePair<>(new ReentrantReadWriteLock(), new AtomicInteger(0));
            }
            pair.getRight().incrementAndGet();
            return pair;
        });
        //2. 上锁
        lockPair.getLeft().readLock().lock();
        try {
            //3. 执行业务代码
            return pjp.proceed();
        } finally {
            //4. 解锁
            lockPair.getLeft().readLock().unlock();
            //5. 还锁
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
}
