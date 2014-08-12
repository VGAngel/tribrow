package mythruna.client.anim;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AnimationState extends AbstractAppState {
    private List<AnimationTask> tasks = new CopyOnWriteArrayList();

    public AnimationState() {
    }

    public List<AnimationTask> add(AnimationTask[] array) {
        List list = Arrays.asList(array);
        this.tasks.addAll(list);
        return list;
    }

    public void remove(AnimationTask[] array) {
        this.tasks.removeAll(Arrays.asList(array));
    }

    public void remove(List<AnimationTask> list) {
        this.tasks.removeAll(list);
    }

    public boolean hasTask(AnimationTask task) {
        return this.tasks.contains(task);
    }

    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
    }

    public void update(float tpf) {
        for (AnimationTask t : this.tasks) {
            boolean keepGoing = t.animate(this, tpf);
            if (!keepGoing)
                this.tasks.remove(t);
        }
    }
}