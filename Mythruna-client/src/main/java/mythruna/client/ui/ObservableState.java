package mythruna.client.ui;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import org.progeeks.util.log.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ObservableState extends AbstractAppState {
    private static Log log = Log.getLog();
    private String name;
    private AppStateManager stateManager;
    private Application app;
    private List<AppStateListener> listeners = new CopyOnWriteArrayList();
    private boolean properlyInitialized = false;

    public ObservableState(String name, boolean enabled) {
        super.setEnabled(enabled);
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public final void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        this.stateManager = stateManager;
        this.app = app;
        initialize(app);

        if (!this.properlyInitialized) {
            log.warn("Warning: Some subclass broke the super.initialize(app) chain:" + getClass().getName());
        }
        if (isEnabled())
            enable();
    }

    public void stateDetached(AppStateManager stateManager) {
        if (isEnabled())
            disable();
        super.stateDetached(stateManager);
        this.stateManager = null;
        this.properlyInitialized = false;
    }

    protected Application getApplication() {
        return this.app;
    }

    protected AppStateManager getStateManager() {
        return this.stateManager;
    }

    protected <T extends AppState> T getState(Class<T> type) {
        return this.stateManager.getState(type);
    }

    public void addStateListener(AppStateListener l) {
        this.listeners.add(l);
    }

    public void removeStateListener(AppStateListener l) {
        this.listeners.remove(l);
    }

    public void setEnabled(boolean enabled) {
        if (isEnabled() == enabled) {
            return;
        }
        if (log.isTraceEnabled())
            log.trace("ObservableState.setEnabled(" + enabled + ")  stateManager:" + getStateManager());
        super.setEnabled(enabled);

        for (AppStateListener l : this.listeners) {
            l.stateEnabled(this, enabled);
        }
        if ((enabled) && (getStateManager() != null))
            enable();
        else if ((!enabled) && (getStateManager() != null))
            disable();
    }

    protected void initialize(Application app) {
        this.properlyInitialized = true;
    }

    protected void enable() {
    }

    protected void disable() {
    }
}