package io.github.spitmaster.warlock.semaphore;

import io.github.spitmaster.warlock.Application;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest(classes = Application.class)
class WsemaphoreAspectTest {

    @Autowired
    private SemaphoreAspectTestService semaphoreAspectTestService;

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
        for (int i = 0; i < 2; i++) {
            int finalI = i;
            tasks.add(() -> {
                semaphoreAspectTestService.testWsemaphore2(finalI);
                return 1;
            });
        }
        List<Future<Integer>> futures = executorService.invokeAll(tasks);
        for (Future<Integer> future : futures) {
            future.get();
        }
        System.out.println(semaphoreAspectTestService.getCounter());
    }
}