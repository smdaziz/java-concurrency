package io.github.smdaziz.thread.deadlock.diningphilosophers;
// Note: This version fixes the “wait without the monitor” issue in DiningPhilosophersDeadlockSol3V3
// by encapsulating wait/notify inside grab()/release().
// The remaining blocker is the classic deadlock:
//  - every philosopher does left.grab() then right.grab().
//  - If all five grab their left chopstick, each will block forever on their right → circular wait.
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
