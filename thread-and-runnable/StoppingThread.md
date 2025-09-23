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

## Any other means to stop a thread?

How about Thread.interrupt()?

```java
package io.github.smdaziz.thread.destruction;

public class StopByInterruptingThreadIncorrect {

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
```

Thread.interrupt() is a way to signal a thread that it should stop what it's doing and do something else (like terminate). However, it doesn't forcibly stop the thread. Instead, it sets an "interrupted" status flag on the thread.
If the thread is blocked in a method that throws InterruptedException (like Thread.sleep() or Object.wait()), it will immediately throw that exception, allowing the thread to handle the interruption gracefully.

So what does the above program do? Does it stop the thread?
No, it does not stop the thread. The thread continues to run even after being interrupted because the InterruptedException is caught, but the loop condition does not change. The thread checks the interrupted status at the start of each loop iteration, but since the exception was caught and handled, the interrupted status is cleared, and the loop continues.

Use interrupt()

If the thread may be blocked (e.g., in sleep(), wait(), or I/O), use interruption.

Call t.interrupt(), and in the thread, check Thread.currentThread().isInterrupted() or handle InterruptedException.

When sleep() throws InterruptedException, it also clears the interrupt flag. Since you neither break out of the loop nor restore the flag, the loop condition

while (!Thread.currentThread().isInterrupted())


becomes true again and the thread keeps running. That’s why your “Worker-1” doesn’t stop.

To properly stop the thread, you need to set a flag that the thread checks in its loop. When you want to stop the thread, you set this flag to false, and the thread will exit its loop and finish execution gracefully.

Two correct fixes
A) Restore the flag (preferred when you want upstream code to “see” the interrupt)
while (!Thread.currentThread().isInterrupted()) {
runCount++;
try {
System.out.println("Thread " + name + " is running. Count: " + runCount);
Thread.sleep(500);
} catch (InterruptedException e) {
System.out.println("Thread " + name + " was interrupted while sleeping.");
Thread.currentThread().interrupt();  // <- restore the interrupt status
}
}
System.out.println("Thread " + name + " has finished execution.");

B) Treat it as a cancel signal and exit immediately
while (true) {
runCount++;
try {
System.out.println("Thread " + name + " is running. Count: " + runCount);
Thread.sleep(500);
} catch (InterruptedException e) {
System.out.println("Thread " + name + " was interrupted while sleeping.");
break;  // <- leave the loop; thread ends
}
}
System.out.println("Thread " + name + " has finished execution.");


Both are valid. Pick one based on intent:

Restore then continue if there’s cleanup or outer logic that will check isInterrupted() again.

Break/return if interruption means “stop now.”

```java
package io.github.smdaziz.thread.destruction;

public class StopByInterruptingThreadIncorrect {

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
```

Instead, design your threads to be stoppable in a safe manner.

There are several ways to stop a thread, but the most common and recommended way is to use a flag variable that the thread checks periodically to determine if it should stop running.

Here is an example of how to stop a thread using a flag variable:

```java
package io.github.smdaziz.thread.destruction;

public class ProgrammaticallyStopThreads {

    public static void main(String[] args) {
        // Main thread is run as soon as program starts
        System.out.println("Main thread is running.");

        Runnable runnable = new StoppableRunnable();

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
        ((StoppableRunnable) runnable).stop(); // Signal the runnable to stop

        // Main thread ends here, but "Worker-1" continues to run
        System.out.println("Main thread has finished execution.");
    }

}

class StoppableRunnable implements Runnable {
    private boolean running = true;

    public void run() {
        long runCount = 0;
        String name = Thread.currentThread().getName();
        // Keep the thread running until it is interrupted/stopped
        while(running) {
            runCount++;
        }
        System.out.println("Thread " + name + " has finished execution. runCount: " + runCount);
    }

    public void stop() {
        this.running = false;
    }
}
```

In this example, we define a `StoppableThread` class that extends `Thread`. It has a `running` flag that is checked in the `run()` method. The `stopRunning()` method sets this flag to `false`, which causes the thread to exit its loop and stop running.

Is the code perfect?
No! Without volatile: a thread may spin forever (never “sees” false).

```java
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
```

The `volatile` keyword is used to ensure that changes to the `running` variable are visible to all threads. The main method starts two threads, lets them run for 5 seconds, and then signals them to stop by calling `stopRunning()`. Finally, it waits for both threads to finish using `join()`.
This approach is safe and allows threads to finish their work gracefully. Avoid using deprecated methods like `stop()`, as they can lead to inconsistent states and resource leaks.

