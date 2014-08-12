package mythruna.client;

import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.StatsView;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.Caps;
import com.jme3.system.AppSettings;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.ScreenController;
import mythruna.GameConstants;
import mythruna.client.ui.InputRedirector;
import mythruna.shell.MemoryCommand;
import mythruna.util.LogAdapter;
import org.progeeks.util.Inspector;
import org.progeeks.util.log.Log;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class MainStart extends SimpleApplication {

    static Log log = Log.getLog();

    public static AssetManager globalAssetManager;
    private boolean hasFocus;
    public static AppStateManager globalStateManager;
    public static MainStart instance;
    private static Thread glThread;
    private static boolean bypassMenus = false;

    public static boolean enableScriptConsole = false;
    private boolean first;
    private boolean statsOnBottom;

    public MainStart() {
        this.first = true;
        this.statsOnBottom = true;
    }

    public static void main(String[] args) throws Exception {
        try {
            System.out.println("Starting up Mythruna...");
            startup(args);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Startup Error", e.getMessage());
        }
    }

    public static void showError(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, 0);
    }

    public static void startup(String[] args) throws Exception {
        rollClientLog();

        Log.initialize(MainStart.class.getResource("/client-log4j.xml"));

        log.info("Build version:" + GameConstants.buildVersion());

        File clientLog = new File("client.log");
        if (!clientLog.exists()) {
            throw new RuntimeException("Error writing client.log to current directory.\nThis may indicate a permission problem writing to this path.\nTry installing in a different location.");
        }

        ErrorHandler.initialize();

        LogAdapter.initialize();

        for (String s : args) {
            if ("-script".equals(s)) {
                enableScriptConsole = true;
            }
        }

        MainStart app = new MainStart();

        app.setPauseOnLostFocus(false);

        BufferedImage[] icons = {
                ImageIO.read(MainStart.class.getResource("/globe-128.png")),
                ImageIO.read(MainStart.class.getResource("/globe-32.png")),
                ImageIO.read(MainStart.class.getResource("/globe-16.png"))};

        AppSettings settings = new AppSettings(false);
        settings.setTitle("Mythruna RCG v0.1");
        settings.setSettingsDialogImage("/Interface/mythruna-title.png");
        settings.setIcons(icons);

        if (ClientOptions.getInstance().isFirstTime()) {
            settings.setWidth(1280);
            settings.setHeight(720);
            ClientOptions.getInstance().setFirstTime(false);
            ClientOptions.getInstance().save();
        }

        app.setSettings(settings);
        app.start();
    }

    public static void rollClientLog() {
        int maxLogs = 5;

        File last = new File("client-" + maxLogs + ".log");
        if (last.exists()) {
            if (!last.delete()) {
                System.err.println("Error removing old log file:" + last);
            }
        }
        for (int i = maxLogs - 1; i > 0; i--) {
            File current = new File("client-" + i + ".log");
            if (current.exists()) {
                if (!current.renameTo(last))
                    System.err.println("Error renaming:" + current + " to:" + last);
            }
            last = current;
        }

        File current = new File("client.log");
        if (current.exists()) {
            if (!current.renameTo(last))
                System.err.println("Error renaming:" + current + " to:" + last);
        }
    }

    public void gainFocus() {
        super.gainFocus();
        this.hasFocus = true;
    }

    public void loseFocus() {
        this.hasFocus = false;
    }

    public BitmapFont getGuiFont() {
        return this.guiFont;
    }

    public void setStatsOnBottom(boolean b) {
        System.out.println("GameAppState.setStatsOnBottom(" + b + ")");
        if (this.statsOnBottom == b)
            return;
        this.statsOnBottom = b;
        StatsAppState statsState = (StatsAppState) this.stateManager.getState(StatsAppState.class);

        BitmapText fps = statsState.getFpsText();
        StatsView stats = statsState.getStatsView();
        if (this.statsOnBottom) {
            System.out.println("Moving stats to:" + fps.getLineHeight());
            fps.setLocalTranslation(0.0F, this.fpsText.getLineHeight(), 0.0F);
            stats.setLocalTranslation(0.0F, this.fpsText.getLineHeight(), 0.0F);
        } else {
            System.out.println("Moving stats to:" + this.cam.getHeight());
            fps.setLocalTranslation(0.0F, this.cam.getHeight(), 0.0F);
            stats.setLocalTranslation(0.0F, this.cam.getHeight() - 165, 0.0F);
        }
    }

    public boolean isStatsOnBottom() {
        return this.statsOnBottom;
    }

    public void handleError(String errMsg, Throwable t) {
        System.out.println("******handleError( " + errMsg + " )");

        log.error("Unhandled error:" + errMsg, t);
        try {
            ErrorHandler.handle(t, false);
            super.handleError(errMsg, t);
        } catch (Throwable e) {
            log.error("Error handling error", e);
        } finally {
            log.error("Default handling did not exit.");

            System.exit(-1);
        }
    }

    public void writeDisplayInfo(PrintWriter out) {
        out.println("Display adapter information:");

        Class display = Inspector.getClassForName("org.lwjgl.opengl.Display");

        if (display != null) {
            Inspector ins = new Inspector(display);
            out.println("Adapter:" + ins.get("adapter"));
            out.println("Driver Version:" + ins.get("version"));
        } else {
            out.println("Could not find display class: org.lwjgl.opengl.Display");
        }

        Class gl11 = Inspector.getClassForName("org.lwjgl.opengl.GL11");
        if (gl11 != null) {
            Inspector ins = new Inspector(gl11);

            Object key = Inspector.getConstant("GL_VENDOR", gl11);
            out.println("Vendor:" + ins.callMethod("glGetString", new Object[]{key}));

            key = Inspector.getConstant("GL_VERSION", gl11);
            out.println("Version:" + ins.callMethod("glGetString", new Object[]{key}));

            key = Inspector.getConstant("GL_RENDERER", gl11);
            out.println("Renderer:" + ins.callMethod("glGetString", new Object[]{key}));
        } else {
            System.out.println("Could not find GL11 class: org.lwjgl.opengl.GL11");
        }

        Class gl20 = Inspector.getClassForName("org.lwjgl.opengl.GL20");

        if (getRenderer().getCaps().contains(Caps.OpenGL20)) {
            Inspector ins = new Inspector(gl11);

            Object key = Inspector.getConstant("GL_SHADING_LANGUAGE_VERSION", gl20);
            out.println("GLSL Ver:" + ins.callMethod("glGetString", new Object[]{key}));
        } else {
            out.println("Driver does not support OpenGL 2.0");
        }
    }

    public void writeMemInfo(PrintWriter out) {
        out.println("Memory usage:\n" + MemoryCommand.getMemoryString());
    }

    public void writeStats(PrintWriter out) {
        String[] labels = getRenderer().getStatistics().getLabels();
        int[] data = new int[labels.length];
        getRenderer().getStatistics().getData(data);

        out.println("Statistics:");
        for (int i = 0; i < labels.length; i++)
            out.println("  " + labels[i] + " = " + data[i]);
    }

    public void writeAppInfo(PrintWriter out) {
        if ((Thread.currentThread() != glThread) && (glThread != null)) {
            System.out.println("Gathering app info from OpenGL thread...");
            out.println("Gathering app info from OpenGL thread...");

            Future f = enqueue(new InfoWriter(out));
            try {
                f.get();
            } catch (Exception e) {
                e.printStackTrace(out);
            }
            return;
        }

        writeMemInfo(out);

        out.println();
        out.println("Rendering caps:");

        for (Caps cap : getRenderer().getCaps()) {
            out.println("    " + cap);
        }
        out.println();
        writeStats(out);

        out.println("Application settings:");
        for (Map.Entry e : getContext().getSettings().entrySet()) {
            if ((!"Title".equals(e.getKey())) &&
                    (!"Icons".equals(e.getKey())) &&
                    (!"SettingsDialogImage".equals(e.getKey()))) {
                out.println(e.getKey() + " = " + e.getValue());
            }
        }
        try {
            out.println();
            writeDisplayInfo(out);
        } catch (Throwable e) {
            out.println("Error writing display adapter information:" + e);
            e.printStackTrace(out);
        }

        out.flush();
    }

    public void simpleInitApp() {
        ErrorHandler.initialize();
        glThread = Thread.currentThread();

        StringWriter appInfo = new StringWriter();
        writeAppInfo(new PrintWriter(appInfo));
        log.info("Application info:\n" + appInfo);

        instance = this;

        setDisplayFps(false);
        setDisplayStatView(false);

        globalAssetManager = this.assetManager;

        this.inputManager.addRawInputListener(InputRedirector.getInstance());

        this.stateManager.attach(new PostProcessingState(this));

        globalStateManager = this.stateManager;

        if (bypassMenus) {
            GameClient gameClient = new LocalGameClient();
            gameClient.start();

            GameAppState gameState = new GameAppState(gameClient);
            getStateManager().attach(gameState);
        } else {
            MainMenuState mainMenu = new MainMenuState();
            this.stateManager.attach(mainMenu);

            NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(this.assetManager, this.inputManager, this.audioRenderer, this.guiViewPort);

            Nifty nifty = niftyDisplay.getNifty();

            nifty.fromXml("Interface/main-menu.xml", "start", new ScreenController[]{mainMenu, new OptionsScreen()});

            this.guiViewPort.addProcessor(niftyDisplay);
        }

        this.flyCam.setEnabled(false);

        writeAppInfo(new PrintWriter(new OutputStreamWriter(System.out)));
    }

    public void simpleUpdate(float tpf) {
        if ((this.first) && (bypassMenus)) {
            this.first = false;
            ((GameAppState) getStateManager().getState(GameAppState.class)).setupMovementAndInputs(false);
        }
    }

    private static class InfoWriter implements Callable {
        private PrintWriter out;

        public InfoWriter(PrintWriter out) {
            this.out = out;
        }

        public Object call() {
            MainStart.instance.writeAppInfo(this.out);
            return null;
        }
    }
}