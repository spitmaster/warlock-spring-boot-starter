package com.zyj.warlock.core.factory;

import com.zyj.warlock.annotation.Leasing;
import com.zyj.warlock.annotation.Waiting;
import com.zyj.warlock.annotation.Warlock;
import com.zyj.warlock.core.LockInfo;
import com.zyj.warlock.core.Wlock;
import com.zyj.warlock.core.lock.PlainWarlock;
import com.zyj.warlock.core.lock.standalone.ReadWarlock;
import com.zyj.warlock.core.lock.standalone.ReentrantWarlock;
import com.zyj.warlock.core.lock.standalone.WriteWarlock;
import com.zyj.warlock.util.SpelExpressionUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.Redisson;
import org.springframework.beans.factory.BeanFactory;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * 生成分布式Warlock
 * 依赖于Redisson
 *
 * @author zhouyijin
 */
public class DistributedWlockFactory extends AbstractWarlockFactory implements WlockFactory {

    private BeanFactory beanFactory;
    private Redisson redisson;

    public DistributedWlockFactory(BeanFactory beanFactory, Redisson redisson) {
        this.beanFactory = beanFactory;
        this.redisson = redisson;
    }

    @Override
    public Wlock build(ProceedingJoinPoint pjp, Warlock warlock) {
        //1. 构造锁
        LockInfo lockInfo = buildLock(pjp, warlock);

        // TODO: 2023/3/7
        return null;
    }

    

    @Override
    protected BeanFactory getBeanFactory() {
        return this.beanFactory;
    }
}