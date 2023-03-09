package io.github.spitmaster.warlock.lock;

import io.github.spitmaster.warlock.Application;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest(classes = Application.class)
class WarlockAspectTest {

    @Autowired
    private LockAspectTestService lockAspectTestService;

    private ExecutorService executorService;

    @BeforeEach
    void setup() {
        executorService = Executors.newFixedThreadPool(1000);
    }

    @AfterEach
    void tearDown() {
        executorService = null;
    }

    @Test
    void warlockPointcut() throws InterruptedException, ExecutionException {
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            tasks.add(() -> {
                lockAspectTestService.add10();
                return 1;
            });
        }
        List<Future<Integer>> futures = executorService.invokeAll(tasks);
        for (Future<Integer> future : futures) {
            Blackhole.consumeCPU(future.get().longValue());
        }
        System.out.println(lockAspectTestService.getCounter());
    }
}