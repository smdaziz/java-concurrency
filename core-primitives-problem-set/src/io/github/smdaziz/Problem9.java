package io.github.smdaziz;

// Goal: Implement a PingPong program where two threads print alternately “Ping” and “Pong” forever.
public class Problem9 {
    public static void main(String[] args) {
        Object pingLock = new Object();
        Object pongLock = new Object();
        Ping ping = new Ping(pongLock, pingLock);
        Pong pong = new Pong(pingLock, pongLock);
        Thread pingThread = new Thread(ping);
        Thread pongThread = new Thread(pong);
        pingThread.start();
        pongThread.start();
    }
}

class Ping implements Runnable {
    private Object pongLock;
    private Object pingLock;
    private boolean isFirst = true;

    public Ping(Object pongLock, Object pingLock) {
        this.pongLock = pongLock;
        this.pingLock = pingLock;
    }

    @Override
    public void run() {
        while(true) {
            if (isFirst) {
                System.out.println("Ping");
                isFirst = false;
                // notify pong thread to print
                synchronized (pingLock) {
                    pingLock.notify();
                }
                continue;
            }
            // wait for pong thread to print
            synchronized (pongLock) {
                try {
                    pongLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Ping");
            // notify pong thread to print
            synchronized (pingLock) {
                pingLock.notify();
                }
        }
    }
}

class Pong implements Runnable {
    private Object pingLock;
    private Object pongLock;

    public Pong(Object pingLock, Object pongLock) {
        this.pingLock = pingLock;
        this.pongLock = pongLock;
    }

    @Override
    public void run() {
        while(true) {
            // wait for ping thread to print
            synchronized (pingLock) {
                try {
                    pingLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Pong");
            // notify ping thread to print
            synchronized (pongLock) {
                pongLock.notify();
            }
        }
    }
}
