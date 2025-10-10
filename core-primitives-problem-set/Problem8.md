Our goal is to implement a Latch where main thread waits for N worker threads finish. 

We will try to solution this without using CountDownLatch, while also making sure there is no busy waiting.

### Naive version using `join()`

```java
package io.github.smdaziz;

import java.util.Arrays;

public class Problem8 {
    public static void main(String[] args) {
        System.out.println("Main thread started.");
        int numThreads = 4;
        Thread[] threads = new Thread[numThreads];

        for(int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(new ArmstrongFinder(), "ArmstrongFinder-" + (i + 1));
            threads[i].start();
        }

        System.out.println("Main thread is waiting for all worker threads to finish.");
        Arrays.stream(threads).forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        // Note: Above join() approach is not allowed as per the problem statement.
        // Added it just to show the naive approach.

        System.out.println("Main thread finished.");
    }
}

class ArmstrongFinder implements Runnable {
    @Override
    public void run() {
        for(int i = 100; i < 1_000_000; i++) {
            if (isArmstrong(i)) {
                System.out.println("[" + Thread.currentThread().getName() + "] : " + i + " is an Armstrong number.");
            }
        }
    }

    private boolean isArmstrong(int number) {
        int originalNumber = number;
        int sum = 0;
        int digits = String.valueOf(number).length();

        while (number != 0) {
            int digit = number % 10;
            sum += Math.pow(digit, digits);
            number /= 10;
        }

        return sum == originalNumber;
    }
}
```

**Does that work?**
Yes. But it uses `join()` which is not allowed as per the problem statement.

### Improved version without using `join()`

Let's implement a custom Latch mechanism.

For that, we can create a latch class (say `ArmstrongFinderLatch`) which will keep track of the number of active threads and will notify the main thread when all worker threads have finished.

```java
package io.github.smdaziz;

import java.util.Arrays;

public class Problem8 {
    public static void main(String[] args) {
        System.out.println("Main thread started.");
        int numThreads = 4;
        ArmstrongFinderLatch latch = new ArmstrongFinderLatch(numThreads);
        Thread[] threads = new Thread[numThreads];

        for(int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(new ArmstrongFinder(latch), "ArmstrongFinder-" + (i + 1));
            threads[i].start();
        }

        System.out.println("Main thread is waiting for all worker threads to finish.");

        latch.waitUntilAllThreadsFinished();
        System.out.println("Main thread finished.");
    }
}

class ArmstrongFinderLatch {
    private int count = 0;

    public ArmstrongFinderLatch(int count) {
        this.count = count;
    }

    public synchronized void waitUntilAllThreadsFinished() {
        while(count > 0) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        notifyAll();
    }

    public synchronized void decrement() {
        count--;
    }
}

class ArmstrongFinder implements Runnable {
    private final ArmstrongFinderLatch latch;

    public ArmstrongFinder(ArmstrongFinderLatch latch) {
        this.latch = latch;
    }

    @Override
    public void run() {
        for(int i = 100; i < 1_000_000; i++) {
            if (isArmstrong(i)) {
                System.out.println("[" + Thread.currentThread().getName() + "] : " + i + " is an Armstrong number.");
            }
        }
        latch.decrement();
    }

    private boolean isArmstrong(int number) {
        int originalNumber = number;
        int sum = 0;
        int digits = String.valueOf(number).length();

        while (number != 0) {
            int digit = number % 10;
            sum += Math.pow(digit, digits);
            number /= 10;
        }

        return sum == originalNumber;
    }
}
```

**Does that work?**

Yes, it does.

The main thread waits until all worker threads finish without using `join()` or busy waiting.

However, there is a small issue. The `decrement()` method does not notify the main thread when the count reaches zero. Due to this, the main thread may remain waiting indefinitely even after all worker threads have finished.

To fix it, we need to add a notification in the `decrement()` method. And also, we should not call `notifyAll()` in `waitUntilAllThreadsFinished()` method. Otherwise, it will notify all waiting threads (if any) unnecessarily.

### Final fixed version

```java
package io.github.smdaziz;

import java.util.Arrays;

// Goal: Implement a Latch (main waits until N worker threads finish).
// No CountDownLatch allowed. No busy waiting.
// No join() allowed because with join, waiting for N things means N joins (and keeping N thread refs)
public class Problem8 {
    public static void main(String[] args) {
        System.out.println("Main thread started.");
        int numThreads = 4;
        ArmstrongFinderLatch latch = new ArmstrongFinderLatch(numThreads);
        Thread[] threads = new Thread[numThreads];

        for(int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(new ArmstrongFinder(latch), "ArmstrongFinder-" + (i + 1));
            threads[i].start();
        }

        System.out.println("Main thread is waiting for all worker threads to finish.");

        latch.waitUntilAllThreadsFinished();
        System.out.println("Main thread finished.");
    }
}

class ArmstrongFinderLatch {
    private int count = 0;

    public ArmstrongFinderLatch(int count) {
        this.count = count;
    }

    public synchronized void waitUntilAllThreadsFinished() {
        while(count > 0) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public synchronized void decrement() {
        count--;
        if (count == 0) {
            this.notifyAll();
        }
    }
}

class ArmstrongFinder implements Runnable {
    private final ArmstrongFinderLatch latch;

    public ArmstrongFinder(ArmstrongFinderLatch latch) {
        this.latch = latch;
    }

    @Override
    public void run() {
        for(int i = 100; i < 10_000_000; i++) {
            if (isArmstrong(i)) {
                System.out.println("[" + Thread.currentThread().getName() + "] : " + i + " is an Armstrong number.");
            }
        }
        latch.decrement();
    }

    private boolean isArmstrong(int number) {
        int originalNumber = number;
        int sum = 0;
        int digits = String.valueOf(number).length();

        while (number != 0) {
            int digit = number % 10;
            sum += Math.pow(digit, digits);
            number /= 10;
        }

        return sum == originalNumber;
    }
}
```
