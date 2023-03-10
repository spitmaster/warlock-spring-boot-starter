package io.github.spitmaster.warlock.core.factory.barrier;

import com.google.common.base.Joiner;
import io.github.spitmaster.warlock.annotation.Leasing;
import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.annotation.WcyclicBarrier;
import io.github.spitmaster.warlock.core.barrier.BarrierInfo;
import io.github.spitmaster.warlock.core.barrier.DistributedWBarrier;
import io.github.spitmaster.warlock.core.barrier.StandaloneWBarrier;
import io.github.spitmaster.warlock.core.barrier.WBarrier;
import io.github.spitmaster.warlock.core.factory.AbstractFactory;
import io.github.spitmaster.warlock.enums.Scope;
import io.github.spitmaster.warlock.exceptions.WarlockException;
import io.github.spitmaster.warlock.util.JoinPointUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.BeanFactory;

import java.time.Duration;
import java.util.Arrays;

/**
 * 默认实现
 * 我今天头很疼, 难受, 不想写注释
 *
 * @author zhouyijin
 */
public class DefaultWBarrierFactory extends AbstractFactory implements WBarrierFactory {

    private final RedissonClient redissonClient;

    public DefaultWBarrierFactory(BeanFactory beanFactory, RedissonClient redissonClient) {
        super(beanFactory);
        this.redissonClient = redissonClient;
    }

    @Override
    public WBarrier build(ProceedingJoinPoint pjp, WcyclicBarrier wcyclicBarrier) {
        Scope scope = wcyclicBarrier.scope();
        switch (scope) {
            case STANDALONE:
                //JVM单例使用的信号量
                return new StandaloneWBarrier(this.buildBarrierInfo(pjp, wcyclicBarrier));
            case DISTRIBUTED:
                //分布式信号量
                if (redissonClient == null) {
                    //如果项目没有使用Redisson,则不支持使用分布式锁
                    throw new WarlockException("Not supported lock scope: DISTRIBUTED ; please use redisson client to active this function; method: " + JoinPointUtil.methodName(pjp));
                }
                return new DistributedWBarrier(this.buildBarrierInfo(pjp, wcyclicBarrier), redissonClient);
        }
        throw new WarlockException("Wrong WcyclicBarrier scope; scope =" + scope);
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
        //4. 租赁策略信息(单机版的不起作用)
        Leasing leasing = wcyclicBarrier.leasing();
        Duration leaseTime = Duration.of(leasing.leaseTime(), leasing.timeUnit().toChronoUnit());
        if (leaseTime.isNegative() || leaseTime.isZero()) {
            throw new WarlockException("LeaseTime cannot Less than or equal to 0; method = " + JoinPointUtil.methodName(pjp));
        }
        barrierInfo.setLeaseTime(leaseTime);
        barrierInfo.setLeaseTimeoutHandler(this.getLeaseTimeoutHandler(leasing));
        return barrierInfo;
    }
}
