package com.zyj.warlock.core;

import com.zyj.warlock.annotation.Wlock;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 构造Warlock实例的工厂
 * 这里提供一个默认实现, 如果不服气, 你可以自己实现一个
 *
 * @author zhouyijin
 */
public interface WarlockFactory {

    /**
     * 构造一个Warlock对象
     *
     * @param pjp   切点
     * @param wlock 锁的元信息
     * @return 构造好的warlock
     */
    Warlock build(ProceedingJoinPoint pjp, Wlock wlock);

}
