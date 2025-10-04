Our goal is to modify Problem1 so both threads must alternate strictly: 1 A 2 B 3 C …

Here goes the as is Problem1 version reference:
```java
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
```

We need to have a way for the two threads to communicate and coordinate their actions.
One way to achieve this is by using two objects as locks and using `wait()` and `notify()` methods to signal between the threads.

Like this?
```java
public class Problem2 {

    public static void main(String[] args) {
        System.out.println("Main thread started.");

        Object letterLock = new Object();
        Object numberLock = new Object();
        
        Thread numberThread = new Thread(new NumberPrinter2(letterLock, numberLock));
        Thread letterThread = new Thread(new LetterPrinter2(numberThread, letterLock));

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

    private Object letterLock;
    private Object numberLock;

    public NumberPrinter2(Object letterLock, Object numberLock) {
        this.letterLock = letterLock;
        this.numberLock = numberLock;
    }

    public synchronized void run() {
        try {
            for (int i = 1; i <= 26; i++) {
                letterLock.wait();
                System.out.println(i);
                numberLock.notify();
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class LetterPrinter2 implements Runnable {

    private Object numberLock;
    private Object letterLock;

    public LetterPrinter2(Object numberLock, Object letterLock) {
        this.numberLock = numberLock;
        this.letterLock = letterLock;
    }

    public synchronized void run() {
        try {
            for (char c = 'A'; c <= 'Z'; c++) {
                numberLock.wait();
                System.out.println(c);
                letterLock.notify();
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

The code will not work as intended because both threads will be waiting on each other indefinitely, leading to a deadlock situation.
Also, the `wait()` and `notify()` methods should be called on the same object that is used for synchronization. Hence, it results in an `IllegalMonitorStateException`.

How about having a single lock object and using a shared state variable to determine whose turn it is to print?

```java
public class Problem2 {

    public static void main(String[] args) {
        System.out.println("Main thread started.");

        PrinterLock printerLock = new PrinterLock(PrinterTurn.NUMBER);

        Thread numberThread = new Thread(new NumberPrinter2(printerLock));
        Thread letterThread = new Thread(new LetterPrinter2(printerLock));

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

    private PrinterLock printerLock;

    public NumberPrinter2(PrinterLock printerLock) {
        this.printerLock = printerLock;
    }

    public void run() {
        try {
            for (int i = 1; i <= 26; i++) {
                synchronized (printerLock) {
                    while (printerLock.getPrinterTurn() != PrinterTurn.NUMBER) {
                        printerLock.wait();
                    }
                    System.out.println(i);
                    printerLock.setPrinterTurn(PrinterTurn.LETTER);
                    printerLock.notify();
                }
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class LetterPrinter2 implements Runnable {

    private PrinterLock printerLock;

    public LetterPrinter2(PrinterLock printerLock) {
        this.printerLock = printerLock;
    }

    public void run() {
        try {
            for (char c = 'A'; c <= 'Z'; c++) {
                synchronized (printerLock) {
                    while (printerLock.getPrinterTurn() != PrinterTurn.LETTER) {
                        printerLock.wait();
                    }
                    System.out.println(c);
                    printerLock.setPrinterTurn(PrinterTurn.NUMBER);
                    printerLock.notify();
                }
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
}

enum PrinterTurn {
    NUMBER,
    LETTER
}

class PrinterLock {

    private PrinterTurn printerTurn;

    public PrinterLock(PrinterTurn printerTurn) {
        this.printerTurn = printerTurn;
    }

    public void setPrinterTurn(PrinterTurn printerTurn) {
        this.printerTurn = printerTurn;
    }

    public PrinterTurn getPrinterTurn() {
        return printerTurn;
    }

}
```

Well, that works! The `NumberPrinter2` and `LetterPrinter2` classes now use a shared `PrinterLock` object to coordinate their actions. Each thread waits for its turn to print, and after printing, it notifies the other thread to proceed. This ensures that the output alternates between numbers and letters as desired.

**What's solid?**
-   **Correctness:** Only one thread proceeds per turn; the other waits. No busy-waiting.
-   **Wait discipline:** `while (...) wait()` is exactly right.
-   **Monitor usage:** `synchronized (printerLock)` and `wait/notify` on the same object, which is correct.

**Nits & upgrades:**
1.  **`notify()` vs `notifyAll()`**\
    With exactly two threads and a single wait set, `notify()` is fine. If we later add more waiting threads (e.g., two letter printers), `notify()` can wake the "wrong" thread which immediately re-waits, causing avoidable stalls. Using `notifyAll()` makes the design robust to evolution. (Not required here; just future-proofing.)
2.  **Encapsulation / misuse-proofing**\
    `getPrinterTurn()`/`setPrinterTurn()` are unsynchronized. We used them correctly *under* the monitor, but nothing prevents misuse later. We're fine as-is **if (and only if)** every read/write of `printerTurn` happens while holding the **same** monitor (`synchronized (printerLock)`).

