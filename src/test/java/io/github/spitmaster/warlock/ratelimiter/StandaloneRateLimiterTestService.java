package io.github.spitmaster.warlock.ratelimiter;

import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.annotation.WrateLimiter;
import io.github.spitmaster.warlock.enums.Scope;
import io.github.spitmaster.warlock.handler.WaitTimeoutHandler;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class StandaloneRateLimiterTestService implements WaitTimeoutHandler {

    private final AtomicInteger counter = new AtomicInteger();

    @WrateLimiter(name = "testRateLimiter",
            key = "#id",
            permitsPerSecond = 2, //每秒允许两个请求
            scope = Scope.STANDALONE,
            waiting = @Waiting(waitTime = 1, timeUnit = TimeUnit.SECONDS, waitTimeoutHandler = StandaloneRateLimiterTestService.class)
    )
    public void testRateLimiter(int id) {
        counter.incrementAndGet();
    }

    @Override
    public Object handleWaitTimeout(MethodInvocation methodInvocation) throws Throwable {
        System.out.println("wait timeout");
        return null;
    }

    public void init() {
        counter.set(0);
    }

    public AtomicInteger getCounter() {
        return counter;
    }
}
