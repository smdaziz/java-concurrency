package io.github.smdaziz.thread.ordering;

public class EndGateThreads {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Main thread started.");
        EndGate endGate = new EndGate();

        Thread thread1 = new Thread(endGate, "Thread-1");
        Thread thread2 = new Thread(endGate, "Thread-2");
        Thread thread3 = new Thread(endGate, "Thread-3");

        thread1.start();
        thread2.start();
        thread3.start();

        System.out.println("Main thread is running.");

        System.out.println("Main thread has finished execution.");

        thread1.join();
        thread2.join();
        thread3.join();

        System.out.println("Main thread waiting for all threads to end.");

        System.out.println("All threads have finished execution. Main thread exiting.");
    }

}

class EndGate implements Runnable {

    public void run() {
        System.out.println("Thread " + Thread.currentThread().getName() + " has started execution.");
        try {
            Thread.sleep(2000); // Simulate some work with sleep
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Thread " + Thread.currentThread().getName() + " has finished execution.");
    }

}
