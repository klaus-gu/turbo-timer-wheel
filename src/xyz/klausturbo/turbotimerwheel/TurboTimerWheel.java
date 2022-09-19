package xyz.klausturbo.turbotimerwheel;

import java.util.concurrent.DelayQueue;

/**
 * 时间轮实现 .
 * @author <a href="mailto:guyue375@outlook.com">Klaus.turbo</a>
 * @program demo
 **/
public class TurboTimerWheel {
    
    /**
     * 用于描述时间轮单个格子的单位.
     */
    private long tickMs;
    
    /**
     * 描述时间轮有多少个格子.
     */
    private int ticksPerWheel;
    
    /**
     * 描述时间轮新建的时间.
     */
    private long startTimeMs;
    
    /**
     * 描述时间轮的一整个环.
     */
    private TurboTimerTaskSlot[] slots;
    
    /**
     * 表示当前的时间.
     * <p>
     * currentTimeMs = startTimeMs - (startTimeMs % tickMs)：用于保证 currentTimeMs 是 tickMs 的整数倍。
     * <p>
     * currentTimeMs:永远都是指向时间轮某一个格子的最开始的位置，最左边。
     */
    private long currentTimeMs;
    
    /**
     * 时间轮总的表示的时间大小：interval = tickMs * ticksPerWheel.
     */
    private long interval;
    
    /**
     * 用于驱动时间轮.
     */
    private DelayQueue<TurboTimerTaskSlot> slotDelayQueue;
    
    /**
     * 上层的时间轮.
     */
    private TurboTimerWheel overflowWheel;
    
    /**
     * 用于上锁
     */
    private final Object monitor = new Object();
    
    /**
     * 构造函数.
     * @param tickMs        等于上一轮时间轮的 interval.
     * @param ticksPerWheel
     * @param startTimeMs   等于上一轮时间轮的结束时间.
     */
    public TurboTimerWheel(long tickMs, int ticksPerWheel, long startTimeMs,
            DelayQueue<TurboTimerTaskSlot> slotDelayQueue) {
        this.tickMs = tickMs;
        this.ticksPerWheel = ticksPerWheel;
        this.startTimeMs = startTimeMs;
        this.slots = new TurboTimerTaskSlot[ticksPerWheel];
        this.currentTimeMs = startTimeMs - (startTimeMs % tickMs);
        this.interval = tickMs * ticksPerWheel;
        this.slotDelayQueue = slotDelayQueue;
        for (int i = 0; i < ticksPerWheel; i++) {
            slots[i] = new TurboTimerTaskSlot();
        }
    }
    
    public boolean addTask(TurboTimerTask task) {
        Long taskExpirationTimeMs = task.getExecuteTimeMs();
        // 若当前添加的任务在当前时间轮的时间指针所属的范围内，则立即执行当前任务
        // 当前指针时间范围 [currentTimeMs - (currentTimeMs+tickMs)]
        if (taskExpirationTimeMs <= currentTimeMs + tickMs) {
            return false;
        } else if (taskExpirationTimeMs < currentTimeMs + interval) {
            // 表示当前时间轮可以在一个完整的轮盘内表示出当前的任务的过期时间.
            // 即：当前时间轮可以容纳这个任务.
            
            // 计算出当前时间在这个时间轮需要转多少圈（要动态的看待这个时间轮，因为它的当前时间是一直在滚动的）
            // 这个出发由于是向下取整得出的，所以其实就是代表一个时间格子的最左边，和currentTimeMs的取之概念差不多
            long rounds = taskExpirationTimeMs / tickMs;
            // 取模计算出其所属的时间格子是哪一个.
            int index = (int) (rounds % ticksPerWheel);
            TurboTimerTaskSlot slot = slots[index];
            slot.addTask(task);
            slot.setSlotExpirationTime(rounds * tickMs);
            this.slotDelayQueue.add(slot);
            task.setSlot(slot);
            
        } else {
            // 这个时间轮已经不能容纳这个任务，即代表，当前时间轮需要转多圈之后才能表示出当前的任务的过期时间.
            // 将任务放入到下一个时间跨度更大的时间轮里面.
            TurboTimerWheel overflowWheel = getOverflowWheel();
            overflowWheel.addTask(task);
        }
        return true;
    }
    
    /**
     * 获取上层时间轮.
     * <p>
     * 时间跨度更大的时间轮.
     * @return
     */
    public TurboTimerWheel getOverflowWheel() {
        if (overflowWheel == null) {
            synchronized (monitor) {
                if (overflowWheel == null) {
                    overflowWheel = new TurboTimerWheel(interval, ticksPerWheel, currentTimeMs, slotDelayQueue);
                }
            }
        }
        return overflowWheel;
    }
    
    public long getInterval() {
        return tickMs * ticksPerWheel;
    }
    
    public void driveWheel(Long slotExpirationTime) {
        // 超出当前时间轮可以表示的时间区间.
        if (slotExpirationTime >= currentTimeMs + tickMs) {
            // 重新设置当前时间轮的当前时间.
            currentTimeMs = slotExpirationTime - (slotExpirationTime % tickMs);
            if (overflowWheel != null) {
                overflowWheel.driveWheel(slotExpirationTime);
            }
        }
    }
}
