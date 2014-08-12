package mythruna.client.ui;

import com.jme3.app.state.AppStateManager;

public class PopupCommand extends ActionCommand {
    private AppStateManager stateManager;
    private ActionCommand[] actions;

    public PopupCommand(AppStateManager stateManager, String message, ActionCommand[] actions) {
        super(message);
        this.stateManager = stateManager;
        this.actions = actions;
    }

    public void execute(Object source, Object a) {
        if (this.stateManager.getState(PopupMessageState.class) != null) {
            System.out.println("Not popping up message because a message is already popped up.");
            return;
        }
        this.stateManager.attach(new PopupMessageState(getName(), this.actions));
    }
}