package io.github.spitmaster.warlock.barrier;

import io.github.spitmaster.warlock.Application;
import io.github.spitmaster.warlock.exceptions.WarlockException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest(classes = Application.class)
public class StandaloneWbarrierTest {

    @Autowired
    private BarrierTestService barrierTestService;

    private static ExecutorService executorService;

    @BeforeAll
    static void setupAll() {
        executorService = Executors.newFixedThreadPool(10);
    }

    @AfterAll
    static void tearDownAll() {
        executorService.shutdown();
        executorService = null;
    }

    @BeforeEach
    void setup() {
        barrierTestService.init();
    }

    @Test
    void tryWaitTimeout() throws InterruptedException, ExecutionException {
        List<Callable<Integer>> tasks = new ArrayList<>();
        //造一个场景只有6个能执行成功, 第七个会timeout
        for (int i = 0; i < 7; i++) {
            tasks.add(() -> {
                try {
                    //会有6次是成功执行
                    //1次是超时,然后抛出异常
                    barrierTestService.add1();
                } catch (WarlockException e) {
                    System.out.println(e);
                }
                return 1;
            });
        }
        long startTime = System.currentTimeMillis();
        List<Future<Integer>> futures = executorService.invokeAll(tasks);
        for (Future<Integer> future : futures) {
            future.get();
        }
        long endTime = System.currentTimeMillis();
        long consumedTime = endTime - startTime;//执行6个任务耗费的时间
        System.out.println("consumedTime = " + consumedTime);
        int counter1 = barrierTestService.getCounter1().get();
        int counter2 = barrierTestService.getCounter2().get();
        System.out.println("counter1 = " + counter1);
        System.out.println("counter2 = " + counter2);
        Assertions.assertEquals(6, counter1);
        Assertions.assertEquals(6, counter2);
        //等待时间超过1秒钟, 因为 @WcyclicBarrier 设置了1秒超时
        Assertions.assertTrue(consumedTime >= 1000 && consumedTime <= 1500);
    }

}
