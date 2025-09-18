## Reusing the Same Target Across Two Wrapper Threads

```
public class io.github.smdaziz.thread.wrapping.ReuseSameTargetAcrossWrappers {

    public static void main(String[] args) throws InterruptedException {
        io.github.smdaziz.thread.MyThread myThread = new io.github.smdaziz.thread.MyThread("io.github.smdaziz.thread.MyThread");
        Thread t1 = new Thread(myThread, "io.github.smdaziz.thread.MyThread-Thread-1");
        t1.start();

        Thread t2 = new Thread(myThread, "io.github.smdaziz.thread.MyThread-Thread-2");
        t2.start();
    }
}
```

### What this does
-   Creates one `io.github.smdaziz.thread.MyThread` instance named `io.github.smdaziz.thread.MyThread` but does **not** start it directly.
-   Wraps that same instance in two **different** wrapper threads (`t1`, `t2`) with distinct names (`io.github.smdaziz.thread.MyThread-Thread-1` and `io.github.smdaziz.thread.MyThread-Thread-2`).
-   Starts both wrappers; each wrapper calls the **same** target's `run()`.

### What you'll see (order may vary)
-   `current/wrapper=io.github.smdaziz.thread.MyThread-Thread-1, target=io.github.smdaziz.thread.MyThread`
-   `current/wrapper=io.github.smdaziz.thread.MyThread-Thread-2, target=io.github.smdaziz.thread.MyThread`

*(Assuming `io.github.smdaziz.thread.MyThread.run()` prints `Thread.currentThread().getName()` as `current` and `this.getName()` as `target`.)*

### Why these names appear
-   `run()` is executed **in the wrapper threads**, so `current` shows the **wrapper's** name.
-   `target` refers to the **shared target instance** (`io.github.smdaziz.thread.MyThread`), so it prints the same target name for both lines.

### Runtime sequence
1.  Construct target: a single `io.github.smdaziz.thread.MyThread("io.github.smdaziz.thread.MyThread")` instance.
2.  Construct two wrappers: `new Thread(target, "io.github.smdaziz.thread.MyThread-Thread-1")` and `new Thread(target, "io.github.smdaziz.thread.MyThread-Thread-2")`.
3.  Start both wrappers. Each wrapper's `run()` invokes the **same** target's `run()` on the **same** object instance.
4.  The two wrapper threads run concurrently, interleaving prints nondeterministically.

### Thread-safety note (important)
-   Reusing the **same target** across multiple threads is fine **if the target is stateless** (only reads its own immutable state or prints names).
-   If `io.github.smdaziz.thread.MyThread` holds **mutable fields** (counters, collections, etc.), those fields are **shared** between both wrappers. You must add proper synchronization (locks, atomics, etc.) to avoid races.
-   Avoid starting the same `io.github.smdaziz.thread.MyThread` instance directly (via `start()`) while it's also being used as a target---this would cause **concurrent invocations** of `run()` on the same object and is non-idiomatic.

### Rule of thumb
-   **Wrapper thread** → `new Thread(runnable)`; the wrapper executes `runnable.run()` and its **name** appears as `current`.
-   **Target instance** → the object whose `run()` is being executed; its **name** appears as `target`.
-   **This example** → two wrappers, **one shared target**; wrappers show up as `current`, shared target shows up as `this`/`target`.
