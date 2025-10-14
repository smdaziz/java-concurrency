package io.github.smdaziz;

import java.sql.SQLOutput;

public class ReadersWritersProblem {
    public static void main(String[] args) {
        RWBuffer buffer = new RWBuffer(3);
        int _writers = 2;
        int _readers = 5;
        RWBufferWriter writer = new RWBufferWriter(buffer);
        RWBufferReader reader = new RWBufferReader(buffer);
        Thread[] writers = new Thread[_writers];
        Thread[] readers = new Thread[_readers];
        for(int i = 1; i <= writers.length; i++) {
            writers[i-1] = new Thread(writer, "Writer-"+i);
            writers[i-1].start();
        }
        for(int i = 1; i <= readers.length; i++) {
            readers[i-1] = new Thread(reader, "Reader-"+i);
            readers[i-1].start();
        }
    }
}

class RWBuffer {
    private Object[] data;
    private int writeIndex = 0;
    private int readIndex = 0;
    private int count = 0;

    public RWBuffer(int size) {
        this.data = new Object[size];
    }

    public synchronized void writeData(Object item) {
        while(count == data.length) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        data[writeIndex] = item;
        writeIndex = (writeIndex + 1) % data.length;
        count++;
        notifyAll();
    }

    public synchronized Object readData() {
        while(count == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Object item = data[readIndex];
        readIndex = (readIndex + 1) % data.length;
        count--;
        notifyAll();
        return item;
    }
}

class RWBufferWriter implements Runnable {
    private RWBuffer buffer;

    public RWBufferWriter(RWBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        for(int i = 1; i <= 10; i++) {
            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Object data = Math.random() * 100;
            System.out.println(Thread.currentThread().getName() + " produced " + data);
            buffer.writeData(data);
        }
    }
}

class RWBufferReader implements Runnable {
    private RWBuffer buffer;

    public RWBufferReader(RWBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        for(int i = 1; i <= 10; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " consumed " + buffer.readData());
        }
    }
}
