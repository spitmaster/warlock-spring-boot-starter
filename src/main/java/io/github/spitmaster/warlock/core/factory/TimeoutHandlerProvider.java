package io.github.spitmaster.warlock.core.factory;

import io.github.spitmaster.warlock.annotation.Leasing;
import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.handler.FastFailTimeoutHandler;
import io.github.spitmaster.warlock.handler.IgnoreTimeoutHandler;
import io.github.spitmaster.warlock.handler.LeaseTimeoutHandler;
import io.github.spitmaster.warlock.handler.WaitTimeoutHandler;
import org.springframework.beans.factory.BeanFactory;

/**
 * 在切面等待超时, 或者运行超时的时候, 找到应该对应的handler处理超时逻辑
 *
 * @see FastFailTimeoutHandler 默认的handler, 一切找不到handler的情况都使用它
 * @see IgnoreTimeoutHandler 框架提供的另外一个handler, 忽略超时问题
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
     * @return Spring环境中的handler, 处理这个注解切面超时的handler
     */
    public WaitTimeoutHandler getWaitTimeoutHandler(Waiting waiting) {
        Class<? extends WaitTimeoutHandler> waitTimeoutHandlerClass = waiting.waitTimeoutHandler();
        if (waitTimeoutHandlerClass == null) {
            return FastFailTimeoutHandler.INSTANCE;
        } else if (waitTimeoutHandlerClass == FastFailTimeoutHandler.class) {
            return FastFailTimeoutHandler.INSTANCE;
        } else if (waitTimeoutHandlerClass == IgnoreTimeoutHandler.class) {
            return IgnoreTimeoutHandler.INSTANCE;
        }
        return beanFactory.getBean(waitTimeoutHandlerClass);
    }

    /**
     * 根据注解获取处理业务方法超时的handler
     *
     * @param leasing 切面上的注解
     * @return Spring环境中的handler, 处理执行超时
     */
    public LeaseTimeoutHandler getLeaseTimeoutHandler(Leasing leasing) {
        Class<? extends LeaseTimeoutHandler> leaseTimeoutHandlerClass = leasing.leaseTimeoutHandler();
        if (leaseTimeoutHandlerClass == null) {
            return FastFailTimeoutHandler.INSTANCE;
        } else if (leaseTimeoutHandlerClass == FastFailTimeoutHandler.class) {
            return FastFailTimeoutHandler.INSTANCE;
        } else if (leaseTimeoutHandlerClass == IgnoreTimeoutHandler.class) {
            return IgnoreTimeoutHandler.INSTANCE;
        }
        return beanFactory.getBean(leaseTimeoutHandlerClass);
    }
}
