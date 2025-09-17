## Reusing the Same Target Across Two Wrapper Threads

```
public class ReuseSameTargetAcrossWrappers {

    public static void main(String[] args) throws InterruptedException {
        MyThread myThread = new MyThread("MyThread");
        Thread t1 = new Thread(myThread, "MyThread-Thread-1");
        t1.start();

        Thread t2 = new Thread(myThread, "MyThread-Thread-2");
        t2.start();
    }
}
```

### What this does
-   Creates one `MyThread` instance named `MyThread` but does **not** start it directly.
-   Wraps that same instance in two **different** wrapper threads (`t1`, `t2`) with distinct names (`MyThread-Thread-1` and `MyThread-Thread-2`).
-   Starts both wrappers; each wrapper calls the **same** target's `run()`.

### What you'll see (order may vary)
-   `current/wrapper=MyThread-Thread-1, target=MyThread`
-   `current/wrapper=MyThread-Thread-2, target=MyThread`

*(Assuming `MyThread.run()` prints `Thread.currentThread().getName()` as `current` and `this.getName()` as `target`.)*

### Why these names appear
-   `run()` is executed **in the wrapper threads**, so `current` shows the **wrapper's** name.
-   `target` refers to the **shared target instance** (`MyThread`), so it prints the same target name for both lines.

### Runtime sequence
1.  Construct target: a single `MyThread("MyThread")` instance.
2.  Construct two wrappers: `new Thread(target, "MyThread-Thread-1")` and `new Thread(target, "MyThread-Thread-2")`.
3.  Start both wrappers. Each wrapper's `run()` invokes the **same** target's `run()` on the **same** object instance.
4.  The two wrapper threads run concurrently, interleaving prints nondeterministically.

### Thread-safety note (important)
-   Reusing the **same target** across multiple threads is fine **if the target is stateless** (only reads its own immutable state or prints names).
-   If `MyThread` holds **mutable fields** (counters, collections, etc.), those fields are **shared** between both wrappers. You must add proper synchronization (locks, atomics, etc.) to avoid races.
-   Avoid starting the same `MyThread` instance directly (via `start()`) while it's also being used as a target---this would cause **concurrent invocations** of `run()` on the same object and is non-idiomatic.

### Rule of thumb
-   **Wrapper thread** → `new Thread(runnable)`; the wrapper executes `runnable.run()` and its **name** appears as `current`.
-   **Target instance** → the object whose `run()` is being executed; its **name** appears as `target`.
-   **This example** → two wrappers, **one shared target**; wrappers show up as `current`, shared target shows up as `this`/`target`.
