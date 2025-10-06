package io.github.smdaziz;
// Goal: Show why notify() can deadlock when multiple threads wait on the same monitor.
public class Problem6 {
    public static void main(String[] args) {
        System.out.println("Main thread started.");
        SingleSlotBufferV1 buffer = new SingleSlotBufferV1();
        int numProducers = 2;
        int numConsumers = 5;
        Thread producerThreads[] = new Thread[numProducers];
        Thread consumerThreads[] = new Thread[numConsumers];
        for(int i = 0; i < numProducers; i++) {
            producerThreads[i] = new Thread(new Problem6ProducerV1(buffer), "Producer-" + (i + 1));
            producerThreads[i].start();
        }
        for(int i = 0; i < numConsumers; i++) {
            consumerThreads[i] = new Thread(new Problem6ConsumerV1(buffer), "Consumer-" + (i + 1));
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
            Thread.currentThread().interrupt();
        }
        // Note: This program may deadlock due to the use of notify() instead of notifyAll().
        // To observe the deadlock, you may need to run the program multiple times.
        // Why? Because notify() wakes up only one waiting thread,
        // and if that thread is not the right one
        // (e.g., a producer when the buffer is full),
        // it can lead to a situation where all threads are waiting indefinitely.
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
        notify();
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
        notify();
        return returnItem;
    }
}

class Problem6ProducerV1 implements Runnable {
    private SingleSlotBufferV1 buffer;

    public Problem6ProducerV1(SingleSlotBufferV1 buffer) {
        this.buffer = buffer;
    }

    public void run() {
        for(int i = 1; i <= 50; i++) {
            System.out.println(Thread.currentThread().getName() + " produced: " + i);
            buffer.put(i);
        }
    }
}

class Problem6ConsumerV1 implements Runnable {
    private SingleSlotBufferV1 buffer;

    public Problem6ConsumerV1(SingleSlotBufferV1 buffer) {
        this.buffer = buffer;
    }

    public void run() {
        for(int i = 1; i <= 20; i++) {
            Object item = buffer.get();
            System.out.println(Thread.currentThread().getName() + " consumed: " + item);
        }
    }
}
