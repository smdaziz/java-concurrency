package io.github.smdaziz.thread.deadlock.diningphilosophers;

public class DiningPhilosophersDeadlock {
    public static void main(String[] args) {
        int totalPhilosophers = 5;
        int totalChopsticks = 5;
        Thread[] diningPhilosophers = new Thread[totalPhilosophers];
        Chopstick[] chopsticks = new Chopstick[totalChopsticks];
        for(int i = 0; i < totalChopsticks; i++) {
            chopsticks[i] = new Chopstick(i+1);
        }
        for(int i = 0; i < totalPhilosophers; i++) {
            Chopstick left = chopsticks[(i) % totalChopsticks];
            Chopstick right = chopsticks[(i+1) % totalChopsticks];
            Philosopher philosopher = new Philosopher(left, right);
            diningPhilosophers[i] = new Thread(philosopher, "Philosopher-"+(i+1));
        }
        for(Thread diningPhilosopher: diningPhilosophers) {
            diningPhilosopher.start();
        }
    }
}

class Chopstick {
    private int chopstickNumber;

    public Chopstick(int chopstickNumber) {
        this.chopstickNumber = chopstickNumber;
    }

    @Override
    public String toString() {
        return "Chopstick-"+chopstickNumber;
    }
}

class Philosopher implements Runnable {
    private Chopstick left;
    private Chopstick right;

    public Philosopher(Chopstick left, Chopstick right) {
        this.left = left;
        this.right = right;
    }

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
        }
    }
}
