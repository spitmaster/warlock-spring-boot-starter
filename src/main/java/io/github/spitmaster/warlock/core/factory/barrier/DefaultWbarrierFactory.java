package io.github.spitmaster.warlock.core.factory.barrier;

import com.google.common.base.Joiner;
import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.annotation.WcyclicBarrier;
import io.github.spitmaster.warlock.core.Waround;
import io.github.spitmaster.warlock.core.barrier.BarrierInfo;
import io.github.spitmaster.warlock.core.barrier.StandaloneWbarrier;
import io.github.spitmaster.warlock.core.factory.TimeoutHandlerProvider;
import io.github.spitmaster.warlock.core.factory.WaroundFactory;
import io.github.spitmaster.warlock.exceptions.WarlockException;
import io.github.spitmaster.warlock.util.SpelExpressionUtil;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;

/**
 * 默认实现
 * 我今天头很疼, 难受, 不想写注释
 *
 * @author zhouyijin
 */
public class DefaultWbarrierFactory implements WaroundFactory {

    private final TimeoutHandlerProvider timeoutHandlerProvider;

    public DefaultWbarrierFactory(TimeoutHandlerProvider timeoutHandlerProvider) {
        this.timeoutHandlerProvider = timeoutHandlerProvider;
    }

    @Override
    public Waround build(MethodInvocation methodInvocation) {
        return new StandaloneWbarrier(this.buildBarrierInfo(methodInvocation));
    }

    private BarrierInfo buildBarrierInfo(MethodInvocation methodInvocation) {
        Method method = methodInvocation.getMethod();
        WcyclicBarrier wcyclicBarrier = AnnotatedElementUtils.findMergedAnnotation(method, WcyclicBarrier.class);
        BarrierInfo barrierInfo = new BarrierInfo();
        //1. 组装唯一key
        String barrierKey = Joiner
                .on(':')
                .skipNulls()
                .join(Arrays.asList(
                        "wcyclicbarrier",
                        wcyclicBarrier.name(),
                        SpelExpressionUtil.parseSpel(method, methodInvocation.getArguments(), wcyclicBarrier.key(), String.class)
                ));
        barrierInfo.setBarrierKey(barrierKey);
        //2. barrier的parties
        int parties = wcyclicBarrier.parties();
        if (parties < 1) {
            throw new WarlockException("WcyclicBarrier parties cannot below than 1; method =" + method.getName());
        }
        barrierInfo.setParties(parties);
        //3. 等待策略信息
        Waiting waiting = wcyclicBarrier.waiting();
        Duration waitTime = Duration.of(waiting.waitTime(), waiting.timeUnit());
        if (waitTime.isNegative() || waitTime.isZero()) {
            throw new WarlockException("WaitTime cannot Less than or equal to 0; method = " + method.getName());
        }
        barrierInfo.setWaitTime(waitTime);
        barrierInfo.setWaitTimeoutHandler(timeoutHandlerProvider.getWaitTimeoutHandler(waiting));
        return barrierInfo;
    }
}
