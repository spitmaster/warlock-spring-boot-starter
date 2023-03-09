package io.github.spitmaster.warlock.core.factory.lock;

import io.github.spitmaster.warlock.annotation.Warlock;
import io.github.spitmaster.warlock.core.lock.Wlock;
import io.github.spitmaster.warlock.enums.Scope;
import io.github.spitmaster.warlock.exceptions.WarlockException;
import io.github.spitmaster.warlock.util.JoinPointUtil;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * WarlockFactory的简易实现
 *
 * @author zhouyijin
 */
public class DefaultWlockFactory implements WlockFactory {

    private final StandaloneWlockFactory standaloneWlockFactory;
    private final DistributedWlockFactory distributedWlockFactory;

    public DefaultWlockFactory(StandaloneWlockFactory standaloneWlockFactory, DistributedWlockFactory distributedWlockFactory) {
        this.standaloneWlockFactory = standaloneWlockFactory;
        this.distributedWlockFactory = distributedWlockFactory;
    }

    @Override
    public Wlock build(ProceedingJoinPoint pjp, Warlock warlock) {
        //根据锁的范围选择合适的锁factory
        Scope scope = warlock.lockScope();
        switch (scope) {
            case STANDALONE:
                //单机锁
                return standaloneWlockFactory.build(pjp, warlock);
            case DISTRIBUTED:
                //分布式锁
                if (distributedWlockFactory == null) {
                    //如果项目没有使用Redisson,则不支持使用分布式锁
                    throw new WarlockException("Not supported lock scope: DISTRIBUTED ; please use redisson client to active this function; method: " + JoinPointUtil.methodName(pjp));
                }
                return distributedWlockFactory.build(pjp, warlock);
            default:
                break;
        }
        //没有选择合适的锁范围, 抛异常,  代码应该跑不到这里
        throw new WarlockException("There is no suitable Warlock for this method: " + JoinPointUtil.methodName(pjp));
    }
}