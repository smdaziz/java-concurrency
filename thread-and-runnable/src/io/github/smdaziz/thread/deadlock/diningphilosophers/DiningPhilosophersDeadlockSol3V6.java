package io.github.smdaziz.thread.deadlock.diningphilosophers;
// Note: this solution works because grabbing the right chopstick is an atomic operation
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
