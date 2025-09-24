## When you do

```
Thread thread = new Thread();
thread.start();
```

- there is **no separate “target”** (`Runnable`) provided,
- the `Thread` instance itself is the **actual thread** that will run.

### So is it target or wrapper?
- it is **just a thread** — not a wrapper, not a separate target.
- in this case, the `Thread` object is _both_:
    - the thread being started (the “wrapper”), **and**
    - the runnable target (because `Thread` implements `Runnable` and its default `run()` does nothing).

### What happens at runtime
- `thread.start()` tells the JVM to create a new OS-level thread, and then call `thread.run()`.
- since you didn’t override `run()` or pass in a `Runnable`, nothing happens.

### Rule of thumb:
- **Wrapper thread** → when you create a new `Thread(runnable)` and the runnable supplies the `run()` code.
- **Target thread** → when the thread itself is the runnable (subclass of `Thread`, or default `Thread` with empty `run()`).
- **Your example** → `thread` is a plain thread with no target, so it’s best described as _just a thread object_ that runs but does nothing.

<hr>

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

<hr>

## Starting a `Thread` subclass directly

```
public class io.github.smdaziz.thread.wrapping.StartThreadSubclassDirect {

    public static void main(String[] args) throws InterruptedException {
        Thread myThread1 = new io.github.smdaziz.thread.MyThread("io.github.smdaziz.thread.MyThread-1");
        myThread1.start();

        Thread myThread2 = new io.github.smdaziz.thread.MyThread("io.github.smdaziz.thread.MyThread-2");
        myThread2.start();
    }
}
```

### What it does
-   Constructs two `io.github.smdaziz.thread.MyThread` instances with explicit names (`io.github.smdaziz.thread.MyThread-1` and `io.github.smdaziz.thread.MyThread-2`).
-   Starts each instance directly with `start()`.
-   Each `io.github.smdaziz.thread.MyThread` object **is its own running thread** (no wrapper thread involved).

### What you'll see (order may vary)
-   `current/wrapper=io.github.smdaziz.thread.MyThread-1, target=io.github.smdaziz.thread.MyThread-1`
-   `current/wrapper=io.github.smdaziz.thread.MyThread-2, target=io.github.smdaziz.thread.MyThread-2`

*(Assumes `io.github.smdaziz.thread.MyThread.run()` prints `Thread.currentThread().getName()` as `current` and `this.getName()` as `target`.)*

### Why these names appear
-   When you start a `Thread` subclass directly, the **instance itself** is the running thread.
-   Therefore, the "current" thread name and the instance's own name are the **same** for each line.

### Runtime sequence
1.  Construct `io.github.smdaziz.thread.MyThread("io.github.smdaziz.thread.MyThread-1")`.
2.  Call `start()` → JVM creates an OS thread and calls that instance's `run()`.
3.  Construct `io.github.smdaziz.thread.MyThread("io.github.smdaziz.thread.MyThread-2")` and `start()` it the same way.
4.  Both threads run concurrently; their outputs may interleave nondeterministically.

### Thread-safety note
-   These two threads are **separate instances**; they don't share instance fields.
-   Only shared/static state (if any) would require synchronization.

### Rule of thumb
-   **Starting a `Thread` subclass directly** makes the instance the running thread (simple, clear for demos).
-   In production, **prefer composition** (`new Thread(runnable)`) or, better, submit tasks to an **executor** (and on Java 21+, consider **virtual threads**).

<hr>

## Wrapping a target in explicitly **named** wrappers

```
public class io.github.smdaziz.thread.wrapping.WrapTargetInThread {

    public static void main(String[] args) throws InterruptedException {
        Thread thread1 = new Thread(new io.github.smdaziz.thread.MyThread(), "Thread-io.github.smdaziz.thread.MyThread-1");
        thread1.start();

        Thread thread2 = new Thread(new io.github.smdaziz.thread.MyThread(), "Thread-io.github.smdaziz.thread.MyThread-2");
        thread2.start();
    }
}
```

### What it does
-   Creates two **wrapper** threads with explicit names: `Thread-io.github.smdaziz.thread.MyThread-1` and `Thread-io.github.smdaziz.thread.MyThread-2`.
-   Each wrapper is given a **target**: a new `io.github.smdaziz.thread.MyThread()` instance (used purely as a `Runnable`, not started directly).

### What you'll see (order may vary)
-   `current/wrapper=Thread-io.github.smdaziz.thread.MyThread-1`, `target=Thread-N`
-   `current/wrapper=Thread-io.github.smdaziz.thread.MyThread-2`, `target=Thread-M`

*`Thread-N` / `Thread-M` are JVM-assigned default names for the **target** `io.github.smdaziz.thread.MyThread` instances created without an explicit name.*

### Why these names appear
-   `run()` executes **in the wrapper threads**, so `current/wrapper` shows the **wrapper's explicit name**.
-   The **target** is a `io.github.smdaziz.thread.MyThread` instance created with the default constructor; even though it isn't started, the `Thread` superclass assigns it a **default name** like `Thread-7`. That's why you see `target=Thread-N`.

### Runtime sequence
1.  Create target A: `new io.github.smdaziz.thread.MyThread()` → receives a default JVM name (e.g., `Thread-7`).
2.  Create wrapper W₁: `new Thread(targetA, "Thread-io.github.smdaziz.thread.MyThread-1")`.
3.  `W₁.start()` → JVM starts W₁ (the wrapper) and calls **W₁**'s `run()`, which invokes **targetA**'s `run()`.
4.  Repeat with target B and wrapper W₂ named `"Thread-io.github.smdaziz.thread.MyThread-2"`.
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

<hr>

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

<hr>

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
