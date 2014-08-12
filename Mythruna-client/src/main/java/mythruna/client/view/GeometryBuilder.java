package mythruna.client.view;

import mythruna.client.ClientOptions;
import mythruna.util.NamedThreadFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class GeometryBuilder {
    private static long instanceCount = 0L;

    private Map<BuilderReference, PrioritizedRef> refMap = new ConcurrentHashMap<>();
    private PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<>();

    private List<BuilderReference> working = new CopyOnWriteArrayList<>();

    private PriorityBlockingQueue<PrioritizedRef> done = new PriorityBlockingQueue<>();
    private ThreadPoolExecutor executor;

    public GeometryBuilder(int poolSize) {
        this.executor = new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, this.queue, new NamedThreadFactory("GeometryBuilder:"));
    }

    public List<BuilderReference> getWorking() {
        return this.working;
    }

    public int getPendingSize() {
        return this.queue.size() + this.done.size();
    }

    public void build(int priority, BuilderReference ref) {
        if (this.refMap.containsKey(ref)) {
            return;
        }

        PrioritizedRef pr = new PrioritizedRef(priority, ref);
        this.refMap.put(ref, pr);
        this.executor.execute(pr);
    }

    public void cancel(BuilderReference ref) {
        PrioritizedRef pr = this.refMap.remove(ref);
        if (pr == null)
            return;
        if (this.executor.remove(pr))
            this.done.remove(ref);
    }

    public int applyUpdates(LocalArea area, int max) {
        ArrayList<PrioritizedRef> temp = new ArrayList<>();
        int count = this.done.drainTo(temp, max);
        for (PrioritizedRef pr : temp) {
            pr.ref.applyUpdates(area);

            if (!(pr.ref instanceof LeafReference))
                count--;
        }
        return count;
    }

    public void terminate() {
        this.executor.shutdown();
    }

    protected class PrioritizedRef implements Runnable, Comparable<PrioritizedRef> {
        private long sequence = GeometryBuilder.instanceCount++;
        private int priority;
        private BuilderReference ref;

        public PrioritizedRef(int priority, BuilderReference ref) {
            this.priority = priority;
            this.ref = ref;
        }

        public int compareTo(PrioritizedRef pr) {
            int diff = this.priority - pr.priority;
            if (diff == 0)
                diff = (int) (this.sequence - pr.sequence);
            return diff;
        }

        public void run() {
            if (GeometryBuilder.this.refMap.remove(this.ref) == null) {
                System.out.println("Geometry already built for:" + this.ref);
                return;
            }

            try {
                GeometryBuilder.this.working.add(this.ref);
                this.ref.build();
                GeometryBuilder.this.done.put(this);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                GeometryBuilder.this.working.remove(this.ref);
            }

            long delay = ClientOptions.getInstance().getGeometryBuildDelay();
            if (delay > 0L) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}