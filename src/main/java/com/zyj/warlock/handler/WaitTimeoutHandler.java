package com.zyj.warlock.handler;

import com.zyj.warlock.core.lock.LockInfo;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 当锁等待超时的时候触发的回调对象
 *
 * @author zhouyijin
 */
public interface WaitTimeoutHandler {

    /**
     * 当锁等待超时的时候触发的回调方法
     *
     * @param pjp      方法切点
     * @param lockInfo 锁的信息
     * @return 替代原来的业务方法的返回值
     * @throws Throwable pjp操作可能会抛出的异常
     */
    Object handle(ProceedingJoinPoint pjp, LockInfo lockInfo) throws Throwable;

}
