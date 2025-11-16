package io.github.smdaziz.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InterruptibleLockDemo {
    public static void main(String[] args) {
        Lock lock = new ReentrantLock();
        InterruptibleWorker interruptibleWorker = new InterruptibleWorker(lock);

        Thread t1 = new Thread(interruptibleWorker, "InterruptibleWorker-1");
        Thread t2 = new Thread(interruptibleWorker, "InterruptibleWorker-2");

        t1.start();
        try {
            Thread.sleep(1000); // let t1 acquire the lock
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        t2.start(); // t2 will be blocked or waiting to acquire the lock
        try {
            Thread.sleep(1000); // let t2 try to acquire the lock
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Main thread interrupting t2");
        t2.interrupt(); // this will unblock t2 and it will no longer wait to acquire the lock
    }
}

class InterruptibleWorker implements Runnable {
    private final Lock lock;

    public InterruptibleWorker(Lock lock) {
        this.lock = lock;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        try {
            System.out.println(threadName + " trying to acquire the lock");
            lock.lockInterruptibly();
            try {
                System.out.println(threadName + " acquired the lock");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println(threadName + " interrupted while sleeping with acquired lock");
            } finally {
                lock.unlock();
                System.out.println(threadName + " released the lock");
            }
        } catch (InterruptedException e) {
            System.out.println(threadName + " interrupted while waiting/trying to acquire the lock");
        }
    }
}
