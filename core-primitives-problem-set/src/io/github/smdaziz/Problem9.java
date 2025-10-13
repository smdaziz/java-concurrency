package io.github.smdaziz;

// Goal: Implement a PingPong program where two threads print alternately “Ping” and “Pong” forever.
public class Problem9 {
    public static void main(String[] args) {
        PingPongLock pingPongLock = new PingPongLock(true);
        Ping ping = new Ping(pingPongLock);
        Pong pong = new Pong(pingPongLock);
        Thread pingThread = new Thread(ping);
        Thread pongThread = new Thread(pong);
        pingThread.start();
        pongThread.start();
    }
}

class PingPongLock {
    private boolean isPing = true;

    public PingPongLock(boolean initial) {
        this.isPing = initial;
    }

    public boolean isPing() {
        return isPing;
    }

    public boolean isPong() {
        return !isPing;
    }

    public void setPing(boolean isPing) {
        this.isPing = isPing;
    }
}

class Ping implements Runnable {
    private PingPongLock pingPongLock;

    public Ping(PingPongLock pingPongLock) {
        this.pingPongLock = pingPongLock;
    }

    @Override
    public void run() {
        while(true) {
            // wait for pong thread to print
            synchronized(pingPongLock) {
                while(pingPongLock.isPong()) {
                    try {
                        pingPongLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            System.out.println("Ping");
            synchronized(pingPongLock) {
                pingPongLock.setPing(false);
                pingPongLock.notify();
            }
        }
    }
}

class Pong implements Runnable {
    private PingPongLock pingPongLock;

    public Pong(PingPongLock pingPongLock) {
        this.pingPongLock = pingPongLock;
    }

    @Override
    public void run() {
        while(true) {
            // wait for ping thread to print
            synchronized(pingPongLock) {
                while(pingPongLock.isPing()) {
                    try {
                        pingPongLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            System.out.println("Pong");
            synchronized(pingPongLock) {
                pingPongLock.setPing(true);
                pingPongLock.notify();
            }
        }
    }
}
