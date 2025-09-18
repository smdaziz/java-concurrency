package io.github.smdaziz.thread.wrapping;

import io.github.smdaziz.thread.MyThread;

public class WrapTargetInThread {

    public static void main(String[] args) throws InterruptedException {
        // --------------------------------------------------------------------
        // Create a thread using the built-in Thread class (wrapping a target)
        //
        // What it does:
        //   - Creates wrapper threads (named "Thread-io.github.smdaziz.thread.MyThread-1" and "Thread-io.github.smdaziz.thread.MyThread-2")
        //   - The target is a io.github.smdaziz.thread.MyThread instance, but used *as a Runnable*
        //
        // What you'll see (order may vary):
        //   current/wrapper=Thread-io.github.smdaziz.thread.MyThread-1, target=(empty or "Thread-n" if named)
        //   current/wrapper=Thread-io.github.smdaziz.thread.MyThread-2, target=(empty or "Thread-n" if named)
        //
        // Why:
        //   - run() executes in the *wrapper* thread; current thread's name is the wrapper's.
        //   - The target's own name is whatever you gave to the io.github.smdaziz.thread.MyThread instance (here default).
        // --------------------------------------------------------------------
        Thread thread1 = new Thread(new MyThread(), "Thread-io.github.smdaziz.thread.MyThread-1");
        thread1.start();

        Thread thread2 = new Thread(new MyThread(), "Thread-io.github.smdaziz.thread.MyThread-2");
        thread2.start();
    }
}

