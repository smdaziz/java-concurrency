package io.github.smdaziz.thread.deadlock;
// Goal: Force a deadlock with 2 threads + 2 locks.
public class DeadLockDemo {
    public static void main(String[] args) {
        System.out.println("Main thread started.");
        NumberGeneratorV1 numberGenerator = new NumberGeneratorV1();
        LetterGeneratorV1 letterGenerator = new LetterGeneratorV1();
        Thread thread1 = new Thread(new NumberLetterPrinterV1(numberGenerator, letterGenerator), "NumberLetterPrinter");
        Thread thread2 = new Thread(new LetterNumberPrinterV1(numberGenerator, letterGenerator), "LetterNumberPrinter");
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

class NumberGeneratorV1 {
    private int number = 0;

    public int getNext() {
        return number++;
    }
}

class LetterGeneratorV1 {
    private char letter = 'A';

    public char getNext() {
        return letter++;
    }
}

class NumberLetterPrinterV1 implements Runnable {
    private final NumberGeneratorV1 numberGenerator;
    private final LetterGeneratorV1 letterGenerator;

    public NumberLetterPrinterV1(NumberGeneratorV1 numberGenerator, LetterGeneratorV1 letterGenerator) {
        this.numberGenerator = numberGenerator;
        this.letterGenerator = letterGenerator;
    }

    @Override
    public void run() {
        for (int i = 0; i < 26; i++) {
            String result = "";
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

class LetterNumberPrinterV1 implements Runnable {
    private final NumberGeneratorV1 numberGenerator;
    private final LetterGeneratorV1 letterGenerator;

    public LetterNumberPrinterV1(NumberGeneratorV1 numberGenerator, LetterGeneratorV1 letterGenerator) {
        this.numberGenerator = numberGenerator;
        this.letterGenerator = letterGenerator;
    }

    @Override
    public void run() {
        for (int i = 0; i < 26; i++) {
            String result = "";
            synchronized (letterGenerator) {
                result += letterGenerator.getNext();
                synchronized (numberGenerator) {
                    result += numberGenerator.getNext();
                }
            }
            System.out.println(result);
        }
    }
}
