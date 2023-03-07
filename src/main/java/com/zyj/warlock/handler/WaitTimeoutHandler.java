package com.zyj.warlock.handler;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 当
 */
public interface WaitTimeoutHandler {

    /**
     * @param pjp
     * @return
     */
    default Object handle(ProceedingJoinPoint pjp) {
        // TODO: 2023/3/7
      throw new RuntimeException("");
    }

}
