package mythruna.client.anim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ContainerAnimation
        implements AnimationTask {
    private boolean keepGoing = true;
    private AnimatedContainer last;
    private AnimatedContainer current;
    private State state = State.IDLE;
    private List<AnimationTask> tasks = new ArrayList();

    public ContainerAnimation() {
    }

    public void stop() {
        this.keepGoing = false;
    }

    public void setContainer(AnimatedContainer container) {
        if (this.current != null) {
            switch (this.state.ordinal()) {
                case 1:
                    close();
                    break;
                case 2:
                    break;
                case 3:
                    this.tasks.clear();
                    close();
            }

        }

        this.current = container;

        if (this.state == State.IDLE)
            open();
    }

    protected void close() {
        if (this.current == null) {
            return;
        }
        this.tasks.addAll(Arrays.asList(this.current.animateClose()));
        this.last = this.current;
        this.state = State.CLOSING;
    }

    protected void open() {
        if (this.current == null) {
            this.state = State.IDLE;
            return;
        }

        this.current.attach();
        this.tasks.addAll(Arrays.asList(this.current.animateOpen()));
        this.state = State.OPENING;
    }

    public boolean animate(AnimationState anim, float seconds) {
        if ((this.state == State.IDLE) && (this.tasks.isEmpty())) {
            return this.keepGoing;
        }
        for (Iterator it = this.tasks.iterator(); it.hasNext(); ) {
            AnimationTask t = (AnimationTask) it.next();
            boolean keep = t.animate(anim, seconds);
            if (!keep) {
                it.remove();
            }
        }
        if (this.tasks.isEmpty()) {
            switch (this.state.ordinal()) {
                case 3:
                    this.state = State.IDLE;
                    break;
                case 2:
                    this.last.detach();
                    this.last = null;

                    open();
            }

        }

        return (this.keepGoing) || (!this.tasks.isEmpty());
    }

    private static enum State {
        IDLE, OPENING, CLOSING
    }
}