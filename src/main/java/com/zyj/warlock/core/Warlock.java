package com.zyj.warlock.core;

/**
 * 构造一个Warlock
 * 用于在业务方法上下文中, 根据需求, 进行锁的操作
 */
public interface Warlock {

    /**
     * 在执行业务函数前所做的一些锁操作
     */
    void beforeBiz();

    /**
     * 在执行业务函数后所做的一些锁操作
     */
    void afterBiz();

    /**
     * 当业务函数报错之后, 所做的一些锁操作
     *
     * @param e 抛出的异常
     */
    void except(Exception e);
}
