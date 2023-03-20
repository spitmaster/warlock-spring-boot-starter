package io.github.spitmaster.warlock.lock;

import io.github.spitmaster.warlock.Application;
import org.junit.jupiter.api.*;
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

    private static ExecutorService executorService;

    @BeforeAll
    static void setupAll() {
        executorService = Executors.newFixedThreadPool(100);
    }

    @AfterAll
    static void tearDownAll() {
        executorService.shutdown();
        executorService = null;
    }

    @BeforeEach
    void setup() {
        lockAspectTestService.setCounter(0);
        lockAspectTestService.setCounter0(0);
        lockAspectTestService.setCounter1(0);
    }

    @Test
    void warlockPointcut1() throws InterruptedException, ExecutionException {
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            tasks.add(() -> {
                lockAspectTestService.add100();
                return 1;
            });
        }
        List<Future<Integer>> futures = executorService.invokeAll(tasks);
        for (Future<Integer> future : futures) {
            Blackhole.consumeCPU(future.get().longValue());
        }
        var counter = lockAspectTestService.getCounter();
        System.out.println(counter);
        Assertions.assertEquals(100000, counter);
    }

    /**
     * 使用分布式锁很慢, 代价很高
     */
    @Test
    void warlockPointcut2() throws InterruptedException, ExecutionException {
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            tasks.add(() -> {
                lockAspectTestService.add100Distributed();
                return 1;
            });
        }
        List<Future<Integer>> futures = executorService.invokeAll(tasks);
        for (Future<Integer> future : futures) {
            Blackhole.consumeCPU(future.get().longValue());
        }
        var counter = lockAspectTestService.getCounter();
        System.out.println(counter);
        Assertions.assertEquals(100000, counter);
    }

    @Test
    void warlockPointcut3() throws InterruptedException, ExecutionException {
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            int finalI = i;
            tasks.add(() -> {
                lockAspectTestService.add100_2(finalI % 2);
                return 1;
            });
        }
        List<Future<Integer>> futures = executorService.invokeAll(tasks);
        for (Future<Integer> future : futures) {
            Blackhole.consumeCPU(future.get().longValue());
        }
        var counter0 = lockAspectTestService.getCounter0();
        var counter1 = lockAspectTestService.getCounter1();
        System.out.println("counter0 = " + counter0);
        System.out.println("counter1 = " + counter1);
        Assertions.assertEquals(50000, counter0);
        Assertions.assertEquals(50000, counter1);
    }

    /**
     * 使用分布式锁很慢, 代价很高
     */
    @Test
    void warlockPointcut4() throws InterruptedException, ExecutionException {
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            int finalI = i;
            tasks.add(() -> {
                lockAspectTestService.add100Distributed2(finalI % 2);
                return 1;
            });
        }
        List<Future<Integer>> futures = executorService.invokeAll(tasks);
        for (Future<Integer> future : futures) {
            Blackhole.consumeCPU(future.get().longValue());
        }
        var counter0 = lockAspectTestService.getCounter0();
        var counter1 = lockAspectTestService.getCounter1();
        System.out.println("counter0 = " + counter0);
        System.out.println("counter1 = " + counter1);
        Assertions.assertEquals(50000, counter0);
        Assertions.assertEquals(50000, counter1);
    }

    @Test
    void waitTimeout() throws InterruptedException, ExecutionException {
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            tasks.add(() -> {
                lockAspectTestService.waitTimeout(finalI);
                return 1;
            });
        }
        List<Future<Integer>> futures = executorService.invokeAll(tasks);
        for (Future<Integer> future : futures) {
            future.get();
        }
        System.out.println(lockAspectTestService.getCounter());
    }

    @Test
    void leaseTimeout() throws InterruptedException, ExecutionException {
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            tasks.add(() -> {
                lockAspectTestService.leaseTimeout(finalI);
                return 1;
            });
        }
        List<Future<Integer>> futures = executorService.invokeAll(tasks);
        for (Future<Integer> future : futures) {
            future.get();
        }
        System.out.println(lockAspectTestService.getCounter());
    }
}