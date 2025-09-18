package io.github.smdaziz.thread.creation;

import io.github.smdaziz.runnable.MyRunnable;

public class RunnableThread {

    public static void main(String[] args) {
        // Recommended way to create a thread: implement Runnable and pass it to Thread
        MyRunnable myRunnable = new MyRunnable("MyRunnable");
        Thread thread = new Thread(myRunnable);
        thread.start();
    }

}
