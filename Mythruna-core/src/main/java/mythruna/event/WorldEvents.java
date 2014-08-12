package mythruna.event;

public class WorldEvents {

    public static final EventType<CellEvent> cellChanged = EventType.create("CellChanged", CellEvent.class);

    public WorldEvents() {
    }
}