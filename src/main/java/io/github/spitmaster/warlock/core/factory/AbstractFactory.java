package io.github.spitmaster.warlock.core.factory;

import io.github.spitmaster.warlock.annotation.Leasing;
import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.handler.lock.LeaseTimeoutHandler;
import io.github.spitmaster.warlock.handler.lock.PlainLockLeaseTimeoutHandler;
import io.github.spitmaster.warlock.handler.lock.PlainLockWaitTimeoutHandler;
import io.github.spitmaster.warlock.handler.lock.WaitTimeoutHandler;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectProvider;

/**
 * 一些公用方法的抽象类
 *
 * @author zhouyijin
 */
public abstract class AbstractFactory {

    /**
     * 子类实现获取BeanFactory
     *
     * @return spring的BeanFactory
     */
    protected abstract BeanFactory getBeanFactory();

    /**
     * 根据注解获取处理等待超时的handler
     *
     * @param waiting 切面上的注解
     * @return Spring环境中的handler
     */
    protected WaitTimeoutHandler getWaitTimeoutHandler(Waiting waiting) {
        Class<? extends WaitTimeoutHandler> waitTimeoutHandlerClass = waiting.waitTimeoutHandler();
        if (waitTimeoutHandlerClass != null && waitTimeoutHandlerClass != PlainLockWaitTimeoutHandler.class) {
            ObjectProvider<? extends WaitTimeoutHandler> beanProvider = getBeanFactory().getBeanProvider(waitTimeoutHandlerClass);
            WaitTimeoutHandler handler = beanProvider.getIfAvailable();
            if (handler != null) {
                return handler;
            }
        }
        return PlainLockWaitTimeoutHandler.INSTANCE;
    }

    /**
     * 根据注解获取处理业务方法超时的handler
     *
     * @param leasing 切面上的注解
     * @return Spring环境中的handler
     */
    protected LeaseTimeoutHandler getLeaseTimeoutHandler(Leasing leasing) {
        Class<? extends LeaseTimeoutHandler> leaseTimeoutHandlerClass = leasing.leaseTimeoutHandler();
        if (leaseTimeoutHandlerClass != null && leaseTimeoutHandlerClass != PlainLockLeaseTimeoutHandler.class) {
            ObjectProvider<? extends LeaseTimeoutHandler> beanProvider = getBeanFactory().getBeanProvider(leaseTimeoutHandlerClass);
            LeaseTimeoutHandler handler = beanProvider.getIfAvailable();
            if (handler != null) {
                return handler;
            }
        }
        return PlainLockLeaseTimeoutHandler.INSTANCE;
    }
}
