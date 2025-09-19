package io.github.smdaziz.thread.destruction;

public class StopByInterruptingThreadCorrect {

    public static void main(String[] args) {
        // Main thread is run as soon as program starts
        System.out.println("Main thread is running.");

        Runnable runnable = () -> {
            int runCount = 0;
            String name = Thread.currentThread().getName();
            // Keep the thread running until it is interrupted/stopped
            while(!Thread.currentThread().isInterrupted()) {
                runCount++;
                try {
                    System.out.println("Thread " + name + " is running. Count: " + runCount);
                    // Simulate some work with sleep
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    System.out.println("Thread " + name + " was interrupted while sleeping.");
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println("Thread " + name + " has finished execution.");
        };

        // Create and start the thread
        // So, main thread created and started this new thread named "Worker-1"
        Thread thread = new Thread(runnable, "Worker-1");
        thread.start();

        try {
            Thread.sleep(2000); // Let the thread run for a while
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Interrupting the thread to stop it.");
        thread.interrupt(); // Interrupt the thread to signal it to stop

        // Main thread ends here, but "Worker-1" continues to run
        System.out.println("Main thread has finished execution.");
    }

}
