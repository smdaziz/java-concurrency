package io.github.smdaziz.executor.instantiations;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleThreadExecutorDemo {
    public static void main(String[] args) {
        Executor singleExecutor = Executors.newSingleThreadExecutor();
        singleExecutor.execute(() -> {
            System.out.println(Thread.currentThread().getName() + " instantiated via Executors.newSingleThreadExecutor() running Task-1");
        });
        singleExecutor.execute(() -> {
            System.out.println(Thread.currentThread().getName() + " instantiated via Executors.newSingleThreadExecutor() running Task-2");
        });
        ((ExecutorService)singleExecutor).shutdown();;
    }
}
