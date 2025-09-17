public class ReuseSameTargetAcrossWrappers {

    public static void main(String[] args) throws InterruptedException {
        // --------------------------------------------------------------------
        // Create multiple threads using the same MyThread instance (shared target)
        //
        // What it does:
        //   - Creates one MyThread ("MyThread") but does NOT start it directly
        //   - Wraps that same instance in two different wrapper threads (t1, t2) with distinct names
        //
        // What you'll see (order may vary):
        //   current/wrapper=MyThread-Thread-1, target=MyThread
        //   current/wrapper=MyThread-Thread-2, target=MyThread
        //
        // Why:
        //   - run() executes in the wrapper threads (their names print as "current")
        //   - The target's own name prints as "target=MyThread"
        //   - Reusing the same target is fine here because it's stateless. Shared mutable state would
        //     need synchronization to avoid races.
        // --------------------------------------------------------------------
        MyThread myThread = new MyThread("MyThread");
        Thread t1 = new Thread(myThread, "MyThread-Thread-1");
        t1.start();

        Thread t2 = new Thread(myThread, "MyThread-Thread-2");
        t2.start();
    }
}

