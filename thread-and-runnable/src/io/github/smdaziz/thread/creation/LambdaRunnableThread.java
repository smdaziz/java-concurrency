package io.github.smdaziz.thread.creation;

public class LambdaRunnableThread {

    public static void main(String[] args) {
        // Recommended way to create a thread: use a lambda expression for Runnable
        Runnable runnable = () -> {
            String name = Thread.currentThread().getName();
            System.out.println("Runnable " + name + " is running.");
            System.out.println("Runnable " + name + " has finished execution.");
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

}
