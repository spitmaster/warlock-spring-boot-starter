package io.github.spitmaster.warlock.exceptions;

/**
 * Warlock 异常类
 *
 * @author zhouyijin
 */
public class WarlockException extends RuntimeException {

    public WarlockException(String message) {
        super(message);
    }

    public WarlockException(Throwable throwable) {
        super(throwable);
    }

    public WarlockException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
