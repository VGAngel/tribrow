package mythruna.shell;

import mythruna.PlayerContext;

public abstract interface Console {

    public abstract void setPlayerContext(PlayerContext paramPlayerContext);

    public abstract void runCommand(String paramString);

    public abstract void echo(Object paramObject);
}