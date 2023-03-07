package com.zyj.warlock.core.lock;

import com.zyj.warlock.core.LockInfo;
import com.zyj.warlock.core.Warlock;

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
    public Object doWithLock(BizFunction bizFunc) throws Throwable {
        return null;
    }
}
