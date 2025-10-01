package io.github.smdaziz.thread.synchronization;

public class SingleSlotBufferThread {

    public static void main(String[] args) {
        SingleSlotBuffer buffer = new SingleSlotBuffer();
        Thread producerThread = new Thread(new IntProducer(buffer), "ProducerThread");
        Thread consumerThread = new Thread(new IntConsumer(buffer), "ConsumerThread");

        producerThread.start();
        consumerThread.start();
    }

}

class IntProducer implements Runnable {

    private SingleSlotBuffer buffer;

    public IntProducer(SingleSlotBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= 10; i++) {
                System.out.println("Producing " + i);
                buffer.put(i);
                Thread.sleep(1000); // Simulate time taken to produce
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class IntConsumer implements Runnable {

    private SingleSlotBuffer buffer;

    public IntConsumer(SingleSlotBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= 10; i++) {
                int value = buffer.get();
                System.out.println("Consuming " + value);
                Thread.sleep(1500); // Simulate time taken to consume
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class SingleSlotBuffer {

    private Integer slot = null;

    public synchronized void put(int value) throws InterruptedException {
        if(slot != null) {
            this.wait();
        }
        slot = value;
        this.notify();
    }

    public synchronized int get() throws InterruptedException {
        if(slot == null) {
            this.wait();
        }
        int value = slot;
        slot = null;
        this.notify();
        return value;
    }

}
