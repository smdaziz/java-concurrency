package io.github.smdaziz.thread.synchronization;

public class CounterSyncMethodThread {
    public static void main(String[] args) throws InterruptedException {
        // Thread-unsafe counter
        // The goal is to increment the counter 3000000 times (1000000 times by each thread)
        MillionCounterV2 millionCounter = new MillionCounterV2();

        Thread t1 = new Thread(millionCounter);
        Thread t2 = new Thread(millionCounter);
        Thread t3 = new Thread(millionCounter);

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        System.out.println("Final count (no synchronization): " + millionCounter.getCount());
    }
}

class MillionCounterV2 implements Runnable {
    private int count = 0;

    public synchronized void run() {
        for (int i = 0; i < 1000000; i++) {
            count++;
        }
    }

    public int getCount() {
        return count;
    }
}
