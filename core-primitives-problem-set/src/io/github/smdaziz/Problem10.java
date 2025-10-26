package io.github.smdaziz;

public class Problem10 {
    public static void main(String[] args) {
        // Unsafe Counter
        Counter counter = new Counter();
        Thread counter1 = new Thread(counter, "Counter-1");
        Thread counter2 = new Thread(counter, "Counter-2");
        Thread counter3 = new Thread(counter, "Counter-3");
        counter1.start();
        counter2.start();
        counter3.start();
        try {
            counter1.join();
            counter2.join();
            counter3.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Unsafe Counter count: " + counter.getCount());

        // Volatile Counter
        VolatileCounter volatileCounter = new VolatileCounter();
        Thread volatileCounter1 = new Thread(volatileCounter, "VolatileCounter-1");
        Thread volatileCounter2 = new Thread(volatileCounter, "VolatileCounter-2");
        Thread volatileCounter3 = new Thread(volatileCounter, "VolatileCounter-3");
        volatileCounter1.start();
        volatileCounter2.start();
        volatileCounter3.start();
        try {
            volatileCounter1.join();
            volatileCounter2.join();
            volatileCounter3.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Volatile Counter count: " + volatileCounter.getCount());

        // Synchronized Run Counter
        SynchronizedRunCounter synchronizedRunCounter = new SynchronizedRunCounter();
        Thread synchronizedRunCounter1 = new Thread(synchronizedRunCounter, "SynchronizedRunCounter-1");
        Thread synchronizedRunCounter2 = new Thread(synchronizedRunCounter, "SynchronizedRunCounter-2");
        Thread synchronizedRunCounter3 = new Thread(synchronizedRunCounter, "SynchronizedRunCounter-3");
        synchronizedRunCounter1.start();
        synchronizedRunCounter2.start();
        synchronizedRunCounter3.start();
        try {
            synchronizedRunCounter1.join();
            synchronizedRunCounter2.join();
            synchronizedRunCounter3.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Synchronized Run Counter count: " + synchronizedRunCounter.getCount());

        // Synchronized Loop Counter
        SynchronizedLoopCounter synchronizedLoopCounter = new SynchronizedLoopCounter();
        Thread synchronizedLoopCounter1 = new Thread(synchronizedLoopCounter, "SynchronizedLoopCounter-1");
        Thread synchronizedLoopCounter2 = new Thread(synchronizedLoopCounter, "SynchronizedLoopCounter-2");
        Thread synchronizedLoopCounter3 = new Thread(synchronizedLoopCounter, "SynchronizedLoopCounter-3");
        synchronizedLoopCounter1.start();
        synchronizedLoopCounter2.start();
        synchronizedLoopCounter3.start();
        try {
            synchronizedLoopCounter1.join();
            synchronizedLoopCounter2.join();
            synchronizedLoopCounter3.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Synchronized Loop Counter count: " + synchronizedLoopCounter.getCount());

        // Synchronized Counter
        SynchronizedCounter synchronizedCounter = new SynchronizedCounter();
        Thread synchronizedCounter1 = new Thread(synchronizedCounter, "SynchronizedCounter-1");
        Thread synchronizedCounter2 = new Thread(synchronizedCounter, "SynchronizedCounter-2");
        Thread synchronizedCounter3 = new Thread(synchronizedCounter, "SynchronizedCounter-3");
        synchronizedCounter1.start();
        synchronizedCounter2.start();
        synchronizedCounter3.start();
        try {
            synchronizedCounter1.join();
            synchronizedCounter2.join();
            synchronizedCounter3.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Synchronized Counter count: " + synchronizedCounter.getCount());
    }
}

class Counter implements Runnable {
    private int count = 0;

    public int getCount() {
        return count;
    }

    public void run() {
        long start = System.currentTimeMillis();
        for(int i = 0; i < 1_000_000; i++) {
            count++;
        }
        long end = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + " took " + (end - start) + " ms to complete");
    }
}

class VolatileCounter implements Runnable {
    private volatile int count = 0;

    public int getCount() {
        return count;
    }

    public void run() {
        long start = System.currentTimeMillis();
        for(int i = 0; i < 1_000_000; i++) {
            count++;
        }
        long end = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + " took " + (end - start) + " ms to complete");
    }
}

class SynchronizedRunCounter implements Runnable {
    private int count = 0;

    public int getCount() {
        return count;
    }

    public synchronized void run() {
        long start = System.currentTimeMillis();
        for(int i = 0; i < 1_000_000; i++) {
            count++;
        }
        long end = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + " took " + (end - start) + " ms to complete");
    }
}

class SynchronizedLoopCounter implements Runnable {
    private int count = 0;

    public int getCount() {
        return count;
    }

    public void run() {
        long start = System.currentTimeMillis();
        synchronized (this) {
            for(int i = 0; i < 1_000_000; i++) {
                count++;
            }
        }
        long end = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + " took " + (end - start) + " ms to complete");
    }
}

class SynchronizedCounter implements Runnable {
    private int count = 0;

    public int getCount() {
        return count;
    }

    public void run() {
        long start = System.currentTimeMillis();
        for(int i = 0; i < 1_000_000; i++) {
            synchronized (this) {
                    count++;
            }
        }
        long end = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + " took " + (end - start) + " ms to complete");
    }
}
