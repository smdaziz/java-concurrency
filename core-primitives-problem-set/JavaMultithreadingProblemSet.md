## 📘 Java Multithreading Problem Set (Core Primitives Only)

👉 Build intuition bottom-up with **raw primitives** (`Thread`, `Runnable`, `synchronized`, `wait/notify`, etc.), then layer complexity to see *why* the higher-level `java.util.concurrent` abstractions exist.

Let's set up a **professor-style curriculum** structured **problem set**. Each problem should be solved *in code* (not just theory), so you experience race conditions, deadlocks, and coordination issues first-hand.

<hr>

Here's a curated **sequence of 30 core questions** (grouped by theme, building one on another).
### Part 1. Threads & Runnable Basics
1.  [Write a program that creates two threads: one prints numbers from 1--50, the other prints letters from A--Z. Run them concurrently --- what interleaving do you see?](src/io/github/smdaziz/Problem1.java)
2.  [Modify the above so both threads must alternate strictly: `1 A 2 B 3 C ...`. (Hint: use `wait/notify`.)](src/io/github/smdaziz/Problem2.java)
3.  [Demonstrate the difference between calling `run()` directly vs `start()`. Why is one concurrent and the other not?](src/io/github/smdaziz/Problem3.java)
4.  Show the difference between [extending `Thread`](../thread-and-runnable/src/io/github/smdaziz/thread/creation/SubClassThread.java) and [implementing `Runnable`](../thread-and-runnable/src/io/github/smdaziz/thread/creation/RunnableThread.java). Which is preferred and why?

<hr>

### Part 2. Race Conditions
1.  [Recreate your unsynchronized counter (`MillionCounter`) with 3 threads incrementing. What final count do you observe vs expected?](../thread-and-runnable/src/io/github/smdaziz/thread/synchronization/CounterNoSyncThread.java)
2.  [Add `synchronized` to `run()`. Does the problem disappear? Why?](../thread-and-runnable/src/io/github/smdaziz/thread/synchronization/CounterSyncMethodThread.java)
3.  [Instead of synchronizing `run()`, try synchronizing just the `count++`. What happens if you synchronize on `this` vs on a static lock object?](../thread-and-runnable/src/io/github/smdaziz/thread/synchronization/CounterSyncBlockThread.java)
4.  Explain why `count++` is not atomic. (Demonstrate by disassembling to bytecode if curious.)

<hr>

### Part 3. Locks & Visibility
1.  Show a case where [one thread updates a boolean `flag`, but another thread loops forever](src/io/github/smdaziz/Problem4Demo.java) not seeing the change. [Fix it with `volatile`.](src/io/github/smdaziz/Problem4Sol.java)
2.  [Write a program where two threads increment a shared variable 1 million times each. Compare results with:](src/io/github/smdaziz/Problem10.java)
-   No synchronization
-   `synchronized`
-   `volatile` (why does volatile alone fail here?)

<hr>

### Part 4. Thread Coordination
1.  [Use `Thread.sleep` to simulate two workers. Show how sleep can lead to unpredictable ordering.](../thread-and-runnable/src/io/github/smdaziz/thread/ordering/CyclicBarrierThread.java)
2.  [Demonstrate `join()` by creating three threads that must finish before the main thread prints "Done."](../thread-and-runnable/src/io/github/smdaziz/thread/synchronization/BoundedBufferThread.java)
3.  [Modify above: main thread prints "Halfway" when the first two finish, then waits for the third.](../thread-and-runnable/src/io/github/smdaziz/thread/ordering/OrderedThreads.java)
4.  [Launch 5 threads. Make the main thread wait until **all** are done without `join()` (hint: use a shared counter + wait/notify).](src/io/github/smdaziz/Problem5.java)

<hr>

### Part 5. Producer--Consumer (Coordination via wait/notify)
1.  [Single-slot buffer (your code): producer produces, consumer consumes. What happens if you forget `notify()`?](src/io/github/smdaziz/Problem6.java)
2.  [Modify single-slot buffer: add a second consumer thread. What happens if you use `notify()` instead of `notifyAll()`?](src/io/github/smdaziz/Problem6Sol.java)
3.  [Extend to bounded buffer of size N (your `BoundedBuffer`). Show interleavings with random delays.](../thread-and-runnable/src/io/github/smdaziz/thread/synchronization/BoundedBufferThread.java)
4.  [Add multiple producers + multiple consumers to bounded buffer. Demonstrate potential issues with fairness.](src/io/github/smdaziz/Problem7.java)

<hr>

### Part 6. Deadlocks & Ordering
1.  [Write a program with 2 threads and 2 locks where each thread acquires locks in opposite order → deadlock.](../thread-and-runnable/src/io/github/smdaziz/thread/deadlock/DeadLockDemo.java)
2.  [Show how to avoid deadlock by always acquiring locks in the same order.](../thread-and-runnable/src/io/github/smdaziz/thread/deadlock/DeadLockSol.java)
3.  [Create a deadlock scenario with `wait()` (e.g., both threads waiting with no notify). Explain why it hangs.](../thread-and-runnable/src/io/github/smdaziz/thread/deadlock/NoNotifierDeadlockDemo.java)

<hr>

### Part 7. Advanced Synchronization Patterns
1.  [Implement a `Barrier` (all threads must reach a point before proceeding). Do it with `wait/notify`.](../thread-and-runnable/src/io/github/smdaziz/thread/ordering/CyclicBarrierThread.java)
2.  [Implement a `Latch` (main waits until N worker threads finish). No `CountDownLatch` allowed.](src/io/github/smdaziz/Problem8.java)
3.  [Implement a `PingPong` program where two threads print alternately "Ping" and "Pong" forever.](src/io/github/smdaziz/Problem9.java)
4.  [Create a `ReaderWriter` problem (1 writer, many readers). Ensure mutual exclusion correctly.](src/io/github/smdaziz/ReadersWritersProblem.java)
5.  [Simulate the **Dining Philosophers Problem** with 5 threads (sticks = locks). Demonstrate deadlock, then fix it.](../thread-and-runnable/src/io/github/smdaziz/thread/deadlock/diningphilosophers)

<hr>

### Part 8. Thread States & Lifecycle
1.  [Write code that prints each thread's state (`NEW`, `RUNNABLE`, `BLOCKED`, `WAITING`, etc.) during execution.](../thread-and-runnable/src/io/github/smdaziz/thread/lifecycle/ThreadStates.java)
2.  [Show how a thread transitions to `TIMED_WAITING` (via `sleep`) and `WAITING` (via `wait`).](../thread-and-runnable/src/io/github/smdaziz/thread/lifecycle/ThreadStates.java)
3.  [Demonstrate how `interrupt()` works on a sleeping thread vs a waiting thread.](../thread-and-runnable/src/io/github/smdaziz/thread/lifecycle/ThreadStates.java)
4.  [Safely stop a thread using a shared `volatile` flag instead of `stop()` (deprecated).](../thread-and-runnable/src/io/github/smdaziz/thread/destruction/ProgrammaticallyStopThreadsV2.java)

<hr>

👉 That's 30 carefully ordered questions. They'll take you from the very basics (Thread/Runnable) through synchronization, coordination, wait/notify, visibility, to deadlocks, barriers, and lifecycle states.

If you solve them all in code, you'll have effectively built your own **"mini java.util.concurrent"** from first principles.

