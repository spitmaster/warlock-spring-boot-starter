package io.github.spitmaster.warlock.core.factory.lock;

import io.github.spitmaster.warlock.core.Waround;
import io.github.spitmaster.warlock.core.factory.WaroundFactory;
import io.github.spitmaster.warlock.core.lock.LockInfo;
import io.github.spitmaster.warlock.core.lock.dist.DistributedReadWlock;
import io.github.spitmaster.warlock.core.lock.dist.DistributedReentrantWlock;
import io.github.spitmaster.warlock.core.lock.dist.DistributedWriteWlock;
import io.github.spitmaster.warlock.exceptions.WarlockException;
import org.aopalliance.intercept.MethodInvocation;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.Method;

/**
 * 生成分布式Warlock
 * 依赖于Redisson
 *
 * @author zhouyijin
 */
public class DistributedWlockFactory extends AbstractWarlockFactory implements WaroundFactory, InitializingBean {

    private RedissonClient redissonClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.redissonClient = beanFactory.getBeanProvider(RedissonClient.class).getIfAvailable();
    }

    @Override
    public Waround build(MethodInvocation methodInvocation) {
        Method method = methodInvocation.getMethod();
        if (redissonClient == null) {
            //如果项目没有使用Redisson,则不支持使用分布式锁
            throw new WarlockException("Not supported lock scope: DISTRIBUTED ; please use redisson client to active this function; method: " + method.getName());
        }
        //1. 构造锁信息
        LockInfo lockInfo = buildLockInfo(methodInvocation);
        //2. 根据锁类型选择合适的锁
        //According lock type decide what wlock should be used
        switch (lockInfo.getLockType()) {
            case REENTRANT:
                return new DistributedReentrantWlock(redissonClient, lockInfo);
            case READ:
                return new DistributedReadWlock(redissonClient, lockInfo);
            case WRITE:
                return new DistributedWriteWlock(redissonClient, lockInfo);
            default:
                throw new WarlockException("Unsupported lock type; type = " + lockInfo.getLockType());
        }
    }

}