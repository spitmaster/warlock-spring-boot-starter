package io.github.spitmaster.warlock.handler.lock;

import io.github.spitmaster.warlock.core.lock.LockInfo;
import io.github.spitmaster.warlock.exceptions.WarlockException;
import io.github.spitmaster.warlock.util.JoinPointUtil;
import org.aspectj.lang.ProceedingJoinPoint;


/**
 * 单例空实现
 *
 * @author zhouyijin
 */
public enum PlainLockWaitTimeoutHandler implements LockWaitTimeoutHandler {
    //单例
    INSTANCE;

    @Override
    public Object handle(ProceedingJoinPoint pjp, LockInfo lockInfo) throws Throwable {
        throw new WarlockException("warlock wait timeout; timeout from " + JoinPointUtil.methodName(pjp));
    }
}