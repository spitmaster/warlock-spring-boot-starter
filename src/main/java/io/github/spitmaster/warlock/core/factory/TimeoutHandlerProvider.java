package io.github.spitmaster.warlock.core.factory;

import io.github.spitmaster.warlock.annotation.Leasing;
import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.handler.FastFailLeaseTimeoutHandler;
import io.github.spitmaster.warlock.handler.FastFailWaitTimeoutHandler;
import io.github.spitmaster.warlock.handler.LeaseTimeoutHandler;
import io.github.spitmaster.warlock.handler.WaitTimeoutHandler;
import org.springframework.beans.factory.BeanFactory;

/**
 * 在切面等待超时, 或者运行超时的时候, 找到应该对应的handler处理超时逻辑
 */
public class TimeoutHandlerProvider {

    private final BeanFactory beanFactory;

    public TimeoutHandlerProvider(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 根据注解获取处理等待超时的handler
     *
     * @param waiting 切面上的注解
     * @return 处理这个注解切面超时的handler
     */
    public WaitTimeoutHandler getWaitTimeoutHandler(Waiting waiting) {
        Class<? extends WaitTimeoutHandler> waitTimeoutHandlerClass = waiting.waitTimeoutHandler();
        if (waitTimeoutHandlerClass != null && waitTimeoutHandlerClass != FastFailWaitTimeoutHandler.class) {
            return beanFactory.getBean(waitTimeoutHandlerClass);
        }
        return FastFailWaitTimeoutHandler.INSTANCE;
    }

    /**
     * 根据注解获取处理业务方法超时的handler
     *
     * @param leasing 切面上的注解
     * @return Spring环境中的handler
     */
    public LeaseTimeoutHandler getLeaseTimeoutHandler(Leasing leasing) {
        Class<? extends LeaseTimeoutHandler> leaseTimeoutHandlerClass = leasing.leaseTimeoutHandler();
        if (leaseTimeoutHandlerClass != null && leaseTimeoutHandlerClass != FastFailLeaseTimeoutHandler.class) {
            return beanFactory.getBean(leaseTimeoutHandlerClass);
        }
        return FastFailLeaseTimeoutHandler.INSTANCE;
    }
}
