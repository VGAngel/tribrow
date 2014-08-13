/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;

/**
 * @author normenhansen
 */
public class ConfigAppState extends AbstractAppState {

    private SimpleApplication app;
    private AssetManager assetManager;
    private QuixoteState state = QuixoteState.RELAXED;
    private Spatial quixote;
    private boolean quixoteRunning = false;

    public enum QuixoteState {

        AGITATED, RELAXED, SLEEPING
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        this.assetManager = app.getAssetManager();
        setQuixoteRunning(true);
    }

    public void setQuixoteRunning(boolean quixoteRunning) {
        this.quixoteRunning = quixoteRunning;
        if (quixoteRunning) {
            quixote = assetManager.loadModel("assets/Models/Quixote.j3o");
            applyQuixoteState(quixote, state);
            Node scene = (Node) app.getRootNode().getChild("Scene");
            if (scene != null) {
                scene.attachChild(quixote);
            }
        } else {
            if (quixote != null) {
                quixote.removeFromParent();
            }
        }
    }

    public boolean isQuixoteRunning() {
        return quixoteRunning;
    }

    public QuixoteState getState() {
        return state;
    }

    public void setState(QuixoteState state) {
        this.state = state;
        final QuixoteState quxSt = state;
        app.getRootNode().depthFirstTraversal(new SceneGraphVisitor() {
            public void visit(Spatial spatial) {
                applyQuixoteState(spatial, quxSt);
            }
        });
    }

    private void applyQuixoteState(Spatial spatial, QuixoteState state) {
        QuixoteControl control = spatial.getControl(QuixoteControl.class);
        if (control != null) {
            switch (state) {
                case AGITATED:
                    control.setSpeed(5.0f);
                    break;
                case RELAXED:
                    control.setSpeed(1.0f);
                    break;
                case SLEEPING:
                    control.setSpeed(0);
                    break;
            }
        }
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
    }

    @Override
    public void cleanup() {
        setQuixoteRunning(false);
        super.cleanup();
    }
}
