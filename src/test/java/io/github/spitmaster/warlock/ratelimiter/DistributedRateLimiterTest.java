package io.github.spitmaster.warlock.ratelimiter;

import io.github.spitmaster.warlock.Application;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest(classes = Application.class)
public class DistributedRateLimiterTest {

    @Autowired
    private StandaloneRateLimiterTestService service;

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
        service.init();
    }

    @Test
    void testRateLimiter1() throws InterruptedException, ExecutionException {
        List<Future<Integer>> futureList = new ArrayList<>();
        //1秒丢10个, 每秒,每个key, 能执行2个;
        //一共丢10~11秒, 2*11 * 2 = 44个
        //我预测一共执行了22次
        for (int i = 0; i < 100; i++) {
            TimeUnit.MILLISECONDS.sleep(101);
            int finalI = i;
            Future<Integer> future = executorService.submit(() -> {
                //一共会有两个key
                service.testDistributedRateLimiter(finalI % 2);
                return 1;
            });
            futureList.add(future);
        }
        for (Future<Integer> future : futureList) {
            future.get();
        }
        int counter = service.getCounter().get();
        System.out.println(counter);
        Assertions.assertEquals(44, counter);
    }

}
