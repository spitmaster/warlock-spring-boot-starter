package io.github.spitmaster.warlock.core.factory.lock;

import com.google.common.base.Joiner;
import io.github.spitmaster.warlock.annotation.Leasing;
import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.annotation.Warlock;
import io.github.spitmaster.warlock.core.factory.TimeoutHandlerProvider;
import io.github.spitmaster.warlock.core.lock.LockInfo;
import io.github.spitmaster.warlock.exceptions.WarlockException;
import io.github.spitmaster.warlock.util.SpelExpressionUtil;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;

/**
 * 一些公用方法的抽象类
 *
 * @author zhouyijin
 */
abstract class AbstractWarlockFactory {

    private final TimeoutHandlerProvider timeoutHandlerProvider;

    public AbstractWarlockFactory(TimeoutHandlerProvider timeoutHandlerProvider) {
        this.timeoutHandlerProvider = timeoutHandlerProvider;
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
