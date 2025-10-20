package io.github.smdaziz.thread.deadlock.diningphilosophers;
// Note: This solution isn't right and can still deadlock
// And here is the evaluation
/*
You’re close conceptually, but this version still has several show-stoppers:

You call left.wait() / right.wait() without holding those monitors → IllegalMonitorStateException.
wait/notify must be inside synchronized(theSameObject).

Data race / visibility:
isAvailable() reads the flag without any synchronization or volatile, while grab/release write it under synchronized.
That’s not a happens-before; readers may see stale values.

TOCTOU:
You check availability, then later call grab(). Another thread can slip in between.
The check and state change need to be atomic.

Deadlock still possible:
Even if you fix the above, each philosopher still tries left then right.
All five can grab their left and wait on right → circular wait.
 */
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
