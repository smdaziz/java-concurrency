Lets talk about stopping threads in Java.

Lets think about it for a moment. Once a thread is started, it runs independently and concurrently with other threads.

Does it stop automatically? Yes, it does, when its `run()` method completes.

```java
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
```

However, there are scenarios where you might want to stop a thread before it completes its task, such as when the application is shutting down or when a specific condition is met.

Does Java provide a direct method to stop a thread? Yes, it does, but using such methods is generally discouraged because they can lead to inconsistent states and resource leaks. Instead, the recommended approach is to design your threads to be stoppable in a safe manner?
Yes, it is.
Java originally provided Thread.stop(), but it’s deprecated and unsafe.

It kills the thread immediately, without giving it a chance to release locks → can corrupt shared state and leave your program in an inconsistent state.

```java
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
```

🚫 Why not Thread.stop()?

Java originally provided Thread.stop(), but it’s deprecated and unsafe.

It kills the thread immediately, without giving it a chance to release locks → can corrupt shared state and leave your program in an inconsistent state.

There are several ways to stop a thread, but the most common and recommended way is to use a flag variable that the thread checks periodically to determine if it should stop running.

Here is an example of how to stop a thread using a flag variable:

```java
class StoppableThread extends Thread {
    private volatile boolean running = true;
    private String threadName;
    public StoppableThread(String name) {
        this.threadName = name;
    }
    public void run() {
        while (running) {
            System.out.println("Thread " + threadName + " is running.");
            try {
                Thread.sleep(1000); // Simulate work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
                System.out.println("Thread " + threadName + " was interrupted.");
            }
        }
        System.out.println("Thread " + threadName + " is stopping.");
    }
    public void stopRunning() {
        running = false;
    }
}
public class StoppingThreadExample {
    public static void main(String[] args) throws InterruptedException {
        StoppableThread thread1 = new StoppableThread("Thread-1");
        StoppableThread thread2 = new StoppableThread("Thread-2");

        thread1.start();
        thread2.start();

        Thread.sleep(5000); // Let the threads run for 5 seconds

        thread1.stopRunning(); // Signal thread1 to stop
        thread2.stopRunning(); // Signal thread2 to stop

        thread1.join(); // Wait for thread1 to finish
        thread2.join(); // Wait for thread2 to finish

        System.out.println("Both threads have been stopped.");
    }
}
```
In this example, we define a `StoppableThread` class that extends `Thread`. It has a `running` flag that is checked in the `run()` method. The `stopRunning()` method sets this flag to `false`, which causes the thread to exit its loop and stop running.
The `volatile` keyword is used to ensure that changes to the `running` variable are visible to all threads. The main method starts two threads, lets them run for 5 seconds, and then signals them to stop by calling `stopRunning()`. Finally, it waits for both threads to finish using `join()`.
This approach is safe and allows threads to finish their work gracefully. Avoid using deprecated methods like `stop()`, as they can lead to inconsistent states and resource leaks.
