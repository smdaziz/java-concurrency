package io.github.smdaziz;
// Goal: Main must block/wait until all workers finish, but without using join()
public class Problem5 {

    public static void main(String[] args) {
        System.out.println("Main thread started.");
        int numThreads = 5;
        MyWaitLock waitLock = new MyWaitLock(numThreads);
        Problem5Thread problem5Thread = new Problem5Thread(waitLock);
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(problem5Thread, "Problem5-Thread-" + (i + 1));
            threads[i].start();
        }
        try {
            synchronized (waitLock) {
                while(waitLock.jobsInProgress()) {
                    waitLock.wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Main thread finished.");
    }

}

class MyWaitLock {
    private int count;

    public MyWaitLock(int count) {
        this.count = count;
    }

    public void decrement() {
        synchronized(this) {
            count--;
            if (count == 0) {
                this.notifyAll();
            }
        }
    }

    public synchronized boolean jobsInProgress() {
        return count > 0;
    }
}

class Problem5Thread implements Runnable {
    private final MyWaitLock lock;

    public Problem5Thread(MyWaitLock lock) {
        this.lock = lock;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " started.");
        for(int i = 0; i < Integer.MAX_VALUE; i++) {
            for(int j = 0; j < Integer.MAX_VALUE; j++) {
                Math.sqrt(i * j);
            }
        }
        System.out.println(Thread.currentThread().getName() + " finished.");
        lock.decrement();
    }
}
