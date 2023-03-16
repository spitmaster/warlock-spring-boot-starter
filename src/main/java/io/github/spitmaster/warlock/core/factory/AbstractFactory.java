package io.github.spitmaster.warlock.core.factory;

import io.github.spitmaster.warlock.annotation.Leasing;
import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.handler.LeaseTimeoutHandler;
import io.github.spitmaster.warlock.handler.PlainLockLeaseTimeoutHandler;
import io.github.spitmaster.warlock.handler.PlainLockWaitTimeoutHandler;
import io.github.spitmaster.warlock.handler.WaitTimeoutHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ObjectProvider;

/**
 * 一些公用方法的抽象类
 *
 * @author zhouyijin
 */
public abstract class AbstractFactory implements BeanFactoryAware {

    protected BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * 根据注解获取处理等待超时的handler
     *
     * @param waiting 切面上的注解
     * @return Spring环境中的handler
     */
    protected WaitTimeoutHandler getWaitTimeoutHandler(Waiting waiting) {
        Class<? extends WaitTimeoutHandler> waitTimeoutHandlerClass = waiting.waitTimeoutHandler();
        if (waitTimeoutHandlerClass != null && waitTimeoutHandlerClass != PlainLockWaitTimeoutHandler.class) {
            ObjectProvider<? extends WaitTimeoutHandler> beanProvider = beanFactory.getBeanProvider(waitTimeoutHandlerClass);
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
            ObjectProvider<? extends LeaseTimeoutHandler> beanProvider = beanFactory.getBeanProvider(leaseTimeoutHandlerClass);
            LeaseTimeoutHandler handler = beanProvider.getIfAvailable();
            if (handler != null) {
                return handler;
            }
        }
        return PlainLockLeaseTimeoutHandler.INSTANCE;
    }
}
