package io.github.smdaziz.thread.deadlock.diningphilosophers;
// Note: this solution is still incorrect
/*
(Markdown format)

Here's what's *specifically* wrong with **this** code:

1) You can deadlock **before** your `while` runs
------------------------------------------------

You do:

`synchronized (left) {
  synchronized (right) {
    while (!left.isAvailable() || !right.isAvailable()) { ... }`

All philosophers take `left` first, then try to enter `synchronized (right)`. That's the classic circular-wait: each holds their left and blocks trying to enter their right. You never even reach the `while` in that deadlocked run.

2) You `wait()` on one chopstick while still holding the other
--------------------------------------------------------------

Inside the nested region you call:

`left.wait();   // while still holding 'right'
right.wait();  // while still holding 'left'`

`wait()` releases **only** the monitor you call it on. So when you `left.wait()`, you still hold `right`. Your neighbor might need `right` to progress and eventually `notify` on `left`, but can't get it---this wedges the system. Never `wait()` on A while holding B (unless you've broken circular wait with a strict global order, see below).

3) Logic bug: the `while` condition skips grabbing when both are free
---------------------------------------------------------------------

Your loop is:

`while (!left.isAvailable() || !right.isAvailable()) { ... }`

-   If **both are available**, condition is **false**, you skip the loop, **never call `grab()`**, and then "eat."\
    You are holding the monitors, but your `isAvailable` flags still say *true*. That means other threads may think the sticks are free and make bad decisions.

4) TOCTOU across two resources
------------------------------

You do "check → (maybe) `grab()`" for each stick, but other threads can interleave between your checks because there are **two** monitors in play. For this problem, "check/wait/mark-taken" must be **atomic per chopstick**.

5) Visibility discipline on `isAvailable`
-----------------------------------------

Here it happens to be *read/modified while the corresponding monitor is held*, which is okay **only if every single access everywhere follows the same rule**. In this class you do, but this pattern is fragile. Better to encapsulate the wait+state change inside the chopstick.
 */
public class DiningPhilosophersDeadlockSol3V3 {
    public static void main(String[] args) {
        int totalPhilosophers = 5;
        int totalChopsticks = 5;
        Thread[] diningPhilosophers = new Thread[totalPhilosophers];
        ChopstickSol3V3[] chopsticks = new ChopstickSol3V3[totalChopsticks];
        for(int i = 0; i < totalChopsticks; i++) {
            chopsticks[i] = new ChopstickSol3V3(i+1);
        }
        for(int i = 0; i < totalPhilosophers; i++) {
            ChopstickSol3V3 left = chopsticks[(i) % totalChopsticks];
            ChopstickSol3V3 right = chopsticks[(i+1) % totalChopsticks];
            PhilosopherSol3V3 philosopher = new PhilosopherSol3V3(left, right);
            diningPhilosophers[i] = new Thread(philosopher, "Philosopher-"+(i+1));
        }
        for(Thread diningPhilosopher: diningPhilosophers) {
            diningPhilosopher.start();
        }
    }
}

class ChopstickSol3V3 {
    private int chopstickNumber;
    private boolean isAvailable = true;

    public ChopstickSol3V3(int chopstickNumber) {
        this.chopstickNumber = chopstickNumber;
    }

    public boolean isAvailable() {
        return this.isAvailable;
    }

    public void grab() {
        this.isAvailable = false;
    }

    public void release() {
        this.isAvailable = true;
    }

    @Override
    public String toString() {
        return "Chopstick-"+chopstickNumber;
    }
}

class PhilosopherSol3V3 implements Runnable {
    private ChopstickSol3V3 left;
    private ChopstickSol3V3 right;

    public PhilosopherSol3V3(ChopstickSol3V3 left, ChopstickSol3V3 right) {
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
                synchronized (left) {
                    synchronized (right) {
                        while(!left.isAvailable() || !right.isAvailable()) {
                            if (left.isAvailable()) {
                                left.grab();
                                System.out.println(philosopher + " picked up " + left);
                            } else {
                                left.wait();
                            }

                            if (right.isAvailable()) {
                                right.grab();
                                System.out.println(philosopher + " picked up " + right);
                            } else {
                                right.wait();
                            }
                        }
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
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
