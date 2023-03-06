package com.zyj.warlock.core;

/**
 * 构造一个Warlock
 * 用于在业务方法上下文中, 根据需求, 进行锁的操作
 */
public interface Warlock {

    /**
     * 在锁的环境下执行业务函数
     *
     * @param bizFunc 业务函数
     * @return 业务函数的返回值
     * @throws Throwable 业务方法可能抛出的异常
     */
    Object doWithLock(BizFunction bizFunc) throws Throwable;


    interface BizFunction {
        /**
         * 被锁保卫的业务方法
         *
         * @return 业务方法的返回值
         * @throws Throwable 业务方法可能抛出的异常
         */
        Object doBiz() throws Throwable;
    }
}
