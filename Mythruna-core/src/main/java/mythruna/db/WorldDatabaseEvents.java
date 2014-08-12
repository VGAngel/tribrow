package mythruna.db;

import mythruna.event.EventType;

public class WorldDatabaseEvents {

    public static final EventType<GeneratorEvent> caveSystemCreated = EventType.create("CaveSystemCreated", GeneratorEvent.class);

    public WorldDatabaseEvents() {
    }
}