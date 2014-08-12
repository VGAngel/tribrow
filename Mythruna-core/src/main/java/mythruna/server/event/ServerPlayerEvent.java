package mythruna.server.event;

import mythruna.event.AbstractEvent;
import mythruna.server.PlayerConnectionContext;

public class ServerPlayerEvent extends AbstractEvent<PlayerConnectionContext> {

    public ServerPlayerEvent(PlayerConnectionContext context) {
        super(context);
    }
}