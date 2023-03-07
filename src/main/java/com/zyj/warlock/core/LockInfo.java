package com.zyj.warlock.core;

import com.zyj.warlock.annotation.Wlock;
import com.zyj.warlock.enums.LockType;
import com.zyj.warlock.handler.WaitTimeoutHandler;
import com.zyj.warlock.util.SpelExpressionUtil;
import lombok.Data;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * 锁的基本信息
 *
 * @author zhouyijin
 */
@Data
public class LockInfo {
    /**
     * 锁的唯一标识
     * 会在WarlockAspect中被组装
     * 由 com.zyj.warlock.annotation.Wlock#name() + SpEL表达式计算结果得到
     *
     * @see com.zyj.warlock.annotation.Wlock
     * @see com.zyj.warlock.util.SpelExpressionUtil
     * @see com.zyj.warlock.aspect.WarlockAspect
     */
    private String lockKey;

    /**
     * Wlock 中指定的锁类型
     *
     * @see com.zyj.warlock.annotation.Wlock
     */
    private LockType lockType;

    /**
     * 尝试获取锁的时间
     * 超过该时间还未获得锁, 则调用自定义的接口处理, 如果未指定自定义处理的Handler处理, 如果没有指定handler则直接抛异常
     *
     * @see com.zyj.warlock.annotation.Wlock
     * @see com.zyj.warlock.annotation.Waiting
     */
    private Duration waitTime;

    /**
     * 等待超时之后的处理策略
     */
    private WaitTimeoutHandler waitTimeoutHandler;

    /**
     * 锁超过租期时间的处理方式
     *
     * @see com.zyj.warlock.annotation.Wlock
     * @see com.zyj.warlock.annotation.Leasing
     */
    private Duration leaseTime;

    /**
     * 通过切点和注解中的信息, 构造锁的信息
     *
     * @param pjp   切点
     * @param wlock 注解
     * @return 锁信息
     */
    public static LockInfo from(ProceedingJoinPoint pjp, Wlock wlock) {
        LockInfo lockInfo = new LockInfo();
        //1. 构造lockKey
        //收集锁的信息
        // 获取方法参数值
        Object[] arguments = pjp.getArgs();
        // 获取method
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();

        String lockName = wlock.name();
        // 获取spel表达式
        String keySpEL = wlock.key();
        String key = SpelExpressionUtil.parseSpel(method, arguments, keySpEL, String.class);

        /*
         * construct a lockkey that indicate a unique lock
         * this lock would be used in Warlock.beforeBiz and Warlock.afterBiz and Warlock.except
         */
        String lockKey = lockName + key;
        lockInfo.setLockKey(lockKey);
        //2. 拿到lockType
        lockInfo.setLockType(wlock.lockType());
        //3. 获取等待时间
        Duration waitTime = Duration.of(wlock.waiting().waitTime(), wlock.waiting().timeUnit().toChronoUnit());
        lockInfo.setWaitTime(waitTime);
        //4. 获取等待时间
        Duration leaseTime = Duration.of(wlock.leasing().leaseTime(), wlock.leasing().timeUnit().toChronoUnit());
        lockInfo.setLeaseTime(leaseTime);
        //5. 返回锁信息
        return lockInfo;
    }
}
