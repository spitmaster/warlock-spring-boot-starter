package io.github.spitmaster.warlock.semaphore;

import io.github.spitmaster.warlock.annotation.Leasing;
import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.annotation.Wsemaphore;
import io.github.spitmaster.warlock.enums.Scope;
import io.github.spitmaster.warlock.handler.lock.LeaseTimeoutHandler;
import io.github.spitmaster.warlock.handler.lock.WaitTimeoutHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SemaphoreAspectTestService implements WaitTimeoutHandler, LeaseTimeoutHandler {

    private long counter = 0;

    @Wsemaphore(name = "mys1", permits = 1, scope = Scope.DISTRIBUTED)
    public void testWsemaphore(int id) {
        for (int i = 0; i < id * 1000; i++) {
            counter++;
        }
    }

    @Wsemaphore(name = "mys2", permits = 1, scope = Scope.DISTRIBUTED,
            waiting = @Waiting(waitTime = 10, timeUnit = TimeUnit.MINUTES, waitTimeoutHandler = SemaphoreAspectTestService.class),
            leasing = @Leasing(leaseTime = 1, timeUnit = TimeUnit.SECONDS, leaseTimeoutHandler = SemaphoreAspectTestService.class)
    )
    public void testWsemaphore2(int id) {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < id * 1000; i++) {
            counter++;
        }
    }

    public long getCounter() {
        return counter;
    }

    @Override
    public Object handleLeaseTimeout(ProceedingJoinPoint pjp, Object result) throws Throwable {
        System.out.println("aaaaaaaaaa lease timeout");
        return result;
    }

    @Override
    public Object handleWaitTimeout(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("wait timeout");
        return null;
    }
}
