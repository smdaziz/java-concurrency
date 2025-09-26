package io.github.smdaziz.thread.ordering;

public class OrderedThreadsV2 {

    public static void main(String[] args) {
        // Goal: Start two threads (Worker-1 and Worker-2) simultaneously.
        // Even if Worker-1 finishes its own task earlier,
        // we want it to wait for Worker-2 to complete before considering itself "done".
        //
        // meaning Worker-1 waits for Worker-2 to finish, regardless of timing.

        // Create two threads with different sleep intervals
        // Worker-1 will sleep for 3 seconds
        WorkerV2 worker1 = new WorkerV2("Worker-1", 3);
        Thread thread1 = new Thread(worker1, "Worker-1");
        // Worker-2 will sleep for 5 seconds
        WorkerV2 worker2 = new WorkerV2("Worker-2", 5);
        Thread thread2 = new Thread(worker2, "Worker-2");

        // Set Worker-1 to wait for Worker-2
        worker1.setThreadToWaitFor(thread2);

        thread1.start();
        thread2.start();

        System.out.println("All workers have finished execution in order.");
    }

}

class WorkerV2 implements Runnable {
    private String name;
    private int sleepInterval = 0;
    private Thread threadToWaitFor;

    public WorkerV2(String name, int sleepInterval) {
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

        // If there's a thread to wait for, wait for it to finish
        if (threadToWaitFor != null) {
            try {
                System.out.println("Worker " + name + " has completed its task and is waiting for " + threadToWaitFor.getName() + " to finish.");
                threadToWaitFor.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Worker " + name + " has finished execution.");
    }

    public void setThreadToWaitFor(Thread thread) {
        this.threadToWaitFor = thread;
    }
}
