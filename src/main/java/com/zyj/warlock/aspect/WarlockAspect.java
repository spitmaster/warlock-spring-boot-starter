package com.zyj.warlock.aspect;

import com.zyj.warlock.annotation.Warlock;
import com.zyj.warlock.core.LockInfo;
import com.zyj.warlock.core.WarlockProcessor;
import com.zyj.warlock.util.SpelExpressionUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;


/**
 * 处理@Warlock注解的切面
 * 让你比较方便的使用锁
 *
 * @author zhouyijin
 */
@Slf4j
@Aspect
@AllArgsConstructor
public class WarlockAspect {

    private final WarlockProcessor warlockProcessor;

    @Around(value = "@annotation(com.zyj.warlock.annotation.Warlock) && @annotation(warlock)")
    public Object warlockPointcut(ProceedingJoinPoint pjp, Warlock warlock) throws Throwable {

        // 获取方法参数值
        Object[] arguments = pjp.getArgs();
        // 获取method
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();

        String lockName = warlock.name();
        // 获取spel表达式
        String keySpEL = warlock.key();
        String key = SpelExpressionUtil.parseSpel(method, arguments, keySpEL, String.class);

        String lockKey = lockName + key;

        LockInfo lockInfo = new LockInfo();
        lockInfo.setLockKey(lockKey);
        lockInfo.setLockType(warlock.lockType());
        return warlockProcessor.invokeWithWarlock(lockInfo, pjp);
    }

}
