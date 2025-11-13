package io.github.smdaziz.thread.local;

public class PrivateCounterDemo {
    public static void main(String[] args) {
        PrivateCounter privateCounter = new PrivateCounter();

        Thread t1 = new Thread(privateCounter, "PrivateCounter-1");
        Thread t2 = new Thread(privateCounter, "PrivateCounter-2");
        Thread t3 = new Thread(privateCounter, "PrivateCounter-3");

        t1.start();
        t2.start();
        t3.start();
    }
}

class PrivateCounter implements Runnable {
    private ThreadLocal<Integer> count = ThreadLocal.withInitial(() -> 0);

    @Override
    public void run() {
        for(int i = 0; i < 1_000_000; i++) {
            count.set(count.get()+1);
        }
        System.out.println(Thread.currentThread().getName() + " counted till " + count.get());
    }
}
