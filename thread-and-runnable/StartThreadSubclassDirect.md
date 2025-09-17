## Starting a `Thread` subclass directly

```
public class StartThreadSubclassDirect {

    public static void main(String[] args) throws InterruptedException {
        Thread myThread1 = new MyThread("MyThread-1");
        myThread1.start();

        Thread myThread2 = new MyThread("MyThread-2");
        myThread2.start();
    }
}
```

### What it does
-   Constructs two `MyThread` instances with explicit names (`MyThread-1` and `MyThread-2`).
-   Starts each instance directly with `start()`.
-   Each `MyThread` object **is its own running thread** (no wrapper thread involved).

### What you'll see (order may vary)
-   `current/wrapper=MyThread-1, target=MyThread-1`
-   `current/wrapper=MyThread-2, target=MyThread-2`

*(Assumes `MyThread.run()` prints `Thread.currentThread().getName()` as `current` and `this.getName()` as `target`.)*

### Why these names appear
-   When you start a `Thread` subclass directly, the **instance itself** is the running thread.
-   Therefore, the "current" thread name and the instance's own name are the **same** for each line.

### Runtime sequence
1.  Construct `MyThread("MyThread-1")`.
2.  Call `start()` → JVM creates an OS thread and calls that instance's `run()`.
3.  Construct `MyThread("MyThread-2")` and `start()` it the same way.
4.  Both threads run concurrently; their outputs may interleave nondeterministically.

### Thread-safety note
-   These two threads are **separate instances**; they don't share instance fields.
-   Only shared/static state (if any) would require synchronization.

### Rule of thumb
-   **Starting a `Thread` subclass directly** makes the instance the running thread (simple, clear for demos).
-   In production, **prefer composition** (`new Thread(runnable)`) or, better, submit tasks to an **executor** (and on Java 21+, consider **virtual threads**).
