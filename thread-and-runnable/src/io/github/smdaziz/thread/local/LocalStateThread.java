package io.github.smdaziz.thread.local;

public class LocalStateThread {
    public static void main(String[] args) {
        LocalNamedThread localNamedThread = new LocalNamedThread();
        Thread t1 = new Thread(localNamedThread, "LocalNamedThread-1");
        Thread t2 = new Thread(localNamedThread, "LocalNamedThread-2");
        Thread t3 = new Thread(localNamedThread, "LocalNamedThread-3");
        t1.start();
        t2.start();
        t3.start();
    }
}

class LocalNamedThread implements Runnable {
    private ThreadLocal<String> threadName = new ThreadLocal<>();

    @Override
    public void run() {
        threadName.set(Thread.currentThread().getName());
        System.out.println(threadName.get() + " started");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println(threadName.get() + " finished");
    }
}
