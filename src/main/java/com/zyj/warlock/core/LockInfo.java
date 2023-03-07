package com.zyj.warlock.core;

import com.zyj.warlock.annotation.Wlock;
import com.zyj.warlock.enums.LockType;
import com.zyj.warlock.util.SpelExpressionUtil;
import lombok.Data;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

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
     * 通过切点和注解中的信息, 构造锁的信息
     *
     * @param pjp   切点
     * @param wlock 注解
     * @return 锁信息
     */
    public static LockInfo from(ProceedingJoinPoint pjp, Wlock wlock) {
        LockInfo lockInfo = new LockInfo();
        //1. 拿到lockType
        lockInfo.setLockType(wlock.lockType());
        //2. 构造lockKey
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
        //3. 返回锁信息
        return lockInfo;
    }
}
