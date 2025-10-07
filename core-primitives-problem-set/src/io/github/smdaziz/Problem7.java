package io.github.smdaziz;
// Bounded buffer with M producers & K consumers
// Goal: Validate a correct, scalable bounded buffer (capacity > 1)
// using only synchronized + wait/notifyAll, under contention.
public class Problem7 {
    public static void main(String[] args) {
        System.out.println("Main thread started.");
        BoundedBuffer buffer = new BoundedBuffer(5);
        int numProducers = 3;
        int numConsumers = 6;
        Thread producerThreads[] = new Thread[numProducers];
        Thread consumerThreads[] = new Thread[numConsumers];
        for(int i = 0; i < numProducers; i++) {
            producerThreads[i] = new Thread(new Producer(buffer), "Producer-" + (i + 1));
            producerThreads[i].start();
        }
        for(int i = 0; i < numConsumers; i++) {
            consumerThreads[i] = new Thread(new Consumer(buffer), "Consumer-" + (i + 1));
            consumerThreads[i].start();
        }
        try {
            for(Thread t : producerThreads) {
                t.join();
            }
            for(Thread t : consumerThreads) {
                t.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Main thread finished.");
    }
}

class BoundedBuffer {
    private final Object[] buffer;
    private int putIndex = 0;
    private int getIndex = 0;
    private int currentCount = 0;

    public BoundedBuffer(int capacity) {
        buffer = new Object[capacity];
    }

    public synchronized void put(Object item) {
        while(isFull()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        buffer[putIndex] = item;
        putIndex = (putIndex + 1) % buffer.length;
        currentCount++;
        notifyAll();
    }

    public synchronized Object get() {
        while(isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        Object item = buffer[getIndex];
        buffer[getIndex] = null; // Empty the slot after consuming
        getIndex = (getIndex + 1) % buffer.length;
        currentCount--;
        notifyAll();
        return item;
    }

    public boolean isEmpty() {
        return currentCount == 0;
    }

    public boolean isFull() {
        return currentCount == buffer.length;
    }
}

class Producer implements Runnable {
    private final BoundedBuffer buffer;

    public Producer(BoundedBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        for(int i = 1; i <= 20; i++) {
            System.out.println(Thread.currentThread().getName() + " produced " + i);
            buffer.put(i);
        }
    }
}

class Consumer implements Runnable {
    private final BoundedBuffer buffer;

    public Consumer(BoundedBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        for(int i = 1; i <= 10; i++) {
            Object item = buffer.get();
            System.out.println(Thread.currentThread().getName() + " consumed " + item);
        }
    }
}
