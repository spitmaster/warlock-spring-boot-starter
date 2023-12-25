package io.github.spitmaster.warlock.core.factory.lock;

import com.google.common.base.Joiner;
import io.github.spitmaster.warlock.annotation.Leasing;
import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.annotation.Warlock;
import io.github.spitmaster.warlock.core.Waround;
import io.github.spitmaster.warlock.core.factory.RedissonProvider;
import io.github.spitmaster.warlock.core.factory.TimeoutHandlerProvider;
import io.github.spitmaster.warlock.core.factory.WaroundFactory;
import io.github.spitmaster.warlock.core.lock.LockInfo;
import io.github.spitmaster.warlock.core.lock.dist.DistributedReadWlock;
import io.github.spitmaster.warlock.core.lock.dist.DistributedReentrantWlock;
import io.github.spitmaster.warlock.core.lock.dist.DistributedWriteWlock;
import io.github.spitmaster.warlock.core.lock.standalone.ReadWlock;
import io.github.spitmaster.warlock.core.lock.standalone.ReentrantWlock;
import io.github.spitmaster.warlock.core.lock.standalone.WriteWlock;
import io.github.spitmaster.warlock.enums.Scope;
import io.github.spitmaster.warlock.exceptions.WarlockException;
import io.github.spitmaster.warlock.util.SpelExpressionUtil;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;

/**
 * WarlockFactory的简易实现
 *
 * @author zhouyijin
 */
public class DefaultWlockFactory implements WaroundFactory {

    private final RedissonProvider redissonProvider;
    private final TimeoutHandlerProvider timeoutHandlerProvider;

    public DefaultWlockFactory(RedissonProvider redissonProvider, TimeoutHandlerProvider timeoutHandlerProvider) {
        this.redissonProvider = redissonProvider;
        this.timeoutHandlerProvider = timeoutHandlerProvider;
    }

    @Override
    public Waround build(MethodInvocation methodInvocation) {
        //根据锁的范围选择合适的锁factory
        Method method = methodInvocation.getMethod();
        Warlock warlock = AnnotatedElementUtils.findMergedAnnotation(method, Warlock.class);
        if (warlock == null) {
            throw new WarlockException("invoke warlock interceptor on non warlock method, method = " + method.getName());
        }
        Scope scope = warlock.lockScope();
        switch (scope) {
            case STANDALONE:
                //单机锁
                return this.buildStandaloneLock(this.buildLockInfo(methodInvocation));
            case DISTRIBUTED:
                if (redissonProvider == null || redissonProvider.getRedisson() == null) {
                    //如果项目没有使用Redisson,则不支持使用分布式锁
                    throw new WarlockException("Not supported lock scope: DISTRIBUTED ; please use redisson to active this function; method: " + method.getName());
                }
                //分布式锁
                return this.buildDistributedLock(this.buildLockInfo(methodInvocation));
            default:
                break;
        }
        //没有选择合适的锁范围, 抛异常,  代码应该跑不到这里
        throw new WarlockException("There is no suitable Warlock for this method: " + method);
    }

    private Waround buildStandaloneLock(LockInfo lockInfo) {
        //2. 根据锁类型选择合适的锁
        //According lock type decide what wlock should be used
        switch (lockInfo.getLockType()) {
            case REENTRANT:
                return new ReentrantWlock(lockInfo);
            case READ:
                return new ReadWlock(lockInfo);
            case WRITE:
                return new WriteWlock(lockInfo);
            default:
        }
        throw new WarlockException("Unsupported lock type; type = " + lockInfo.getLockType());
    }

    private Waround buildDistributedLock(LockInfo lockInfo) {
        //2. 根据锁类型选择合适的锁
        //According lock type decide what wlock should be used
        switch (lockInfo.getLockType()) {
            case REENTRANT:
                return new DistributedReentrantWlock(redissonProvider.getRedisson(), lockInfo);
            case READ:
                return new DistributedReadWlock(redissonProvider.getRedisson(), lockInfo);
            case WRITE:
                return new DistributedWriteWlock(redissonProvider.getRedisson(), lockInfo);
            default:
                throw new WarlockException("Unsupported lock type; type = " + lockInfo.getLockType());
        }
    }

    /**
     * 构建锁的信息
     *
     * @param methodInvocation 切点
     * @return 通过切点和 注解 综合得到的锁在使用中锁需要的信息
     */
    protected LockInfo buildLockInfo(MethodInvocation methodInvocation) {
        Method method = methodInvocation.getMethod();
        Warlock warlock = AnnotatedElementUtils.findMergedAnnotation(method, Warlock.class);
        LockInfo lockInfo = new LockInfo();
        //1. 构造lockKey
        //收集锁的信息

        /*
         * construct a lockkey that indicate a unique lock
         * this lock would be used in Warlock.beforeBiz and Warlock.afterBiz and Warlock.except
         */
        String lockKey = Joiner.on(':').skipNulls().join(Arrays.asList("warlock", warlock.name(), SpelExpressionUtil.parseSpel(method, methodInvocation.getArguments(), warlock.key(), String.class)));
        lockInfo.setLockKey(lockKey);
        //2. 拿到lockType
        lockInfo.setLockType(warlock.lockType());
        lockInfo.setLockScope(warlock.lockScope());
        //3. 获取等待时间
        Waiting waiting = warlock.waiting();
        Duration waitTime = Duration.of(waiting.waitTime(), waiting.timeUnit());
        if (waitTime.isNegative() || waitTime.isZero()) {
            throw new WarlockException("WaitTime cannot Less than or equal to 0; method = " + method.getName());
        }
        lockInfo.setWaitTime(waitTime);
        lockInfo.setWaitTimeoutHandler(timeoutHandlerProvider.getWaitTimeoutHandler(waiting));
        //4. 获取等待时间
        Leasing leasing = warlock.leasing();
        Duration leaseTime = Duration.of(leasing.leaseTime(), leasing.timeUnit());
        if (leaseTime.isNegative() || leaseTime.isZero()) {
            throw new WarlockException("LeaseTime cannot Less than or equal to 0; method = " + method.getName());
        }
        lockInfo.setLeaseTime(leaseTime);
        lockInfo.setLockLeaseTimeoutHandler(timeoutHandlerProvider.getLeaseTimeoutHandler(leasing));
        //5. 返回锁信息
        return lockInfo;
    }
}