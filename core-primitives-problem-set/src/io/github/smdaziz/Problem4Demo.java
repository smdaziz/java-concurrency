package io.github.smdaziz;
// Goal: Show a worker spinning on a boolean flag that sometimes never sees the update.
public class Problem4Demo {

    public static void main(String[] args) {
        System.out.println("Main thread started.");
        Problem4DemoThread problem4DemoThread = new Problem4DemoThread();
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(problem4DemoThread, "Problem4-Thread-" + (i + 1));
            threads[i].start();
        }
        try {
            Thread.sleep(2000); // Let the threads run for a while
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Requesting to stop the thread.");
        problem4DemoThread.stop(); // Signal the thread to stop
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

class Problem4DemoThread implements Runnable {

    private boolean stopped = false;

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
