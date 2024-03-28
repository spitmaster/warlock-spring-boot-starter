package io.github.spitmaster.warlock.util;

import java.lang.reflect.Method;

public class MethodNameUtil {

    private MethodNameUtil() {}

    public static String methodName(Method method) {
        // 获取 Method 所在的类的完整名称
        String className = method.getDeclaringClass().getName();
        // 获取 Method 的名称
        String methodName = method.getName();
        return className + "#" + methodName;
    }
}
