package com.zyj.warlock.aspect;

import com.zyj.warlock.annotation.Wlock;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.stereotype.Service;

@Service
public class AspectTestService {

    @Wlock(name = "test1", key = "#id")
    public void testWarlock(int id) {
        for (int i = 0; i < id; i++) {
            Blackhole.consumeCPU(i);//防止编译优化
        }
    }

    @PlainAspect.WBenchmark
    public void testPlainAspect(int id) {
        for (int i = 0; i < id; i++) {
            Blackhole.consumeCPU(i);//防止编译优化
        }
    }

    public void testPlain(int id) {
        for (int i = 0; i < id; i++) {
            Blackhole.consumeCPU(i);//防止编译优化
        }
    }

}
