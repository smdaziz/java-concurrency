package io.github.smdaziz.thread.ordering;

public class PriorityThreads {

    public static void main(String[] args) {
        Thread thread1 = new Thread(new MyCounter(), "Thread-1");
        Thread thread2 = new Thread(new MyCounter(), "Thread-2");
        Thread thread3 = new Thread(new MyCounter(), "Thread-3");

        // Setting different priorities
        thread1.setPriority(Thread.MIN_PRIORITY); // 1
        thread2.setPriority(Thread.NORM_PRIORITY); // 5
        thread3.setPriority(Thread.MAX_PRIORITY); // 10

        thread1.start();
        thread2.start();
        thread3.start();
    }

}

class MyCounter implements Runnable {
    @Override
    public void run() {
        String name = Thread.currentThread().getName();
        System.out.println("Thread " + name + " is running.");
        int count = 0;
        while (count++ < Integer.MAX_VALUE - 1) {
            if(isAmstrongNumber(count)) {
                System.out.println(name + " found Armstrong number: " + count);
            };
        }
        System.out.println("count: " + count);
        System.out.println("Thread " + name + " has finished execution.");
    }

    private boolean isAmstrongNumber(int number) {
        int originalNumber = number;
        int sum = 0;
        int digits = String.valueOf(number).length();

        while (number != 0) {
            int digit = number % 10;
            sum += Math.pow(digit, digits);
            number /= 10;
        }

        return sum == originalNumber;
    }
}
