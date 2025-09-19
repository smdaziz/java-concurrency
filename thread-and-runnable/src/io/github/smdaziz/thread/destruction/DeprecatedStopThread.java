package io.github.smdaziz.thread.destruction;

public class DeprecatedStopThread {

    public static void main(String[] args) {
        // Main thread is run as soon as program starts
        System.out.println("Main thread is running.");

        System.out.println(System.getProperty("java.version"));
        System.out.println(System.getProperty("java.vendor"));

        Runnable runnable = () -> {
            String name = Thread.currentThread().getName();
            try {
                while (true) {
                    System.out.println("Thread " + name + " is running.");
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                System.out.println("Thread was interrupted");
            } finally {
                System.out.println("Cleanup in finally block");
            }
        };

        Thread thread = new Thread(runnable, "Worker-1");
        thread.start();

        try {
            Thread.sleep(2000); // Let the thread run for a while
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Stopping the thread using deprecated stop()");
        try {
            // On latest java versions, this may throw an UnsupportedOperationException
            // and may not actually stop the thread.
            thread.stop(); // Deprecated and unsafe way to stop a thread
        } catch (Exception e) {
            System.out.println("Caught Exception trying to stop the thread: " + e.getMessage());
            System.out.println(e);
            e.printStackTrace();
        } finally {
            System.out.println(thread.getName() + " has been stopped");
            System.out.println("Main thread has finished execution.");
        }
    }

}
