package unclassified;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionTest {
    ReentrantLock reentrantLock = new ReentrantLock();
    Condition condition = reentrantLock.newCondition();


    public static void main(String[] args) throws InterruptedException {
        //ConditionTest conditionTest = new ConditionTest();
        //new Thread(() -> {
        //    try {
        //        TimeUnit.SECONDS.sleep(5);
        //    } catch (InterruptedException e) {
        //        e.printStackTrace();
        //    }
        //    conditionTest.reentrantLock.lock();
        //    conditionTest.condition.signal();
        //    conditionTest.reentrantLock.unlock();
        //}).start();
        //
        //conditionTest.reentrantLock.lock();
        //conditionTest.condition.await();
        //conditionTest.reentrantLock.unlock();
        //System.out.println("aaa");

        CountDownLatch countDownLatch = new CountDownLatch(1);

        //new Thread(() -> {
        //    try {
        //        TimeUnit.SECONDS.sleep(2);
        //    } catch (InterruptedException e) {
        //        e.printStackTrace();
        //    }
        //    countDownLatch.countDown();
        //    countDownLatch.countDown();
        //}).start();
        countDownLatch.countDown();
        countDownLatch.await();
        System.out.println("aa");
    }


}
