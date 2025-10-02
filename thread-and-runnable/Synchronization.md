## 🔒 Synchronization

### What is Synchronization in Java?

Let's think about it for a moment. Multiple threads updating the **same** thing sounds harmless... until it isn't. If two threads read the same value, increment it, and write back, one increment can **disappear**. That's the essence of a **race condition**.

**Does Java fix this automatically?**\
**No.** You, the programmer, must protect the **critical section** where shared state is read-modify-written.

### Lost updates without synchronization

```java
package io.github.smdaziz.thread.synchronization;

public class CounterNoSyncThread {
    public static void main(String[] args) throws InterruptedException {
        // Thread-unsafe counter
        // The goal is to increment the counter 3000000 times (1000000 times by each thread)
        MillionCounter millionCounter = new MillionCounter();

        Thread t1 = new Thread(millionCounter);
        Thread t2 = new Thread(millionCounter);
        Thread t3 = new Thread(millionCounter);

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        System.out.println("Final count (no synchronization): " + millionCounter.getCount());
    }
}

class MillionCounter implements Runnable {
    private int count = 0;

    public void run() {
        for (int i = 0; i < 1000000; i++) {
            count++;
        }
    }

    public int getCount() {
        return count;
    }
}
```

We launch three threads, all hammering on a shared counter. Intuitively, you expect the final number to be exactly the total increments.

**What actually happens:** the result is often **less** or sometimes **more** than expected.\
Why? `count++` isn't atomic --- it's read → add → write. Two threads can interleave and overwrite each other's updates.

**What it shows:**
-   **Race condition** in the wild.
-   "But it worked on my machine once!" --- yep, timing-dependent bugs are sneaky.

**Takeaways:**
-   A plain `int` (or `long`) plus `++` is not safe across threads.
-   `volatile` alone won't help here --- it gives **visibility**, not **atomicity**.
-   You need mutual exclusion (e.g., `synchronized`) or atomic utilities (e.g., `AtomicInteger`).

### Coarse-grained locking with a synchronized block

```java
package io.github.smdaziz.thread.synchronization;

public class CounterSyncBlockThread {
    public static void main(String[] args) throws InterruptedException {
        // Thread-unsafe counter
        // The goal is to increment the counter 3000000 times (1000000 times by each thread)
        MillionCounterV3 millionCounter = new MillionCounterV3();

        Thread t1 = new Thread(millionCounter);
        Thread t2 = new Thread(millionCounter);
        Thread t3 = new Thread(millionCounter);

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        System.out.println("Final count (no synchronization): " + millionCounter.getCount());
    }
}

class MillionCounterV3 implements Runnable {
    private int count = 0;

    public void run() {
        synchronized(this) {
            for (int i = 0; i < 1000000; i++) {
                count++;
            }
        }
    }

    public int getCount() {
        return count;
    }
}
```

We add a `synchronized (this)` around the whole increment loop.

**What it shows:**
-   The final count becomes **correct** and **deterministic**.
-   All three threads now **serialize** at that block --- only one runs the loop at a time. Correctness ↑, parallelism ↓.

**Takeaways:**
-   Coarse-grained locking is the easiest fix but can **eliminate concurrency**.
-   Keep the **critical section minimal**: protect just the shared mutation, not unrelated work.
-   (Minor note for your print line: the label says "no synchronization" --- you'll want to update that message to reflect that this version is synchronized.)

### Coarse-grained locking via a synchronized method

```java
package io.github.smdaziz.thread.synchronization;

public class CounterSyncMethodThread {
    public static void main(String[] args) throws InterruptedException {
        // Thread-unsafe counter
        // The goal is to increment the counter 3000000 times (1000000 times by each thread)
        MillionCounterV2 millionCounter = new MillionCounterV2();

        Thread t1 = new Thread(millionCounter);
        Thread t2 = new Thread(millionCounter);
        Thread t3 = new Thread(millionCounter);

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        System.out.println("Final count (no synchronization): " + millionCounter.getCount());
    }
}

class MillionCounterV2 implements Runnable {
    private int count = 0;

    public synchronized void run() {
        for (int i = 0; i < 1000000; i++) {
            count++;
        }
    }

    public int getCount() {
        return count;
    }
}
```

Same idea as Example 2, different syntax: mark the `run()` method `synchronized`.

**What it shows:**
-   Equivalent to Example 2: the entire method is a critical section.
-   Again: **correct** result, but threads run the loop one after another.

**Takeaways:**
-   Method-level `synchronized` is a clean, readable way to guard critical sections.
-   Still coarse-grained. If performance matters, **shrink** the lock's scope.
-   If you only need atomic increments, consider `AtomicInteger.incrementAndGet()` as a lock-free alternative.

### Producer/Consumer with a single-slot buffer

```java
package io.github.smdaziz.thread.synchronization;

public class SingleSlotBufferThread {

    public static void main(String[] args) {
        SingleSlotBuffer buffer = new SingleSlotBuffer();
        Thread producerThread = new Thread(new IntProducer(buffer), "ProducerThread");
        Thread consumerThread = new Thread(new IntConsumer(buffer), "ConsumerThread");

        producerThread.start();
        consumerThread.start();
    }

}

class IntProducer implements Runnable {

    private SingleSlotBuffer buffer;

    public IntProducer(SingleSlotBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= 10; i++) {
                System.out.println("Producing " + i);
                buffer.put(i);
                Thread.sleep(1000); // Simulate time taken to produce
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class IntConsumer implements Runnable {

    private SingleSlotBuffer buffer;

    public IntConsumer(SingleSlotBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= 10; i++) {
                int value = buffer.get();
                System.out.println("Consuming " + value);
                Thread.sleep(1500); // Simulate time taken to consume
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class SingleSlotBuffer {

    private Integer slot = null;

    public void put(int value) throws InterruptedException {
        if(slot != null) {
            this.wait();
        }
        slot = value;
        this.notify();
    }

    public int get() throws InterruptedException {
        if(slot == null) {
            this.wait();
        }
        int value = slot;
        slot = null;
        this.notify();
        return value;
    }

}
```

As much as it looks it should work, it actually doesn't! It crashes at runtime with an `IllegalMonitorStateException`.

#### What happens?

-   You'll likely see:
    -   `Producing 1`
    -   Then an exception like:
        `Exception in thread "ProducerThread" java.lang.IllegalMonitorStateException
            at java.lang.Object.notify(Native Method)
            at SingleSlotBuffer.put(...)`
    -   (If the consumer runs first, you'll instead get the same exception from `wait()` in `get()`.)

#### Why it happens
-   `wait()` and `notify()/notifyAll()` **must be called while holding the same monitor** you're waiting/notifying on.
-   In our `SingleSlotBuffer`, both `put()` and `get()` **are not synchronized**, so the calling thread does **not** own `this`'s monitor when it calls `wait()`/`notify()`.
-   The JVM throws `IllegalMonitorStateException` to enforce this rule.

```java
package io.github.smdaziz.thread.synchronization;

public class SingleSlotBufferThread {

    public static void main(String[] args) {
        SingleSlotBuffer buffer = new SingleSlotBuffer();
        Thread producerThread = new Thread(new IntProducer(buffer), "ProducerThread");
        Thread consumerThread = new Thread(new IntConsumer(buffer), "ConsumerThread");

        producerThread.start();
        consumerThread.start();
    }

}

class IntProducer implements Runnable {

    private SingleSlotBuffer buffer;

    public IntProducer(SingleSlotBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= 10; i++) {
                System.out.println("Producing " + i);
                buffer.put(i);
                Thread.sleep(1000); // Simulate time taken to produce
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class IntConsumer implements Runnable {

    private SingleSlotBuffer buffer;

    public IntConsumer(SingleSlotBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= 10; i++) {
                int value = buffer.get();
                System.out.println("Consuming " + value);
                Thread.sleep(1500); // Simulate time taken to consume
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class SingleSlotBuffer {

    private Integer slot = null;

    public synchronized void put(int value) throws InterruptedException {
        if(slot != null) {
            this.wait();
        }
        slot = value;
        this.notify();
    }

    public synchronized int get() throws InterruptedException {
        if(slot == null) {
            this.wait();
        }
        int value = slot;
        slot = null;
        this.notify();
        return value;
    }

}
```

We wire up one producer (puts integers 1..10) and one consumer (takes them). The buffer holds **one** item at a time.

**What it shows:**
-   **`wait()` / `notify()`** basics:
    -   When the buffer is **full**, producer calls `wait()` and releases the lock.
    -   When the buffer is **empty**, consumer calls `wait()` and releases the lock.
    -   After a successful `put` or `get`, the thread calls `notify()` to wake the other.
-   **Must be inside `synchronized`**: both `wait()` and `notify()` are monitor methods; calling them outside a synchronized context is illegal.

- **Important correctness notes (edge cases you can demo next):**
-   Use **`while`** instead of **`if`** around the wait conditions. Spurious wakeups are allowed; recheck the condition after waking up.
    -   Replace: `if (slot == null) wait();` with `while (slot == null) wait();` (and similarly for "buffer full").
-   `notify()` vs `notifyAll()`: with exactly **one** producer and **one** consumer, `notify()` is fine.
    -   If you ever extend to **multiple** producers/consumers, prefer `notifyAll()` to avoid **missed signals** / wrong thread waking.
-   The different `sleep()` timings (producer 1s, consumer 1.5s) let you **see blocking** in action: sometimes the producer must wait for the consumer to make room, sometimes the consumer waits for the producer to fill the slot.

**Takeaways:**
-   `wait()` **releases** the lock and suspends the thread; `notify()` wakes a waiter, but the woken thread still needs to **reacquire** the lock.
-   Always guard wait conditions with a **loop**.
-   For more complex flows, higher-level tools (`BlockingQueue`, `Condition`, `Semaphore`) are simpler and less error-prone.

