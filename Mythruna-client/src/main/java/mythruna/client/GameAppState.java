package mythruna.client;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.input.InputManager;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.GeometryComparator;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.TransparentComparator;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import mythruna.MaterialIndex;
import mythruna.World;
import mythruna.client.anim.AnimationState;
import mythruna.client.bm.BuildModeState;
import mythruna.client.env.Environment;
import mythruna.client.gm.ItemToolState;
import mythruna.client.shell.DefaultConsole;
import mythruna.client.sound.AmbientSoundManager;
import mythruna.client.tabs.TabState;
import mythruna.client.tabs.bp.BlueprintEditorState;
import mythruna.client.tabs.map.MapState;
import mythruna.client.tabs.property.PropertyTabState;
import mythruna.client.ui.*;
import mythruna.client.view.CameraLeafPriority;
import mythruna.client.view.LocalArea;
import mythruna.client.view.RadialMenuState;
import mythruna.script.ScriptManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class GameAppState extends AbstractAppState
        implements ScreenController {
    public static final String SORT_BIAS = "sortBias";
    private Nifty nifty;
    private Screen screen;
    private ScriptManager uiInit;
    private GameClient gameClient;
    private ConveyerCamera camera;
    public static Environment environment;
    private RadialMenuState radialMenu;
    private PopupMenuState popupMenu;
    private DialogState dialogState;
    private LocalArea localArea;
    private DebugHudState debugHud;
    private boolean hudActive = true;

    private int statsType = 0;

    private float yMessageMargin = 0.0F;
    private MessageLog messageLog = MessageLog.getInstance();
    private TextField textField;
    private boolean textEntryEnabled = true;
    private MainStart app;
    private Node rootNode;
    private Node guiNode;
    private BitmapFont guiFont;
    private Camera cam;
    private Node hud;
    private boolean inGameMenuActive = false;
    private StateGroup overlays;
    private StateGroup gameModes;
    private int lastClip = -1;
    private float wavePeriod;
    private boolean lastHeadInWater = false;

    private boolean first = true;

    public GameAppState(GameClient gameClient) {
        this.gameClient = gameClient;

        this.uiInit = new ScriptManager(new Object[0]);
        this.uiInit.setScriptExtension("ui.groovy");
        this.uiInit.setBinding("gameClient", gameClient);
        this.uiInit.setBinding("console", gameClient.getConsole());
        this.uiInit.setBinding("shell", ((DefaultConsole) gameClient.getConsole()).getShell());

        this.uiInit.addScript("/mythruna/script/BaseUiEnvironment.groovy");
        this.uiInit.addScript("/mythruna/script/standard-commands.groovy");
        this.uiInit.addScript(new File("mods/ui-init"));
    }

    public GameClient getGameClient() {
        return this.gameClient;
    }

    public int getPendingSize() {
        if (this.localArea == null)
            return -1;
        return this.localArea.getPendingSize();
    }

    public int getTilesLoaded() {
        if (this.localArea == null)
            return 0;
        return this.localArea.getTilesLoaded();
    }

    public LocalArea getLocalArea() {
        return this.localArea;
    }

    public void setupMovementAndInputs(boolean includeNiftyBindings) {
        this.camera.setEnabled(true);

        initInputs(includeNiftyBindings);

        toggleStats();

        MessageLog.addMessage("Press F1 for help.");
    }

    public <T extends AppState> T getState(Class<T> type) {
        return this.app.getStateManager().getState(type);
    }

    public ConveyerCamera getCamera() {
        return this.camera;
    }

    public Node getGuiNode() {
        return this.guiNode;
    }

    public void cleanup() {
        System.out.println("----------------------------GameAppState.cleanup()---------------------------");
        this.gameClient.close();
    }

    protected void dumpMaterials() {
        try {
            FileWriter fOut = new FileWriter("materials.txt");
            PrintWriter out = new PrintWriter(fOut);
            try {
                MaterialIndex.dump(out);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            PrintWriter out;
            throw new RuntimeException("Error dumping material log", e);
        }
    }

    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        TimeLog.log(getClass().getSimpleName(), "Initializing game app state.");

        stateManager.attach(new LoadingUpdateState(this));

        MainStart mainStart = (MainStart) app;
        this.app = mainStart;
        this.rootNode = mainStart.getRootNode();
        this.guiNode = mainStart.getGuiNode();
        this.guiFont = mainStart.getGuiFont();
        this.cam = mainStart.getCamera();

        AssetManager assetManager = app.getAssetManager();
        InputManager inputManager = app.getInputManager();

        TimeLog.log(getClass().getSimpleName(), "Initializing materials");
        MaterialIndex.initialize(assetManager);
        MaterialIndex.setResolution(this.cam.getWidth(), this.cam.getHeight());

        TimeLog.log(getClass().getSimpleName(), "Setting up UI");
        app.getViewPort().getQueue().setGeometryComparator(RenderQueue.Bucket.Transparent, new MyComparator(MaterialIndex.getInstance()));

        app.getGuiViewPort().getQueue().setGeometryComparator(RenderQueue.Bucket.Gui, new GuiLayerComparator());

        app.getGuiViewPort().setClearFlags(false, true, false);

        ModeManager.instance.attachInput(inputManager);

        stateManager.attach(this.gameClient.getPlayerState());
        stateManager.attach(new AnimationState());
        stateManager.attach(new AmbientSoundManager(this.gameClient));

        stateManager.attach(new GuiAppState(this.guiNode));

        this.overlays = new StateGroup("Main Overlays", false);

        TabState tabs = new TabState(this.gameClient);
        tabs.addStates(new ObservableState[]{new MapState(this.gameClient, this)});
        tabs.addStates(new ObservableState[]{new ObservableState("Inventory", false)});
        tabs.addStates(new ObservableState[]{new PropertyTabState(this.gameClient, this)});
        tabs.addStates(new ObservableState[]{new ObservableState("Character", false)});
        tabs.addStates(new ObservableState[]{new BlueprintEditorState(this.gameClient)});

        this.overlays.addStates(new ObservableState[]{tabs});
        this.overlays.addStateListener(new OverlayStateObserver());

        this.dialogState = new DialogState(this.gameClient);
        this.overlays.addStates(new ObservableState[]{this.dialogState});

        this.radialMenu = new RadialMenuState(this.gameClient);
        this.overlays.addStates(new ObservableState[]{this.radialMenu});

        stateManager.attach(this.overlays);

        this.popupMenu = new PopupMenuState(this.gameClient);
        stateManager.attach(this.popupMenu);

        if (MainStart.enableScriptConsole) {
            GroovyConsoleState groovyConsole = new GroovyConsoleState(this.gameClient);
            stateManager.attach(groovyConsole);
        }

        System.out.println("----------------------------GameAppState.initialize()---------------------------");
        System.out.println("From thread:" + Thread.currentThread());
        this.initialized = true;

        setStats(0);

        ErrorHandler.registerExceptionHandler();

        System.out.println("bottom:" + this.cam.getFrustumBottom() + " top:" + this.cam.getFrustumTop() + " left:" + this.cam.getFrustumLeft() + " rightt:" + this.cam.getFrustumRight() + " near:" + this.cam.getFrustumNear() + " far:" + this.cam.getFrustumFar());

        System.out.println("width:" + this.cam.getWidth() + " height:" + this.cam.getHeight());

        float aspect = this.cam.getWidth() / this.cam.getHeight();
        this.cam.setFrustumPerspective(70.0F, aspect, 0.1F, this.cam.getFrustumFar());

        System.out.println("bottom:" + this.cam.getFrustumBottom() + " top:" + this.cam.getFrustumTop() + " left:" + this.cam.getFrustumLeft() + " rightt:" + this.cam.getFrustumRight() + " near:" + this.cam.getFrustumNear() + " far:" + this.cam.getFrustumFar());

        System.out.println("width:" + this.cam.getWidth() + " height:" + this.cam.getHeight());
        TimeController timeController;
        if (!this.gameClient.isRemote()) {
            timeController = new TimeController(this.gameClient.getTimeProvider(), inputManager);
        }

        World world = this.gameClient.getWorld();

        TimeLog.log(getClass().getSimpleName(), "Setting up environment.");

        environment = Environment.getInstance();
        environment.setGameClient(this.gameClient);
        stateManager.attach(environment);

        TimeLog.log(getClass().getSimpleName(), "Setting up local area");
        this.localArea = new LocalArea(this.gameClient, world);
        this.localArea.setLeafPriority(new CameraLeafPriority(this.cam));

        this.rootNode.attachChild(this.localArea);

        this.cam.setLocation(new Vector3f(0.0F, 0.0F, 0.0F));

        TimeLog.log(getClass().getSimpleName(), "Setting up camera.");

        this.camera = new ConveyerCamera(this.cam, this.gameClient, this.localArea);
        this.camera.setEnabled(false);
        stateManager.attach(this.camera);

        TimeLog.log(getClass().getSimpleName(), "Setting up additional states.");

        this.hud = new Node("HUD");
        this.guiNode.attachChild(this.hud);

        stateManager.attach(new CrosshairState(this.gameClient, this.hud, "Textures/cursor.png", 32, 32));

        this.gameModes = new StateGroup("Play Modes", true);
        this.gameModes.addStates(new ObservableState[]{new BuildModeState(this.hud, this.gameClient, this.localArea, this.camera)});
        this.gameModes.addStates(new ObservableState[]{new ItemToolState(this.gameClient, this.localArea, this.hud)});
        stateManager.attach(this.gameModes);

        this.textField = new TextField(assetManager.loadFont("Interface/knights24.fnt"), assetManager, this.cam.getWidth());

        this.hud.attachChild(this.textField);

        stateManager.attach(MessageLog.getInstance());

        this.debugHud = new DebugHudState(this.hud, this.guiFont);
        stateManager.attach(this.debugHud);

        stateManager.attach(new BloomHdrState(this.localArea));

        ((PostProcessingState) getState(PostProcessingState.class)).setLightPosition(environment.getLightPosition());

        initChatInputs();

        ScreenshotAppState screenshot = new ScreenshotAppState();
        stateManager.attach(screenshot);

        TimeLog.log(getClass().getSimpleName(), "Running startup scripts");

        this.uiInit.initialize();
        TimeLog.log(getClass().getSimpleName(), "GameAppState initialized");
    }

    public void update(float tpf) {
        long updateStart = System.nanoTime();
        if (this.first) {
            this.first = false;
        }

        if (!this.gameClient.isLoggedIn()) {
            return;
        }
        this.camera.update();

        if (!this.messageLog.isEnabled()) {
            this.messageLog.update(tpf);
        }

        this.radialMenu.checkActive();
        this.popupMenu.checkActive();
        this.dialogState.checkActive();

        this.wavePeriod = (float) (this.wavePeriod + tpf * 3.141593F * 1.5D);
        if (this.wavePeriod > 6.283186F) {
            this.wavePeriod %= 6.283186F;
        }
        MaterialIndex.setWaterWaves(FastMath.cos(this.wavePeriod) * 0.1F, FastMath.sin(this.wavePeriod) * 0.1F);

        if (this.camera.isHeadInWater() != this.lastHeadInWater) {
            this.lastHeadInWater = this.camera.isHeadInWater();
            ((PostProcessingState) getState(PostProcessingState.class)).setUnderwater(this.lastHeadInWater);
            ((AmbientSoundManager) getState(AmbientSoundManager.class)).setUnderwater(this.lastHeadInWater);
        }

        if (this.lastClip != this.localArea.getClipDistance()) {
            this.lastClip = this.localArea.getClipDistance();

            environment.setFogDistance(this.localArea.getClipDistance() * 32);

            MaterialIndex.getFogColor().a = this.lastClip * 32;
        }

        this.localArea.updateGeometry();

        long end = System.nanoTime();
        long delta = end - updateStart;
        if (delta > 5000000L) {
            System.out.println("GameAppState.update() took > 5 ms:" + delta / 1000000.0D + " ms.");
        }
    }

    public void setMessageMargin(float y) {
        if (this.yMessageMargin == y)
            return;
        this.yMessageMargin = y;
        resetLogLocation();
    }

    protected float getPreferredLogLocation() {
        float y = this.yMessageMargin;

        if (this.app.isStatsOnBottom()) {
            if (this.statsType > 0)
                y += 24.0F;
            if (this.statsType > 1) {
                y += 160.0F;
            }
        }
        if (this.textField.isActive()) {
            y += 24.0F;
        }
        return y;
    }

    protected void resetLogLocation() {
        float y = getPreferredLogLocation();
        System.out.println("Log location:" + y);
        if (y == 0.0F)
            this.messageLog.move(y, 0.2F);
        else {
            this.messageLog.move(y, 0.2F);
        }
        if (this.textField.isActive()) {
            this.textField.setLocalTranslation(0.0F, y - 24.0F, 0.0F);
        }
    }

    public void setEnableTextEntry(boolean b) {
        if (this.textEntryEnabled == b)
            return;
        this.textEntryEnabled = b;
        if ((!this.textEntryEnabled) && (this.textField.isActive())) {
            this.textField.setText("");
            toggleTextEntry();
        }
    }

    public void toggleTextEntry2() {
        toggleTextEntry();
    }

    public void toggleTextEntry() {
        boolean a = this.textField.isActive();
        a = !a;
        if ((a) && (!this.textEntryEnabled)) {
            return;
        }
        this.textField.setActive(a);
        if (a) {
            this.overlays.setEnabled(false);
            InputRedirector.getInstance().addRawKeyListener(this.textField);
            this.messageLog.setAllVisible(true);
        } else {
            InputRedirector.getInstance().removeRawKeyListener(this.textField);
            this.messageLog.setAllVisible(false);

            String s = this.textField.getText();
            if (s.length() > 0) {
                this.gameClient.getConsole().runCommand(s);
                this.textField.setText("");
            }
        }

        resetLogLocation();
    }

    public void toggleQuality() {
        MaterialIndex.setVertexLighting(!MaterialIndex.getVertexLighting());
        MaterialIndex.setLowQuality(!MaterialIndex.getLowQuality());
        if (MaterialIndex.getLowQuality())
            MessageLog.addMessage("Set low quality shading. (Incomplete and experimental.)");
        else
            MessageLog.addMessage("Set high quality shading.");
    }

    public void setStatsOnBottom(boolean b) {
        System.out.println("GameAppState.setStatsOnBottom(" + b + ")");
        this.app.setStatsOnBottom(b);
        resetLogLocation();
    }

    public void setStats(int t) {
        this.statsType = t;
        if (this.statsType > 2)
            this.statsType = 0;
        this.app.setDisplayFps(this.statsType > 0);
        this.app.setDisplayStatView(this.statsType > 1);
    }

    public void toggleStats() {
        setStats(this.statsType + 1);

        resetLogLocation();

        if (this.statsType == 0)
            MessageLog.addMessage("Stats off.");
        else if (this.statsType == 1)
            MessageLog.addMessage("FPS display on.");
        else if (this.statsType == 2)
            MessageLog.addMessage("Full stats display on.");
    }

    public void incrementClip() {
        int current = this.localArea.getClipDistance();
        current++;
        if (current > this.localArea.getMaxClipDistance())
            current = 2;
        System.out.println("New clip distance:" + current);
        this.localArea.setClipDistance(current);
        MessageLog.addMessage("Set clip to: " + current * 32 + " m");
    }

    public boolean toggleHud() {
        return setHudEnabled(!this.hudActive);
    }

    public boolean toggleDebugHud() {
        if (this.debugHud.isEnabled()) {
            this.debugHud.setEnabled(false);
            MessageLog.addMessage("Debug info off.");
            return false;
        }
        MessageLog.addMessage("Debug info on.");
        this.debugHud.setEnabled(true);
        return true;
    }

    public boolean toggleGameMode() {
        if (((BuildModeState) getState(BuildModeState.class)).isEnabled()) {
            MessageLog.addMessage("Game Mode on.");
            ((ItemToolState) getState(ItemToolState.class)).setEnabled(true);
            return true;
        }

        MessageLog.addMessage("Build Mode on.");
        ((BuildModeState) getState(BuildModeState.class)).setEnabled(true);
        return false;
    }

    public void toggleVideoRecording() {
        VideoRecorderAppState state = (VideoRecorderAppState) getState(VideoRecorderAppState.class);
        if (state == null) {
            File f = new File("Mythruna-" + System.currentTimeMillis() + ".mpg");
            MessageLog.addMessage("Recording: " + f);
            state = new VideoRecorderAppState();
            state.setFile(f);
            this.app.getStateManager().attach(state);
        } else {
            this.app.getStateManager().detach(state);
            MessageLog.addMessage("Stopped recording: " + state.getFile());
        }
    }

    public boolean setHudEnabled(boolean enabled) {
        if (this.hudActive == enabled) {
            return this.hudActive;
        }
        this.hudActive = enabled;
        if (this.hudActive) {
            this.gameModes.setEnabled(true);
            this.messageLog.setEnabled(true);
            this.hud.setCullHint(Spatial.CullHint.Never);
        } else {
            this.gameModes.setEnabled(false);
            this.messageLog.setEnabled(false);
            this.hud.setCullHint(Spatial.CullHint.Always);
        }

        return this.hudActive;
    }

    private void initChatInputs() {
        KeyMethodAction a5 = new KeyMethodAction(this, "toggleTextEntry", 28);
        KeyMethodAction a6 = new KeyMethodAction(this, "toggleTextEntry2", 156);
        a5.attach(this.app.getInputManager());
        a6.attach(this.app.getInputManager());
    }

    private void initInputs(boolean includeNiftyBindings) {
        KeyMethodAction a1 = new KeyMethodAction(this, "toggleHud", 61);
        KeyMethodAction a6 = new KeyMethodAction(this, "toggleDebugHud", 63);
        KeyMethodAction a2 = new KeyMethodAction(this, "incrementClip", 88);
        KeyMethodAction a4 = new KeyMethodAction(this, "toggleStats", 87);
        KeyMethodAction a5 = new KeyMethodAction(this, "openHelp", 59);
        KeyMethodAction a7 = new KeyMethodAction(this, "toggleQuality", 66);
        KeyMethodAction a8 = new KeyMethodAction(this, "toggleGameMode", 34);
        KeyMethodAction a9 = new KeyMethodAction(this, "toggleVideoRecording", 65);

        a1.attach(this.app.getInputManager());
        a2.attach(this.app.getInputManager());
        a4.attach(this.app.getInputManager());
        a5.attach(this.app.getInputManager());
        a6.attach(this.app.getInputManager());
        a7.attach(this.app.getInputManager());
        a8.attach(this.app.getInputManager());
        a9.attach(this.app.getInputManager());

        if (includeNiftyBindings) {
            this.app.getInputManager().deleteMapping("SIMPLEAPP_Exit");
        }
        this.app.getInputManager().deleteMapping("SIMPLEAPP_HideStats");
        this.app.getInputManager().deleteMapping("SIMPLEAPP_Memory");

        if (includeNiftyBindings) {
            KeyMethodAction escape = new KeyMethodAction(this, "inGameMenu", 1);
            escape.attach(this.app.getInputManager());
        }

        KeyMethodAction tab = new KeyMethodAction(this, "toggleTabs", 15);
        tab.attach(this.app.getInputManager());
    }

    public void openHelp() {
        this.gameClient.execute("Open Help", this.gameClient.getPlayer(), null);
    }

    public void inGameMenu() {
        if (this.textField.isActive()) {
            this.textField.setText("");
            toggleTextEntry();
            return;
        }

        if (this.inGameMenuActive) {
            resume();
            return;
        }

        if (this.nifty == null) {
            return;
        }

        this.overlays.setEnabled(false);

        this.inGameMenuActive = true;
        System.out.println("Opening in-game menu.");
        setHudEnabled(false);
        this.camera.setEnabled(false);
        this.nifty.gotoScreen("in-game-menu");
    }

    public void toggleTabs() {
        System.out.println("toggleTabs()");
        TabState tabs = (TabState) getState(TabState.class);
        boolean enabled = !tabs.isEnabled();
        tabs.setEnabled(enabled);
    }

    public void exit() {
        this.app.stop();
    }

    public void go(String screen) {
        System.out.println("go(" + screen + ")");
        this.nifty.gotoScreen(screen);
    }

    public void resume() {
        System.out.println("resume()");
        this.inGameMenuActive = false;
        this.nifty.gotoScreen("empty");
        setHudEnabled(true);
        this.camera.setEnabled(true);
    }

    public void bind(Nifty nifty, Screen screen) {
        System.out.println("---------------------bind:" + screen);
        this.nifty = nifty;
        this.screen = screen;
    }

    public void onStartScreen() {
        System.out.println("---------------------onStartScreen:" + this.screen.getScreenId());
    }

    public void onEndScreen() {
        System.out.println("---------------------onEndScreen:" + this.screen.getScreenId());
    }

    private static class MyComparator
            implements GeometryComparator {
        private Material water;
        private Material water2;
        private Material leaves;
        private Material leaves2;
        private Material leaves3;
        private Material pine;
        private Material glass;
        private Material fire;
        private Material grass;
        private GeometryComparator delegate = new TransparentComparator();

        public MyComparator(MaterialIndex index) {
            this.water = MaterialIndex.getMaterial(7);
            this.water2 = MaterialIndex.getMaterial(24);
            this.leaves = MaterialIndex.getMaterial(9);
            this.leaves2 = MaterialIndex.getMaterial(25);
            this.leaves3 = MaterialIndex.getMaterial(26);
            this.pine = MaterialIndex.getMaterial(32);
            this.glass = MaterialIndex.getMaterial(18);
            this.fire = MaterialIndex.getMaterial(12);
            this.grass = MaterialIndex.getMaterial(22);
        }

        public void setCamera(Camera cam) {
            this.delegate.setCamera(cam);
        }

        public int getMaterialPriority(Material m) {
            if (m == this.water2)
                return 2;
            if (m == this.water)
                return 1;
            if ((m == this.leaves) || (m == this.leaves2) || (m == this.leaves3) || (m == this.pine))
                return 0;
            if (m == this.glass)
                return 3;
            if (m == this.grass) {
                return 4;
            }
            return 5;
        }

        public int getLayer(Geometry g) {
            Integer layer = (Integer) g.getUserData("sortBias");
            if (layer == null)
                return 0;
            return layer.intValue();
        }

        public int compare(Geometry o1, Geometry o2) {
            Material m1 = o1.getMaterial();
            Material m2 = o2.getMaterial();

            int p1 = getMaterialPriority(m1);
            int p2 = getMaterialPriority(m2);

            if (p1 < p2)
                return -1;
            if (p1 > p2) {
                return 1;
            }
            p1 = getLayer(o1);
            p2 = getLayer(o2);

            if (p1 < p2)
                return -1;
            if (p1 > p2) {
                return 1;
            }
            return this.delegate.compare(o1, o2);
        }
    }

    private class OverlayStateObserver
            implements AppStateListener {
        private OverlayStateObserver() {
        }

        public void stateEnabled(ObservableState state, boolean enabled) {
            GameAppState.this.setHudEnabled(!enabled);
            GameAppState.this.getCamera().setEnabled(!enabled);
        }
    }
}