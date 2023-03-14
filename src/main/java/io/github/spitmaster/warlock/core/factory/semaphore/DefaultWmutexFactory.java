package io.github.spitmaster.warlock.core.factory.semaphore;

import com.google.common.base.Joiner;
import io.github.spitmaster.warlock.annotation.Leasing;
import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.annotation.Wsemaphore;
import io.github.spitmaster.warlock.core.factory.AbstractFactory;
import io.github.spitmaster.warlock.core.semaphore.DistributedWmutex;
import io.github.spitmaster.warlock.core.semaphore.SemaphoreInfo;
import io.github.spitmaster.warlock.core.semaphore.StandaloneWmutex;
import io.github.spitmaster.warlock.core.semaphore.Wmutex;
import io.github.spitmaster.warlock.enums.Scope;
import io.github.spitmaster.warlock.exceptions.WarlockException;
import io.github.spitmaster.warlock.util.JoinPointUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.BeanFactory;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Arrays;

/**
 * 默认的Wmutex工厂实现类
 *
 * @author zhouyijin
 */
public class DefaultWmutexFactory extends AbstractFactory implements WmutexFactory {

    private final RedissonClient redissonClient;

    public DefaultWmutexFactory(BeanFactory beanFactory, @Nullable RedissonClient redissonClient) {
        super(beanFactory);
        this.redissonClient = redissonClient;
    }

    @Override
    public Wmutex build(ProceedingJoinPoint pjp, Wsemaphore wsemaphore) {
        Scope scope = wsemaphore.scope();
        switch (scope) {
            case STANDALONE:
                //JVM单例使用的信号量
                return new StandaloneWmutex(this.buildLockInfo(pjp, wsemaphore));
            case DISTRIBUTED:
                //分布式信号量
                if (redissonClient == null) {
                    //如果项目没有使用Redisson,则不支持使用分布式锁
                    throw new WarlockException("Not supported lock scope: DISTRIBUTED ; please use redisson client to active this function; method: " + JoinPointUtil.methodName(pjp));
                }
                return new DistributedWmutex(this.buildLockInfo(pjp, wsemaphore), redissonClient);
        }
        throw new WarlockException("Wrong semaphore scope; scope = " + scope);
    }

    private SemaphoreInfo buildLockInfo(ProceedingJoinPoint pjp, Wsemaphore wsemaphore) {
        SemaphoreInfo semaphoreInfo = new SemaphoreInfo();
        //1. 信号量的key
        String semaphoreKey = Joiner
                .on(':')
                .skipNulls()
                .join(Arrays.asList(
                        "wsemaphore",
                        wsemaphore.name(),
                        JoinPointUtil.parseSpEL(pjp, wsemaphore.key())
                ));

        semaphoreInfo.setSemaphoreKey(semaphoreKey);
        //2. 信号量的permits
        int permits = wsemaphore.permits();
        if (permits < 1) {
            throw new WarlockException("Wsemaphore permits cannot below than 1; method =" + JoinPointUtil.methodName(pjp));
        }
        semaphoreInfo.setPermits(permits);
        //3. 等待策略信息
        Waiting waiting = wsemaphore.waiting();
        Duration waitTime = Duration.of(waiting.waitTime(), waiting.timeUnit().toChronoUnit());
        if (waitTime.isNegative() || waitTime.isZero()) {
            throw new WarlockException("WaitTime cannot Less than or equal to 0; method = " + JoinPointUtil.methodName(pjp));
        }
        semaphoreInfo.setWaitTime(waitTime);
        semaphoreInfo.setWaitTimeoutHandler(this.getWaitTimeoutHandler(waiting));
        //4. 租赁策略信息(单机版的不起作用)
        Leasing leasing = wsemaphore.leasing();
        Duration leaseTime = Duration.of(leasing.leaseTime(), leasing.timeUnit().toChronoUnit());
        if (leaseTime.isNegative() || leaseTime.isZero()) {
            throw new WarlockException("LeaseTime cannot Less than or equal to 0; method = " + JoinPointUtil.methodName(pjp));
        }
        semaphoreInfo.setLeaseTime(leaseTime);
        semaphoreInfo.setLeaseTimeoutHandler(this.getLeaseTimeoutHandler(leasing));
        return semaphoreInfo;
    }

}
