package mythruna.client;

import com.jme3.app.state.AppStateManager;

public class GameLoader extends Thread {
    private GameClient client;
    private AppStateManager stateManager;

    public GameLoader(GameClient client, AppStateManager stateManager) {
        this.client = client;
        this.stateManager = stateManager;
        setDaemon(true);
        setName("Game Loader");
    }

    public void run() {
        try {
            TimeLog.log(getClass().getSimpleName(), "Starting game client.");
            this.client.initialize();
            this.client.start();

            TimeLog.log(getClass().getSimpleName(), "Creating app state.");
            GameAppState gameState = new GameAppState(this.client);
            System.out.println("Attaching:" + gameState + "   to:" + this.stateManager);
            TimeLog.log(getClass().getSimpleName(), "Attaching game state.");
            this.stateManager.attach(gameState);
        } catch (Exception e) {
            ErrorHandler.handle(e, true);
        }
    }
}