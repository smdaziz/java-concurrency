package io.github.smdaziz.executor.instantiations;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LimitedWorkStealingPoolDemo {
    public static void main(String[] args) {
        System.out.println("Available Processors: " + Runtime.getRuntime().availableProcessors());
        ExecutorService executorService = Executors.newWorkStealingPool(5);
        // Note: this creates a ForkJoinPool with parallelism = 5,
        // i.e., up to 5 worker threads running tasks in parallel,
        // regardless of how many processors are available.
        for(int i = 1; i <= 100; i++) {
            final String taskName = "Task-"+i;
            executorService.execute(() -> {
                System.out.println(Thread.currentThread().getName() + " instantiated via Executors.newWorkStealingPool() running " + taskName);
            });
        }
        // Note: the program could exit before all tasks print.
        // Why? Because worker threads are daemon threads,
        // so the JVM can terminate as soon as the main thread finishes
        // (even before workers complete).
        // Hence we can try sleeping for 10 secs before shutting down executorService
        // and maybe even await termination
        try {
            Thread.sleep(10*1000);
            executorService.shutdown();
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
