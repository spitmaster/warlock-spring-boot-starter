package com.zyj.warlock.sample;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MyCountDownLatchTest {
    public static void main(String[] args) throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(100);

        for (int i = 0; i < 100; i++) {
            int finalI = i;
            new Thread(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(finalI * 100);
                    System.out.println(countDownLatch.getCount());
                    countDownLatch.countDown();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }


        //countDownLatch.getCount() == 0 的时候,会释放await
        countDownLatch.await();
        System.out.println("pass await");

    }
}
