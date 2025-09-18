package io.github.smdaziz.thread.creation;

import io.github.smdaziz.thread.MyThread;

public class SubClassThread {

    public static void main(String[] args) {
        // Recommended way to create a thread: subclass Thread and override run()
        MyThread myThread = new MyThread("MyThread");
        myThread.start();

        // Not recommended: create a Thread instance and pass a Thread instance as the target Runnable
//        Thread thread = new Thread(new MyThread("WrappedMyThread"));
        Thread thread = new Thread(myThread);
        thread.start();
    }

}
