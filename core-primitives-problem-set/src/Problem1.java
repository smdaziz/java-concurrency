// Write a program that creates two threads:
// one prints numbers from 1–50,
// the other prints letters from A–Z.
// Run them concurrently — what interleaving do you see?
public class Problem1 {
    public static void main(String[] args) {
        System.out.println("Main thread started.");

        Thread numberThread = new Thread(new NumberPrinter1());
        Thread letterThread = new Thread(new LetterPrinter1());

        numberThread.start();
        letterThread.start();

        try {
            numberThread.join();
            letterThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Main thread finished.");

        // The interleaving of numbers and letters will vary with each execution.
        // Without coordination, interleaving is nondeterministic.
        // sleep() only hints to the scheduler; it doesn’t guarantee order.
        // Also note: System.out.println is synchronized per-call (so lines don’t get mixed mid-line)
        // but cross-thread ordering of those lines is not defined.
    }
}

class NumberPrinter1 implements Runnable {
    @Override
    public void run() {
        for (int i = 1; i <= 50; i++) {
            System.out.println(i);
        }
    }
}

class LetterPrinter1 implements Runnable {
    @Override
    public void run() {
        for (char c = 'A'; c <= 'Z'; c++) {
            System.out.println(c);
        }
    }
}
