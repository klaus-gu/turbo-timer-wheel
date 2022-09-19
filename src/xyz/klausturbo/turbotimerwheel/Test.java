package xyz.klausturbo.turbotimerwheel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用于测试 .
 * @author <a href="mailto:guyue375@outlook.com">Klaus.turbo</a>
 * @program turbo-timer-wheel
 **/
public class Test {
    
    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1000);
        TurboTimerWheelDriver driver = new TurboTimerWheelDriver();
        AtomicInteger count = new AtomicInteger(0);
    
        for (int i = 1; i <= 1000; i++) {
            int finalI = i;
            TurboTimerTask task = new TurboTimerTask(i, () -> {
                countDownLatch.countDown();
                System.out.println(finalI +" == 开始执行 == "+(count.incrementAndGet()));
            });
            driver.addTask(task);
        }
        countDownLatch.await(5, TimeUnit.SECONDS);
        System.out.println(count);
    }
}
