package mythruna.event;

public class PlayerEvents {

    public static final EventType<PlayerEvent> playerJoined = EventType.create("PlayerJoined", PlayerEvent.class);

    public static final EventType<PlayerEvent> playerLeft = EventType.create("PlayerLeft", PlayerEvent.class);

    public PlayerEvents() {
    }
}