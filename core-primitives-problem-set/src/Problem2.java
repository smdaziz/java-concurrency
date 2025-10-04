// Modify Problem1 so both threads must alternate strictly: 1 A 2 B 3 C …
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
