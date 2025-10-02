package io.github.smdaziz.thread.synchronization;

import java.util.Random;

public class BoundedBufferThread {

    public static void main(String[] args) {
        System.out.println("Main thread started");
        BoundedBuffer buffer = new BoundedBuffer(3);
        Thread producerThread = new Thread(new BoundedBufferProducer(buffer));
        Thread consumerThread = new Thread(new BoundedBufferConsumer(buffer));
        producerThread.start();
        consumerThread.start();
        try {
            producerThread.join();
            consumerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Main thread finished");
    }

}

class BoundedBufferProducer implements Runnable {

    private BoundedBuffer buffer;

    public BoundedBufferProducer(BoundedBuffer buffer) {
        this.buffer = buffer;
    }

    public void run() {
        Random random = new Random();
        for(int i = 1; i <= 10; i++) {
            try {
                buffer.put(i);
                Thread.sleep(random.nextInt(10) * 250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}

class BoundedBufferConsumer implements Runnable {

    private BoundedBuffer buffer;

    public BoundedBufferConsumer(BoundedBuffer buffer) {
        this.buffer = buffer;
    }

    public void run() {
        Random random = new Random();
        for(int i = 1; i <= 10; i++) {
            try {
                int value = buffer.get();
                Thread.sleep(random.nextInt(10) * 200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}

class BoundedBuffer {

    private Integer[] buffer;
    private int putIndex = 0;
    private int takeIndex = 0;

    public BoundedBuffer(int size) {
        buffer = new Integer[size];
    }

    private boolean isFull() {
        for(Integer i : buffer) {
            if(i == null) {
                return false;
            }
        }
        return true;
    }

    private boolean isEmpty() {
        for(Integer i : buffer) {
            if(i != null) {
                return false;
            }
        }
        return true;
    }

    public synchronized void put(int value) throws InterruptedException {
        while(isFull()) {
            wait();
        }
        buffer[putIndex] = value;
        putIndex = (putIndex + 1) % buffer.length;
        System.out.println("Produced: " + value);
        notifyAll();
    }

    public synchronized int get() throws InterruptedException {
        while(isEmpty()) {
            wait();
        }
        int value = buffer[takeIndex];
        buffer[takeIndex] = null;
        takeIndex = (takeIndex + 1) % buffer.length;
        notifyAll();
        System.out.println("Consumed: " + value);
        return value;
    }

}
