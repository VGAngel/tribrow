package mythruna.server.event;

import mythruna.event.AbstractEvent;
import mythruna.server.PlayerConnectionContext;

public class ChatEvent extends AbstractEvent<PlayerConnectionContext> {

    private String message;

    public ChatEvent(PlayerConnectionContext context, String message) {
        super(context);
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public String toString() {
        return "ChatEvent[" + this.message + " from:" + ((PlayerConnectionContext) getContext()).getPlayer() + "]";
    }
}