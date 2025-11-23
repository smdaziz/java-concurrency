package io.github.smdaziz.executor.instantiations;

import java.util.concurrent.*;

public class ThreadPoolExecutorDemo {
    public static void main(String[] args) {
        int corePoolSize = 3;
        int maximumPoolSize = 5;
        long keepAliveTime = 3000;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(10);
        ExecutorService executorService =
                new ThreadPoolExecutor(
                        corePoolSize,
                        maximumPoolSize,
                        keepAliveTime,
                        TimeUnit.MILLISECONDS,
                        workQueue
                );
        for(int i = 1; i <= 2 * workQueue.size(); i++) {
            final String taskName = "Task-"+i;
            executorService.execute(() -> {
                System.out.println(Thread.currentThread().getName() + " instantiated via ThreadPoolExecutor running " + taskName);
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        executorService.shutdown();
    }
}
