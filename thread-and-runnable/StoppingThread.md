## Let's talk about stopping threads in Java.

Let's think about it for a moment. Once a thread is started, it runs independently and concurrently with other threads.

**Does it stop automatically?**

**Yes** - when its `run()` method completes.

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

*What it shows:* a worker starts, does some work, and **stops naturally** when `run()` returns. No explicit stop needed. (Also, there's no guarantee about the ordering of prints between main and the worker.)

<hr>

However, there are scenarios where you might want to stop a thread **before** it completes its task---e.g., during application shutdown or when a specific condition is met.

**Does Java provide a direct method to stop a thread?**

**Yes**, but using such methods is generally discouraged because they can lead to inconsistent states and resource leaks. Instead, the recommended approach is to **design your threads to be stoppable** in a safe manner.

Java originally provided `Thread.stop()`, but it's **deprecated and unsafe**.

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

*What it shows:* calling `Thread.stop()` appears to work, but it's dangerous because it can terminate a thread while it holds locks.\
*Minor note:* `Thread.stop()` is deprecated (not removed). The main reason to avoid it is **state corruption**; on some setups a security policy might throw a `SecurityException`, but the big problem is correctness, not a guaranteed exception type.

<hr>

**Any other means to stop a thread?**

How about `Thread.interrupt()`?

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

*What it shows:* calling `thread.interrupt()` **does not** forcibly kill the thread. It sets the thread's interrupted status and, if the thread is blocked in a method like `sleep()`/`wait()`, it throws `InterruptedException`. In this example, the catch block **swallows the interrupt** and continues, so the loop condition later sees "not interrupted" and the thread keeps running.

**Two correct fixes (conceptually, no code here):**
- **Restore the flag:** on catching `InterruptedException`, call `Thread.currentThread().interrupt()` and let the loop condition notice it next iteration.
- **Treat as cancel:** on catching `InterruptedException`, `break`/`return` and exit the loop immediately.

Pick one based on intent:
- **Restore** if upstream code needs to observe the interrupt later.
- **Break** if interruption means "stop now."

```java
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
```

<hr>

**Instead, design your threads to be stoppable in a safe manner.**

**A common way:** use a **flag** the thread checks periodically to determine if it should stop running.

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

*What it shows:* a shared boolean `running` flag **without `volatile`**. In a tight loop this can become a Heisenbug: a worker may never observe the write and **spin forever**. (FYI, frequent `println`/`sleep` calls can accidentally mask the problem by introducing synchronization/safepoints.)

**Is the code perfect?**

**No.** Without `volatile`, a thread may spin forever (never "sees" `false`).

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

*What it shows:* the same pattern, but the flag is **`volatile`**. Now writes by one thread are **visible** to others, so the loop exits reliably.\

<hr>

**Quick takeaways**

- **Natural stop:** let `run()` finish when the task is finite.
- **Don't use `Thread.stop()`:** deprecated and unsafe; can corrupt shared state.
- **`interrupt()` correctly:** either **restore** the interrupt or **exit**; never swallow it and continue blindly.
- **Flag pattern:** if you poll a flag, make it **`volatile`** (or use `AtomicBoolean`).
- **Blocked threads:** combine the flag with **`interrupt()`** so the thread wakes up and exits promptly.
- **Accidental masking:** `println`/`sleep` can hide visibility bugs - use a tight loop to demo the need for `volatile`.
