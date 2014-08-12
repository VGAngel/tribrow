package mythruna.client.ui;

import com.jme3.app.Application;
import org.progeeks.util.log.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class StateGroup extends ObservableState {
    static Log log = Log.getLog();

    private List<ObservableState> states = new CopyOnWriteArrayList();
    private ChildObserver childObserver = new ChildObserver();
    private ObservableState defaultState = null;
    private ObservableState lastState = null;
    private boolean ignoreEvents = false;

    public StateGroup(String name, boolean enabled) {
        super(name, enabled);
    }

    public ObservableState getCurrentState() {
        return this.lastState;
    }

    protected void initialize(Application app) {
        super.initialize(app);
        log.trace("StateGroup.initialize()***************************");

        for (ObservableState s : this.states) {
            if (!isEnabled()) {
                s.setEnabled(false);
            }
            if (log.isTraceEnabled())
                log.trace("attaching:" + s);
            getStateManager().attach(s);
        }
    }

    protected void enable() {
        super.enable();
        log.trace("StateGroup.enable()");

        resetDefaultState();
    }

    protected void disable() {
        super.disable();
        log.trace("StateGroup.disable()");
        this.ignoreEvents = true;
        try {
            for (ObservableState as : this.states)
                as.setEnabled(false);
        } finally {
            this.ignoreEvents = false;
        }
    }

    public void setDefaultState(ObservableState state) {
        if ((state != null) && (this.states.indexOf(state) < 0))
            addStates(new ObservableState[]{state});
        this.defaultState = state;
    }

    public void addStates(ObservableState[] array) {
        for (ObservableState as : array) {
            this.states.add(as);
            as.addStateListener(this.childObserver);
        }
    }

    public void removeStates(ObservableState[] array) {
        for (ObservableState as : array) {
            as.removeStateListener(this.childObserver);
            this.states.remove(as);
        }
    }

    public List<ObservableState> getStates() {
        return this.states;
    }

    protected ObservableState getStartingState() {
        if (this.defaultState != null)
            return this.defaultState;
        if (this.lastState != null)
            return this.lastState;
        if (!this.states.isEmpty())
            return (ObservableState) this.states.get(0);
        return null;
    }

    protected void resetDefaultState() {
        ObservableState state = getStartingState();
        if (state != null)
            resetModalState(state);
    }

    protected void isFocused(ObservableState state) {
    }

    protected void resetModalState(ObservableState state) {
        this.ignoreEvents = true;
        try {
            this.lastState = state;

            if (!isEnabled()) {
                setEnabled(true);
            }

            for (ObservableState as : this.states) {
                if (as != state) {
                    as.setEnabled(false);
                }
            }
            state.setEnabled(true);
            isFocused(state);
        } finally {
            this.ignoreEvents = false;
        }
    }

    private class ChildObserver implements AppStateListener {
        private ChildObserver() {
        }

        public void stateEnabled(ObservableState state, boolean enabled) {
            if (StateGroup.this.ignoreEvents) {
                return;
            }
            if (!enabled)
                StateGroup.this.setEnabled(false);
            else
                StateGroup.this.resetModalState(state);
        }
    }
}