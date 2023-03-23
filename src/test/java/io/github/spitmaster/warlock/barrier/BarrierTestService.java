package io.github.spitmaster.warlock.barrier;

import io.github.spitmaster.warlock.annotation.Waiting;
import io.github.spitmaster.warlock.annotation.WcyclicBarrier;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BarrierTestService {

    AtomicInteger counter1 = new AtomicInteger(0);
    AtomicInteger counter2 = new AtomicInteger(0);

    public void init() {
        counter1.set(0);
        counter2.set(0);
    }

    @WcyclicBarrier(
            parties = 2,
            name = "add1",
            waiting = @Waiting(waitTime = 1, timeUnit = ChronoUnit.SECONDS)
    )
    public void add1() {
        counter1.incrementAndGet();
        counter2.incrementAndGet();
    }

    public AtomicInteger getCounter1() {
        return counter1;
    }

    public AtomicInteger getCounter2() {
        return counter2;
    }
}
