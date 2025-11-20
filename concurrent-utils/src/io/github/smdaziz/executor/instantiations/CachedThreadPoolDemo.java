package io.github.smdaziz.executor.instantiations;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CachedThreadPoolDemo {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        for(int i = 1; i <= 3; i++) {
            final String taskName = "Task-A-"+i;
            executorService.execute(() -> {
                System.out.println(Thread.currentThread().getName() + " instantiated via Executors.newCachedThreadPool() running " + taskName);
            });
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // Note: if we don't sleep above, there would be no gap and
        // newCachedThreadPool attempts creating new threads because no threads would be available
        for(int i = 1; i <= 3; i++) {
            final String taskName = "Task-B-"+i;
            executorService.execute(() -> {
                System.out.println(Thread.currentThread().getName() + " instantiated via Executors.newCachedThreadPool() running " + taskName);
            });
        }
        executorService.shutdown();
    }
}
