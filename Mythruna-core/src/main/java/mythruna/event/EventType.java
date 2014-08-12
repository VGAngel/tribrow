package mythruna.event;

public class EventType<E> {

    private String name;
    private Class<E> eventClass;

    protected EventType(String name, Class<E> eventClass) {
        this.name = name;
        this.eventClass = eventClass;
    }

    public static <E> EventType<E> create(String name, Class<E> eventClass) {
        return new EventType(name, eventClass);
    }

    public static <E> EventType<E> create(Class<E> eventClass) {
        return create(eventClass.getSimpleName(), eventClass);
    }

    public String getName() {
        return this.name;
    }

    public Class<E> getEventClass() {
        return this.eventClass;
    }

    public String toString() {
        return this.name;
    }
}