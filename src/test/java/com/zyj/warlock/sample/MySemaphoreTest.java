package com.zyj.warlock.sample;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MySemaphoreTest {
    public static void main(String[] args) throws InterruptedException {
        Semaphore semaphore = new Semaphore(10);


        for (int i = 0; i < 100; i++) {
            int finalI = i;
            new Thread(() -> {
                try {
                    semaphore.acquire();
                    TimeUnit.MILLISECONDS.sleep(500);
                    System.out.println("print + " + finalI);
                    semaphore.release();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }


        TimeUnit.SECONDS.sleep(10000);
    }
}
