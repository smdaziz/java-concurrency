public class WrapNamedTargetsInUnnamedWrappers {

    public static void main(String[] args) throws InterruptedException {
        // --------------------------------------------------------------------
        // Create multiple threads using different named MyThread instances,
        // but wrap them in *unnamed* Thread wrappers
        //
        // What it does:
        //   - Creates two named MyThread instances ("Thread-MyThread-3", "Thread-MyThread-4")
        //   - Wraps each in a new Thread *without* giving the wrapper a name
        //
        // What you'll see (names vary, order may vary):
        //   current/wrapper=Thread-0 (or Thread-3, etc.), target=Thread-MyThread-3
        //   current/wrapper=Thread-1 (or Thread-4, etc.), target=Thread-MyThread-4
        //
        // Why:
        //   - run() executes in the *wrapper* thread, which has a JVM-assigned default name
        //     like "Thread-0", "Thread-1", ...
        //   - The target's own name is preserved and printed as "target=Thread-MyThread-3/4".
        // --------------------------------------------------------------------
        Thread thread3 = new Thread(new MyThread("Thread-MyThread-3"));
        thread3.start();

        Thread thread4 = new Thread(new MyThread("Thread-MyThread-4"));
        thread4.start();
    }
}

