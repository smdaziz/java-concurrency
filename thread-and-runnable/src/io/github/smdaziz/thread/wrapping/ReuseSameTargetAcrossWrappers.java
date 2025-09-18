package io.github.smdaziz.thread.wrapping;

import io.github.smdaziz.thread.MyThread;

public class ReuseSameTargetAcrossWrappers {

    public static void main(String[] args) throws InterruptedException {
        // --------------------------------------------------------------------
        // Create multiple threads using the same io.github.smdaziz.thread.MyThread instance (shared target)
        //
        // What it does:
        //   - Creates one io.github.smdaziz.thread.MyThread ("io.github.smdaziz.thread.MyThread") but does NOT start it directly
        //   - Wraps that same instance in two different wrapper threads (t1, t2) with distinct names
        //
        // What you'll see (order may vary):
        //   current/wrapper=io.github.smdaziz.thread.MyThread-Thread-1, target=io.github.smdaziz.thread.MyThread
        //   current/wrapper=io.github.smdaziz.thread.MyThread-Thread-2, target=io.github.smdaziz.thread.MyThread
        //
        // Why:
        //   - run() executes in the wrapper threads (their names print as "current")
        //   - The target's own name prints as "target=io.github.smdaziz.thread.MyThread"
        //   - Reusing the same target is fine here because it's stateless. Shared mutable state would
        //     need synchronization to avoid races.
        // --------------------------------------------------------------------
        MyThread myThread = new MyThread("io.github.smdaziz.thread.MyThread");
        Thread t1 = new Thread(myThread, "io.github.smdaziz.thread.MyThread-Thread-1");
        t1.start();

        Thread t2 = new Thread(myThread, "io.github.smdaziz.thread.MyThread-Thread-2");
        t2.start();
    }
}

