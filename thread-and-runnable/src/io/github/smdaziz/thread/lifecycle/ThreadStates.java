package io.github.smdaziz.thread.lifecycle;

public class ThreadStates {
    public static void main(String[] args) {
        TurnLock turnLock = new TurnLock();
        Runnable numberPrinter = new NumberPrinter(turnLock);
        Thread numberPrinterThread = new Thread(numberPrinter, "NumberPrinter");
        System.out.println("NumberPrinter thread state before starting: " + numberPrinterThread.getState());
        numberPrinterThread.start();
        Runnable characterPrinter = new CharacterPrinter(turnLock);
        Thread characterPrinterThread = new Thread(characterPrinter, "CharacterPrinter");
        System.out.println("CharacterPrinter thread state before starting: " + characterPrinterThread.getState());
        characterPrinterThread.start();
        try {
            while (true) {
                System.out.println("NumberPrinter thread state as seen by main thread: " + numberPrinterThread.getState());
                System.out.println("CharacterPrinter thread state as seen by main thread: " + characterPrinterThread.getState());
                if (numberPrinterThread.getState() == Thread.State.WAITING) {
                    System.out.println("Main thread interrupting NumberPrinter");
                    numberPrinterThread.interrupt();
                }
                if (characterPrinterThread.getState() == Thread.State.TIMED_WAITING) {
                    System.out.println("Main thread interrupting CharacterPrinter");
                    characterPrinterThread.interrupt();
                }
                if (numberPrinterThread.getState() == Thread.State.TERMINATED &&
                        characterPrinterThread.getState() == Thread.State.TERMINATED) {
                    break;
                }
                Thread.sleep(200);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}

class TurnLock {
    private boolean isNumber;

    public void setNumber(boolean isNumber) {
        this.isNumber = isNumber;
    }

    public boolean isNumber() {
        return isNumber;
    }
}

class NumberPrinter implements Runnable {
    private TurnLock turnLock;

    public NumberPrinter(TurnLock turnLock) {
        this.turnLock = turnLock;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " thread state while running: " + Thread.currentThread().getState());
        int count = 0;
        try {
            while (true) {
                synchronized (turnLock) {
                    while(!turnLock.isNumber()) {
                        System.out.println(Thread.currentThread().getName() + " thread waiting on turnLock");
                        turnLock.wait();
                    }
                    System.out.println(Thread.currentThread().getName() + " thread about to sleep for " + count + " sec");
                    Thread.sleep(1000);
                    System.out.println(count);
                    count++;
                    turnLock.setNumber(false);
                    turnLock.notifyAll();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}

class CharacterPrinter implements Runnable {
    private TurnLock turnLock;

    public CharacterPrinter(TurnLock turnLock) {
        this.turnLock = turnLock;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " thread state while running: " + Thread.currentThread().getState());
        int count = 0;
        char c = 'a';
        try {
            while (true) {
                synchronized (turnLock) {
                    while(turnLock.isNumber()) {
                        System.out.println(Thread.currentThread().getName() + " thread waiting on turnLock");
                        turnLock.wait();
                    }
                    System.out.println(Thread.currentThread().getName() + " thread about to sleep for " + count + " sec");
                    Thread.sleep(1000);
                    System.out.println(c);
                    count++;
                    c++;
                    turnLock.setNumber(true);
                    turnLock.notifyAll();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
