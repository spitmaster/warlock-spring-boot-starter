package com.zyj.warlock.core.lock;

import com.zyj.warlock.core.LockInfo;
import com.zyj.warlock.core.Warlock;

public class WriteWarlock implements Warlock {

    private LockInfo lockInfo;

    public WriteWarlock(LockInfo lockInfo) {
        this.lockInfo = lockInfo;
    }

    @Override
    public void beforeBiz() {

    }

    @Override
    public void afterBiz() {

    }

    @Override
    public void except(Exception e) {

    }

}
