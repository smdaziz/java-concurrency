package io.github.smdaziz.thread.pool;

public class ThreadPoolDemo {
    public static void main(String[] args) {
        ThreadPool threadPool = new ThreadPool(3, 10);
        for(int i = 1; i <= 20; i++) {
            final String taskName = "Task"+i;
            threadPool.submit(() -> {
                System.out.println(Thread.currentThread().getName() + " executing " + taskName);
            });
        }
        threadPool.waitUntilFinished();
        threadPool.shutdown();
    }
}

class ThreadPool {
    private final TaskQueue<Runnable> taskQueue;
    private volatile boolean isActive;
    private Thread[] threads;

    public ThreadPool(int maxThreads, int maxTasks) {
        threads = new Thread[maxThreads];
        this.taskQueue = new TaskQueue<>(maxTasks);
        this.isActive = true;
        for(int i = 0; i < maxThreads; i++) {
            threads[i] = new Thread(new WorkerThread(this, taskQueue), "WorkerThread-"+(i+1));
            threads[i].start();
        }
    }

    public void submit(Runnable task) {
        try {
            if (isActive) {
                taskQueue.add(task);
            } else {
                System.out.println("Cannot submit task as the ThreadPool is shutdown");
            }
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
        }
    }

    public synchronized boolean isActive() {
        return this.isActive;
    }

    public synchronized void shutdown() {
        isActive = false;
        // Interrupt worker threads to release them from blocking calls.
        for(Thread t: threads) {
            t.interrupt();
        }
    }
}

class WorkerThread implements Runnable {
    private final ThreadPool threadPool;
    private final TaskQueue<Runnable> taskQueue;

    public WorkerThread(ThreadPool threadPool, TaskQueue<Runnable> taskQueue) {
        this.threadPool = threadPool;
        this.taskQueue = taskQueue;
    }
    @Override
    public void run() {
        // the || !taskQueue.isEmpty() is very much needed
        // because when shutdown() is called, isActive = false
        // but workers will stop looping immediately,
        // even if there are still tasks left in the queue that haven’t been executed.
        while(threadPool.isActive() || !taskQueue.isEmpty()) {
            try {
                Runnable task = taskQueue.remove();
                task.run();
                // threads waiting on the threadPool should be notified after removing task because
                // if taskQueue.remove() blocks while shutdown() happens,
                // the worker will stay stuck forever.
                synchronized (threadPool) {
                    if (taskQueue.isEmpty()) {
                        threadPool.notifyAll();
                    }
                }
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
