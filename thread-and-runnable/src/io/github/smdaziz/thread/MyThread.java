package io.github.smdaziz.thread;

public class MyThread extends Thread {

    /**
     * Default constructor (unnamed thread/target).
     */
    public MyThread() {
        // No name provided; when used as a target, the wrapper's name will print as "current"
        // and this.getName() will likely be empty (or JVM-assigned if started directly).
    }

    /**
     * Creates a named thread/target instance.
     *
     * @param name the thread/target name
     */
    public MyThread(String name) {
        super(name);
    }

    /**
     * Prints both the current thread's name and this instance's own name.
     * Shows how wrapping vs. starting directly changes what you see.
     */
    @Override
    public void run() {
        String current = Thread.currentThread().getName();
        String target = this.getName(); // name assigned to this io.github.smdaziz.thread.MyThread instance
        System.out.println("current/wrapper=" + current + ", target=" + target + " is running");
    }
}
