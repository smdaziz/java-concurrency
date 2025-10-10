package io.github.smdaziz;

import java.util.Arrays;

// Goal: Implement a Latch (main waits until N worker threads finish).
// No CountDownLatch allowed. No busy waiting.
// No join() allowed because with join, waiting for N things means N joins (and keeping N thread refs)
public class Problem8 {
    public static void main(String[] args) {
        System.out.println("Main thread started.");
        int numThreads = 4;
        ArmstrongFinderLatch latch = new ArmstrongFinderLatch(numThreads);
        Thread[] threads = new Thread[numThreads];

        for(int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(new ArmstrongFinder(latch), "ArmstrongFinder-" + (i + 1));
            threads[i].start();
        }

        System.out.println("Main thread is waiting for all worker threads to finish.");

        latch.waitUntilAllThreadsFinished();
        System.out.println("Main thread finished.");
    }
}

class ArmstrongFinderLatch {
    private int count = 0;

    public ArmstrongFinderLatch(int count) {
        this.count = count;
    }

    public synchronized void waitUntilAllThreadsFinished() {
        while(count > 0) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public synchronized void decrement() {
        count--;
        if (count == 0) {
            this.notifyAll();
        }
    }
}

class ArmstrongFinder implements Runnable {
    private final ArmstrongFinderLatch latch;

    public ArmstrongFinder(ArmstrongFinderLatch latch) {
        this.latch = latch;
    }

    @Override
    public void run() {
        for(int i = 100; i < 10_000_000; i++) {
            if (isArmstrong(i)) {
                System.out.println("[" + Thread.currentThread().getName() + "] : " + i + " is an Armstrong number.");
            }
        }
        latch.decrement();
    }

    private boolean isArmstrong(int number) {
        int originalNumber = number;
        int sum = 0;
        int digits = String.valueOf(number).length();

        while (number != 0) {
            int digit = number % 10;
            sum += Math.pow(digit, digits);
            number /= 10;
        }

        return sum == originalNumber;
    }
}
