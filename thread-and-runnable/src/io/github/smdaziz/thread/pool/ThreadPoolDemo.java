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
    private int maxThreads;
    private int maxTasks;

    public ThreadPool(int maxThreads, int maxTasks) {
        this.maxThreads = maxThreads;
        this.maxTasks = maxTasks;
    }

    public void submit(Runnable task) {

    }

    public void waitUntilFinished() {

    }

    public void shutdown() {

    }
}
