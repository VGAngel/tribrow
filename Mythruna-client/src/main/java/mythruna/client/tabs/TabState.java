package mythruna.client.tabs;

import com.jme3.app.Application;
import com.jme3.audio.AudioNode;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import mythruna.client.ClientOptions;
import mythruna.client.GameAppState;
import mythruna.client.GameClient;
import mythruna.client.anim.Animation;
import mythruna.client.anim.AnimationState;
import mythruna.client.anim.AnimationTask;
import mythruna.client.ui.*;

import java.util.HashMap;
import java.util.Map;

public class TabState extends StateGroup {
    private GameClient gameClient;
    private GuiAppState guiState;
    private AudioNode switchTabs;
    private AudioNode switchOff;
    private Node buttons;
    private float buttonScale = 1.0F;

    private ColorRGBA enabledColor = new ColorRGBA(0.2039216F, 0.4431373F, 0.839216F, 1.0F);

    private Map<ObservableState, Tab> tabs = new HashMap();

    public TabState(GameClient gameClient) {
        super("Tabs", false);
        this.gameClient = gameClient;
    }

    public Node getTabRoot() {
        return this.guiState.getPerspectiveRoot();
    }

    public Camera getTabCamera() {
        return this.guiState.getCamera();
    }

    public void toggleEnabled() {
        setEnabled(!isEnabled());
    }

    protected void isFocused(ObservableState state) {
        this.switchTabs.setVolume(ClientOptions.getInstance().getSoundEffectsVolume() * 0.8F);
        this.switchTabs.play();
    }

    protected void initialize(Application app) {
        super.initialize(app);

        this.guiState = ((GuiAppState) getState(GuiAppState.class));

        Camera cam = app.getCamera();

        this.buttonScale = Math.min(1.0F, cam.getWidth() / 1000.0F);
        this.buttons = new Node("TabButtons");

        float buttonSpacing = 34.0F * this.buttonScale;
        int i = getStates().size() - 1;
        for (ObservableState state : getStates()) {
            Tab tab = new Tab(state);
            Button b = tab.getButton();
            b.setLocalTranslation(0.0F, i * buttonSpacing, 0.0F);
            i--;
            this.tabs.put(state, tab);
            this.buttons.attachChild(b);
        }

        this.buttons.setLocalTranslation(10.0F, cam.getHeight() - buttonSpacing * getStates().size() - 10.0F, 0.0F);

        this.switchTabs = new AudioNode(app.getAudioRenderer(), app.getAssetManager(), "Sounds/switch.ogg", false);
        this.switchTabs.setPositional(false);
        this.switchOff = new AudioNode(app.getAudioRenderer(), app.getAssetManager(), "Sounds/switch-off-short.ogg", false);
        this.switchOff.setPositional(false);
    }

    protected void enable() {
        super.enable();
        Node gui = ((GameAppState) getState(GameAppState.class)).getGuiNode();
        this.guiState.addDependent(this);
        gui.attachChild(this.buttons);
    }

    protected void disable() {
        super.disable();
        Node gui = ((GameAppState) getState(GameAppState.class)).getGuiNode();

        this.switchOff.setVolume(ClientOptions.getInstance().getSoundEffectsVolume() * 0.8F);
        this.switchOff.play();

        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.detach(this.buttons, 0.25F)});

        this.guiState.removeDependent(this, 0.25F);
    }

    protected Button createButton(String text, float x, float y) {
        Button button = new Button(getApplication(), 151.0F, 34.0F);
        button.setText(text);
        button.setLocalTranslation(x, y, 10.0F);
        button.addControl(new MouseEventControl());
        button.setLocalScale(this.buttonScale);
        return button;
    }

    public void render(RenderManager rm) {
        super.render(rm);
    }

    public void update(float tpf) {
    }

    private class Tab implements Command, AppStateListener {
        private Button tabSelector;
        private ObservableState appState;

        public Tab(ObservableState appState) {
            this.appState = appState;
            appState.addStateListener(this);
            this.tabSelector = TabState.this.createButton(appState.getName(), 0.0F, 0.0F);
            this.tabSelector.addCommand(this);
            stateEnabled(appState, appState.isEnabled());
        }

        public Button getButton() {
            return this.tabSelector;
        }

        public void stateEnabled(ObservableState state, boolean enabled) {
            if (enabled)
                this.tabSelector.setTextColor(TabState.this.enabledColor);
            else
                this.tabSelector.setTextColor(null);
        }

        public void execute(Object b, Object action) {
            this.appState.setEnabled(true);
        }
    }
}