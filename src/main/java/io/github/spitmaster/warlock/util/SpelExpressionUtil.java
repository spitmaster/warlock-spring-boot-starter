package io.github.spitmaster.warlock.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

public class SpelExpressionUtil {
    private static final ExpressionParser PARSER = new SpelExpressionParser();

    public static <T> T parseSpel(Method targetMethod, Object[] args, String spel, Class<T> paramType) {
        EvaluationContext context = new StandardEvaluationContext();
        LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
        String[] paramNames = discoverer.getParameterNames(targetMethod);
        if (ArrayUtils.isEmpty(paramNames)) {
            return null;
        }

        for (int len = 0; len < paramNames.length; ++len) {
            context.setVariable(paramNames[len], args[len]);
        }

        if (StringUtils.isBlank(spel)) {
            return null;
        } else {
            Expression expression = PARSER.parseExpression(spel);
            return expression.getValue(context, paramType);
        }
    }
}