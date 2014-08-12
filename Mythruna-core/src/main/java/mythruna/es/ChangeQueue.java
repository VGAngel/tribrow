package mythruna.es;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChangeQueue extends ConcurrentLinkedQueue<EntityChange> {

    private ObservableEntityData parent;
    private QueueChangeListener listener;

    public ChangeQueue(ObservableEntityData parent, Class[] types) {
        this.parent = parent;
        this.listener = new QueueChangeListener(types);
    }

    protected EntityComponentListener getListener() {
        return this.listener;
    }

    public void release() {
        this.parent.releaseChangeQueue(this);
    }

    protected class QueueChangeListener implements EntityComponentListener {
        private Set<Class> types = new HashSet<>();

        public QueueChangeListener(Class[] types) {
            this.types.addAll(Arrays.asList(types));
        }

        public void componentChange(EntityChange change) {
            if (this.types.contains(change.getComponentType()))
                ChangeQueue.this.add(change);
        }
    }
}