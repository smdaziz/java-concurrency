package io.github.smdaziz.thread.deadlock;
// Goal: Show that without notify() or notifyAll(), wait() will cause a deadlock.
public class NoNotifierDeadlockDemo {
    public static void main(String[] args) {
        System.out.println("Main thread started.");
        SingleSlotBufferV1 buffer = new SingleSlotBufferV1();
        Thread producerThread = new Thread(new SingleSlotProducer(buffer), "Producer");
        Thread consumerThread = new Thread(new SingleSlotConsumer(buffer), "Consumer");
        producerThread.start();
        consumerThread.start();
        try {
            producerThread.join();
            consumerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // Note: This program will always deadlock because there are no notify() or notifyAll() calls.
        // Both producer and consumer will end up waiting indefinitely.
        System.out.println("Main thread finished.");
    }
}

class SingleSlotBufferV1 {
    private Object item;

    public synchronized void put(Object item) {
        while(this.item != null) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        this.item = item;
        // Missing notify() or notifyAll() here causes deadlock.
    }

    public synchronized Object get() {
        while(this.item == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        Object returnItem = this.item;
        this.item = null;
        // Missing notify() or notifyAll() here causes deadlock.
        return returnItem;
    }
}

class SingleSlotProducer implements Runnable {
    private final SingleSlotBufferV1 buffer;

    public SingleSlotProducer(SingleSlotBufferV1 buffer) {
        this.buffer = buffer;
    }

    public void run() {
        for(int i = 1; i <= 50; i++) {
            System.out.println(Thread.currentThread().getName() + " produced: " + i);
            buffer.put(i);
        }
    }
}

class SingleSlotConsumer implements Runnable {
    private final SingleSlotBufferV1 buffer;

    public SingleSlotConsumer(SingleSlotBufferV1 buffer) {
        this.buffer = buffer;
    }

    public void run() {
        for(int i = 1; i <= 50; i++) {
            Object item = buffer.get();
            System.out.println(Thread.currentThread().getName() + " consumed: " + item);
        }
    }
}
