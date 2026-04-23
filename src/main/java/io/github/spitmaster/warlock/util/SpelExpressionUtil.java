package io.github.spitmaster.warlock.util;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

public class SpelExpressionUtil {

    private SpelExpressionUtil(){}

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    public static <T> T parseSpel(Method targetMethod, Object[] args, String spel, Class<T> paramType) {
        //空表达式直接返回null (无论方法是否有参数)
        if (isBlank(spel)) {
            return null;
        }
        EvaluationContext context = new StandardEvaluationContext();
        //只在方法有参数时才绑定变量; 无参数方法仍可使用不依赖参数的表达式
        //例如 'fixedKey' 或 T(Thread).currentThread().getName()
        LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
        String[] paramNames = discoverer.getParameterNames(targetMethod);
        if (paramNames != null && args != null) {
            int bindCount = Math.min(paramNames.length, args.length);
            for (int i = 0; i < bindCount; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        Expression expression = PARSER.parseExpression(spel);
        return expression.getValue(context, paramType);
    }

    //copied from org.apache.commons.lang3.StringUtils#isBlank
    private static boolean isBlank(final CharSequence cs) {
        final int strLen = cs == null ? 0 : cs.length();
        if (strLen == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}