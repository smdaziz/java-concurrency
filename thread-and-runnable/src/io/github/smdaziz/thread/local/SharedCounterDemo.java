package io.github.smdaziz.thread.local;

public class SharedCounterDemo {
    public static void main(String[] args) {
        SharedCounter sharedCounter = new SharedCounter();

        Thread t1 = new Thread(sharedCounter, "SharedCounter-1");
        Thread t2 = new Thread(sharedCounter, "SharedCounter-2");
        Thread t3 = new Thread(sharedCounter, "SharedCounter-3");

        t1.start();
        t2.start();
        t3.start();
    }
}

class SharedCounter implements Runnable {
    private int count = 0;

    @Override
    public void run() {
        for(int i = 0; i < 1_000_000; i++) {
            count++;
        }
        System.out.println(Thread.currentThread().getName() + " counted till " + count);
    }
}
