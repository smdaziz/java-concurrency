package io.github.smdaziz.thread.creation;

public class AnonymousRunnableThread {

    public static void main(String[] args) {
        // Create a thread using an anonymous Runnable implementation
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String name = Thread.currentThread().getName();
                System.out.println("Runnable " + name + " is running.");
                System.out.println("Runnable " + name + " has finished execution.");
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

}
