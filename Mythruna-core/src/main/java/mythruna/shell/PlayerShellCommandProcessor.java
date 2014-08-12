package mythruna.shell;

import mythruna.PlayerContext;
import org.progeeks.tool.console.ShellCommandProcessor;

public class PlayerShellCommandProcessor extends ShellCommandProcessor {

    private PlayerContext playerContext;

    public PlayerShellCommandProcessor(PlayerContext playerContext) {
        this.playerContext = playerContext;
    }

    public void setPlayerContext(PlayerContext context) {
        this.playerContext = context;
    }

    public PlayerContext getPlayerContext() {
        return this.playerContext;
    }
}