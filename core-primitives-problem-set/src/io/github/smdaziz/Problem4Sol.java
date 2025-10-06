package io.github.smdaziz;
// Goal: Solve the worker spinning on a boolean flag that never saw the update earlier in Problem4Demo.
public class Problem4Sol {

    public static void main(String[] args) {
        System.out.println("Main thread started.");
        Problem4SolThread problem4SolThread = new Problem4SolThread();
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(problem4SolThread, "Problem4-Thread-" + (i + 1));
            threads[i].start();
        }
        try {
            Thread.sleep(2000); // Let the threads run for a while
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Requesting to stop the thread.");
        problem4SolThread.stop(); // Signal the thread to stop
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join(); // Wait for the thread to finish
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Main thread has finished execution.");
    }

}

class Problem4SolThread implements Runnable {

    private volatile boolean stopped = false;

    public void stop() {
        stopped = true;
    }

    @Override
    public void run() {
        long count = 0;
        while(!stopped) {
            count++;
        }
        System.out.println(Thread.currentThread().getName() + " counted " + count + " before stopping.");
    }

}
