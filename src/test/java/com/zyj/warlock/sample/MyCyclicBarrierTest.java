package com.zyj.warlock.sample;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

public class MyCyclicBarrierTest {
    public static void main(String[] args) throws InterruptedException {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(10);
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            TimeUnit.SECONDS.sleep(1);
            new Thread(() -> {
                try {
                    cyclicBarrier.await();
                    System.out.println("print : " + finalI);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }


    }
}
