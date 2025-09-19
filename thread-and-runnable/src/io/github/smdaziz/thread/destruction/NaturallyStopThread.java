package io.github.smdaziz.thread.destruction;

public class NaturallyStopThread {

    public static void main(String[] args) {
        // Main thread is run as soon as program starts
        System.out.println("Main thread is running.");
        Runnable runnable = () -> {
            String name = Thread.currentThread().getName();
            System.out.println("Thread " + name + " is running.");
            try {
                // Simulate some work with sleep
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.println("Thread " + name + " was interrupted.");
            }
            System.out.println("Thread " + name + " has finished execution.");
            // If we don't have any more code to run, the thread will naturally stop
        };

        // Create and start the thread
        // So, main thread created and started this new thread named "Worker-1"
        Thread thread = new Thread(runnable, "Worker-1");
        thread.start();

        // Main thread ends here, but "Worker-1" continues to run
        System.out.println("Main thread has finished execution.");

        // Note: There is no need to explicitly stop the thread;
        // it will stop naturally after completing its run method.
        // Also, there is no guarantee about the order of execution of main thread and worker thread.
    }

}
