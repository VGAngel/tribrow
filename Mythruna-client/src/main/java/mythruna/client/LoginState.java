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
import mythruna.client.net.RemoteGameClient;
import org.progeeks.util.log.Log;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class LoginState extends AbstractAppState implements ScreenController {
    static Log log = Log.getLog();
    private static final String MD5_KEY = "passwordMd5";
    private static final String PASSWORD_VIEW = "????????";
    private Nifty nifty;
    private Screen screen;
    private Application app;
    private String lastErrorTitle;
    private String lastError;
    private String messageTitle;
    private String message;
    private Preferences prefs;
    private MainMenuState parent;
    private LoginClient loginClient;
    private String host;
    private String userId;
    private String password;
    private String passwordMd5;
    private boolean loggingIn = false;

    public LoginState(MainMenuState parent, String host, LoginClient loginClient) {
        System.out.println("new LoginState(" + loginClient + ")  " + this);
        this.parent = parent;
        this.host = host;

        this.loginClient = loginClient;
    }

    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = app;
    }

    public void cleanup() {
        if (this.app.getStateManager().hasState(this)) {
            System.out.println("******************************");
            log.warn("Early exit");
            if (this.loginClient.isConnected())
                this.loginClient.close();
            System.out.println("******************************");
        }
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

    protected Preferences getPrefs() {
        if (this.prefs == null)
            this.prefs = ClientOptions.getInstance().getPrefs();
        return this.prefs;
    }

    public String getServer() {
        return this.host;
    }

    public String getDefault(String name) {
        String val = getPrefs().get(name, null);
        if (val == null) {
            if ("name".equals(name))
                return "";
            if ("password".equals(name)) {
                if (getPrefs().get("passwordMd5", null) != null) {
                    return "????????";
                }
            }
        }

        return val == null ? "" : val;
    }

    public void go(String screen) {
        System.out.println("go(" + screen + ")");
        this.nifty.gotoScreen(screen);
    }

    protected String getFieldValue(String id) {
        TextField field = (TextField) this.screen.findNiftyControl(id, TextField.class);
        String s = field.getText();
        return s.trim();
    }

    public void login() {
        if (this.loggingIn)
            return;
        this.loggingIn = true;
        try {
            System.out.println("login-------------------" + this);
            this.userId = getFieldValue("userId");
            this.password = getFieldValue("password");
            if ("????????".equals(this.password)) {
                this.passwordMd5 = getPrefs().get("passwordMd5", null);
            }

            if (this.passwordMd5 == null) {
                this.passwordMd5 = DigestUtils.getMd5(this.password);
            }
            System.out.println("logging in with game client:" + this.loginClient);

            this.loginClient.login(this.userId, this.passwordMd5, new LoginStatusObserver());
        } catch (RuntimeException e) {
            this.loggingIn = false;
            log.error("Unhandled login exception", e);

            if ((e instanceof IllegalStateException))
                showError("Error Logging In", "IllegalState: The client needs to be restarted.");
            else
                showError("Error Logging In", e.getClass().getSimpleName() + ":" + e.getMessage());
            throw e;
        }
    }

    public void createUser() {
        this.userId = getFieldValue("userId");
        this.password = getFieldValue("password");
        String verify = getFieldValue("verifyPassword");
        String email = getFieldValue("email");
        String name = getFieldValue("name");

        if (this.userId.length() == 0) {
            showError("Invalid User ID", "Please specify a user ID.");
            return;
        }
        if (this.password.length() == 0) {
            showError("Invalid Password", "Please specify a password.");
            return;
        }
        if (!this.password.equals(verify)) {
            showError("Invalid Password", "Verification did not match password.");
            return;
        }
        if (name.length() == 0) {
            showError("Invalid Character Name", "Please specify a character name.");
            return;
        }

        String md5 = DigestUtils.getMd5(this.password);

        this.loginClient.createAccount(this.userId, md5, email, name, new AccountStatusObserver());
    }

    protected void runGame() {
        System.out.println("-----------runGame() " + this);
        try {
            Preferences prefs = getPrefs();
            prefs.put("userId", this.userId);
            prefs.remove("password");
            prefs.put("passwordMd5", this.passwordMd5);
            prefs.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }

        RemoteGameClient gameClient = new RemoteGameClient(this.loginClient);

        GameLoader loader = new GameLoader(gameClient, MainStart.globalStateManager);
        loader.start();

        LoadingState loading = new LoadingState(null);
        this.app.getStateManager().attach(loading);

        this.nifty.fromXml("Interface/loading-screen.xml", "loading", new ScreenController[]{loading});
        this.app.getStateManager().detach(this);

        this.loggingIn = false;
    }

    public void cancel() {
        if (this.loggingIn)
            return;
        this.nifty.createPopupWithId("popupReturn", "popupReturn");
        this.nifty.showPopup(this.screen, "popupReturn", null);
    }

    protected void returnToMain() {
        try {
            this.loginClient.close();
            this.nifty.fromXml("Interface/main-menu.xml", "multiplayer", new ScreenController[]{this.parent});
            this.app.getStateManager().detach(this);
        } catch (RuntimeException e) {
            log.error("Unhandled disconnect exception", e);
            throw e;
        }
    }

    public String getLastErrorTitle() {
        return this.lastErrorTitle;
    }

    public String getLastError() {
        return this.lastError;
    }

    public String getMessage() {
        return this.message;
    }

    public String getMessageTitle() {
        return this.messageTitle;
    }

    public void showMessage(String title, String message) {
        this.messageTitle = title;
        this.message = message;
        this.nifty.createPopupWithId("popupMessage", "popupMessage");
        this.nifty.showPopup(this.screen, "popupMessage", null);
    }

    public void showError(String title, String error) {
        this.lastErrorTitle = title;
        this.lastError = error;
        this.nifty.createPopupWithId("popupError", "popupError");
        this.nifty.showPopup(this.screen, "popupError", null);
    }

    public void closePopup(String id) {
        this.nifty.closePopup(id);
    }

    public void closeMessage(String id) {
        this.nifty.closePopup(id);

        go("login");
    }

    public void popupReturn(final String exit) {
        this.nifty.closePopup("popupReturn", new EndNotify() {
            public void perform() {
                if ("yes".equals(exit)) {
                    LoginState.this.returnToMain();
                }
            }
        });
    }

    protected class AccountStatusObserver
            implements CommandStatusListener {
        protected AccountStatusObserver() {
        }

        public void successful(String message) {
            LoginState.this.showMessage("New Account", message);
        }

        public void failed(String message) {
            LoginState.this.showError("Account Creation Failed", message);
        }
    }

    protected class LoginStatusObserver
            implements CommandStatusListener {
        protected LoginStatusObserver() {
        }

        public void successful(String message) {
            LoginState.this.runGame();
        }

        public void failed(String message) {
            LoginState.this.showError("Login Failed", message);
            LoginState.this.loggingIn = false;
        }
    }
}