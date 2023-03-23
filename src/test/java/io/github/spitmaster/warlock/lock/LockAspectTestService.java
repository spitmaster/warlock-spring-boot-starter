package io.github.spitmaster.warlock.lock;

import io.github.spitmaster.warlock.annotation.Leasing;
import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.annotation.Warlock;
import io.github.spitmaster.warlock.enums.Scope;
import io.github.spitmaster.warlock.handler.LeaseTimeoutHandler;
import io.github.spitmaster.warlock.handler.WaitTimeoutHandler;
import org.aopalliance.intercept.MethodInvocation;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Service
public class LockAspectTestService implements WaitTimeoutHandler, LeaseTimeoutHandler {

    int counter = 0;

    @Warlock(name = "test1", key = "#id")
    public void testWarlock(int id) {
        for (int i = 0; i < id; i++) {
            Blackhole.consumeCPU(i);//防止编译优化
        }
    }

    @PlainAspect.WBenchmark
    public void testPlainAspect(int id) {
        for (int i = 0; i < id; i++) {
            Blackhole.consumeCPU(i);//防止编译优化
        }
    }

    public void testPlain(int id) {
        for (int i = 0; i < id; i++) {
            Blackhole.consumeCPU(i);//防止编译优化
        }
    }

    @Warlock(name = "add100")
    public void add100() {
        for (int i = 0; i < 100; i++) {
            counter++;
        }
    }

    private int counter0 = 0;
    private int counter1 = 0;

    @Warlock(name = "add100_2", key = "#id")
    public void add100_2(int id) {
        for (int i = 0; i < 100; i++) {
            int counterNum = id % 2;
            if (counterNum == 0) {
                counter0++;
            } else if (counterNum == 1) {
                counter1++;
            }

        }
    }

    @Warlock(name = "add100Distributed", lockScope = Scope.DISTRIBUTED)
    public void add100Distributed() {
        for (int i = 0; i < 100; i++) {
            counter++;
        }
    }

    @Warlock(name = "add100Distributed2",
            key = "#id",
            lockScope = Scope.DISTRIBUTED)
    public void add100Distributed2(int id) {
        for (int i = 0; i < 100; i++) {
            int counterNum = id % 2;
            if (counterNum == 0) {
                counter0++;
            } else if (counterNum == 1) {
                counter1++;
            }
        }
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public int getCounter0() {
        return counter0;
    }

    public void setCounter0(int counter0) {
        this.counter0 = counter0;
    }

    public int getCounter1() {
        return counter1;
    }

    public void setCounter1(int counter1) {
        this.counter1 = counter1;
    }

    @Warlock(name = "waitTimeout",
            lockScope = Scope.DISTRIBUTED,
            waiting = @Waiting(waitTime = 1, timeUnit = ChronoUnit.SECONDS, waitTimeoutHandler = LockAspectTestService.class)
    )
    public void waitTimeout(int id) throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
        for (int i = 0; i < id; i++) {
            counter++;
        }
    }

    @Warlock(name = "leaseTimeout",
            lockScope = Scope.DISTRIBUTED,
            waiting = @Waiting(waitTime = 1, timeUnit = ChronoUnit.SECONDS, waitTimeoutHandler = LockAspectTestService.class),
            leasing = @Leasing(leaseTime = 1, timeUnit = ChronoUnit.SECONDS, leaseTimeoutHandler = LockAspectTestService.class)
    )
    public void leaseTimeout(int id) throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
        for (int i = 0; i < id; i++) {
            counter++;
        }
    }

    @Override
    public Object handleLeaseTimeout(MethodInvocation pjp, Object result) throws Throwable {
        System.out.println("aaaaaaaa lease timeout");
        return result;
    }

    @Override
    public Object handleWaitTimeout(MethodInvocation pjp) throws Throwable {
        System.out.println("wait timeout");
        return null;
    }
}
