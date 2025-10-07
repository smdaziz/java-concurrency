package io.github.smdaziz.thread.deadlock;
public class DeadLockSol {
// Goal: Solution to deadlock with 2 threads + 2 locks problem shown in DeadLockDemo.
    public static void main(String[] args) {
        System.out.println("Main thread started.");
        NumberGeneratorV2 numberGenerator = new NumberGeneratorV2();
        LetterGeneratorV2 letterGenerator = new LetterGeneratorV2();
        Thread thread1 = new Thread(new NumberLetterPrinterV2(numberGenerator, letterGenerator), "NumberLetterPrinter");
        Thread thread2 = new Thread(new LetterNumberPrinterV2(numberGenerator, letterGenerator), "LetterNumberPrinter");
        thread1.start();
        thread2.start();
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Main thread finished.");
    }
}

class NumberGeneratorV2 {
    private int number = 0;

    public int getNext() {
        return number++;
    }
}

class LetterGeneratorV2 {
    private char letter = 'A';

    public char getNext() {
        return letter++;
    }
}

class NumberLetterPrinterV2 implements Runnable {
    private final NumberGeneratorV2 numberGenerator;
    private final LetterGeneratorV2 letterGenerator;

    public NumberLetterPrinterV2(NumberGeneratorV2 numberGenerator, LetterGeneratorV2 letterGenerator) {
        this.numberGenerator = numberGenerator;
        this.letterGenerator = letterGenerator;
    }

    @Override
    public void run() {
        for (int i = 0; i < 26; i++) {
            String result = "";
            // Lock ordering to prevent deadlock
            synchronized (numberGenerator) {
                result += numberGenerator.getNext();
                synchronized (letterGenerator) {
                    result += letterGenerator.getNext();
                }
            }
            System.out.println(result);
        }
    }
}

class LetterNumberPrinterV2 implements Runnable {
    private final NumberGeneratorV2 numberGenerator;
    private final LetterGeneratorV2 letterGenerator;

    public LetterNumberPrinterV2(NumberGeneratorV2 numberGenerator, LetterGeneratorV2 letterGenerator) {
        this.numberGenerator = numberGenerator;
        this.letterGenerator = letterGenerator;
    }

    @Override
    public void run() {
        for (int i = 0; i < 26; i++) {
            String result = "";
            // Lock ordering to prevent deadlock
            synchronized (numberGenerator) {
                result += numberGenerator.getNext();
                synchronized (letterGenerator) {
                    result = letterGenerator.getNext() + result;
                }
            }
            System.out.println(result);
        }
    }
}
