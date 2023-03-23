package io.github.spitmaster.warlock.semaphore;

import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.annotation.Wsemaphore;
import io.github.spitmaster.warlock.enums.Scope;
import io.github.spitmaster.warlock.handler.LeaseTimeoutHandler;
import io.github.spitmaster.warlock.handler.WaitTimeoutHandler;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SemaphoreAspectTestService implements WaitTimeoutHandler, LeaseTimeoutHandler {

    private AtomicInteger counter = new AtomicInteger();

    @Wsemaphore(name = "mys1",
            key = "#id",
            permits = 7,
            waiting = @Waiting(waitTime = 1, timeUnit = ChronoUnit.SECONDS, waitTimeoutHandler = SemaphoreAspectTestService.class),
            scope = Scope.STANDALONE)
    public void testWsemaphore(String id) {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        counter.incrementAndGet();
    }

    @Wsemaphore(name = "testWsemaphore2",
            key = "#id",
            permits = 3,
            scope = Scope.DISTRIBUTED,
            waiting = @Waiting(waitTime = 1, timeUnit = ChronoUnit.SECONDS, waitTimeoutHandler = SemaphoreAspectTestService.class)
    )
    public void testWsemaphore2(String id) {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        counter.incrementAndGet();
    }

    public int getCounter() {
        return counter.get();
    }

    @Override
    public Object handleLeaseTimeout(MethodInvocation pjp, Object result) throws Throwable {
        System.out.println("aaaaaaaaaa lease timeout");
        return result;
    }

    @Override
    public Object handleWaitTimeout(MethodInvocation pjp) throws Throwable {
        System.out.println("wait timeout");
        return null;
    }


    public void init() {
        this.counter.set(0);
    }
}
