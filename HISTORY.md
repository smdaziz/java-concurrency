## Java Concurrency Timeline & Cheatsheet

A practical, skim-friendly history of how Java concurrency evolved and what you should use in each era.

------------

### TL;DR by era
- Pre--Java 5 (JDK 1.0--1.4): "Raw" threads, `synchronized`, `wait/notify`, `Timer`. Old memory model; lots of foot-guns.
- Java 5 (2004): The big bang. New Java Memory Model (JSR-133) + `java.util.concurrent` (executors, futures, locks, atomics, blocking queues...).
- Java 6--7: Refinements + Fork/Join, Phaser, better concurrent collections.
- Java 8: Lambdas, `CompletableFuture`, parallel streams, `LongAdder`, `StampedLock`.
- Java 9--17: `Flow` (Reactive Streams), VarHandles, `Thread.onSpinWait`, APIs maturing; LTS on 11/17.
- Java 19--21: Project Loom lands. Virtual threads (final in 21), structured concurrency (preview), scoped values (preview).

-------------------------

### Version map (at a glance)

| Version | Year | Headliners for concurrency |
| --- | --- | --- |
| 1.0--1.4 | 1996--2002 | Threads, `synchronized`, `wait/notify/notifyAll`, `ThreadLocal`, `Timer/TimerTask`, deprecated `stop/suspend/resume` |
| 5.0 | 2004 | New JMM (JSR-133); `Executor`/`ExecutorService`, `Callable`/`Future`, `ThreadFactory`, `ScheduledExecutorService`; `Lock`/`ReentrantLock`, `Condition`; `Semaphore`, `CountDownLatch`, `CyclicBarrier`, `Exchanger`; `Atomic*`; `BlockingQueue`; `ConcurrentHashMap` |
| 6 | 2006 | Concurrent skip-list map/set; performance/stability improvements across `java.util.concurrent` |
| 7 | 2011 | Fork/Join (`ForkJoinPool`, `RecursiveTask/Action`), `Phaser`, `ThreadLocalRandom`, `ConcurrentLinkedDeque`, `LinkedTransferQueue` |
| 8 | 2014 | Lambdas; `CompletableFuture` (+ async composition); parallel streams; `LongAdder/LongAccumulator`; `StampedLock` |
| 9 | 2017 | `Flow` (Reactive Streams API), VarHandles, `Thread.onSpinWait` |
| 11 | 2018 | LTS; HTTP Client (async, CF-based) helps ecosystem patterns |
| 17 | 2021 | LTS; steady improvements, baseline for many shops |
| 19--21 | 2022--2023 | Virtual Threads (preview → final in 21), Structured Concurrency (incubator/preview), Scoped Values (preview) |


-----------------------------------------------

### Pre--Java 5 (JDK 1.0--1.4): the "raw threads" era

- Create and start work with `new Thread(runnable).start()` or subclass `Thread`.
- Mutual exclusion via `synchronized` methods/blocks.
- Coordination via `wait/notify/notifyAll` (must hold the monitor; always use `while (!condition) wait()`).
- Visibility with `volatile` (but semantics were weaker pre-JSR-133).
- Scheduling with `Timer/TimerTask` (single thread; one bad task can kill/delay the timer).
- Dangerous APIs: `Thread.stop/suspend/resume` (deprecated and unsafe).
- Frequent pitfalls: stale reads, missed signals, broken double-checked locking, ad-hoc thread pools.

-----------------------------------------

### Java 5 (2004): modern concurrency arrives

- New Java Memory Model (JSR-133): clear happens-before rules; `volatile` fixed; safe publication patterns; DCL viable with `volatile`.
- Task execution: `Executor` / `ExecutorService`, `ThreadFactory`, `ScheduledExecutorService`.
- Return values & cancellation: `Callable<V>` + `Future<V>`.
- Locks & conditions: `Lock` / `ReentrantLock`, `Condition` (multiple wait sets per lock).
- Synchronizers: `Semaphore`, `CountDownLatch`, `CyclicBarrier`, `Exchanger`.
- Atomics: `AtomicInteger/Long/Reference`, `AtomicBoolean`, etc.
- Concurrent collections: `ConcurrentHashMap`; `BlockingQueue` family such as `ArrayBlockingQueue`, `LinkedBlockingQueue`, `PriorityBlockingQueue`, `SynchronousQueue`.

**Result:** you stop hand-rolling pools and queues; you use executors, futures, atomics, and robust collections.

--------------------------------

### Java 6--7: refinement + Fork/Join

- Java 6: wider set of concurrent collections (e.g., skip-list map/set), performance and correctness hardening.
- Java 7:
  - Fork/Join (`ForkJoinPool`, `RecursiveTask/Action`) for divide-and-conquer CPU workloads.
  - `Phaser` (flexible barrier across dynamic parties).
  - `ThreadLocalRandom` (fast RNG per thread).
  - `ConcurrentLinkedDeque`, `LinkedTransferQueue` (advanced non-blocking queues).
  - Try-with-resources helps manage shutdown (when you wrap executors or resources).

-----------------------------------------------

### Java 8: async composition and better primitives

- Lambdas enable succinct `Runnable`/`Callable` and async pipelines.
- `CompletableFuture` for powerful async composition (`thenCompose`, `allOf`, timeouts, exception handling). Provide your own executor for blocking I/O; avoid overwhelming the common pool.
- Parallel streams (use with care for CPU-bound operations).
- `LongAdder` / `LongAccumulator` outperform atomics under contention.
- `StampedLock` (read/write with optimistic read), useful when reads dominate.

--------------------------

### Java 9--17: platform polish

- `Flow` (Reactive Streams API), enabling back-pressure-aware libraries (RxJava, Reactor bridges).
- VarHandles (low-level, safer than `sun.misc.Unsafe` for advanced cases).
- `Thread.onSpinWait` (hint for spin loops).
- 11 & 17 are LTS releases many teams standardize on.

------------------------

### Java 19--21: Project Loom

- Virtual Threads: lightweight threads scheduled by the JVM, making "thread-per-task" simple and scalable (final in Java 21).
- Structured Concurrency (preview): treat a set of related tasks as a unit for cancellation, scoping, observability.
- Scoped Values (preview): safer, faster alternative to some `ThreadLocal` use-cases.

**Practical impact:** for I/O-heavy workloads, use virtual threads per task; fewer pools to tune, simpler reasoning.

-----------------------------------


### Before/After patterns (cheat sheet)

| Problem | Pre-5 / "Old way" | Modern way (5+) | Notes |
| --- | --- | --- | --- |
| Run some work | `new Thread(r).start()` | `ExecutorService.submit(r)`; Java 21: virtual threads | Prefer tasks over manual thread mgmt |
| Get a result | Hand-rolled callbacks/joins | `Callable` + `Future` / `CompletableFuture` | CF for composition, timeouts, errors |
| Repeated/Delayed task | `Timer/TimerTask` | `ScheduledExecutorService` | Multiple threads, robust to task failure |
| Producer/Consumer | `wait/notify` on list | `BlockingQueue` + worker pool | Simpler, safer, back-pressure |
| Shared counter | `synchronized` | `AtomicLong` or `LongAdder` | `LongAdder` under high contention |
| Read-mostly lock | `synchronized` everywhere | `ReentrantReadWriteLock` or `StampedLock` | StampedLock optimistic reads |
| Barriers/latches | Hand-rolled notifyAll | `CountDownLatch`, `CyclicBarrier`, `Phaser` | Clear semantics |
| Double-checked locking | Broken pre-5 | Use `volatile` or init-on-demand holder | JMM fixed in 5 |
| Cancellation | Flags; ad-hoc | `Future.cancel(true)`; respond to interrupts | Always propagate interrupt |

------------------------------

### Defaults to recommend (by era)

- Teaching basics: demonstrate `Thread` vs `Runnable`, naming, `join`, interrupt handling.
- Java 5--17: fixed pools for CPU, scheduled executors for timers, `CompletableFuture` for composition; avoid blocking the Fork/Join common pool.
- Java 21+: virtual threads per task for I/O/mixed workloads; keep CPU-bound work on small, fixed pools.
- Always: name threads (via `ThreadFactory`), set `UncaughtExceptionHandler`, honor interrupts, and shut down executors cleanly.

-------------------------------

### Footnotes & gotchas to remember

- Always guard condition waits with a while-loop (spurious wakeups).
- Visibility matters: without proper publication (`final` fields, `volatile`, happens-before), other threads can see stale state.
- `Thread.stop/suspend/resume` are unsafe and should not be used.
- `CompletableFuture`'s common pool is Fork/Join; don't block it with long I/O---supply your own executor.

----------------------------

### Where this fits in the repo
- Fundamentals in `thread-and-runnable` (what a thread is, names, wrapper vs target).
- `executors-and-futures` with executors, scheduled tasks, and `CompletableFuture`.
- `locks-and-atomics`, `barriers-and-synchronizers`, and `loom-virtual-threads` mirror the timeline with runnable, small examples.
