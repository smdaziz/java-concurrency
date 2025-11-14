package io.github.smdaziz.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UnfairReentrancyDemo {
    public static void main(String[] args) {
        UnfairFactorial factorial = new UnfairFactorial();

        Thread t1 = new Thread(factorial, "Factorial-Thread-1");
        Thread t2 = new Thread(factorial, "Factorial-Thread-2");
        Thread t3 = new Thread(factorial, "Factorial-Thread-3");

        t1.start();
        t2.start();
        t3.start();
/* Notice the lock unfairness among threads
Possible Output:
Factorial-Thread-1: 1! = 1
Factorial-Thread-1: 2! = 2
Factorial-Thread-1: 3! = 6
Factorial-Thread-1: 4! = 24
Factorial-Thread-1: 5! = 120
Factorial-Thread-1: 6! = 720
Factorial-Thread-1: 7! = 5040
Factorial-Thread-1: 8! = 40320
Factorial-Thread-1: 9! = 362880
Factorial-Thread-1: 10! = 3628800
Factorial-Thread-1: 11! = 39916800
Factorial-Thread-3: 12! = 479001600
Factorial-Thread-3: 13! = 6227020800
Factorial-Thread-3: 14! = 87178291200
Factorial-Thread-3: 15! = 1307674368000
 */
    }
}

class UnfairFactorial implements Runnable {
    private int factorialSeed = 1;
    private Lock lock = new ReentrantLock();

    @Override
    public void run() {
        while(factorialSeed <= 15) {
            try {
                lock.lock();
                if(factorialSeed > 15)
                    return;
                // Note: this if-check is required and it has to be within the lock.
                // Reason 1 (TOCTOU): multiple threads may pass the while-condition before locking;
                // by the time a thread acquires the lock, another may have advanced factorialSeed.
                // This second gate prevents overshoot/duplicates.
                // Reason 2 (JMM visibility): checking inside the lock establishes a happens-before
                // with prior unlocks, ensuring a fresh (not stale/cached) read of factorialSeed.
                // Outside the lock you have neither atomicity nor a visibility guarantee.
                System.out.println(Thread.currentThread().getName() + ": " + factorialSeed + "! = " + factorial(factorialSeed));
                factorialSeed++;
            } finally {
                lock.unlock();
            }
        }
    }

    private long factorial(long number) {
        try {
            lock.lock();
            if(number == 0)
                return 1;
            return number * factorial(number-1);
        } finally {
            lock.unlock();
        }
    }
}
