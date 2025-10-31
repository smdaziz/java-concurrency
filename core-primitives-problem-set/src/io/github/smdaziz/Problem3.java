package io.github.smdaziz;
// Goal: Demonstrate the difference between calling run() directly vs start().
// Why is one concurrent and the other not?
// Calling `run()` does not start a thread rather invokes the `run` method as a regular method/function call.
// Where as calling `start()` instantiates a thread and `run()` is invoked when JVM/OS executes the thread
public class Problem3 {

    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getName() + " thread started.");
        Problem3Thread problem3Thread = new Problem3Thread();
        problem3Thread.run(); // Direct call to run(), not concurrent. This runs in the main thread.
        Thread thread = new Thread(problem3Thread, "Problem3Thread");
        thread.start(); // This starts a new thread, running concurrently.
        try {
            thread.join(); // Wait for the thread to finish
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + " thread finished.");
    }

}

class Problem3Thread implements Runnable {
    @Override
    public void run() {
        for(int i = 1; i <= 5; i++) {
            System.out.println(Thread.currentThread().getName() + " thread is running: " + i);
            try {
                Thread.sleep(500); // Sleep for 500 milliseconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
