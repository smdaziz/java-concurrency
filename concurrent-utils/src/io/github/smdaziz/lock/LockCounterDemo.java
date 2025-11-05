package io.github.smdaziz.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockCounterDemo {
    public static void main(String[] args) {
        LockCounter lockCounter = new LockCounter();

        Thread t1 = new Thread(lockCounter, "LockCounter-1");
        Thread t2 = new Thread(lockCounter, "LockCounter-2");
        Thread t3 = new Thread(lockCounter, "LockCounter-3");

        t1.start();
        t2.start();
        t3.start();
    }
}

class LockCounter implements Runnable {
    private int count;
    private Lock lock = new ReentrantLock();

    @Override
    public void run() {
        for(int i = 0; i < 1_000_000; i++) {
            try {
                lock.lock();
                count++;
            } finally {
                lock.unlock();
            }
        }
        System.out.println(Thread.currentThread().getName() + " final count: " + count);
    }
}
