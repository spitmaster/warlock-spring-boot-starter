package com.zyj.warlock.aspect;

import com.zyj.warlock.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest(classes = Application.class)
class WarlockAspectTest {

    @Autowired
    private AspectTestService aspectTestService;

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(1000);
    }

    @Test
    void warlockPointcut() throws InterruptedException, ExecutionException {
        List<Callable<Integer>> tasks = IntStream.range(1, 10000)
                .mapToObj((IntFunction<Callable<Integer>>) value -> () -> {
                    aspectTestService.testWarlock(value % 1000);
                    return 1;
                }).collect(Collectors.toList());
        List<Future<Integer>> futures = executorService.invokeAll(tasks);
        for (Future<Integer> future : futures) {
            Blackhole.consumeCPU(future.get());
        }
    }
}