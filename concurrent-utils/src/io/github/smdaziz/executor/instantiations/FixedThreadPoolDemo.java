package io.github.smdaziz.executor.instantiations;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FixedThreadPoolDemo {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        for(int i = 1; i <= 5; i++) {
            final int taskNum = i;
            executorService.execute(() -> {
                System.out.println(Thread.currentThread().getName() + " instantiated via Executors.newFixedThreadPool(size) running Task-"+taskNum);
            });
        }
        executorService.shutdown();
    }
}
