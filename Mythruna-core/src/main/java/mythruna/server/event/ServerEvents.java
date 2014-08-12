package mythruna.server.event;

import mythruna.event.EventType;

public class ServerEvents {
    public static final EventType<ServerEvent> serverStarted = EventType.create("ServerStarted", ServerEvent.class);

    public static final EventType<ServerEvent> serverStopping = EventType.create("ServerStopping", ServerEvent.class);

    public static final EventType<ServerEvent> newConnection = EventType.create("NewConnection", ServerEvent.class);

    public static final EventType<ServerPlayerEvent> playerConnected = EventType.create("PlayerConnected", ServerPlayerEvent.class);

    public static final EventType<ServerPlayerEvent> playerDisconnected = EventType.create("PlayerDisconnected", ServerPlayerEvent.class);

    public static final EventType<ChatEvent> playerChatted = EventType.create("PlayerChatted", ChatEvent.class);

    public ServerEvents() {
    }
}