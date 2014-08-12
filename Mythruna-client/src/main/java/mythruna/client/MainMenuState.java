package mythruna.client;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import de.lessvoid.nifty.EndNotify;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import mythruna.client.net.LoginClient;
import org.progeeks.util.log.Log;

import java.util.prefs.Preferences;

public class MainMenuState extends AbstractAppState implements ScreenController {

    static Log log = Log.getLog();
    private Nifty nifty;
    private Screen screen;
    private Application app;
    private String lastError;
    private Preferences prefs;
    private boolean connecting = false;

    public MainMenuState() {
    }

    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = app;
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

    public String getLastError() {
        return this.lastError;
    }

    protected Preferences getPrefs() {
        if (this.prefs == null)
            this.prefs = Preferences.userNodeForPackage(getClass());
        return this.prefs;
    }

    public String getDefault(String name) {
        String val = getPrefs().get(name, null);
        if (val == null) {
            if ("host".equals(name))
                return "mythruna.game-host.org";
            if ("port".equals(name))
                return "4234";
            if ("name".equals(name)) {
                return "";
            }
        }
        return val;
    }

    public void go(String screen) {
        System.out.println("go(" + screen + ")");
        this.nifty.gotoScreen(screen);
    }

    public void startSinglePlayer() {
        try {
            System.out.println("startSinglePlayer()");

            TimeLog.log(getClass().getSimpleName(), "Creating game client.");
            GameClient gameClient = new LocalGameClient();
            TimeLog.log(getClass().getSimpleName(), "Starting game client.");

            GameLoader loader = new GameLoader(gameClient, MainStart.globalStateManager);
            loader.start();

            TimeLog.log(getClass().getSimpleName(), "Creating loading state.");
            LoadingState loading = new LoadingState(null);
            this.app.getStateManager().attach(loading);

            this.nifty.fromXml("Interface/loading-screen.xml", "loading", new ScreenController[]{loading});
        } catch (Exception e) {
            ErrorHandler.handle(e, false);
        }
    }

    protected String getFieldValue(String id) {
        TextField field = (TextField) this.screen.findNiftyControl(id, TextField.class);
        String s = field.getText();
        return s.trim();
    }

    public void connect() {
        System.out.println("Connect!");
        if (this.connecting)
            return;
        this.connecting = true;

        TextField hostField = (TextField) this.screen.findNiftyControl("host", TextField.class);
        TextField portField = (TextField) this.screen.findNiftyControl("port", TextField.class);
        TextField nameField = (TextField) this.screen.findNiftyControl("name", TextField.class);
        System.out.println("hostField:" + hostField + "  host[" + hostField.getText() + "]");
        System.out.println("portField:" + portField + "  port[" + portField.getText() + "]");
        System.out.println("nameField:" + nameField + "  name[" + nameField.getText() + "]");

        String host = hostField.getText().trim();
        String port = portField.getText().trim();

        int portNumber = 0;
        try {
            portNumber = Integer.parseInt(port);
        } catch (Exception e) {
            System.out.println("Error:" + e);
            ErrorHandler.handle(e, false);
            this.connecting = false;
            this.lastError = (e.getClass().getSimpleName() + ":" + e.getMessage());

            this.nifty.createPopupWithId("popupConnectError", "popupConnectError");
            this.nifty.showPopup(this.screen, "popupConnectError", null);
            return;
        }

        try {
            System.out.println("Creating remote game client for:" + host + ":" + portNumber);

            LoginClient loginClient = new LoginClient(host, portNumber);
            loginClient.start();

            Preferences prefs = getPrefs();
            prefs.put("host", host);
            prefs.put("port", port);

            prefs.flush();

            System.out.println("Going to login state with:" + loginClient);

            LoginState loginState = new LoginState(this, host, loginClient);
            this.app.getStateManager().attach(loginState);
            System.out.println("Launching nifty screen with:" + loginState);
            this.nifty.fromXml("Interface/login-screens.xml", "login", new ScreenController[]{loginState});
        } catch (Exception e) {
            System.out.println("Error:" + e);
            ErrorHandler.handle(e, false);
            e.printStackTrace();
            this.lastError = (e.getClass().getSimpleName() + ":" + e.getMessage());

            this.nifty.createPopupWithId("popupConnectError", "popupConnectError");
            this.nifty.showPopup(this.screen, "popupConnectError", null);
        } finally {
            this.connecting = false;
        }
    }

    public void close() {
        System.out.println("-----MainMenu CLOSE");
        this.nifty.createPopupWithId("popupExit", "popupExit");
        this.nifty.showPopup(this.screen, "popupExit", null);
    }

    public void closePopup(String id) {
        this.nifty.closePopup(id);
    }

    public void popupExit(final String exit) {
        this.nifty.closePopup("popupExit", new EndNotify() {
            public void perform() {
                if ("yes".equals(exit)) {
                    MainMenuState.this.nifty.setAlternateKey("exit");
                    MainMenuState.this.nifty.exit();

                    MainMenuState.this.app.stop();
                }
            }
        });
    }
}