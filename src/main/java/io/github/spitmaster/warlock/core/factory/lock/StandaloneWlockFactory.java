package io.github.spitmaster.warlock.core.factory.lock;

import io.github.spitmaster.warlock.core.Waround;
import io.github.spitmaster.warlock.core.factory.WaroundFactory;
import io.github.spitmaster.warlock.core.lock.LockInfo;
import io.github.spitmaster.warlock.core.lock.standalone.ReadWlock;
import io.github.spitmaster.warlock.core.lock.standalone.ReentrantWlock;
import io.github.spitmaster.warlock.core.lock.standalone.WriteWlock;
import io.github.spitmaster.warlock.exceptions.WarlockException;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 生产单机使用的Warlock的Factory实现
 *
 * @author zhouyijin
 */
public class StandaloneWlockFactory extends AbstractWarlockFactory implements WaroundFactory {

    @Override
    public Waround build(MethodInvocation methodInvocation) {
        //1. 构造锁
        LockInfo lockInfo = buildLockInfo(methodInvocation);

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

}