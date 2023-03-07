package com.zyj.warlock.aspect;

import com.zyj.warlock.Application;
import com.zyj.warlock.util.BeanHolder;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = Application.class)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class WarlockAspectJMHTest {

    @Autowired
    public void catchBean(AspectTestService aspectTestService) {
        //启动Spring环境的时候, 捕获待测试的Bean
        BeanHolder.setBean(aspectTestService);
    }

    //JMH的成员变量
    private AspectTestService aspectTestService;
    private Integer param;

    @Setup(Level.Trial)
    public void init1() {
        this.aspectTestService = BeanHolder.getBean();
    }

    @Setup(Level.Invocation)
    public void init2() {
        this.param = RandomUtils.nextInt(0, 100);
    }

    @Benchmark
    @Warmup(iterations = 2, time = 2, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 2, time = 2, timeUnit = TimeUnit.SECONDS)
    public void callWithWarlock() {
        aspectTestService.testWarlock(param);
    }

    @Benchmark
    @Warmup(iterations = 2, time = 2, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 2, time = 2, timeUnit = TimeUnit.SECONDS)
    public void callWithPlainAspect() {
        aspectTestService.testPlainAspect(param);
    }

    @Benchmark
    @Warmup(iterations = 2, time = 2, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 2, time = 2, timeUnit = TimeUnit.SECONDS)
    public void callWithPlain() {
        aspectTestService.testPlain(param);
    }

    @Test
    void runWithJMHTest() throws RunnerException {
        Options jmhRunnerOptions = new OptionsBuilder()
                .include(WarlockAspectJMHTest.class.getName())
                .forks(0)//要使用JMH必须与Spring环境再一个JVM中
                .build();
        new Runner(jmhRunnerOptions).run();

    }
}
