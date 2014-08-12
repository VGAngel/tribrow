package mythruna.client;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import org.progeeks.util.ObjectUtils;

public class LoadingState extends AbstractAppState implements ScreenController {

    private Nifty nifty;
    private Screen screen;
    private Application app;
    private GameAppState gameState;
    private int max = 125;
    private boolean closed = false;

    private int lastProgress = -1;
    private int lastPercent = -1;
    private String lastMessage = null;

    public LoadingState(GameAppState gameState) {
        this.gameState = gameState;
    }

    public void startGame() {
        if (this.closed)
            return;
        if ((this.app == null) || (this.nifty == null)) {
            return;
        }
        this.closed = true;

        this.app.getStateManager().detach(this);

        GameAppState gameState = (GameAppState) this.app.getStateManager().getState(GameAppState.class);
        this.nifty.fromXml("Interface/in-game-menu.xml", "empty", new ScreenController[]{gameState, new OptionsScreen()});

        gameState.setupMovementAndInputs(true);
    }

    public void update(float tpf) {
        if (this.screen == null) {
            return;
        }

        int percentage = Progress.getTotalPercent();
        String message = Progress.getLastMessage();
        if ((this.lastPercent != percentage) || (!ObjectUtils.areEqual(message, this.lastMessage))) {
            this.lastPercent = percentage;
            this.lastMessage = message;
            Label progress = (Label) this.screen.findNiftyControl("progress", Label.class);

            if (percentage > 0) {
                String text = percentage + " %";
                progress.setText(text);
                if (percentage == 100) {
                    startGame();
                }
            } else {
                progress.setText("");
            }

            Label status = (Label) this.screen.findNiftyControl("status", Label.class);
            if (message != null) {
                status.setText(message);
            } else {
                status.setText("");
            }
        }
    }

    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = app;

        KeyMethodAction a1 = new KeyMethodAction(this, "startGame", 68);

        a1.attach(app.getInputManager());
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
}