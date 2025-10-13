Our goal is to implement a PingPong program where two threads print alternately “Ping” and “Pong” forever.

Sounds simple enough, right?

Let's create
1. a Ping class that continues printing "Ping"
2. a Pong class that continues printing "Pong"
3. a main class to start both threads

```java
package io.github.smdaziz;

public class Problem9 {
    public static void main(String[] args) {
        Ping ping = new Ping();
        Pong pong = new Pong();
        Thread pingThread = new Thread(ping);
        Thread pongThread = new Thread(pong);
        pingThread.start();
        pongThread.start();
    }
}

class Ping implements Runnable {
    @Override
    public void run() {
        while(true) {
            System.out.println("Ping");
        }
    }
}

class Pong implements Runnable {
    @Override
    public void run() {
        while(true) {
            System.out.println("Pong");
        }
    }
}
```

When you run this code, you will see that the output is not alternating between "Ping" and "Pong".

Instead, you might see a series of "Ping" followed by a series of "Pong", or vice versa OR a mix of both in no particular order.

This is because the two threads are running independently and there is no coordination between them.

So, clearly, we need a way for the two threads to communicate and coordinate their actions.

One way to achieve this is by using two objects as locks and using `wait()` and `notify()` methods to signal between the threads.

Is this how?

```java
package io.github.smdaziz;

// Goal: Implement a PingPong program where two threads print alternately “Ping” and “Pong” forever.
public class Problem9 {
    public static void main(String[] args) {
        Ping ping = new Ping();
        Pong pong = new Pong();
        Thread pingThread = new Thread(ping);
        Thread pongThread = new Thread(pong);
        pingThread.start();
        pongThread.start();
    }
}

class Ping implements Runnable {
    private Object pongLock;

    public Ping() {
        this.pongLock = new Object();
    }

    @Override
    public void run() {
        while(true) {
            // wait for pong thread to print
            synchronized (pongLock) {
                try {
                    pongLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Ping");
            // notify pong thread to print
            pongLock.notify();
        }
    }
}

class Pong implements Runnable {
    private Object pingLock;

    public Pong() {
        this.pingLock = new Object();
    }

    @Override
    public void run() {
        while(true) {
            // wait for ping thread to print
            synchronized (pingLock) {
                try {
                    pingLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Pong");
            // notify ping thread to print
            pingLock.notify();
        }
    }
}
```

**Does this work?**
No, it doesn't. The program hangs and nothing is printed.

The issue here is that each thread is using its own lock object, so they are not actually coordinating with each other.
In other words Ping thread has pongLock and Pong thread has pingLock, none of which is used for cross communication.
i.e Ping thread is not aware of Pong thread's lock and Pong thread is not aware of Ping thread's lock.

To fix this, we need to use a shared lock object and a shared state variable to determine whose turn it is to print.

**Like this you mean?**

```java
package io.github.smdaziz;

// Goal: Implement a PingPong program where two threads print alternately “Ping” and “Pong” forever.
public class Problem9 {
    public static void main(String[] args) {
        Object pingLock = new Object();
        Object pongLock = new Object();
        Ping ping = new Ping(pongLock);
        Pong pong = new Pong(pingLock);
        Thread pingThread = new Thread(ping);
        Thread pongThread = new Thread(pong);
        pingThread.start();
        pongThread.start();
    }
}

class Ping implements Runnable {
    private Object pongLock;

    public Ping(Object pongLock) {
        this.pongLock = pongLock;
    }

    @Override
    public void run() {
        while(true) {
            // wait for pong thread to print
            synchronized (pongLock) {
                try {
                    pongLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Ping");
            // notify pong thread to print
            pongLock.notify();
        }
    }
}

class Pong implements Runnable {
    private Object pingLock;

    public Pong(Object pingLock) {
        this.pingLock = pingLock;
    }

    @Override
    public void run() {
        while(true) {
            // wait for ping thread to print
            synchronized (pingLock) {
                try {
                    pingLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Pong");
            // notify ping thread to print
            pingLock.notify();
        }
    }
}
```

**No, this still doesn't work. The program still hangs and nothing is printed.**
The issue here is that both threads are waiting on each other to notify them, but neither thread gets a chance to run first and print.
The only improvement this code has over the previous one is that now both threads receive respective lock object as a constructor argument.

_Can we pass both the locks to both threads and use them for synchronization? Does it solve the problem?_

```java
package io.github.smdaziz;

// Goal: Implement a PingPong program where two threads print alternately “Ping” and “Pong” forever.
public class Problem9 {
    public static void main(String[] args) {
        Object pingLock = new Object();
        Object pongLock = new Object();
        Ping ping = new Ping(pongLock, pingLock);
        Pong pong = new Pong(pingLock, pongLock);
        Thread pingThread = new Thread(ping);
        Thread pongThread = new Thread(pong);
        pingThread.start();
        pongThread.start();
    }
}

class Ping implements Runnable {
    private Object pongLock;
    private Object pingLock;

    public Ping(Object pongLock, Object pingLock) {
        this.pongLock = pongLock;
        this.pingLock = pingLock;
    }

    @Override
    public void run() {
        while(true) {
            // wait for pong thread to print
            synchronized (pongLock) {
                try {
                    pongLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Ping");
            // notify pong thread to print
            pingLock.notify();
        }
    }
}

class Pong implements Runnable {
    private Object pingLock;
    private Object pongLock;

    public Pong(Object pingLock, Object pongLock) {
        this.pingLock = pingLock;
        this.pongLock = pongLock;
    }

    @Override
    public void run() {
        while(true) {
            // wait for ping thread to print
            synchronized (pingLock) {
                try {
                    pingLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Pong");
            // notify ping thread to print
            pongLock.notify();
        }
    }
}
```

**Does this work?**
No, it still doesn't work.

The program still hangs and nothing is printed.

The issue here is that both threads are still waiting on each other to notify them, but neither thread gets a chance to run first and print.

**What if we use a flag variable to start with Ping thread?**

```java
package io.github.smdaziz;

// Goal: Implement a PingPong program where two threads print alternately “Ping” and “Pong” forever.
public class Problem9 {
    public static void main(String[] args) {
        Object pingLock = new Object();
        Object pongLock = new Object();
        Ping ping = new Ping(pongLock, pingLock);
        Pong pong = new Pong(pingLock, pongLock);
        Thread pingThread = new Thread(ping);
        Thread pongThread = new Thread(pong);
        pingThread.start();
        pongThread.start();
    }
}

class Ping implements Runnable {
    private Object pongLock;
    private Object pingLock;
    private boolean isFirst = true;

    public Ping(Object pongLock, Object pingLock) {
        this.pongLock = pongLock;
        this.pingLock = pingLock;
    }

    @Override
    public void run() {
        while(true) {
            if (isFirst) {
                System.out.println("Ping");
                isFirst = false;
                // notify pong thread to print
                pongLock.notify();
                continue;
            }
            // wait for pong thread to print
            synchronized (pongLock) {
                try {
                    pongLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Ping");
            // notify pong thread to print
            pingLock.notify();
        }
    }
}

class Pong implements Runnable {
    private Object pingLock;
    private Object pongLock;

    public Pong(Object pingLock, Object pongLock) {
        this.pingLock = pingLock;
        this.pongLock = pongLock;
    }

    @Override
    public void run() {
        while(true) {
            // wait for ping thread to print
            synchronized (pingLock) {
                try {
                    pingLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Pong");
            // notify ping thread to print
            pongLock.notify();
        }
    }
}
```

**Does it solve the problem?**

No, it still doesn't work.

After `Ping` is printed, the program runs into `IllegalMonitorStateException` because we are calling `notify()` on `pongObject` without holding its monitor.

**How about this?**

```java
package io.github.smdaziz;

// Goal: Implement a PingPong program where two threads print alternately “Ping” and “Pong” forever.
public class Problem9 {
    public static void main(String[] args) {
        Object pingLock = new Object();
        Object pongLock = new Object();
        Ping ping = new Ping(pongLock, pingLock);
        Pong pong = new Pong(pingLock, pongLock);
        Thread pingThread = new Thread(ping);
        Thread pongThread = new Thread(pong);
        pingThread.start();
        pongThread.start();
    }
}

class Ping implements Runnable {
    private Object pongLock;
    private Object pingLock;
    private boolean isFirst = true;

    public Ping(Object pongLock, Object pingLock) {
        this.pongLock = pongLock;
        this.pingLock = pingLock;
    }

    @Override
    public void run() {
        while(true) {
            synchronized (pingLock) {
                synchronized (pongLock) {
                    if (isFirst) {
                        System.out.println("Ping");
                        isFirst = false;
                        // notify pong thread to print
                        pongLock.notify();
                        continue;
                    }
                    try {
                        pongLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Ping");
                    // notify pong thread to print
                    pingLock.notify();
                }
            }
        }
    }
}

class Pong implements Runnable {
    private Object pingLock;
    private Object pongLock;

    public Pong(Object pingLock, Object pongLock) {
        this.pingLock = pingLock;
        this.pongLock = pongLock;
    }

    @Override
    public void run() {
        while(true) {
            // wait for ping thread to print
            synchronized (pingLock) {
                synchronized (pongLock) {
                    try {
                        pingLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Pong");
                    // notify ping thread to print
                    pongLock.notify();
                }
            }
        }
    }
}
```

**Does it work now?**

No, it still doesn't work.

In fact, it results in a deadlock because one thread waits while holding both locks.

Ok, what about this version which tries to address the previous `IllegalMonitorStateException` issue?

```java
package io.github.smdaziz;

// Goal: Implement a PingPong program where two threads print alternately “Ping” and “Pong” forever.
public class Problem9 {
    public static void main(String[] args) {
        Object pingLock = new Object();
        Object pongLock = new Object();
        Ping ping = new Ping(pongLock, pingLock);
        Pong pong = new Pong(pingLock, pongLock);
        Thread pingThread = new Thread(ping);
        Thread pongThread = new Thread(pong);
        pingThread.start();
        pongThread.start();
    }
}

class Ping implements Runnable {
    private Object pongLock;
    private Object pingLock;
    private boolean isFirst = true;

    public Ping(Object pongLock, Object pingLock) {
        this.pongLock = pongLock;
        this.pingLock = pingLock;
    }

    @Override
    public void run() {
        while(true) {
            if (isFirst) {
                System.out.println("Ping");
                isFirst = false;
                // notify pong thread to print
                synchronized (pingLock) {
                    pingLock.notify();
                }
                continue;
            }
            // wait for pong thread to print
            synchronized (pongLock) {
                try {
                    pongLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Ping");
            // notify pong thread to print
            synchronized (pingLock) {
                pingLock.notify();
                }
        }
    }
}

class Pong implements Runnable {
    private Object pingLock;
    private Object pongLock;

    public Pong(Object pingLock, Object pongLock) {
        this.pingLock = pingLock;
        this.pongLock = pongLock;
    }

    @Override
    public void run() {
        while(true) {
            // wait for ping thread to print
            synchronized (pingLock) {
                try {
                    pingLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Pong");
            // notify ping thread to print
            synchronized (pongLock) {
                pongLock.notify();
            }
        }
    }
}
```

While this code does not result in `IllegalMonitorStateException`, it still doesn't work as expected.

It is still racy and can deadlock

It is vulnerable to **lost notifications** and **spurious wakeups**. That's why it can deadlock.

The concrete failure (step-by-step)

_One timing that hangs forever:_

1.  **Ping** prints first "Ping", then does `synchronized(pingLock){ pingLock.notify(); }`
2.  **Pong** hasn't started `pingLock.wait()` yet → that notify is **lost** (not buffered).
3.  Next loop:
    -   **Ping** executes `synchronized(pongLock){ pongLock.wait(); }` → now **waiting on `pongLock`**.
    -   **Pong** executes `synchronized(pingLock){ pingLock.wait(); }` → now **waiting on `pingLock`**.
4.  Nobody will ever call `notify()` on either lock again (Ping only notifies `pingLock`, Pong only notifies `pongLock`, but both are asleep). **Deadlock.**

### What is a spurious wakeup?

A thread can return from `wait()` **even if nobody called `notify/notifyAll` and no timeout/interrupt happened**. The Java spec allows this (it mirrors how OS condition variables behave).

#### Why it matters

If you write:

```java
// ❌ WRONG
if (!condition) wait();   // wakes once, then assumes condition is true
doWork();                 // may run when condition is still false`
```

a spurious wakeup (or a notify meant for a different state/phase) lets the thread run when it **should still be waiting** → broken ordering, races, or hangs.

#### The rule (always)

```java
// ✅ RIGHT
synchronized(lock) {
  while (!condition) {    // re-check after every wake
    lock.wait();
  }
  // condition is true here
}
```

The `while` loop re-checks the **predicate** after every wake---spurious or not---so you only proceed when the state you need is actually true.

So? let's use boolean condition to avoid the lost notification and spurious wakeup issues.

```java
package io.github.smdaziz;

// Goal: Implement a PingPong program where two threads print alternately “Ping” and “Pong” forever.
public class Problem9 {
    public static void main(String[] args) {
        PingPongLock pingLock = new PingPongLock(true);
        PingPongLock pongLock = new PingPongLock(true);
        Ping ping = new Ping(pongLock, pingLock);
        Pong pong = new Pong(pingLock, pongLock);
        Thread pingThread = new Thread(ping);
        Thread pongThread = new Thread(pong);
        pingThread.start();
        pongThread.start();
    }
}

class PingPongLock {
    private boolean isLocked = true;

    public PingPongLock(boolean initial) {
        this.isLocked = initial;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }
}

class Ping implements Runnable {
    private PingPongLock pongLock;
    private PingPongLock pingLock;
    private boolean isFirst = true;

    public Ping(PingPongLock pongLock, PingPongLock pingLock) {
        this.pongLock = pongLock;
        this.pingLock = pingLock;
    }

    @Override
    public void run() {
        while(true) {
            if (isFirst) {
                System.out.println("Ping");
                isFirst = false;
                // notify pong thread to print
                synchronized (pingLock) {
                    pingLock.setLocked(false);
                    pingLock.notify();
                }
                continue;
            }
            // wait for pong thread to print
            synchronized (pongLock) {
                while(pongLock.isLocked()) {
                    try {
                        pongLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("Ping");
            pongLock.setLocked(true);
            // notify pong thread to print
            synchronized (pingLock) {
                pingLock.setLocked(false);
                pingLock.notify();
            }
        }
    }
}

class Pong implements Runnable {
    private PingPongLock pingLock;
    private PingPongLock pongLock;

    public Pong(PingPongLock pingLock, PingPongLock pongLock) {
        this.pingLock = pingLock;
        this.pongLock = pongLock;
    }

    @Override
    public void run() {
        while(true) {
            // wait for ping thread to print
            synchronized (pingLock) {
                while(pingLock.isLocked()) {
                    try {
                        pingLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("Pong");
            pingLock.setLocked(true);
            // notify ping thread to print
            synchronized (pongLock) {
                pongLock.setLocked(false);
                pongLock.notify();
            }
        }
    }
}
```

We’re very close, but this version is still racy because read/write the permit outside the lock we use for wait/notify.

In order to correct this, we need to ensure that the read/write of the permit is done inside the synchronized block.

```java
package io.github.smdaziz;

// Goal: Implement a PingPong program where two threads print alternately “Ping” and “Pong” forever.
public class Problem9 {
    public static void main(String[] args) {
        PingPongLock pingLock = new PingPongLock(true);
        PingPongLock pongLock = new PingPongLock(true);
        Ping ping = new Ping(pongLock, pingLock);
        Pong pong = new Pong(pingLock, pongLock);
        Thread pingThread = new Thread(ping);
        Thread pongThread = new Thread(pong);
        pingThread.start();
        pongThread.start();
    }
}

class PingPongLock {
    private boolean isLocked = true;

    public PingPongLock(boolean initial) {
        this.isLocked = initial;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }
}

class Ping implements Runnable {
    private PingPongLock pongLock;
    private PingPongLock pingLock;
    private boolean isFirst = true;

    public Ping(PingPongLock pongLock, PingPongLock pingLock) {
        this.pongLock = pongLock;
        this.pingLock = pingLock;
    }

    @Override
    public void run() {
        while(true) {
            if (isFirst) {
                System.out.println("Ping");
                isFirst = false;
                // notify pong thread to print
                synchronized (pingLock) {
                    pingLock.setLocked(false);
                    pingLock.notify();
                }
                continue;
            }
            // wait for pong thread to print
            synchronized (pongLock) {
                while(pongLock.isLocked()) {
                    try {
                        pongLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                pongLock.setLocked(true);
            }
            System.out.println("Ping");
            // notify pong thread to print
            synchronized (pingLock) {
                pingLock.setLocked(false);
                pingLock.notify();
            }
        }
    }
}

class Pong implements Runnable {
    private PingPongLock pingLock;
    private PingPongLock pongLock;

    public Pong(PingPongLock pingLock, PingPongLock pongLock) {
        this.pingLock = pingLock;
        this.pongLock = pongLock;
    }

    @Override
    public void run() {
        while(true) {
            // wait for ping thread to print
            synchronized (pingLock) {
                while(pingLock.isLocked()) {
                    try {
                        pingLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                pingLock.setLocked(true);
            }
            System.out.println("Pong");
            // notify ping thread to print
            synchronized (pongLock) {
                pongLock.setLocked(false);
                pongLock.notify();
            }
        }
    }
}
```

While this code works, it is still not ideal because we are using two lock objects and two boolean flags to coordinate between the two threads.

It can be simplified further by using a single lock object and a single boolean flag to indicate whose turn it is to print.

```java
package io.github.smdaziz;

// Goal: Implement a PingPong program where two threads print alternately “Ping” and “Pong” forever.
public class Problem9 {
    public static void main(String[] args) {
        PingPongLock pingPongLock = new PingPongLock(true);
        Ping ping = new Ping(pingPongLock);
        Pong pong = new Pong(pingPongLock);
        Thread pingThread = new Thread(ping);
        Thread pongThread = new Thread(pong);
        pingThread.start();
        pongThread.start();
    }
}

class PingPongLock {
    private boolean isPing = true;

    public PingPongLock(boolean initial) {
        this.isPing = initial;
    }

    public boolean isPing() {
        return isPing;
    }

    public boolean isPong() {
        return !isPing;
    }

    public void setPing(boolean isPing) {
        this.isPing = isPing;
    }
}

class Ping implements Runnable {
    private PingPongLock pingPongLock;

    public Ping(PingPongLock pingPongLock) {
        this.pingPongLock = pingPongLock;
    }

    @Override
    public void run() {
        while(true) {
            // wait for pong thread to print
            synchronized(pingPongLock) {
                while(pingPongLock.isPong()) {
                    try {
                        pingPongLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            System.out.println("Ping");
            synchronized(pingPongLock) {
                pingPongLock.setPing(false);
                pingPongLock.notify();
            }
        }
    }
}

class Pong implements Runnable {
    private PingPongLock pingPongLock;

    public Pong(PingPongLock pingPongLock) {
        this.pingPongLock = pingPongLock;
    }

    @Override
    public void run() {
        while(true) {
            // wait for ping thread to print
            synchronized(pingPongLock) {
                while(pingPongLock.isPing()) {
                    try {
                        pingPongLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            System.out.println("Pong");
            synchronized(pingPongLock) {
                pingPongLock.setPing(true);
                pingPongLock.notify();
            }
        }
    }
}
```

It works fine and no issues

Here is another similar version (AI generated)

```java
package demo.pingpong;

public class PingPongDemo {
    public static void main(String[] args) throws InterruptedException {
        final int N = 20;                 // how many alternations per side
        PingPong pp = new PingPong(true); // true → Ping goes first

        Thread ping = new Thread(() -> {
            for (int i = 0; i < N; i++) {
                try { pp.ping(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
        }, "Ping");

        Thread pong = new Thread(() -> {
            for (int i = 0; i < N; i++) {
                try { pp.pong(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
        }, "Pong");

        ping.start();
        pong.start();
        ping.join();
        pong.join();
    }
}

final class PingPong {
    private final Object lock = new Object();
    private boolean pingTurn;

    PingPong(boolean pingFirst) { this.pingTurn = pingFirst; }

    void ping() throws InterruptedException {
        synchronized (lock) {
            while (!pingTurn) lock.wait();   // wait until it's Ping's turn
            System.out.println("Ping");
            pingTurn = false;                // hand off to Pong
            lock.notifyAll();
        }
    }

    void pong() throws InterruptedException {
        synchronized (lock) {
            while (pingTurn) lock.wait();    // wait until it's Pong's turn
            System.out.println("Pong");
            pingTurn = true;                 // hand off to Ping
            lock.notifyAll();
        }
    }
}
```
