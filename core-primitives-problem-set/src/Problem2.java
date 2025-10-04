// Modify Problem1 so both threads must alternate strictly: 1 A 2 B 3 C …
public class Problem2 {

    public static void main(String[] args) {
        System.out.println("Main thread started.");

        Thread numberThread = new Thread(new NumberPrinter2());
        Thread letterThread = new Thread(new LetterPrinter2());

        numberThread.start();
        letterThread.start();
        try {
            numberThread.join(); // Wait for numberThread to finish
            letterThread.join(); // Wait for letterThread to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Main thread finished.");
    }

}

class NumberPrinter2 implements Runnable {
    public void run() {
        for (int i = 1; i <= 26; i++) {
            System.out.println(i);
        }
    }
}

class LetterPrinter2 implements Runnable {
    public void run() {
        for (char c = 'A'; c <= 'Z'; c++) {
            System.out.println(c);
        }
    }
}
