package io.github.smdaziz.thread.local;

public class SharedStateThread {
    public static void main(String[] args) {
        NamedThread namedThread = new NamedThread();
        Thread t1 = new Thread(namedThread, "NamedThread-1");
        Thread t2 = new Thread(namedThread, "NamedThread-2");
        Thread t3 = new Thread(namedThread, "NamedThread-3");
        t1.start();
        t2.start();
        t3.start();
    }
}

class NamedThread implements Runnable {
    private String threadName;

    @Override
    public void run() {
        threadName = Thread.currentThread().getName();
        System.out.println(threadName + " started");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println(threadName + " finished");
    }
}
