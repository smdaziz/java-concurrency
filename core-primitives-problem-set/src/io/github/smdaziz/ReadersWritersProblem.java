package io.github.smdaziz;

import java.util.HashMap;
import java.util.Map;

// Goal: Create a ReaderWriter problem (2 writers, many readers). Ensure mutual exclusion correctly.

public class ReadersWritersProblem {
    public static void main(String[] args) {
        int _writers = 2;
        int _readers = 5;
        System.out.println("Reader Preferred Lock");
        // Part-1: Reader Preferred Lock
        RWLock lock = new RWReaderPreferredLock();
        ConfigStore configStore = new ConfigStore(lock);
        RWBufferWriter writer = new RWBufferWriter(configStore);
        RWBufferReader reader = new RWBufferReader(configStore);
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
        for(Thread t : writers) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        for(Thread t : readers) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Writer Preferred Lock");
        // Part-2: Writer Preferred Lock
        RWLock wLock = new RWWriterPreferredLock();
        ConfigStore wConfigStore = new ConfigStore(wLock);
        RWBufferWriter writer2 = new RWBufferWriter(wConfigStore);
        RWBufferReader reader2 = new RWBufferReader(wConfigStore);
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

interface RWLock {
    void beginRead();
    void endRead();
    void beginWrite();
    void endWrite();
}

class RWReaderPreferredLock implements RWLock {
    private final Object monitor = new Object();
    private int readers = 0;
    private boolean isActiveWriter = false;

    public void beginRead() {
        synchronized (monitor) {
            boolean wasInterrupted = false;
            while (isActiveWriter) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    wasInterrupted = true; // keep waiting, preserve status
                }
            }
            readers++;
            if (wasInterrupted) Thread.currentThread().interrupt();
        }
    }

    public void endRead() {
        synchronized (monitor) {
            readers--;
            if (readers == 0) {
                monitor.notifyAll();
            }
        }
    }

    public void beginWrite() {
        synchronized (monitor) {
            boolean wasInterrupted = false;
            while (readers > 0 || isActiveWriter) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    wasInterrupted = true; // keep waiting, preserve status
                }
            }
            isActiveWriter = true;
            if (wasInterrupted) Thread.currentThread().interrupt();
        }
    }

    public void endWrite() {
        synchronized (monitor) {
            isActiveWriter = false;
            monitor.notifyAll();
        }
    }
}

class RWWriterPreferredLock implements RWLock {
    private final Object monitor = new Object();
    private int readers = 0;
    private int waitingWriters = 0;
    private boolean isActiveWriter = false;

    public void beginRead() {
        synchronized (monitor) {
            boolean wasInterrupted = false;
            while (isActiveWriter || waitingWriters > 0) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    wasInterrupted = true; // keep waiting, preserve status
                }
            }
            readers++;
            if (wasInterrupted) Thread.currentThread().interrupt();
        }
    }

    public void endRead() {
        synchronized (monitor) {
            readers--;
            if (readers == 0) {
                monitor.notifyAll();
            }
        }
    }

    public void beginWrite() {
        synchronized (monitor) {
            waitingWriters++;
            boolean wasInterrupted = false;
            try {
                while (readers > 0 || isActiveWriter) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        wasInterrupted = true; // keep waiting, preserve status
                    }
                }
                isActiveWriter = true;
                if (wasInterrupted) Thread.currentThread().interrupt();
            } finally {
                waitingWriters--;
            }
        }
    }

    public void endWrite() {
        synchronized (monitor) {
            isActiveWriter = false;
            monitor.notifyAll();
        }
    }
}

class ConfigStore {
    private final Map<String, String> data;
    private final RWLock lock;

    public ConfigStore(RWLock lock) {
        this.data = new HashMap<>();
        this.lock = lock;
    }

    public void add(String key, String value) {
        lock.beginWrite();
        try {
            data.put(key, value);
        } finally {
            lock.endWrite();
        }
    }

    public String get(String key) {
        lock.beginRead();
        try {
            return data.get(key);
        } finally {
            lock.endRead();
        }
    }
}

class RWBufferWriter implements Runnable {
    private final ConfigStore configStore;

    public RWBufferWriter(ConfigStore configStore) {
        this.configStore = configStore;
    }

    @Override
    public void run() {
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
    }
}

class RWBufferReader implements Runnable {
    private final ConfigStore configStore;

    public RWBufferReader(ConfigStore configStore) {
        this.configStore = configStore;
    }

    @Override
    public void run() {
        for (int i = 1; i <= 10; i++) {
            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            System.out.println(Thread.currentThread().getName() + " consumed " + configStore.get("Key-" + i));
        }
    }
}
