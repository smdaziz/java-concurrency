package io.github.smdaziz.thread.ordering;

public class OrderedThreads {

    public static void main(String[] args) {
        // We want to demonstrate ordered execution of threads
        // The intention is to ensure one thread completes before the next starts.

        // Create two threads with different sleep intervals
        // Worker-1 will sleep for 3 seconds
        Thread thread1 = new Thread(new Worker("Worker-1", 3));
        // Worker-2 will sleep for 5 seconds
        Thread thread2 = new Thread(new Worker("Worker-2", 5));

        try {
            thread1.start();
            thread1.join(); // Wait for thread1 to finish
            System.out.println("Halfway");
            thread2.start();
            thread2.join(); // Wait for thread2 to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("All workers have finished execution in order.");
    }

}

class Worker implements Runnable {
    private String name;
    private int sleepInterval = 0;

    public Worker(String name, int sleepInterval) {
        this.name = name;
        this.sleepInterval = sleepInterval;
    }

    @Override
    public void run() {
        System.out.println("Worker " + name + " is running.");
        try {
            Thread.sleep(sleepInterval * 1000); // Simulate work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Worker " + name + " has finished execution.");
    }
}
