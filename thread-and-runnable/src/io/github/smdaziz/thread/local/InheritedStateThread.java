package io.github.smdaziz.thread.local;

public class InheritedStateThread {
    public static void main(String[] args) {
        InheritedNamedThread inheritedNamedThread = new InheritedNamedThread();
        Thread t1 = new Thread(inheritedNamedThread, "InheritedNamedThread-1");
        Thread t2 = new Thread(inheritedNamedThread, "InheritedNamedThread-2");
        Thread t3 = new Thread(inheritedNamedThread, "InheritedNamedThread-3");
        t1.start();
        t2.start();
        t3.start();
    }
}

class InheritedNamedThread implements Runnable {
    private InheritableThreadLocal<String> threadName = new InheritableThreadLocal<>();

    @Override
    public void run() {
        if (null == threadName.get()) {
            threadName.set(Thread.currentThread().getName());
            Thread t = new Thread(this, "Child-of-" + Thread.currentThread().getName());
            System.out.println("Starting " + "Child-of-" + Thread.currentThread().getName());
            t.start();
        }
        System.out.println(threadName.get() + " started");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println(threadName.get() + " finished");
    }
}
