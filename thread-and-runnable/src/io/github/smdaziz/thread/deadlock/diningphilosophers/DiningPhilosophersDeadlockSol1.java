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

    // Ensure lower chopstick number gets picked
    // This avoids circular wait deadlock because,
    // in an attempt to pick up chopsticks, P4 (last philosopher) doesn't pick up P4 and hold
    // Ex: Philosopher (Left-Chopstick, Right-Chopstick)
    // P0 (C0, C1), P1 (C1, C2), P2 (C2, C3), P3 (C3, C4), P4 (C4, C0)
    // Remember circular wait happens when each philosopher picks left chopstick and waits to pick right chopstick
    // In Circular wait situation, before the last philosopher's turn comes, here is the state
    // P0 -> C0, P1 -> C1, P2 -> C2, P3 -> C3
    // when its P4's turn, it attempts to pick right chopstick first, which is C0 and since C0 isn't available, it waits
    // this gives a chance to P3 to unblock because C4 is available and is not picked by P4
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
