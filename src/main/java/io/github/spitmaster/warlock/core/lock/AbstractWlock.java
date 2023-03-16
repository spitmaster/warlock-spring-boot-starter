package io.github.spitmaster.warlock.core.lock;

import io.github.spitmaster.warlock.core.Waround;
import org.springframework.lang.NonNull;

/**
 * 锁的抽象类
 *
 * @author zhouyijin
 */
public abstract class AbstractWlock implements Waround {

    /**
     * 锁的信息
     *
     * @return LockInfo对象, 不能为空
     */
    @NonNull
    protected abstract LockInfo getLockInfo();

}
