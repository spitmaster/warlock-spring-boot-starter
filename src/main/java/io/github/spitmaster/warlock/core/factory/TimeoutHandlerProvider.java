package io.github.spitmaster.warlock.core.factory;

import io.github.spitmaster.warlock.annotation.Leasing;
import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.handler.FastFailLeaseTimeoutHandler;
import io.github.spitmaster.warlock.handler.FastFailWaitTimeoutHandler;
import io.github.spitmaster.warlock.handler.LeaseTimeoutHandler;
import io.github.spitmaster.warlock.handler.WaitTimeoutHandler;

import java.util.List;

public class TimeoutHandlerProvider {

    private final List<WaitTimeoutHandler> waitTimeoutHandlerList;
    private final List<LeaseTimeoutHandler> leaseTimeoutHandlerList;

    public TimeoutHandlerProvider(List<WaitTimeoutHandler> waitTimeoutHandlerList, List<LeaseTimeoutHandler> leaseTimeoutHandlerList) {
        this.waitTimeoutHandlerList = waitTimeoutHandlerList;
        this.leaseTimeoutHandlerList = leaseTimeoutHandlerList;
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
            if (waitTimeoutHandlerList != null) {
                for (WaitTimeoutHandler waitTimeoutHandler : waitTimeoutHandlerList) {
                    if (waitTimeoutHandlerClass.isInstance(waitTimeoutHandler)) {
                        return waitTimeoutHandler;
                    }
                }
            }
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
            if (leaseTimeoutHandlerList != null) {
                for (LeaseTimeoutHandler leaseTimeoutHandler : leaseTimeoutHandlerList) {
                    if (leaseTimeoutHandlerClass.isInstance(leaseTimeoutHandler)) {
                        return leaseTimeoutHandler;
                    }
                }
            }
        }
        return FastFailLeaseTimeoutHandler.INSTANCE;
    }
}
