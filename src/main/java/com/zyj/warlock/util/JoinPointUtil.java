package com.zyj.warlock.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * 操作切点的一些工具类
 *
 * @author zhouyijin
 */
public class JoinPointUtil {

    /**
     * 获取方法名
     *
     * @param pjp 切点
     * @return 切点所在的方法名
     */
    public static String methodName(ProceedingJoinPoint pjp) {
        return method(pjp).getName();
    }

    /**
     * 获取切点的方法
     *
     * @param pjp 切点
     * @return 切点所在的方法
     */
    public static Method method(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        return signature.getMethod();
    }

}
