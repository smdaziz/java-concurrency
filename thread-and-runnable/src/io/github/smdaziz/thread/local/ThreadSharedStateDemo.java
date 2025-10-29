package io.github.smdaziz.thread.local;

public class ThreadSharedStateDemo {
}

class SharedCounter implements Runnable {
    private long count = 0;

    @Override
    public void run() {
        while(true) {
            count++;
        }
        System.out.println(Thread.currentThread().getName() + " counted till " + count);
    }
}
