package io.github.spitmaster.warlock.sample;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MyCyclicBarrierTest {
    public static void main(String[] args) throws InterruptedException {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(10);
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            TimeUnit.SECONDS.sleep(1);
            new Thread(() -> {
                try {
                    cyclicBarrier.await(1, TimeUnit.SECONDS);
                    System.out.println("print : " + finalI);
                } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
                    cyclicBarrier.reset();
                    throw new RuntimeException(e);
                }
            }).start();
        }


    }
}
