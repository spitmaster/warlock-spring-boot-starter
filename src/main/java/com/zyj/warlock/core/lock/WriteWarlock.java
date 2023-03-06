package com.zyj.warlock.core.lock;

import com.zyj.warlock.core.LockInfo;
import com.zyj.warlock.core.Warlock;

public class WriteWarlock implements Warlock {

    private final LockInfo lockInfo;

    public WriteWarlock(LockInfo lockInfo) {
        this.lockInfo = lockInfo;
    }

    @Override
    public Object doWithLock(BizFunction bizFunc) throws Throwable {
        return null;
    }
}
