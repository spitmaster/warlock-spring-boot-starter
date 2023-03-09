package io.github.spitmaster.warlock.core.semaphore;

import io.github.spitmaster.warlock.annotation.Wsemaphore;
import io.github.spitmaster.warlock.enums.Scope;
import io.github.spitmaster.warlock.exceptions.WarlockException;
import io.github.spitmaster.warlock.util.JoinPointUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.redisson.api.RedissonClient;

/**
 * 默认的Wmutex工厂实现类
 *
 * @author zhouyijin
 */
public class DefaultWmutexFactory implements WmutexFactory {

    private final RedissonClient redissonClient;

    public DefaultWmutexFactory(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public Wmutex build(ProceedingJoinPoint pjp, Wsemaphore wsemaphore) {
        int permits = wsemaphore.permits();
        if (permits < 1) {
            throw new WarlockException("Wsemaphore permits cannot below than 1; method =" + JoinPointUtil.methodName(pjp));
        }
        Scope scope = wsemaphore.scope();
        String semaphoreKey = wsemaphore.name() + JoinPointUtil.parseSpEL(pjp, wsemaphore.key());
        switch (scope) {
            case STANDALONE:
                //JVM单例使用的信号量
                return new StandaloneWmutex(semaphoreKey, wsemaphore);
            case DISTRIBUTED:
                //分布式信号量
                if (redissonClient == null) {
                    //如果项目没有使用Redisson,则不支持使用分布式锁
                    throw new WarlockException("Not supported lock scope: DISTRIBUTED ; please use redisson client to active this function; method: " + JoinPointUtil.methodName(pjp));
                }
                return new DistributedWmutex(semaphoreKey, wsemaphore, redissonClient);
        }
        throw new WarlockException("Wrong semaphore scope; scope =" + wsemaphore.scope());
    }
}
