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

import java.util.concurrent.TimeUnit;

@Service
public class LockAspectTestService implements WaitTimeoutHandler, LeaseTimeoutHandler {

    private int counter = 0;

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

    @Warlock(name = "test1")
    public void add10() {
        for (int i = 0; i < 100; i++) {
            counter++;
        }
    }

    public int getCounter(){
        return counter;
    }



    @Warlock(name = "test1-dist",
//            key = "#id",
            lockScope = Scope.DISTRIBUTED,
            waiting = @Waiting(waitTime = 1, timeUnit = TimeUnit.SECONDS, waitTimeoutHandler = LockAspectTestService.class),
            leasing = @Leasing(leaseTime = 1, timeUnit = TimeUnit.SECONDS, leaseTimeoutHandler = LockAspectTestService.class)
    )
    public void testWarlockDistributed(int id) throws InterruptedException {
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
