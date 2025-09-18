package io.github.smdaziz.thread.wrapping;

import io.github.smdaziz.thread.MyThread;

public class StartThreadSubclassDirect {

    public static void main(String[] args) throws InterruptedException {
        // --------------------------------------------------------------------
        // Create a thread using the custom io.github.smdaziz.thread.MyThread class (start subclass directly)
        //
        // What it does:
        //   - Constructs io.github.smdaziz.thread.MyThread instances with names and starts them directly
        //
        // What you'll see (order may vary):
        //   current/wrapper=io.github.smdaziz.thread.MyThread-1, target=io.github.smdaziz.thread.MyThread-1
        //   current/wrapper=io.github.smdaziz.thread.MyThread-2, target=io.github.smdaziz.thread.MyThread-2
        //
        // Why:
        //   - The subclass instance *is* the running thread, so current thread name equals its own name.
        // --------------------------------------------------------------------
        Thread myThread1 = new MyThread("io.github.smdaziz.thread.MyThread-1");
        myThread1.start();

        Thread myThread2 = new MyThread("io.github.smdaziz.thread.MyThread-2");
        myThread2.start();
    }
}

