package io.github.spitmaster.warlock.util;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class SpelExpressionUtilBenchmark {

    private Method method;
    private String name;
    private Long age;

    @Setup(Level.Trial)
    public void init1() {
        this.method = MethodUtils.getMatchingMethod(SpelExpressionUtilBenchmark.class, "method1", String.class, Long.class);
    }

    @Setup(Level.Invocation)
    public void init2() {
        this.age = RandomUtils.nextLong();
        this.name = UUID.randomUUID().toString();
    }

    @Benchmark
    @Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
    public String callWithPlainAspect() {
        return SpelExpressionUtil.parseSpel(method, new Object[]{name, age}, "#name + '-'+ #age", String.class);
    }

    public void method1(String name, Long age) {
        //靶子
    }

    public static void main(String[] args) throws RunnerException {
        Options jmhRunnerOptions = new OptionsBuilder()
                .include(SpelExpressionUtilBenchmark.class.getName())
                .forks(1)
                .build();
        new Runner(jmhRunnerOptions).run();
    }

}
//我的破电脑 执行速度大概如下:
//Benchmark                                         Mode  Cnt   Score   Error   Units
//SpelExpressionUtilBenchmark.callWithPlainAspect  thrpt    5  24.457 ± 1.148  ops/ms
