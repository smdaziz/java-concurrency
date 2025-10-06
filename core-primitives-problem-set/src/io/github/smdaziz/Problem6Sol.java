package io.github.smdaziz;
// Goal: Show how notifyAll() can avoid deadlock when multiple threads wait on the same monitor.
public class Problem6Sol {
    public static void main(String[] args) {
        System.out.println("Main thread started.");
        SingleSlotBufferV2 buffer = new SingleSlotBufferV2();
        int numProducers = 2;
        int numConsumers = 5;
        Thread producerThreads[] = new Thread[numProducers];
        Thread consumerThreads[] = new Thread[numConsumers];
        for(int i = 0; i < numProducers; i++) {
            producerThreads[i] = new Thread(new Problem6ProducerV2(buffer), "Producer-" + (i + 1));
            producerThreads[i].start();
        }
        for(int i = 0; i < numConsumers; i++) {
            consumerThreads[i] = new Thread(new Problem6ConsumerV2(buffer), "Consumer-" + (i + 1));
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
        // Note: This program will never deadlock due to the use of notifyAll() instead of notify().
        // Why? Because notifyAll() wakes up all waiting threads,
        // allowing the correct thread (producer or consumer) to proceed.
        // Therefore, it avoids the deadlock scenario that can occur with notify().
        System.out.println("Main thread finished.");
    }
}

class SingleSlotBufferV2 {
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
        notifyAll();
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
        notifyAll();
        return returnItem;
    }
}

class Problem6ProducerV2 implements Runnable {
    private SingleSlotBufferV2 buffer;

    public Problem6ProducerV2(SingleSlotBufferV2 buffer) {
        this.buffer = buffer;
    }

    public void run() {
        for(int i = 1; i <= 50; i++) {
            System.out.println(Thread.currentThread().getName() + " produced: " + i);
            buffer.put(i);
        }
    }
}

class Problem6ConsumerV2 implements Runnable {
    private SingleSlotBufferV2 buffer;

    public Problem6ConsumerV2(SingleSlotBufferV2 buffer) {
        this.buffer = buffer;
    }

    public void run() {
        for(int i = 1; i <= 20; i++) {
            Object item = buffer.get();
            System.out.println(Thread.currentThread().getName() + " consumed: " + item);
        }
    }
}
