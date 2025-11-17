package io.github.smdaziz.lock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Goal: Create a ReaderWriter problem (2 writers, many readers). Ensure mutual exclusion correctly.

public class ReadersWritersProblemWithLocks {
    public static void main(String[] args) {
        int _writers = 2;
        int _readers = 5;
        System.out.println("Unfair ReadWriteLock");
        // Part-1: ReadWriteLock, unfair! Writers can starve under heavy Read load
        ReadWriteLock lock = new ReentrantReadWriteLock();
        ConfigStore configStore = new ConfigStore(lock);
        CountDownLatch latch = new CountDownLatch(_writers + _readers);
        RWBufferWriter writer = new RWBufferWriter(configStore, latch);
        RWBufferReader reader = new RWBufferReader(configStore, latch);
        Thread[] writers = new Thread[_writers];
        Thread[] readers = new Thread[_readers];
        for (int i = 1; i <= writers.length; i++) {
            writers[i - 1] = new Thread(writer, "Writer1-" + i);
            writers[i - 1].start();
        }
        for (int i = 1; i <= readers.length; i++) {
            readers[i - 1] = new Thread(reader, "Reader1-" + i);
            readers[i - 1].start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Fair ReadWriteLock");
        // Part-2: ReadWriteLock, fair! Could be a little slower, but avoids starvation.
        // Writers won't be blocked by a steady stream of Readers
        ReadWriteLock fairLock = new ReentrantReadWriteLock(true);
        ConfigStore wConfigStore = new ConfigStore(fairLock);
        latch = new CountDownLatch(_writers + _readers);
        RWBufferWriter writer2 = new RWBufferWriter(wConfigStore, latch);
        RWBufferReader reader2 = new RWBufferReader(wConfigStore, latch);
        Thread[] writers2 = new Thread[_writers];
        Thread[] readers2 = new Thread[_readers];
        for (int i = 1; i <= writers2.length; i++) {
            writers2[i - 1] = new Thread(writer2, "Writer2-" + i);
            writers2[i - 1].start();
        }
        for (int i = 1; i <= readers2.length; i++) {
            readers2[i - 1] = new Thread(reader2, "Reader2-" + i);
            readers2[i - 1].start();
        }
        // Notice the Writer2-* threads gets more priority if you run Part2 in isolation
    }
}

class ConfigStore {
    private final Map<String, String> data;
    private final Lock readLock;
    private final Lock writeLock;

    public ConfigStore(ReadWriteLock lock) {
        this.data = new HashMap<>();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
    }

    public void add(String key, String value) {
        try {
            writeLock.lock();
            data.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    public String get(String key) {
        try {
            readLock.lock();
            return data.get(key);
        } finally {
            readLock.unlock();
        }
    }
}

class RWBufferWriter implements Runnable {
    private final ConfigStore configStore;
    private final CountDownLatch latch;

    public RWBufferWriter(ConfigStore configStore, CountDownLatch latch) {
        this.configStore = configStore;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= 10; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                System.out.println(Thread.currentThread().getName() + " produced { \"Key-" + i + "\", \"Value-" + i + "\"}");
                configStore.add("Key-" + i, "Value-" + i);
            }
        } finally {
            // Always countDown() in a finally inside each run() method.
            // If a runtime exception slips out of the loop (e.g., NullPointerException, OutOfMemoryError, etc.),
            // the CountDownLatch will never reach zero and main() will block forever.
            latch.countDown();
        }
    }
}

class RWBufferReader implements Runnable {
    private final ConfigStore configStore;
    private final CountDownLatch latch;

    public RWBufferReader(ConfigStore configStore, CountDownLatch latch) {
        this.configStore = configStore;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= 10; i++) {
                try {
                    Thread.sleep(2 * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                System.out.println(Thread.currentThread().getName() + " consumed " + configStore.get("Key-" + i));
            }
        } finally {
            // Always countDown() in a finally inside each run() method.
            // If a runtime exception slips out of the loop (e.g., NullPointerException, OutOfMemoryError, etc.),
            // the CountDownLatch will never reach zero and main() will block forever.
            latch.countDown();
        }
    }
}
