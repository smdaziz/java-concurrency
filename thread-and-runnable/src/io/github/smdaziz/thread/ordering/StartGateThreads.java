package io.github.smdaziz.thread.ordering;

public class StartGateThreads {

    public static void main(String[] args) throws InterruptedException {
        StartGate startGate = new StartGate();

        Thread thread1 = new Thread(startGate, "Thread-1");
        Thread thread2 = new Thread(startGate, "Thread-2");
        Thread thread3 = new Thread(startGate, "Thread-3");

        thread1.start();
        thread2.start();
        thread3.start();

        System.out.println("Main thread is preparing to open the gate...");
        Thread.sleep(3000); // Simulate some preparation time
        System.out.println("Main thread is opening the gate!");

        // Open the gate by setting isOpen to true
        startGate.setOpen(true);

        thread1.join();
        thread2.join();
        thread3.join();

        System.out.println("All threads have finished execution.");
    }

}

class StartGate implements Runnable {

    private volatile boolean isOpen = false;

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public void run() {
        while(!isOpen);
        System.out.println("Thread " + Thread.currentThread().getName() + " has started execution.");
        try {
            Thread.sleep(2000); // Simulate some work with sleep
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Thread " + Thread.currentThread().getName() + " has finished execution.");
    }

}
