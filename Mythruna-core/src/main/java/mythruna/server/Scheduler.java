package mythruna.server;

import org.progeeks.util.log.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Scheduler {

    static Log log = Log.getLog();

    private Map<String, TaskEntry> namedTasks = new ConcurrentHashMap();
    private ScheduledThreadPoolExecutor executor;

    public Scheduler(int poolSize) {
        this.executor = new ScheduledThreadPoolExecutor(poolSize);
    }

    public void shutdown() {
        log.info("Shutting down scheduler.");
        this.executor.shutdown();
        log.info("Scheduler shutdown.");
    }

    public synchronized void scheduleNamedTask(String name, long minTime, Runnable r) {
        TaskEntry e = (TaskEntry) this.namedTasks.get(name);
        if (e == null) {
            e = new TaskEntry(name);
            this.namedTasks.put(name, e);
        }
        e.scheduleNext(r, minTime);
    }

    public void cancelNamedTask(String name) {
        TaskEntry e = (TaskEntry) this.namedTasks.remove(name);
        if (e == null)
            return;
        e.cancel();
    }

    public ScheduledThreadPoolExecutor getExecutor() {
        return this.executor;
    }

    private class TaskEntry implements Runnable {
        String name;
        long lastRuntime;
        AtomicReference<Runnable> task = new AtomicReference();

        public TaskEntry(String name) {
            this.name = name;
        }

        public void scheduleNext(Runnable r, long minTime) {
            Runnable old = (Runnable) this.task.getAndSet(r);
            if (old != null) {
                System.out.println("Task is already scheduled.");
                return;
            }

            long delay = 0L;
            if (this.lastRuntime > 0L) {
                long target = this.lastRuntime + minTime;
                delay = target - System.currentTimeMillis();
                if (delay < 0L) {
                    delay = 0L;
                }
            }
            Scheduler.log.info("Running " + this.name + " in:" + delay + " ms.");
            Scheduler.this.executor.schedule(this, delay, TimeUnit.MILLISECONDS);
        }

        public void cancel() {
            this.task.set(null);
        }

        public void run() {
            this.lastRuntime = System.currentTimeMillis();
            Runnable toRun = (Runnable) this.task.getAndSet(null);
            toRun.run();
        }
    }
}