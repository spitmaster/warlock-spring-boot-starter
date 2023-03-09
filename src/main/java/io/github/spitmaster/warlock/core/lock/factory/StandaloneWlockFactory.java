package io.github.spitmaster.warlock.core.lock.factory;

import io.github.spitmaster.warlock.annotation.Warlock;
import io.github.spitmaster.warlock.core.lock.LockInfo;
import io.github.spitmaster.warlock.core.lock.Wlock;
import io.github.spitmaster.warlock.core.lock.standalone.ReadWlock;
import io.github.spitmaster.warlock.core.lock.standalone.ReentrantWlock;
import io.github.spitmaster.warlock.core.lock.standalone.WriteWlock;
import io.github.spitmaster.warlock.exceptions.WarlockException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.BeanFactory;

/**
 * 生产单机使用的Warlock的Factory实现
 *
 * @author zhouyijin
 */
public class StandaloneWlockFactory extends AbstractWarlockFactory implements WlockFactory {

    private final BeanFactory beanFactory;

    public StandaloneWlockFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public Wlock build(ProceedingJoinPoint pjp, Warlock warlock) {
        //1. 构造锁
        LockInfo lockInfo = buildLock(pjp, warlock);

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

    @Override
    protected BeanFactory getBeanFactory() {
        return beanFactory;
    }

}