package com.zyj.warlock.enums;

/**
 * 支持的锁类型
 *
 * @author zhouyijin
 */
public enum LockType {
    /**
     * 可重入锁
     */
    REENTRANT,
    /**
     * 读锁
     */
    READ,
    /**
     * 写锁
     */
    WRITE;
}
