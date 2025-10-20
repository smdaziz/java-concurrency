package io.github.smdaziz.thread.deadlock.diningphilosophers;
// Note: This solution is still not accurate
// Evaluation
/*
What’s still wrong (surgical)

You call left.wait() / right.wait() without holding those monitors → IllegalMonitorStateException.
wait/notify must be inside synchronized(theSameObject).

The loop condition is while (!left.isAvailable() || !right.isAvailable()) then you try to grab() whichever is available.
That’s a split check→act across two objects; another philosopher can grab in between (TOCTOU race).

If both are available, the loop is skipped, and you never call grab() at all—your flags and the actual locking diverge.

Even if you fix waits, this still permits the classic circular wait: everyone can grab their “left” and block on “right”.
 */
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
