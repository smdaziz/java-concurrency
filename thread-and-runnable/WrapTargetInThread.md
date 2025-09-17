## Wrapping a target in explicitly **named** wrappers

```
public class WrapTargetInThread {

    public static void main(String[] args) throws InterruptedException {
        Thread thread1 = new Thread(new MyThread(), "Thread-MyThread-1");
        thread1.start();

        Thread thread2 = new Thread(new MyThread(), "Thread-MyThread-2");
        thread2.start();
    }
}
```

### What it does
-   Creates two **wrapper** threads with explicit names: `Thread-MyThread-1` and `Thread-MyThread-2`.
-   Each wrapper is given a **target**: a new `MyThread()` instance (used purely as a `Runnable`, not started directly).

### What you'll see (order may vary)
-   `current/wrapper=Thread-MyThread-1`, `target=Thread-N`
-   `current/wrapper=Thread-MyThread-2`, `target=Thread-M`

*`Thread-N` / `Thread-M` are JVM-assigned default names for the **target** `MyThread` instances created without an explicit name.*

### Why these names appear
-   `run()` executes **in the wrapper threads**, so `current/wrapper` shows the **wrapper's explicit name**.
-   The **target** is a `MyThread` instance created with the default constructor; even though it isn't started, the `Thread` superclass assigns it a **default name** like `Thread-7`. That's why you see `target=Thread-N`.

### Runtime sequence
1.  Create target A: `new MyThread()` → receives a default JVM name (e.g., `Thread-7`).
2.  Create wrapper W₁: `new Thread(targetA, "Thread-MyThread-1")`.
3.  `W₁.start()` → JVM starts W₁ (the wrapper) and calls **W₁**'s `run()`, which invokes **targetA**'s `run()`.
4.  Repeat with target B and wrapper W₂ named `"Thread-MyThread-2"`.
5.  Outputs from W₁ and W₂ may interleave nondeterministically.

### Naming note
-   **Wrappers** use the explicit names you provide.
-   **Targets** created with no explicit name still have a JVM-assigned default name (`Thread-N`), even if they are never started.

### Thread-safety note
-   Safe as shown because each wrapper has its **own** target and the target is **stateless** (just prints names).
-   If the target carried **mutable shared state** and was reused across wrappers, you'd need proper synchronization (locks/atomics) to avoid races.

### Rule of thumb
-   **Wrapper thread** → the `Thread` you start; its name appears as `current/wrapper`.
-   **Target instance** → the object whose `run()` is invoked; its (possibly default) name appears as `target`.
