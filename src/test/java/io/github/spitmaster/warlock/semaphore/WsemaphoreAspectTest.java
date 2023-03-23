package io.github.spitmaster.warlock.semaphore;

import io.github.spitmaster.warlock.Application;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@SpringBootTest(classes = Application.class)
class WsemaphoreAspectTest {

    @Autowired
    private SemaphoreAspectTestService semaphoreAspectTestService;

    private static ExecutorService executorService;


    @BeforeAll
    static void beforeAll() {
        executorService = Executors.newFixedThreadPool(100);
    }

    @AfterAll
    static void tearDownAll() {
        executorService.shutdown();
        executorService = null;
    }

    @BeforeEach
    public void setup() {
        semaphoreAspectTestService.init();
    }

    @Test
    void testWsemaphore1() throws InterruptedException, ExecutionException {
        List<Callable<Integer>> tasks = new ArrayList<>();
        String uuid = UUID.randomUUID().toString();

        for (int i = 0; i < 100; i++) {
            int finalI = i;
            tasks.add(() -> {
                semaphoreAspectTestService.testWsemaphore(uuid + String.valueOf(finalI % 4));
                return 1;
            });
        }
        List<Future<Integer>> futures = executorService.invokeAll(tasks);
        for (Future<Integer> future : futures) {
            future.get();
        }
        int counter = semaphoreAspectTestService.getCounter();
        System.out.println(semaphoreAspectTestService.getCounter());
        Assertions.assertEquals(4 * 7, counter);
    }

    @Test
    void testWsemaphore2() throws InterruptedException, ExecutionException {
        List<Callable<Integer>> tasks = new ArrayList<>();
        String uuid = UUID.randomUUID().toString();
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            tasks.add(() -> {
                String id = uuid + String.valueOf(finalI % 8);
                semaphoreAspectTestService.testWsemaphore2(id);
                return 1;
            });
        }
        List<Future<Integer>> futures = executorService.invokeAll(tasks);
        for (Future<Integer> future : futures) {
            future.get();
        }
        int counter = semaphoreAspectTestService.getCounter();
        System.out.println(semaphoreAspectTestService.getCounter());
        Assertions.assertEquals(8 * 3, counter);
    }
}