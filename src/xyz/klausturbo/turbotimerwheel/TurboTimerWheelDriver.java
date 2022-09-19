package xyz.klausturbo.turbotimerwheel;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 装饰器模式，将时间轮驱动起来 .
 * @author <a href="mailto:guyue375@outlook.com">Klaus.turbo</a>
 * @program demo
 **/
@SuppressWarnings("all")
public class TurboTimerWheelDriver {
    
    /**
     * 初始化给出的时间轮.
     */
    private final TurboTimerWheel turboTimerWheel;
    
    /**
     * 用于运行任务的线程组.
     */
    private ExecutorService workers;
    
    /**
     * 实际用于驱动时间轮的线程.
     */
    private ExecutorService driver;
    
    /**
     * 延迟队列，用于驱动时间轮.
     */
    private DelayQueue<TurboTimerTaskSlot> slotDelayQueue = new DelayQueue<>();
    
    public TurboTimerWheelDriver() {
        this.turboTimerWheel = new TurboTimerWheel(1,20,System.currentTimeMillis(),slotDelayQueue);
        this.workers = Executors.newFixedThreadPool(200);
        this.driver = Executors.newFixedThreadPool(1);
        driver.execute(() -> {
            while (true) {
                driveWheel(turboTimerWheel.getInterval());
            }
        });
    }
    
    public void addTask(TurboTimerTask task) {
        if (!turboTimerWheel.addTask(task)) {
            workers.submit(task.getRunnable());
        }
    }
    
    private void driveWheel(long timeout) {
        try {
            /*
             由于 xyz.klausturbo.turbotimerwheel.TurboTimerTaskSlot 中维护了一个 当前任务插槽的过期时间（插槽到期那就是当前插槽的任务都到期了）。
             所以在驱动类中轮训维护好的 slotDelayQueue ，从中取出到期的插槽（也就代表时间已经走到了现在）去驱动时间轮。
             */
            TurboTimerTaskSlot slot = slotDelayQueue.poll(timeout, TimeUnit.MILLISECONDS);
            if (slot != null) {
                turboTimerWheel.driveWheel(slot.getSlotExpirationTime());
                slot.execute(this::addTask);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
}
