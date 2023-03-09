package io.github.spitmaster.warlock.handler.lock;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 当你的业务代码耗时太长, 以至于最后释放锁的时候, 发现锁或者信号量等已经过期了, 这时候的处理handler
 *
 * @author zhouyijin
 */
public interface LeaseTimeoutHandler {

    /**
     * 在业务代码执行完之后, 检查如果锁租期超时的时候触发的回调方法
     *
     * @param pjp      方法切点
     * @return 如果返回null, 则继续使用业务代码的返回值, 如果返回值不为空, 则替代原来的业务方法的返回值
     * @throws Throwable pjp操作可能会抛出的异常
     */
    Object handle(ProceedingJoinPoint pjp) throws Throwable;
}
