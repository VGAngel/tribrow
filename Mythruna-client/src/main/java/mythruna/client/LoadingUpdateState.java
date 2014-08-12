package mythruna.client;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;

public class LoadingUpdateState extends AbstractAppState {

    private Progress progress;
    private GameAppState gameState;
    private AppStateManager stateManager;
    private int lastSize;

    public LoadingUpdateState(GameAppState gameState) {
        this.gameState = gameState;
        this.progress = Progress.get("tileLoading");
        this.progress.setMax(125);
    }

    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.stateManager = stateManager;
    }

    public void update(float tpf) {
        int size = this.gameState.getTilesLoaded();
        if (size != this.lastSize) {
            this.lastSize = size;
            this.progress.setProgress(size);
            this.progress.setMessage("Loading World...");
        }

        if (this.progress.isDone()) {
            this.stateManager.detach(this);
        }
    }
}