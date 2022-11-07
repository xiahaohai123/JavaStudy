package unclassified;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class T {

    ReentrantLock lock = new ReentrantLock();

    Runnable runnable = () -> {
        try {
            boolean b = lock.tryLock(6, TimeUnit.SECONDS);
            if (b) {
                Thread.sleep(1000);
                System.out.println(System.currentTimeMillis());
                lock.unlock();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };

    public static void main(String[] args) {
        T t = new T();
        t.run();
    }

    public void run() {
        new Thread(runnable).start();
        new Thread(runnable).start();
        new Thread(runnable).start();
        new Thread(runnable).start();
        new Thread(runnable).start();
    }
}
