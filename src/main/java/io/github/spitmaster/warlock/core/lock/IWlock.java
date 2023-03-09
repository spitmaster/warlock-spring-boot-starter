package io.github.spitmaster.warlock.core.lock;

import org.springframework.lang.NonNull;

/**
 * 由我(zhouyijin)实现的Wlock需要使用到的函数
 *
 * @author zhouyijin
 */
public interface IWlock {

    /**
     * 锁的信息
     *
     * @return LockInfo对象, 不能为空
     */
    @NonNull
    LockInfo getLockInfo();
}
