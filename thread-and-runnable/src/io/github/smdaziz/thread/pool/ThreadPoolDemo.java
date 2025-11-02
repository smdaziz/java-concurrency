package io.github.smdaziz.thread.pool;

public class ThreadPoolDemo {
    public static void main(String[] args) {
        ThreadPool threadPool = new ThreadPool(3, 10);
        for(int i = 1; i <= 10; i++) {
            final String taskName = "Task"+i;
            threadPool.submit(() -> {
                Thread.currentThread().setName(taskName);
            });
        }
        threadPool.waitUntilFinished();
        threadPool.shutdown();
    }
}

class ThreadPool {
    private final int maxTasks;
    private final TaskQueue<Runnable> taskQueue;
    private volatile boolean isActive;
    private ThreadPoolRunner threadPoolRunner;

    public ThreadPool(int maxThreads, int maxTasks) {
        this.maxTasks = maxTasks;
        this.taskQueue = new TaskQueue<>(maxTasks);
        this.isActive = true;
        this.threadPoolRunner = new ThreadPoolRunner(maxThreads, taskQueue);
        new Thread(threadPoolRunner, "ThreadPoolRunner").start();
    }

    public void submit(Runnable task) {
        try {
            taskQueue.add(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void waitUntilFinished() {
        synchronized (this) {
            while(!taskQueue.isEmpty()) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            this.notifyAll();
        }
    }

    public void shutdown() {
        isActive = false;
    }
}

class ThreadPoolRunner implements Runnable {
    private final TaskQueue<Runnable> taskQueue;
    private Thread[] threads;

    public ThreadPoolRunner(int maxThreads, TaskQueue<Runnable> taskQueue) {
        threads = new Thread[maxThreads];
        this.taskQueue = taskQueue;

        for(int i = 1; i <= maxThreads; i++) {
            threads[i] = new Thread("Thread-"+i);
        }
    }
    @Override
    public void run() {
        while(!taskQueue.isEmpty()) {
            try {
                Runnable task = taskQueue.remove();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

class TaskQueue<T> {
    private final int capacity;
    private final T[] data;
    private int readIndex;
    private int writeIndex;
    private int size;

    public TaskQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Queue capacity cannot be <= 0");
        }

        this.capacity = capacity;
        data = (T[]) new Object[capacity];
        this.readIndex = 0;
        this.writeIndex = 0;
        this.size = 0;
    }

    public synchronized void add(T item) throws InterruptedException {
        while(this.isFull()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw e;
            }
        }
        data[writeIndex] = item;
        writeIndex = (writeIndex+1) % capacity;
        size++;
        this.notifyAll();
    }

    public synchronized T remove() throws InterruptedException {
        while(this.isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw e;
            }
        }
        T item = data[readIndex];
        data[readIndex] = null;
        readIndex = (readIndex+1) % capacity;
        size--;
        this.notifyAll();
        return item;
    }

    public synchronized boolean isFull() {
        return size == capacity;
    }

    public synchronized boolean isEmpty() {
        return size == 0;
    }
}
