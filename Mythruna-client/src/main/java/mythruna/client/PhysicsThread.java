package mythruna.client;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PhysicsThread extends Thread {
    public static PhysicsThread instance = new PhysicsThread();

    private long targetFrameLength = 20000000L;

    private long maxFrameLength = 50000000L;

    private long lastTime = -1L;
    private List<PhysicsTask> tasks = new CopyOnWriteArrayList();
    private PhysicsTask[] taskArray;

    public PhysicsThread() {
        setName("AnimationThread");
        setDaemon(true);
    }

    public void addTask(PhysicsTask t) {
        this.tasks.add(t);
        this.taskArray = null;
    }

    public void removeTask(PhysicsTask t) {
        this.tasks.remove(t);
        this.taskArray = null;
    }

    private PhysicsTask[] getTaskArray() {
        if (this.taskArray == null)
            this.taskArray = ((PhysicsTask[]) this.tasks.toArray(new PhysicsTask[this.tasks.size()]));
        return this.taskArray;
    }

    private void doTasks(long delta) {
        double secs = delta / 1000000000.0D;

        PhysicsTask[] array = getTaskArray();
        for (PhysicsTask t : array) {
            if (!t.updatePhysics(this, secs))
                removeTask(t);
        }
    }

    public void poll() {
        long time = System.nanoTime();
        if (this.lastTime < 0L) {
            this.lastTime = (time - 1L);
        }

        long delta = time - this.lastTime;
        doTasks(delta);
        this.lastTime = time;
    }

    public void run() {
        long timeTest = System.nanoTime();
        while (true) {
            long time = System.nanoTime();
            long delta = time - this.lastTime;
            long testDelta = time - timeTest;
            timeTest = time;

            if (testDelta < 0L) {
                System.out.println("Time went backwards:" + testDelta);
            } else if (delta > this.targetFrameLength) {
                long off = delta - this.targetFrameLength;

                if (this.lastTime > 0L) {
                    int count = 0;
                    while (delta > this.maxFrameLength) {
                        System.out.println("Catchup frame:" + delta);
                        doTasks(this.maxFrameLength);
                        delta -= this.maxFrameLength;
                        count++;
                        if ((count > 10) && (delta > this.maxFrameLength)) {
                            delta = this.maxFrameLength;
                        }
                    }

                }

                doTasks(delta);
                this.lastTime = time;
            } else {
                try {
                    Thread.sleep(1L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}