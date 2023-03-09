package io.github.spitmaster.warlock.core.factory.lock;

import io.github.spitmaster.warlock.annotation.Leasing;
import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.annotation.Warlock;
import io.github.spitmaster.warlock.core.factory.AbstractFactory;
import io.github.spitmaster.warlock.core.lock.LockInfo;
import io.github.spitmaster.warlock.exceptions.WarlockException;
import io.github.spitmaster.warlock.util.JoinPointUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.BeanFactory;

import java.time.Duration;

/**
 * 一些公用方法的抽象类
 *
 * @author zhouyijin
 */
abstract class AbstractWarlockFactory extends AbstractFactory {

    public AbstractWarlockFactory(BeanFactory beanFactory) {
        super(beanFactory);
    }

    /**
     * 构建锁的信息
     *
     * @param pjp     切点
     * @param warlock Warlock注解, 元信息
     * @return 通过切点和 注解 综合得到的锁在使用中锁需要的信息
     */
    protected LockInfo buildLockInfo(ProceedingJoinPoint pjp, Warlock warlock) {
        LockInfo lockInfo = new LockInfo();
        //1. 构造lockKey
        //收集锁的信息

        /*
         * construct a lockkey that indicate a unique lock
         * this lock would be used in Warlock.beforeBiz and Warlock.afterBiz and Warlock.except
         */
        String lockKey = "warlock:" + warlock.name() + JoinPointUtil.parseSpEL(pjp, warlock.key());

        lockInfo.setLockKey(lockKey);
        //2. 拿到lockType
        lockInfo.setLockType(warlock.lockType());
        //3. 获取等待时间
        Waiting waiting = warlock.waiting();
        Duration waitTime = Duration.of(waiting.waitTime(), waiting.timeUnit().toChronoUnit());
        if (waitTime.isNegative() || waitTime.isZero()) {
            throw new WarlockException("WaitTime cannot Less than or equal to 0; method = " + JoinPointUtil.methodName(pjp));
        }
        lockInfo.setWaitTime(waitTime);
        lockInfo.setWaitTimeoutHandler(getWaitTimeoutHandler(waiting));
        //4. 获取等待时间
        Leasing leasing = warlock.leasing();
        Duration leaseTime = Duration.of(leasing.leaseTime(), leasing.timeUnit().toChronoUnit());
        if (leaseTime.isNegative() || leaseTime.isZero()) {
            throw new WarlockException("LeaseTime cannot Less than or equal to 0; method = " + JoinPointUtil.methodName(pjp));
        }
        lockInfo.setLeaseTime(leaseTime);
        lockInfo.setLockLeaseTimeoutHandler(getLeaseTimeoutHandler(leasing));
        //5. 返回锁信息
        return lockInfo;
    }

}
