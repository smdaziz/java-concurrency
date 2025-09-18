package io.github.smdaziz.runnable;

public class MyRunnable implements Runnable {

    private String name;

    public MyRunnable() {
    }

    public MyRunnable(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        System.out.println("Runnable " + name + " is running.");
        System.out.println("Runnable " + name + " has finished execution.");
    }

}
