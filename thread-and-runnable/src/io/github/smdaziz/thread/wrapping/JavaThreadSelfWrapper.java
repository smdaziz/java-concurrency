package io.github.smdaziz.thread.wrapping;

public class JavaThreadSelfWrapper {

    public static void main(String[] args) {
        Thread thread = new Thread(new Thread());
        thread.start();
        Thread thread2 = new Thread(new Thread() {
            public void run() {
                System.out.println("Inside run of target Thread");
            }
        });
        thread2.start();
    }

}
