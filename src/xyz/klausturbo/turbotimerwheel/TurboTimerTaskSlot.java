package xyz.klausturbo.turbotimerwheel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 延迟任务插槽 .
 * @author <a href="mailto:guyue375@outlook.com">Klaus.turbo</a>
 * @program demo
 **/
public class TurboTimerTaskSlot implements Delayed {
    
    private AtomicLong slotExpirationTime = new AtomicLong(-1);
    
    private LinkedList<TurboTimerTask> timerTasks = new LinkedList<>();
    
    public void addTask(TurboTimerTask task) {
        synchronized (this) {
            timerTasks.add(task);
        }
    }
    
    public void execute(Consumer<TurboTimerTask> consumer) {
        synchronized (this) {
            Iterator<TurboTimerTask> iter = timerTasks.iterator();
            while (iter.hasNext()) {
                TurboTimerTask timerTask = iter.next();
                if (timerTask.getSlot() == this) {
                    consumer.accept(timerTask);
                    iter.remove();
                }
            }
            this.slotExpirationTime = new AtomicLong(-1);
        }
    }
    
    public Long getSlotExpirationTime() {
        return slotExpirationTime.get();
    }
    
    public void setSlotExpirationTime(Long slotExpirationTime) {
        this.slotExpirationTime.getAndSet(slotExpirationTime);
    }
    
    @Override
    public long getDelay(TimeUnit unit) {
        return Math
                .max(0, unit.convert((slotExpirationTime.get() - System.currentTimeMillis()), TimeUnit.MICROSECONDS));
    }
    
    @Override
    public int compareTo(Delayed o) {
        if (o instanceof TurboTimerTaskSlot) {
            return Long.compare(slotExpirationTime.get(), ((TurboTimerTaskSlot) o).slotExpirationTime.get());
        }
        return 0;
    }
}
