package io.github.smdaziz.thread.deadlock.diningphilosophers;
// Note: as much as this seems to work, it could still deadlock
// Here's a possible situation when that could happen
//     if (right.isAvailable()) {
//         right.grab();
// Between the isAvailable() check and right.grab(), a neighbor can take right.
// Then right.grab() waits while you’re still holding left → everyone grabs their left and waits on right → circular wait (deadlock).
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
