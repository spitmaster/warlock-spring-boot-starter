package com.zyj.warlock.core.lock;

import com.zyj.warlock.core.LockInfo;
import org.springframework.lang.NonNull;

/**
 * 由我(zhouyijin)实现的Warlock需要使用到的函数
 *
 * @author zhouyijin
 */
public interface DefaultWarlock {

    /**
     * 锁的信息
     *
     * @return LockInfo对象, 不能为空
     */
    @NonNull
    LockInfo getLockInfo();
}
