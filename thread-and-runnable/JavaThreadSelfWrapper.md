## Wrapping a `Thread` instance as the target of another `Thread`

```
public class io.github.smdaziz.thread.wrapping.JavaThreadSelfWrapper {
    public static void main(String[] args) {
        Thread thread = new Thread(new Thread());
        thread.start();
    }
}
```

### What this does
-   Creates an **inner** `Thread` instance (`new Thread()`) and passes it as the **target** to an **outer** `Thread` (`new Thread(inner)`).
-   Calls `start()` on the **outer** thread only.

### Is it target or wrapper?
-   The **outer** `Thread` is the **wrapper** (it's the one you start).
-   The **inner** `Thread` is used as the **target `Runnable`** (because `Thread` implements `Runnable`) but it is **not started** itself.

### What you'll see (output)
-   **No output.**\
    The inner `Thread`'s default `run()` does nothing, and only the outer thread is started.

### Why
-   `Thread.run()` (outer) delegates to its target's `run()` if a target was provided.
-   The **inner** `Thread` has no target and does not override `run()`, so its default `run()` does nothing.
-   Therefore the outer thread starts, calls the inner's `run()`, and exits almost immediately.

### Runtime sequence

1.  `new Thread()` → creates the **inner** `Thread` object (receives a JVM default name like `Thread-0`).
2.  `new Thread(inner)` → creates the **outer** `Thread` object (default name like `Thread-1`).
3.  `thread.start()` → starts **only** the outer thread; JVM creates an OS thread and invokes the outer's `run()`.
4.  Outer `run()` calls `inner.run()` **synchronously**.
5.  Inner `run()` (default) does nothing → program ends quickly, with no visible output.

### Naming note
-   Both threads receive default JVM names when constructed (e.g., `Thread-0`, `Thread-1`), but **only the outer runs**.
-   The **inner** is never a running thread here; it's just used as a `Runnable` target.

### Why this pattern is unusual
-   Passing a `Thread` as the target of another `Thread` is **confusing** and rarely useful.
-   Prefer:
    -   passing a plain `Runnable` (or lambda) to `new Thread(runnable)`, or
    -   subclassing `Thread` and overriding `run()`.

### ✅ Rule of thumb
-   **Wrapper thread** → `new Thread(runnable)`; the wrapper executes `runnable.run()`.
-   **Target thread** → a `Thread` you actually **start** (e.g., a subclass with an overridden `run()`).
-   **This example** → the **outer** is the wrapper; the **inner** is a `Thread` used as a `Runnable` target that never starts.
