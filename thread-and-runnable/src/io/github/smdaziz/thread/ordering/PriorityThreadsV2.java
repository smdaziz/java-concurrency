package io.github.smdaziz.thread.ordering;

public class PriorityThreadsV2 {

    public static void main(String[] args) {
        Thread[] threads = new Thread[500];
        for(int i = 0; i < threads.length; i++) {
            if(i%2 == 0) {
                threads[i] = new Thread(new AmstrongCounter(), "PriorityThread-"+i);
                threads[i].setPriority(Thread.MAX_PRIORITY);
            } else {
                threads[i] = new Thread(new AmstrongCounter(), "Thread-"+i);
                threads[i].setPriority(Thread.MIN_PRIORITY);
            }
        }

        for(Thread thread : threads) {
            thread.start();
        }
    }

}

class AmstrongCounter implements Runnable {
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
