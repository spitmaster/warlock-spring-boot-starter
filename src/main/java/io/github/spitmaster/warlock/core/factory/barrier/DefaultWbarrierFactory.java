package io.github.spitmaster.warlock.core.factory.barrier;

import com.google.common.base.Joiner;
import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.annotation.WcyclicBarrier;
import io.github.spitmaster.warlock.core.barrier.BarrierInfo;
import io.github.spitmaster.warlock.core.barrier.StandaloneWbarrier;
import io.github.spitmaster.warlock.core.barrier.Wbarrier;
import io.github.spitmaster.warlock.core.factory.AbstractFactory;
import io.github.spitmaster.warlock.exceptions.WarlockException;
import io.github.spitmaster.warlock.util.JoinPointUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.BeanFactory;

import java.time.Duration;
import java.util.Arrays;

/**
 * 默认实现
 * 我今天头很疼, 难受, 不想写注释
 *
 * @author zhouyijin
 */
public class DefaultWbarrierFactory extends AbstractFactory implements WbarrierFactory {

    public DefaultWbarrierFactory(BeanFactory beanFactory) {
        super(beanFactory);
    }

    @Override
    public Wbarrier build(ProceedingJoinPoint pjp, WcyclicBarrier wcyclicBarrier) {
        return new StandaloneWbarrier(this.buildBarrierInfo(pjp, wcyclicBarrier));
    }

    private BarrierInfo buildBarrierInfo(ProceedingJoinPoint pjp, WcyclicBarrier wcyclicBarrier) {
        BarrierInfo barrierInfo = new BarrierInfo();
        //1. 组装唯一key
        String barrierKey = Joiner
                .on(':')
                .skipNulls()
                .join(Arrays.asList(
                        "wcyclicbarrier",
                        wcyclicBarrier.name(),
                        JoinPointUtil.parseSpEL(pjp, wcyclicBarrier.key())
                ));
        barrierInfo.setBarrierKey(barrierKey);
        //2. barrier的parties
        int parties = wcyclicBarrier.parties();
        if (parties < 1) {
            throw new WarlockException("WcyclicBarrier parties cannot below than 1; method =" + JoinPointUtil.methodName(pjp));
        }
        barrierInfo.setParties(parties);
        //3. 等待策略信息
        Waiting waiting = wcyclicBarrier.waiting();
        Duration waitTime = Duration.of(waiting.waitTime(), waiting.timeUnit().toChronoUnit());
        if (waitTime.isNegative() || waitTime.isZero()) {
            throw new WarlockException("WaitTime cannot Less than or equal to 0; method = " + JoinPointUtil.methodName(pjp));
        }
        barrierInfo.setWaitTime(waitTime);
        barrierInfo.setWaitTimeoutHandler(this.getWaitTimeoutHandler(waiting));
        return barrierInfo;
    }
}
