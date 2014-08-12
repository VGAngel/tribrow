package mythruna.event;

import mythruna.PlayerContext;

public class PlayerEvent extends AbstractEvent<PlayerContext> {

    public PlayerEvent(PlayerContext context) {
        super(context);
    }
}