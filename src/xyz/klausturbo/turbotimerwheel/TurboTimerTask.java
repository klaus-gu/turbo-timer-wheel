package xyz.klausturbo.turbotimerwheel;

/**
 * 时间轮任务的包装 .
 * @author <a href="mailto:guyue375@outlook.com">Klaus.turbo</a>
 * @program demo
 **/
public class TurboTimerTask {
    
    /**
     * 延迟的毫秒数.
     */
    private long delayMs;
    
    /**
     * 延迟任务本身.
     */
    private Runnable runnable;
    
    /**
     * 所属的时间轮的插槽.
     */
    private TurboTimerTaskSlot slot;
    
    /**
     * 任务描述：用于测试使用.
     */
    private String desc;
    
    /**
     * 任务实际的执行时间.
     */
    private long executeTimeMs;
    
    public TurboTimerTask(long delayMs, Runnable runnable) {
        this.delayMs = delayMs;
        this.runnable = runnable;
        this.executeTimeMs = System.currentTimeMillis() + delayMs;
    }
    
    public long getDelayMs() {
        return executeTimeMs;
    }
    
    public Runnable getRunnable() {
        return runnable;
    }
    
    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }
    
    public TurboTimerTaskSlot getSlot() {
        return slot;
    }
    
    public void setSlot(TurboTimerTaskSlot slot) {
        this.slot = slot;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    public long getExecuteTimeMs() {
        return executeTimeMs;
    }
    
    public void setExecuteTimeMs(long executeTimeMs) {
        this.executeTimeMs = executeTimeMs;
    }
}
