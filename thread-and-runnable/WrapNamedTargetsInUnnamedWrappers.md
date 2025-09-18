## Wrapping **named targets** in **unnamed wrappers**

```
public class io.github.smdaziz.thread.wrapping.WrapNamedTargetsInUnnamedWrappers {

    public static void main(String[] args) throws InterruptedException {
        Thread thread3 = new Thread(new io.github.smdaziz.thread.MyThread("Thread-io.github.smdaziz.thread.MyThread-3"));
        thread3.start();

        Thread thread4 = new Thread(new io.github.smdaziz.thread.MyThread("Thread-io.github.smdaziz.thread.MyThread-4"));
        thread4.start();
    }
}
```

### What it does
-   Constructs two **named** `io.github.smdaziz.thread.MyThread` instances: `Thread-io.github.smdaziz.thread.MyThread-3` and `Thread-io.github.smdaziz.thread.MyThread-4`.
-   Wraps each target in a **new, unnamed** `Thread` wrapper.
-   Starts the **wrappers**; each wrapper calls its target's `run()`.

### What you'll see (names vary; order may vary)
-   `current/wrapper=Thread-0`, `target=Thread-io.github.smdaziz.thread.MyThread-3`
-   `current/wrapper=Thread-1`, `target=Thread-io.github.smdaziz.thread.MyThread-4`

*(Wrapper names like `Thread-0`, `Thread-1`, `Thread-3`, ... are JVM-assigned defaults and can differ based on what else has been created.)*

### Why these names appear
-   `run()` executes **in the wrapper threads**, so the **current/wrapper** name is the wrapper's default JVM name (since you didn't set one).
-   The **target** name is the name you assigned to each `io.github.smdaziz.thread.MyThread` instance; it's preserved and printed as `Thread-io.github.smdaziz.thread.MyThread-3/4`.

### Runtime sequence
1.  Create target A: `io.github.smdaziz.thread.MyThread("Thread-io.github.smdaziz.thread.MyThread-3")`.
2.  Create wrapper W₁: `new Thread(targetA)` → unnamed, gets a default JVM name.
3.  `W₁.start()` → JVM creates an OS thread; `W₁.run()` calls `targetA.run()`.
4.  Repeat for target B (`"Thread-io.github.smdaziz.thread.MyThread-4"`) and wrapper W₂.
5.  The two wrappers run concurrently; their outputs interleave nondeterministically.

### Naming note
-   **Wrappers** without explicit names get **default** names (`Thread-N`).
-   **Targets** keep the names you set on the `io.github.smdaziz.thread.MyThread` instances.

### Thread-safety note
-   This is safe when the target is **stateless** (e.g., only prints names).
-   If `io.github.smdaziz.thread.MyThread` holds **mutable state**, it would be **shared** if the same instance were reused across wrappers; you'd need synchronization/atomics. (Here, each wrapper has its **own** target instance.)

### Rule of thumb
-   **Wrapper thread** → the `Thread` you start; its name shows up as **current/wrapper**.
-   **Target instance** → the object whose `run()` code is invoked; its assigned name shows up as **target**.
