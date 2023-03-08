package io.github.spitmaster.warlock.core.semaphore;

import io.github.spitmaster.warlock.annotation.Wsemaphore;
import io.github.spitmaster.warlock.enums.Scope;
import io.github.spitmaster.warlock.exceptions.WarlockException;
import io.github.spitmaster.warlock.util.JoinPointUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.redisson.Redisson;

/**
 * 默认的Wmutex工厂实现类
 *
 * @author zhouyijin
 */
public class DefaultWmutexFactory implements WmutexFactory {

    private final Redisson redisson;

    public DefaultWmutexFactory(Redisson redisson) {
        this.redisson = redisson;
    }

    @Override
    public Wmutex build(ProceedingJoinPoint pjp, Wsemaphore wsemaphore) {
        int permits = wsemaphore.permits();
        if (permits < 1) {
            //并发数小于1就没有意义了, 直接不对其进行处理
            return PlainWmutex.INSTANCE;
        }
        Scope scope = wsemaphore.scope();
        String semaphoreKey = wsemaphore.name() + JoinPointUtil.parseSpEL(pjp, wsemaphore.key());
        switch (scope) {
            case STANDALONE:
                //JVM单例使用的信号量
                return new StandaloneWmutex(semaphoreKey, wsemaphore);
            case DISTRIBUTED:
                //分布式信号量
                if (redisson == null) {
                    //如果项目没有使用Redisson,则不支持使用分布式锁
                    throw new WarlockException("Not supported lock scope: DISTRIBUTED ; please use redisson client to active this function; method: " + JoinPointUtil.methodName(pjp));
                }
                return new DistributedWmutex(semaphoreKey, wsemaphore, redisson);
            default:
                break;
        }
        throw new WarlockException("Wrong semaphore scope; scope=" + wsemaphore.scope());
    }

    /**
     * 不需要信号量处理的话, 使用这个单例
     */
    private enum PlainWmutex implements Wmutex {
        //单例
        INSTANCE;

        @Override
        public Object doBizWithSemaphore(ProceedingJoinPoint pjp, Wsemaphore wsemaphore) throws Throwable {
            //什么都不做的信号量处理
            return pjp.proceed();
        }
    }
}
