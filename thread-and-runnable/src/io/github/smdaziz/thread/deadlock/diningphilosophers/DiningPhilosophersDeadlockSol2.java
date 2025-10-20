package io.github.smdaziz.thread.deadlock.diningphilosophers;

public class DiningPhilosophersDeadlockSol2 {
    public static void main(String[] args) {
        int totalPhilosophers = 5;
        int totalChopsticks = 5;
        Thread[] diningPhilosophers = new Thread[totalPhilosophers];
        ChopstickV2[] chopsticks = new ChopstickV2[totalChopsticks];
        for(int i = 0; i < totalChopsticks; i++) {
            chopsticks[i] = new ChopstickV2(i+1);
        }
        for(int i = 0; i < totalPhilosophers; i++) {
            ChopstickV2 left = chopsticks[(i) % totalChopsticks];
            ChopstickV2 right = chopsticks[(i+1) % totalChopsticks];
            PhilosopherV2 philosopher = new PhilosopherV2(i, left, right);
            diningPhilosophers[i] = new Thread(philosopher, "Philosopher-"+(i+1));
        }
        for(Thread diningPhilosopher: diningPhilosophers) {
            diningPhilosopher.start();
        }
    }
}

class ChopstickV2 {
    private int chopstickNumber;

    public ChopstickV2(int chopstickNumber) {
        this.chopstickNumber = chopstickNumber;
    }

    @Override
    public String toString() {
        return "Chopstick-"+chopstickNumber;
    }
}

class PhilosopherV2 implements Runnable {
    private int index;
    private ChopstickV2 left;
    private ChopstickV2 right;

    public PhilosopherV2(int index, ChopstickV2 left, ChopstickV2 right) {
        this.index = index;
        this.left = left;
        this.right = right;
    }

    // Goal: Break circular-wait by making neighbors acquire in opposite orders.
    // Policy: Even-index philosophers pick LEFT first; odd-index philosophers pick RIGHT first.
    //
    // Intuition with 5 philosophers (P0..P4) and chopsticks (C0..C4):
    //   Left/Right mapping (same as usual): Pi needs {Ci, C(i+1)}.
    //
    // What deadlocks in the classic case:
    //   Everyone grabs LEFT then waits for RIGHT → P0 holds C0→C1, P1 holds C1→C2, ..., P4 holds C4→C0.
    //   All five hold one stick and wait for the next in a perfect cycle.
    //
    // What happens with our alternating policy when all start together:
    //   - P0 (even) grabs LEFT = C0, then tries RIGHT = C1.
    //   - P1 (odd) grabs RIGHT = C2, then tries LEFT = C1.
    //   - P2 (even) tries to grab LEFT = C2 but **blocks immediately** (P1 already has C2), so P2 holds nothing.
    //   - P3 (odd) grabs RIGHT = C4, then tries LEFT = C3.
    //   - P4 (even) tries to grab LEFT = C4 but **blocks immediately** (P3 already has C4), so P4 holds nothing.
    //
    // Key observation:
    //   At least one philosopher on each side (here P2 and P4) fails on the **first** pickup and holds nothing.
    //   That prevents the “everyone holds exactly one” configuration required for deadlock.
    //   Since the ring cannot reach the state where all five hold one and wait for the other,
    //   a circular wait cannot form. Eventually, someone who holds one stick acquires the second and eats,
    //   releasing progress for others.
    //
    // Trade-off:
    //   This removes deadlock but not theoretical starvation (an unlucky philosopher could keep losing).
    //   Random think/eat delays or a host/arbitrator can improve fairness if needed.
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
            // Let even philosophers pick left first
            if (index%2 == 0) {
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
                // Let odd philosophers pick right first
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
