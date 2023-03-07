package com.zyj.warlock.core.factory;

import com.zyj.warlock.annotation.Warlock;
import com.zyj.warlock.core.Wlock;
import com.zyj.warlock.enums.LockScope;
import com.zyj.warlock.exceptions.WarlockException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * WarlockFactory的简易实现
 *
 * @author zhouyijin
 */
public class DefaultWlockFactory implements WlockFactory {

    private StandaloneWlockFactory standaloneWlockFactory;
    private DistributedWlockFactory distributedWlockFactory;

    @Override
    public Wlock build(ProceedingJoinPoint pjp, Warlock warlock) {
        LockScope lockScope = warlock.lockScope();
        switch (lockScope) {
            case STANDALONE:
                return standaloneWlockFactory.build(pjp, warlock);
            case DISTRIBUTED:
                return distributedWlockFactory.build(pjp, warlock);
            default:
                break;
        }
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        throw new WarlockException("There is no suitable Warlock for this method: " + method.getName());
    }
}