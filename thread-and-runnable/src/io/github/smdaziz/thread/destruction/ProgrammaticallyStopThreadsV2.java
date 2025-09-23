package io.github.smdaziz.thread.destruction;

public class ProgrammaticallyStopThreadsV2 {

    public static void main(String[] args) {
        // Main thread is run as soon as program starts
        System.out.println("Main thread is running.");

        Runnable runnable = new SafeStoppableRunnable();

        // Create and start the thread
        // So, main thread created and started this new thread named "Worker-1"
        Thread thread1 = new Thread(runnable, "Worker-1");
        thread1.start();

        Thread thread2 = new Thread(runnable, "Worker-2");
        thread2.start();

        try {
            Thread.sleep(2000); // Let the thread run for a while
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Stopping the thread(s) by setting a flag.");
        ((SafeStoppableRunnable) runnable).stop(); // Signal the runnable to stop

        // Main thread ends here, but "Worker-1" continues to run
        System.out.println("Main thread has finished execution.");
    }

}

class SafeStoppableRunnable implements Runnable {
    private volatile boolean running = true;

    public void run() {
        long runCount = 0;
        String name = Thread.currentThread().getName();
        // Keep the thread running until it is interrupted/stopped
        while(running) {
            runCount++;
        }
        System.out.println("Thread " + name + " has finished execution. runCount: " + runCount);
    }

    public void stop()
    {
        this.running = false;
    }
}
