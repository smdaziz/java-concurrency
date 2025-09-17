public class StartThreadSubclassDirect {

    public static void main(String[] args) throws InterruptedException {
        // --------------------------------------------------------------------
        // Create a thread using the custom MyThread class (start subclass directly)
        //
        // What it does:
        //   - Constructs MyThread instances with names and starts them directly
        //
        // What you'll see (order may vary):
        //   current/wrapper=MyThread-1, target=MyThread-1
        //   current/wrapper=MyThread-2, target=MyThread-2
        //
        // Why:
        //   - The subclass instance *is* the running thread, so current thread name equals its own name.
        // --------------------------------------------------------------------
        Thread myThread1 = new MyThread("MyThread-1");
        myThread1.start();

        Thread myThread2 = new MyThread("MyThread-2");
        myThread2.start();
    }
}

