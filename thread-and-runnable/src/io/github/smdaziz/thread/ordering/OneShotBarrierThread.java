package io.github.smdaziz.thread.ordering;

public class OneShotBarrierThread {
    public static void main(String[] args) {
        System.out.println("Main thread started.");
        OneShotBarrier barrier = new OneShotBarrier(3);
        Thread thread1 = new Thread(new OneShotBarrierCounter(barrier), "Thread-1");
        Thread thread2 = new Thread(new OneShotBarrierCounter(barrier), "Thread-2");
        Thread thread3 = new Thread(new OneShotBarrierCounter(barrier), "Thread-3");
        System.out.println("Main thread is running.");
        thread1.start();
        try{
            Thread.sleep(5000); // Simulate some work with sleep
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread2.start();
        try{
            Thread.sleep(5000); // Simulate some work with sleep
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread3.start();
        System.out.println("Main thread has finished execution.");
        System.out.println("Main thread waiting for all threads to end.");
        try {
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("All threads have finished execution. Main thread exiting.");
    }
}

class OneShotBarrier {
    private int count;

    public OneShotBarrier(int count) {
        this.count = count;
    }

    public synchronized void decrement() {
        count--;
        if (count == 0) {
            this.notifyAll();
        }
    }

    public boolean shouldWait() {
        return count > 0;
    }
}

class OneShotBarrierCounter implements Runnable {
    private final OneShotBarrier barrier;

    public OneShotBarrierCounter(OneShotBarrier barrier) {
        this.barrier = barrier;
    }

    @Override
    public void run() {
        // Approach 1: (incorrect)
        /* barrier.decrement();
        while(barrier.shouldWait()) {
            synchronized (barrier) {
                try {
                    barrier.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }*/
        // Note: in the commented code,
        // check shouldWait() outside the monitor → classic missed-signal risk.
        // shouldWait() isn’t synchronized → unsafely reads count

        // Approach 2: (incorrect)
        /* barrier.decrement();
        synchronized (barrier) {
            while(barrier.shouldWait()) {
                try {
                    barrier.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }*/
        // Note: in the commented code,
        // even though shouldWait() is called inside the monitor,
        // decrement() is called outside the monitor → classic missed-signal risk.
        // shouldWait() isn’t synchronized → unsafely reads count
        // decrement() being synchronized protects the update itself, but you also need atomicity with the check+wait.
        // If we drop the lock between decrement() and wait(),
        // another thread can flip the state (possibly count==0 and notifyAll())
        // between the check and the wait, creating a missed-signal window.

        // Approach 3: (correct)
        synchronized (barrier) {
            barrier.decrement();
            while(barrier.shouldWait()) {
                try {
                    barrier.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        // Note: in the above code,
        // both decrement() and shouldWait() are called inside the monitor,
        // so there is no missed-signal risk.
        // shouldWait() is synchronized → safely reads count
        // decrement() being synchronized protects the update itself, and we have atomicity with the check
        System.out.println(Thread.currentThread().getName() + " started.");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + " finished.");
    }
}
