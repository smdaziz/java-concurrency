package io.github.smdaziz.thread.ordering;

public class CyclicBarrierThread {
    public static void main(String[] args) {
        System.out.println("Main thread started.");
        CyclicBarrier barrier = new CyclicBarrier(3);
        Thread thread1 = new Thread(new CyclicBarrierCounter(barrier), "Thread-1");
        Thread thread2 = new Thread(new CyclicBarrierCounter(barrier), "Thread-2");
        Thread thread3 = new Thread(new CyclicBarrierCounter(barrier), "Thread-3");
        Thread thread4 = new Thread(new CyclicBarrierCounter(barrier), "Thread-4");
        Thread thread5 = new Thread(new CyclicBarrierCounter(barrier), "Thread-5");
        Thread thread6 = new Thread(new CyclicBarrierCounter(barrier), "Thread-6");
        thread1.start();
        thread2.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        thread3.start();
        System.out.println("Main thread is running.");
        try {
            thread1.join();
            thread2.join();
            thread3.join();
            System.out.println("First phase completed. Starting next phase...");
            thread4.start();
            thread5.start();
            thread6.start();
            thread4.join();
            thread5.join();
            thread6.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Main thread has finished execution.");
    }
}

class CyclicBarrier {
    private final int totalThreads;
    private int waitingThreads;
    private int phase = 0;

    public CyclicBarrier(int totalThreads) {
        this.totalThreads = totalThreads;
        this.waitingThreads = totalThreads;
    }

    public synchronized void waitForOthers() {
        /*int currentPhase = phase;
        waitingThreads--;
        while (waitingThreads > 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        notifyAll();
        waitingThreads = totalThreads; // Reset for next phase
        phase++;*/
        // Note: The above code is commented out to avoid deadlock in this example.
        // Wrong wait condition. waits on while (waitingThreads > 0).
        // The last arriver sets waitingThreads = totalThreads and notifyAll().
        // Woken threads recheck the loop → condition is true again → they go back to wait() → hang.

        int currentPhase = phase;
        waitingThreads--;
        if (waitingThreads == 0) {
            waitingThreads = totalThreads; // Reset for next phase
            phase++;
            notifyAll();
            return;
        }
        while (currentPhase == phase) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class CyclicBarrierCounter implements Runnable {
    private final CyclicBarrier barrier;

    public CyclicBarrierCounter(CyclicBarrier barrier) {
        this.barrier = barrier;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " is waiting at the barrier.");
        barrier.waitForOthers();
        System.out.println(Thread.currentThread().getName() + " has crossed the barrier.");
        try {
            System.out.println(Thread.currentThread().getName() + " performing some work.");
            Thread.sleep(10000); // Simulate some work with sleep
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println(Thread.currentThread().getName() + " has finished execution.");
    }
}
