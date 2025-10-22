## 📘 Java Multithreading Problem Set (Core Primitives Only)

👉 Build intuition bottom-up with **raw primitives** (`Thread`, `Runnable`, `synchronized`, `wait/notify`, etc.), then layer complexity to see *why* the higher-level `java.util.concurrent` abstractions exist.

Let's set up a **professor-style curriculum** structured **problem set**. Each problem should be solved *in code* (not just theory), so you experience race conditions, deadlocks, and coordination issues first-hand.

<hr>

Here's a curated **sequence of 30 core questions** (grouped by theme, building one on another).

### Part 1. Threads & Runnable Basics
1.  Write a program that creates two threads: one prints numbers from 1--50, the other prints letters from A--Z. Run them concurrently --- what interleaving do you see?
2.  Modify the above so both threads must alternate strictly: `1 A 2 B 3 C ...`. (Hint: use `wait/notify`.)
3.  Demonstrate the difference between calling `run()` directly vs `start()`. Why is one concurrent and the other not?
4.  Show the difference between extending `Thread` and implementing `Runnable`. Which is preferred and why?

<hr>

### Part 2. Race Conditions
1.  Recreate your unsynchronized counter (`MillionCounter`) with 2 threads incrementing. What final count do you observe vs expected?
2.  Add `synchronized` to `run()`. Does the problem disappear? Why?
3.  Instead of synchronizing `run()`, try synchronizing just the `count++`. What happens if you synchronize on `this` vs on a static lock object?
4.  Explain why `count++` is not atomic. (Demonstrate by disassembling to bytecode if curious.)

<hr>

### Part 3. Locks & Visibility
1.  Show a case where one thread updates a boolean `flag`, but another thread loops forever not seeing the change. Fix it with `volatile`.
2.  Write a program where two threads increment a shared variable 1 million times each. Compare results with:
-   No synchronization
-   `synchronized`
-   `volatile` (why does volatile alone fail here?)

<hr>

### Part 4. Thread Coordination
1.  Use `Thread.sleep` to simulate two workers. Show how sleep can lead to unpredictable ordering.
2.  Demonstrate `join()` by creating three threads that must finish before the main thread prints "Done."
3.  Modify above: main thread prints "Halfway" when the first two finish, then waits for the third.
4.  Launch 5 threads. Make the main thread wait until **all** are done without `join()` (hint: use a shared counter + wait/notify).

<hr>

### Part 5. Producer--Consumer (Coordination via wait/notify)
1.  Single-slot buffer (your code): producer produces, consumer consumes. What happens if you forget `notify()`?
2.  Modify single-slot buffer: add a second consumer thread. What happens if you use `notify()` instead of `notifyAll()`?
3.  Extend to bounded buffer of size N (your `BoundedBuffer`). Show interleavings with random delays.
4.  Add multiple producers + multiple consumers to bounded buffer. Demonstrate potential issues with fairness.

<hr>

### Part 6. Deadlocks & Ordering
1.  Write a program with 2 threads and 2 locks where each thread acquires locks in opposite order → deadlock.
2.  Show how to avoid deadlock by always acquiring locks in the same order.
3.  Create a deadlock scenario with `wait()` (e.g., both threads waiting with no notify). Explain why it hangs.

<hr>

### Part 7. Advanced Synchronization Patterns
1.  Implement a `Barrier` (all threads must reach a point before proceeding). Do it with `wait/notify`.
2.  Implement a `Latch` (main waits until N worker threads finish). No `CountDownLatch` allowed.
3.  Implement a `PingPong` program where two threads print alternately "Ping" and "Pong" forever.
4.  Create a `ReaderWriter` problem (1 writer, many readers). Ensure mutual exclusion correctly.
5.  Simulate the **Dining Philosophers Problem** with 5 threads (sticks = locks). Demonstrate deadlock, then fix it.

<hr>

### Part 8. Thread States & Lifecycle
1.  Write code that prints each thread's state (`NEW`, `RUNNABLE`, `BLOCKED`, `WAITING`, etc.) during execution.
2.  Show how a thread transitions to `TIMED_WAITING` (via `sleep`) and `WAITING` (via `wait`).
3.  Demonstrate how `interrupt()` works on a sleeping thread vs a waiting thread.
4.  Safely stop a thread using a shared `volatile` flag instead of `stop()` (deprecated).

<hr>

👉 That's 30 carefully ordered questions. They'll take you from the very basics (Thread/Runnable) through synchronization, coordination, wait/notify, visibility, to deadlocks, barriers, and lifecycle states.

If you solve them all in code, you'll have effectively built your own **"mini java.util.concurrent"** from first principles.

