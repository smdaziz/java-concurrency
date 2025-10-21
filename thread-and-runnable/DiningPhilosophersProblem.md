## 🍽️ Dining Philosophers --- Deadlock → Livelock → Working Solutions

### Why this problem matters

Five philosophers sit around a table; five chopsticks (one between each pair). Each philosopher repeatedly **thinks → picks up left chopstick → picks up right chopstick → eats → puts both down**.

Looks innocent. It's a perfect storm for classic concurrency bugs:
 - **Deadlock**: everyone holds one chopstick, waits forever for the other.
 - **Livelock**: everyone is "polite," constantly releasing/retrying, yet no one eats.
 - **Starvation**: some philosopher(s) never get to eat.

Let's walk through **the broken version** or **the problem itself**, then **try fixing the behavior**.

### ❌ The broken baseline (guaranteed deadlock)

Each philosopher locks left, then right---same order for all. With bad timing, they form a wait cycle.

```java
package io.github.smdaziz.thread.deadlock.diningphilosophers;

public class DiningPhilosophersDeadlock {
    public static void main(String[] args) {
        int totalPhilosophers = 5;
        int totalChopsticks = 5;
        Thread[] diningPhilosophers = new Thread[totalPhilosophers];
        Chopstick[] chopsticks = new Chopstick[totalChopsticks];
        for(int i = 0; i < totalChopsticks; i++) {
            chopsticks[i] = new Chopstick(i+1);
        }
        for(int i = 0; i < totalPhilosophers; i++) {
            Chopstick left = chopsticks[(i) % totalChopsticks];
            Chopstick right = chopsticks[(i+1) % totalChopsticks];
            Philosopher philosopher = new Philosopher(left, right);
            diningPhilosophers[i] = new Thread(philosopher, "Philosopher-"+(i+1));
        }
        for(Thread diningPhilosopher: diningPhilosophers) {
            diningPhilosopher.start();
        }
    }
}

class Chopstick {
    private int chopstickNumber;

    public Chopstick(int chopstickNumber) {
        this.chopstickNumber = chopstickNumber;
    }

    @Override
    public String toString() {
        return "Chopstick-"+chopstickNumber;
    }
}

class Philosopher implements Runnable {
    private Chopstick left;
    private Chopstick right;

    public Philosopher(Chopstick left, Chopstick right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void run() {
        String philosopher = Thread.currentThread().getName();
        while(true) {
            // Think
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // Try to Eat
            synchronized (left) {
                System.out.println(philosopher + " picked up " + left);
                synchronized (right) {
                    System.out.println(philosopher + " picked up " + right);
                    // Eat
                    System.out.println(philosopher + " is eating with " + left + " and " + right);
                }
                System.out.println(philosopher + " put down " + right);
            }
            System.out.println(philosopher + " put down " + left);
        }
    }
}
```

### What goes wrong

If all 5 pick up their **left** simultaneously, each waits on their **right** (which is someone else's left). No one can progress → **deadlock**. This satisfies all Coffman conditions: mutual exclusion, hold-and-wait, no preemption, **circular wait**.

**How to see it:** take a thread dump; you'll see each philosopher `BLOCKED` on the neighbor's chopstick.

<hr>

### ✅ Solution 1: **Global resource ordering** (a.k.a. hierarchy)

**Rule:** Always lock the lower-ID chopstick first, then the higher-ID one.\
This breaks the possibility of a cycle.

```java
package io.github.smdaziz.thread.deadlock.diningphilosophers;

    public class DiningPhilosophersDeadlockSol1 {
    public static void main(String[] args) {
        int totalPhilosophers = 5;
        int totalChopsticks = 5;
        Thread[] diningPhilosophers = new Thread[totalPhilosophers];
        ChopstickV1[] chopsticks = new ChopstickV1[totalChopsticks];
        for(int i = 0; i < totalChopsticks; i++) {
            chopsticks[i] = new ChopstickV1(i+1);
        }
        for(int i = 0; i < totalPhilosophers; i++) {
            ChopstickV1 left = chopsticks[(i) % totalChopsticks];
            ChopstickV1 right = chopsticks[(i+1) % totalChopsticks];
            PhilosopherV1 philosopher = new PhilosopherV1(left, right);
            diningPhilosophers[i] = new Thread(philosopher, "Philosopher-"+(i+1));
        }
        for(Thread diningPhilosopher: diningPhilosophers) {
            diningPhilosopher.start();
        }
    }
}

class ChopstickV1 {
    private int chopstickNumber;

    public ChopstickV1(int chopstickNumber) {
        this.chopstickNumber = chopstickNumber;
    }

    public int getChopstickNumber() {
        return chopstickNumber;
    }

    @Override
    public String toString() {
        return "Chopstick-"+chopstickNumber;
    }
}

class PhilosopherV1 implements Runnable {
    private ChopstickV1 left;
    private ChopstickV1 right;

    public PhilosopherV1(ChopstickV1 left, ChopstickV1 right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void run() {
        String philosopher = Thread.currentThread().getName();
        while(true) {
            // Think
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (left.getChopstickNumber() < right.getChopstickNumber()) {
                // Try to Eat
                synchronized (left) {
                    System.out.println(philosopher + " picked up " + left);
                    synchronized (right) {
                        System.out.println(philosopher + " picked up " + right);
                        // Eat
                        System.out.println(philosopher + " is eating with " + left + " and " + right);
                    }
                    System.out.println(philosopher + " put down " + right);
                }
                System.out.println(philosopher + " put down " + left);
            } else {
                // Try to Eat
                synchronized (right) {
                    System.out.println(philosopher + " picked up " + right);
                    synchronized (left) {
                        System.out.println(philosopher + " picked up " + left);
                        // Eat
                        System.out.println(philosopher + " is eating with " + right + " and " + left);
                    }
                    System.out.println(philosopher + " put down " + left);
                }
                System.out.println(philosopher + " put down " + right);
            }
        }
    }
}
```

**Why it works**

With a **total order** on locks, you cannot form a cycle: everyone climbs "uphill" (low → high). No circular wait, no deadlock.

Ensure lower chopstick number gets picked. This avoids circular wait deadlock because, in an attempt to pick up chopsticks, P4 (last philosopher) doesn't pick up P4 and hold

Let's notate Philosopher (Left-Chopstick, Right-Chopstick).

P0 (C0, C1), P1 (C1, C2), P2 (C2, C3), P3 (C3, C4), P4 (C4, C0)

Remember circular wait happens when each philosopher picks left chopstick and waits to pick right chopstick

| Philosopher | Chopstick Picked Up | Chopstick Waiting On |
|-------------|---------------------|----------------------|
| P0          | C0                  | C1                   |
| P1          | C1                  | C2                   |
| P2          | C2                  | C3                   |
| P3          | C3                  | C4                   |
| P4          | C4                  | C0                   |

In Circular wait situation, before the last philosopher's turn comes, here is the state

| Philosopher | Chopstick Picked Up | Chopstick Waiting On |
|-------------|---------------------|----------------------|
| P0          | C0                  | C1                   |
| P1          | C1                  | C2                   |
| P2          | C2                  | C3                   |
| P3          | C3                  | C4                   |

when its P4's turn, it attempts to pick right chopstick first, which is C0 and since C0 isn't available, it waits
this gives a chance to P3 to unblock because C4 is available and is not picked by P4

| Philosopher | Chopstick Picked Up | Chopstick Waiting On |
|-------------|---------------------|----------------------|
| P0          | C0                  | C1                   |
| P1          | C1                  | C2                   |
| P2          | C2                  | C3                   |
| P3          | C3                  | C4                   |
| P4          | None                | C0                   |

**Trade-offs:** simple, fast, no extra objects. You must maintain the ordering discipline everywhere these locks are used.

<hr>

✅ Solution 2: **Asymmetric acquisition** (odd/even flip)
--------------------------------------------------------

Make half the philosophers acquire in the opposite order (e.g., evens: left→right, odds: right→left).

```java
package io.github.smdaziz.thread.deadlock.diningphilosophers;

public class DiningPhilosophersDeadlockSol2 {
    public static void main(String[] args) {
        int totalPhilosophers = 5;
        int totalChopsticks = 5;
        Thread[] diningPhilosophers = new Thread[totalPhilosophers];
        ChopstickV2[] chopsticks = new ChopstickV2[totalChopsticks];
        for(int i = 0; i < totalChopsticks; i++) {
            chopsticks[i] = new ChopstickV2(i+1);
        }
        for(int i = 0; i < totalPhilosophers; i++) {
            ChopstickV2 left = chopsticks[(i) % totalChopsticks];
            ChopstickV2 right = chopsticks[(i+1) % totalChopsticks];
            PhilosopherV2 philosopher = new PhilosopherV2(i, left, right);
            diningPhilosophers[i] = new Thread(philosopher, "Philosopher-"+(i+1));
        }
        for(Thread diningPhilosopher: diningPhilosophers) {
            diningPhilosopher.start();
        }
    }
}

class ChopstickV2 {
    private int chopstickNumber;

    public ChopstickV2(int chopstickNumber) {
        this.chopstickNumber = chopstickNumber;
    }

    @Override
    public String toString() {
        return "Chopstick-"+chopstickNumber;
    }
}

class PhilosopherV2 implements Runnable {
    private int index;
    private ChopstickV2 left;
    private ChopstickV2 right;

    public PhilosopherV2(int index, ChopstickV2 left, ChopstickV2 right) {
        this.index = index;
        this.left = left;
        this.right = right;
    }

    @Override
    public void run() {
        String philosopher = Thread.currentThread().getName();
        while(true) {
            // Think
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // Let even philosophers pick left first
            if (index%2 == 0) {
                // Try to Eat
                synchronized (left) {
                    System.out.println(philosopher + " picked up " + left);
                    synchronized (right) {
                        System.out.println(philosopher + " picked up " + right);
                        // Eat
                        System.out.println(philosopher + " is eating with " + left + " and " + right);
                    }
                    System.out.println(philosopher + " put down " + right);
                }
                System.out.println(philosopher + " put down " + left);
            } else {
                // Let odd philosophers pick right first
                // Try to Eat
                synchronized (right) {
                    System.out.println(philosopher + " picked up " + right);
                    synchronized (left) {
                        System.out.println(philosopher + " picked up " + left);
                        // Eat
                        System.out.println(philosopher + " is eating with " + right + " and " + left);
                    }
                    System.out.println(philosopher + " put down " + left);
                }
                System.out.println(philosopher + " put down " + right);
            }
        }
    }
}
```

**Why it works**

**Goal:** Break circular-wait by making neighbors acquire in opposite orders.

**Policy:** Even-index philosophers pick LEFT first; odd-index philosophers pick RIGHT first.

Intuition with 5 philosophers (P0..P4) and chopsticks (C0..C4):
   Left/Right mapping (same as usual): Pi needs {Ci, C(i+1)}.

What deadlocks in the classic case:
 - Everyone grabs LEFT then waits for RIGHT → P0 holds C0→C1, P1 holds C1→C2, ..., P4 holds C4→C0.
 - All five hold one stick and wait for the next in a perfect cycle.

What happens with our alternating policy when all start together:
 - P0 (even) grabs LEFT = C0, then tries RIGHT = C1.
 - P1 (odd) grabs RIGHT = C2, then tries LEFT = C1.
 - P2 (even) tries to grab LEFT = C2 but **blocks immediately** (P1 already has C2), so P2 holds nothing.
 - P3 (odd) grabs RIGHT = C4, then tries LEFT = C3.
 - P4 (even) tries to grab LEFT = C4 but **blocks immediately** (P3 already has C4), so P4 holds nothing.

**Key observation:**
At least one philosopher on each side (here P2 and P4) fails on the **first** pickup and holds nothing. That prevents the “everyone holds exactly one” configuration required for deadlock. 
Since the ring cannot reach the state where all five hold one and wait for the other, a circular wait cannot form. Eventually, someone who holds one stick acquires the second and eats, releasing progress for others.

**Trade-off:**
 - This removes deadlock but not theoretical starvation (an unlucky philosopher could keep losing).
 - Random think/eat delays or a host/arbitrator can improve fairness if needed.

The asymmetric pattern **usually** prevents *all* of them from forming a single directed cycle. It's a practical trick for this topology.

**Caveat:** It's tailored to a ring with two resources per actor. Not a general solution for arbitrary graphs.

<hr>

### ✅ Solution 3: Using locking and synchronization primitives `wait` and `notifyAll`

```java
package io.github.smdaziz.thread.deadlock.diningphilosophers;
public class DiningPhilosophersDeadlockSol3V1 {
    public static void main(String[] args) {
        int totalPhilosophers = 5;
        int totalChopsticks = 5;
        Thread[] diningPhilosophers = new Thread[totalPhilosophers];
        ChopstickSol3V1[] chopsticks = new ChopstickSol3V1[totalChopsticks];
        for(int i = 0; i < totalChopsticks; i++) {
            chopsticks[i] = new ChopstickSol3V1(i+1);
        }
        for(int i = 0; i < totalPhilosophers; i++) {
            ChopstickSol3V1 left = chopsticks[(i) % totalChopsticks];
            ChopstickSol3V1 right = chopsticks[(i+1) % totalChopsticks];
            PhilosopherSol3V1 philosopher = new PhilosopherSol3V1(left, right);
            diningPhilosophers[i] = new Thread(philosopher, "Philosopher-"+(i+1));
        }
        for(Thread diningPhilosopher: diningPhilosophers) {
            diningPhilosopher.start();
        }
    }
}

class ChopstickSol3V1 {
    private int chopstickNumber;
    private boolean isAvailable = true;

    public ChopstickSol3V1(int chopstickNumber) {
        this.chopstickNumber = chopstickNumber;
    }

    public boolean isAvailable() {
        return this.isAvailable;
    }

    public synchronized void grab() {
        this.isAvailable = false;
    }

    public synchronized void release() {
        this.isAvailable = true;
    }

    @Override
    public String toString() {
        return "Chopstick-"+chopstickNumber;
    }
}

class PhilosopherSol3V1 implements Runnable {
    private ChopstickSol3V1 left;
    private ChopstickSol3V1 right;

    public PhilosopherSol3V1(ChopstickSol3V1 left, ChopstickSol3V1 right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void run() {
        String philosopher = Thread.currentThread().getName();
        while(true) {
            try {
                // Think
                Thread.sleep(1000);
                // Try to Eat
                while(!left.isAvailable() || !right.isAvailable()) {
                    if (left.isAvailable()) {
                        left.grab();
                    } else {
                        left.wait();
                    }

                    if (right.isAvailable()) {
                        right.grab();
                    } else {
                        right.wait();
                    }
                }
                synchronized (left) {
                    System.out.println(philosopher + " picked up " + left);
                    synchronized (right) {
                        System.out.println(philosopher + " picked up " + right);
                        // Eat
                        System.out.println(philosopher + " is eating with " + left + " and " + right);
                        right.release();
                        right.notifyAll();
                    }
                    System.out.println(philosopher + " put down " + right);
                    left.release();
                    left.notifyAll();
                }
                System.out.println(philosopher + " put down " + left);
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

**Does it solve the problem?** No, the solution isn't right and can still deadlock

We’re close conceptually, but this version still has several show-stoppers:

We call `left.wait()` / `right.wait()` without holding those monitors → `IllegalMonitorStateException`.

`wait`/`notify` must be inside `synchronized(theSameObject)`.

**Data race / visibility:**

`isAvailable()` reads the flag without any synchronization or volatile, while `grab`/`release` write it under `synchronized`.
That’s not a happens-before; readers may see stale values.

**TOC/TOU (Time-of-check to time-of-use):**

We check availability, then later call `grab()`. Another thread can slip in between.
The check and state change need to be atomic.

**Deadlock still possible:**

Even if we fix the above, each philosopher still tries `left` then `right`.
All five can grab their `left` and wait on `right` → circular wait.

**_Let's try to improve it_**

```java
package io.github.smdaziz.thread.deadlock.diningphilosophers;
public class DiningPhilosophersDeadlockSol3V2 {
    public static void main(String[] args) {
        int totalPhilosophers = 5;
        int totalChopsticks = 5;
        Thread[] diningPhilosophers = new Thread[totalPhilosophers];
        ChopstickSol3V2[] chopsticks = new ChopstickSol3V2[totalChopsticks];
        for(int i = 0; i < totalChopsticks; i++) {
            chopsticks[i] = new ChopstickSol3V2(i+1);
        }
        for(int i = 0; i < totalPhilosophers; i++) {
            ChopstickSol3V2 left = chopsticks[(i) % totalChopsticks];
            ChopstickSol3V2 right = chopsticks[(i+1) % totalChopsticks];
            PhilosopherSol3V2 philosopher = new PhilosopherSol3V2(left, right);
            diningPhilosophers[i] = new Thread(philosopher, "Philosopher-"+(i+1));
        }
        for(Thread diningPhilosopher: diningPhilosophers) {
            diningPhilosopher.start();
        }
    }
}

class ChopstickSol3V2 {
    private int chopstickNumber;
    private boolean isAvailable = true;

    public ChopstickSol3V2(int chopstickNumber) {
        this.chopstickNumber = chopstickNumber;
    }

    public synchronized boolean isAvailable() {
        return this.isAvailable;
    }

    public synchronized void grab() {
        this.isAvailable = false;
    }

    public synchronized void release() {
        this.isAvailable = true;
    }

    @Override
    public String toString() {
        return "Chopstick-"+chopstickNumber;
    }
}

class PhilosopherSol3V2 implements Runnable {
    private ChopstickSol3V2 left;
    private ChopstickSol3V2 right;

    public PhilosopherSol3V2(ChopstickSol3V2 left, ChopstickSol3V2 right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void run() {
        String philosopher = Thread.currentThread().getName();
        while(true) {
            try {
                // Think
                Thread.sleep(1000);
                // Try to Eat
                while(!left.isAvailable() || !right.isAvailable()) {
                    if (left.isAvailable()) {
                        left.grab();
                    } else {
                        left.wait();
                    }

                    if (right.isAvailable()) {
                        right.grab();
                    } else {
                        right.wait();
                    }
                }
                synchronized (left) {
                    System.out.println(philosopher + " picked up " + left);
                    synchronized (right) {
                        System.out.println(philosopher + " picked up " + right);
                        // Eat
                        System.out.println(philosopher + " is eating with " + left + " and " + right);
                        right.release();
                        right.notifyAll();
                    }
                    System.out.println(philosopher + " put down " + right);
                    left.release();
                    left.notifyAll();
                }
                System.out.println(philosopher + " put down " + left);
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

**Does it solve the problem?** No, the solution is still not accurate

**What’s still wrong**

We call `left.wait()` / `right.wait()` without holding those monitors → `IllegalMonitorStateException`.
`wait`/`notify` must be inside `synchronized(theSameObject)`.

The loop condition is `while (!left.isAvailable() || !right.isAvailable())` then it tries to `grab()` whichever is available.
That’s a split check→act across two objects; another philosopher can grab in between (TOC/TOU race).

If both are available, the loop is skipped, and it never calls `grab()` at all—the flags and the actual locking diverge.

Even if we fix waits, this still permits the classic circular wait: everyone can grab their `left` and block on `right`.

**_Let's try to improve it further_**

```java
package io.github.smdaziz.thread.deadlock.diningphilosophers;
public class DiningPhilosophersDeadlockSol3V3 {
    public static void main(String[] args) {
        int totalPhilosophers = 5;
        int totalChopsticks = 5;
        Thread[] diningPhilosophers = new Thread[totalPhilosophers];
        ChopstickSol3V3[] chopsticks = new ChopstickSol3V3[totalChopsticks];
        for(int i = 0; i < totalChopsticks; i++) {
            chopsticks[i] = new ChopstickSol3V3(i+1);
        }
        for(int i = 0; i < totalPhilosophers; i++) {
            ChopstickSol3V3 left = chopsticks[(i) % totalChopsticks];
            ChopstickSol3V3 right = chopsticks[(i+1) % totalChopsticks];
            PhilosopherSol3V3 philosopher = new PhilosopherSol3V3(left, right);
            diningPhilosophers[i] = new Thread(philosopher, "Philosopher-"+(i+1));
        }
        for(Thread diningPhilosopher: diningPhilosophers) {
            diningPhilosopher.start();
        }
    }
}

class ChopstickSol3V3 {
    private int chopstickNumber;
    private boolean isAvailable = true;

    public ChopstickSol3V3(int chopstickNumber) {
        this.chopstickNumber = chopstickNumber;
    }

    public boolean isAvailable() {
        return this.isAvailable;
    }

    public void grab() {
        this.isAvailable = false;
    }

    public void release() {
        this.isAvailable = true;
    }

    @Override
    public String toString() {
        return "Chopstick-"+chopstickNumber;
    }
}

class PhilosopherSol3V3 implements Runnable {
    private ChopstickSol3V3 left;
    private ChopstickSol3V3 right;

    public PhilosopherSol3V3(ChopstickSol3V3 left, ChopstickSol3V3 right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void run() {
        String philosopher = Thread.currentThread().getName();
        while(true) {
            try {
                // Think
                Thread.sleep(1000);
                // Try to Eat
                synchronized (left) {
                    synchronized (right) {
                        while(!left.isAvailable() || !right.isAvailable()) {
                            if (left.isAvailable()) {
                                left.grab();
                                System.out.println(philosopher + " picked up " + left);
                            } else {
                                left.wait();
                            }

                            if (right.isAvailable()) {
                                right.grab();
                                System.out.println(philosopher + " picked up " + right);
                            } else {
                                right.wait();
                            }
                        }
                        // Eat
                        System.out.println(philosopher + " is eating with " + left + " and " + right);
                        right.release();
                        right.notifyAll();
                    }
                    System.out.println(philosopher + " put down " + right);
                    left.release();
                    left.notifyAll();
                }
                System.out.println(philosopher + " put down " + left);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

**Does it solve the problem?** No, the solution is still incorrect

Here's what's *specifically* wrong with _above_ code:

1) It can deadlock **before** `while` runs

```java
synchronized (left) {
  synchronized (right) {
    while (!left.isAvailable() || !right.isAvailable()) { ... }
```

All philosophers take `left` first, then try to enter `synchronized (right)`. That's the classic circular-wait: each holds their left and blocks trying to enter their right. You never even reach the `while` in that deadlocked run.

2) We `wait()` on one chopstick while still holding the other

Inside the nested region:

```java
left.wait();   // while still holding 'right'
right.wait();  // while still holding 'left'
```

`wait()` releases **only** the monitor you call it on. So when we `left.wait()`, we still hold `right`. The neighbor might need `right` to progress and eventually `notify` on `left`, but can't get it---this wedges the system. Never `wait()` on A while holding B (unless you've broken circular wait with a strict global order, see below).

3) **Logic bug:** the `while` condition skips grabbing when both are free

The loop is:

```java
while (!left.isAvailable() || !right.isAvailable()) { ... }
```
-   If **both are available**, condition is **false**, we skip the loop, **never call `grab()`**, and then "eat."\
    We are holding the monitors, but the `isAvailable` flags still say *true*. That means other threads may think the sticks are free and make bad decisions.

4) TOC/TOU across two resources

We do "check → (maybe) `grab()`" for each stick, but other threads can interleave between the checks because there are **two** monitors in play. For this problem, "_check/wait/mark-taken_" must be **atomic per chopstick**.

5) Visibility discipline on `isAvailable`

Here it happens to be *read/modified while the corresponding monitor is held*, which is okay **only if every single access everywhere follows the same rule**. In this class it does, but this pattern is fragile.

Better to encapsulate the wait+state change inside the chopstick.

**_Let's continue refining_**

A quick recap of Java Memory Model concepts with regards to multi-threading
 - `synchronized (x) { x.someMethod(); }` and `x.someMethod()` where `someMethod` is **`synchronized` (instance)** are **equivalent only if** both lock the **same monitor (`x`)**.
   - A `synchronized` instance method locks **`this`**. So they're equivalent iff `x == this` inside `someMethod`.
 - You **don't** have to mark a method `synchronized` if you always call it **while holding the same lock** that guards the state. But then **every** access everywhere must obey that rule.
 - Visibility/happens-before comes from **monitor enter/exit** on the **same monitor**. If a reader looks at a field **without** entering that monitor (and the writer wrote it **with** the monitor), the reader can see **stale** values --- even if the field was written in a `synchronized` block.

**Why our earlier code has a visibility bug**

```java
class Chopstick {
  private boolean available = true;
  synchronized void grab() { available = false; }
  synchronized void release() { available = true; }
  boolean isAvailable() { return available; } // not synchronized
}
```

If a reader calls `isAvailable()` **without** holding the same monitor, there's **no happens-before** from the writer's `grab()/release()` to that read. Result: the reader may see an old value. Making `grab/release` synchronized is not enough; the **read** must also participate in the same synchronization (or the field must be `volatile`, but see below).

**Atomicity (check-then-act) vs. visibility**
 - `volatile` only gives **visibility** for single reads/writes; it does **not** make "check then set" atomic and cannot be used with `wait/notify`.
 - For conditions like "wait until available, then take," you need **both** visibility **and** atomicity → use the **same monitor** for:
   - reading the condition,
   - deciding to wait,
   - waiting (`wait()`),
   - and updating the state.

```java
package io.github.smdaziz.thread.deadlock.diningphilosophers;
public class DiningPhilosophersDeadlockSol3V4 {
    public static void main(String[] args) {
        int totalPhilosophers = 5;
        int totalChopsticks = 5;
        Thread[] diningPhilosophers = new Thread[totalPhilosophers];
        ChopstickSol3V4[] chopsticks = new ChopstickSol3V4[totalChopsticks];
        for(int i = 0; i < totalChopsticks; i++) {
            chopsticks[i] = new ChopstickSol3V4(i+1);
        }
        for(int i = 0; i < totalPhilosophers; i++) {
            ChopstickSol3V4 left = chopsticks[(i) % totalChopsticks];
            ChopstickSol3V4 right = chopsticks[(i+1) % totalChopsticks];
            PhilosopherSol3V4 philosopher = new PhilosopherSol3V4(left, right);
            diningPhilosophers[i] = new Thread(philosopher, "Philosopher-"+(i+1));
        }
        for(Thread diningPhilosopher: diningPhilosophers) {
            diningPhilosopher.start();
        }
    }
}

class ChopstickSol3V4 {
    private int chopstickNumber;
    private boolean isAvailable = true;

    public ChopstickSol3V4(int chopstickNumber) {
        this.chopstickNumber = chopstickNumber;
    }

    public synchronized void grab() {
        while (!this.isAvailable) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        this.isAvailable = false;
    }

    public synchronized void release() {
        this.isAvailable = true;
        this.notifyAll();
    }

    @Override
    public String toString() {
        return "Chopstick-"+chopstickNumber;
    }
}

class PhilosopherSol3V4 implements Runnable {
    private ChopstickSol3V4 left;
    private ChopstickSol3V4 right;

    public PhilosopherSol3V4(ChopstickSol3V4 left, ChopstickSol3V4 right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void run() {
        String philosopher = Thread.currentThread().getName();
        while(true) {
            try {
                // Think
                Thread.sleep(1000);
                // Try to Eat
                left.grab();
                System.out.println(philosopher + " picked up " + left);
                right.grab();
                System.out.println(philosopher + " picked up " + right);
                // Eat
                System.out.println(philosopher + " is eating with " + left + " and " + right);
                right.release();
                System.out.println(philosopher + " put down " + right);
                left.release();
                System.out.println(philosopher + " put down " + left);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

**Does it solve the problem?** No! This version fixes the “wait without the monitor” issue in `DiningPhilosophersDeadlockSol3V3` by encapsulating `wait`/`notifyAll` inside `grab()`/`release()`.

The remaining blocker is the classic deadlock:
 - every philosopher does `left.grab()` then `right.grab()`.
 - If all five grab their `left` chopstick, each will block forever on their `right` → circular wait.

**_Ok! Let's address all of those concerns_**

```java
package io.github.smdaziz.thread.deadlock.diningphilosophers;
public class DiningPhilosophersDeadlockSol3V5 {
    public static void main(String[] args) {
        int totalPhilosophers = 5;
        int totalChopsticks = 5;
        Thread[] diningPhilosophers = new Thread[totalPhilosophers];
        ChopstickSol3V5[] chopsticks = new ChopstickSol3V5[totalChopsticks];
        for(int i = 0; i < totalChopsticks; i++) {
            chopsticks[i] = new ChopstickSol3V5(i+1);
        }
        for(int i = 0; i < totalPhilosophers; i++) {
            ChopstickSol3V5 left = chopsticks[(i) % totalChopsticks];
            ChopstickSol3V5 right = chopsticks[(i+1) % totalChopsticks];
            PhilosopherSol3V5 philosopher = new PhilosopherSol3V5(left, right);
            diningPhilosophers[i] = new Thread(philosopher, "Philosopher-"+(i+1));
        }
        for(Thread diningPhilosopher: diningPhilosophers) {
            diningPhilosopher.start();
        }
    }
}

class ChopstickSol3V5 {
    private int chopstickNumber;
    private boolean isAvailable = true;

    public ChopstickSol3V5(int chopstickNumber) {
        this.chopstickNumber = chopstickNumber;
    }

    public synchronized void grab() {
        while (!this.isAvailable) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        this.isAvailable = false;
    }

    public synchronized void release() {
        this.isAvailable = true;
        this.notifyAll();
    }

    public synchronized boolean isAvailable() {
        return isAvailable;
    }

    @Override
    public String toString() {
        return "Chopstick-"+chopstickNumber;
    }
}

class PhilosopherSol3V5 implements Runnable {
    private ChopstickSol3V5 left;
    private ChopstickSol3V5 right;

    public PhilosopherSol3V5(ChopstickSol3V5 left, ChopstickSol3V5 right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void run() {
        String philosopher = Thread.currentThread().getName();
        while(true) {
            try {
                // Think
                Thread.sleep(1000);
                // Try to Eat
                left.grab();
                System.out.println(philosopher + " picked up " + left);
                if (right.isAvailable()) {
                    right.grab();
                    System.out.println(philosopher + " picked up " + right);
                } else {
                    left.release();
                    System.out.println(philosopher + " put down " + left);
                    continue;
                }
                // Eat
                System.out.println(philosopher + " is eating with " + left + " and " + right);
                right.release();
                System.out.println(philosopher + " put down " + right);
                left.release();
                System.out.println(philosopher + " put down " + left);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

**Does it solve the problem?** No! As much as it seems to work, it could still deadlock

Here's a possible situation when that could happen
```java
    if (right.isAvailable()) {
        right.grab();
```
Between the `isAvailable()` check and `right.grab()`, a neighbor can take `right`.

Then `right.grab()` waits while you’re still holding `left` → everyone grabs their `left` and waits on `right` → circular wait (deadlock).

**_How about this?_**

```java
package io.github.smdaziz.thread.deadlock.diningphilosophers;
public class DiningPhilosophersDeadlockSol3V6 {
    public static void main(String[] args) {
        int totalPhilosophers = 5;
        int totalChopsticks = 5;
        Thread[] diningPhilosophers = new Thread[totalPhilosophers];
        ChopstickSol3V6[] chopsticks = new ChopstickSol3V6[totalChopsticks];
        for(int i = 0; i < totalChopsticks; i++) {
            chopsticks[i] = new ChopstickSol3V6(i+1);
        }
        for(int i = 0; i < totalPhilosophers; i++) {
            ChopstickSol3V6 left = chopsticks[(i) % totalChopsticks];
            ChopstickSol3V6 right = chopsticks[(i+1) % totalChopsticks];
            PhilosopherSol3V6 philosopher = new PhilosopherSol3V6(left, right);
            diningPhilosophers[i] = new Thread(philosopher, "Philosopher-"+(i+1));
        }
        for(Thread diningPhilosopher: diningPhilosophers) {
            diningPhilosopher.start();
        }
    }
}

class ChopstickSol3V6 {
    private int chopstickNumber;
    private boolean isAvailable = true;

    public ChopstickSol3V6(int chopstickNumber) {
        this.chopstickNumber = chopstickNumber;
    }

    public synchronized void grab() {
        while (!this.isAvailable) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        this.isAvailable = false;
    }

    public synchronized boolean tryGrab() {
        if (!isAvailable) {
            return false;
        }
        isAvailable = false;
        return true;
    }

    public synchronized void release() {
        this.isAvailable = true;
        this.notifyAll();
    }

    @Override
    public String toString() {
        return "Chopstick-"+chopstickNumber;
    }
}

class PhilosopherSol3V6 implements Runnable {
    private ChopstickSol3V6 left;
    private ChopstickSol3V6 right;

    public PhilosopherSol3V6(ChopstickSol3V6 left, ChopstickSol3V6 right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void run() {
        String philosopher = Thread.currentThread().getName();
        while(true) {
            try {
                // Think
                Thread.sleep(1000);
                // Try to Eat
                left.grab();
                System.out.println(philosopher + " picked up " + left);
                if (right.tryGrab()) {
                    System.out.println(philosopher + " picked up " + right);
                } else {
                    left.release();
                    System.out.println(philosopher + " put down " + left);
                    continue;
                }
                // Eat
                System.out.println(philosopher + " is eating with " + left + " and " + right);
                right.release();
                System.out.println(philosopher + " put down " + right);
                left.release();
                System.out.println(philosopher + " put down " + left);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

**Does it solve the problem?** **Yes**

We’ve broken hold-and-wait by releasing the first stick if you can’t get the second, so the deadlock is gone

In other words, this solution works because grabbing the `right` chopstick is an atomic operation

<hr>

### 🧠 Lessons learned (and the pitfalls we ran into)
- **Call `wait/notifyAll` only while holding that object's monitor.**\
  _Otherwise:_ `IllegalMonitorStateException`.
- **Use `while` around waits.**\
  Spurious wakeups are allowed; re-check the condition.
- **Synchronized method vs `synchronized (obj)` block:**\
  They're equivalent **iff** the monitor is the same `obj`. E.g., `public synchronized void m()` locks on `this`.\
  `synchronized (this) { m(); }` is redundant; `synchronized (other) { m(); }` is **not** the same.
- **Visibility is tied to the monitor.**\
  `isAvailable` must be read and written **inside the same synchronized methods** to avoid stale reads. Declaring it `volatile` is an alternative, but monitor discipline already gives you the happens-before you need.
- **`notifyAll` vs `notify`:**\
  With multiple possible waiters (many philosophers), prefer **`notifyAll()`** to avoid waking the "wrong" waiter and deadlocking by accident.
- **Deadlock vs Livelock vs Starvation:**
   - Deadlock: nobody moves (everyone waiting).
   - Livelock: everybody moves (retries) but no progress.
   - Starvation: some never progress (e.g., writer-starvation in reader-biased locks).\
        Our **try+backoff** eliminates deadlock; the backoff addresses livelock.
- **Debugging tip:**\
   If you suspect a hang, capture a thread dump; look for threads `BLOCKED` on neighbor's chopstick to confirm the cycle.

<hr>

### ✅ When to use which
- **Ordering (IDs)**: fastest, simplest, general best practice if you control all lock sites.
- **Asymmetric**: quick fix for this ring topology.
- **try+backoff (wait/notify)**: good exercise in monitor design; avoids deadlock, watch for livelock.

<hr>

### 🎯 Takeaways
- The **dining philosophers** is a compact lab for deadlock, livelock, and starvation.
- Our final versions captured the essentials:
  - **Break circular wait** (ordering/asymmetry), or
- Keep your **monitor discipline** tight: `while`-guarded `wait`, `notifyAll` when multiple waiters, and never perform monitor ops without owning the lock.
