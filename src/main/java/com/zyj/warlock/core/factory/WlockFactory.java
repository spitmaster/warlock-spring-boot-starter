package com.zyj.warlock.core.factory;

import com.zyj.warlock.annotation.Warlock;
import com.zyj.warlock.core.Wlock;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 构造Wlock实例的工厂
 * 这里提供一个默认实现 DefaultWlockFactory,
 * 如果不服气, 你可以自己实现一个替代我的 DefaultWlockFactory
 *
 * @author zhouyijin
 */
public interface WlockFactory {

    /**
     * 构造一个Warlock对象
     *
     * @param pjp     切点
     * @param warlock 锁的元信息
     * @return 构造好的warlock
     */
    Wlock build(ProceedingJoinPoint pjp, Warlock warlock);

}
