package io.github.spitmaster.warlock.util;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

public class SpelExpressionUtil {
    private static final ExpressionParser PARSER = new SpelExpressionParser();

    public static <T> T parseSpel(Method targetMethod, Object[] args, String spel, Class<T> paramType) {
        EvaluationContext context = new StandardEvaluationContext();
        LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
        String[] paramNames = discoverer.getParameterNames(targetMethod);
        if (paramNames == null || Array.getLength(paramNames) == 0) {
            return null;
        }

        for (int len = 0; len < paramNames.length; ++len) {
            context.setVariable(paramNames[len], args[len]);
        }

        if (isBlank(spel)) {
            return null;
        } else {
            Expression expression = PARSER.parseExpression(spel);
            return expression.getValue(context, paramType);
        }
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