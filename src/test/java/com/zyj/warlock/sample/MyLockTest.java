package com.zyj.warlock.sample;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MyLockTest {


    public static void main(String[] args) throws InterruptedException {
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        lock.writeLock().lock();
        System.out.println("write lock acquired");

        new Thread(() -> {
            try {
                boolean b = lock.writeLock().tryLock(5, TimeUnit.SECONDS);
                System.out.println(b);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("another end");
        }).start();


        TimeUnit.SECONDS.sleep(10);
        lock.writeLock().unlock();
        System.out.println("unlock");
        TimeUnit.SECONDS.sleep(2);


    }
}
