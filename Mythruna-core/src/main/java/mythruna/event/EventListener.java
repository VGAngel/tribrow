package mythruna.event;

public abstract interface EventListener<E> {
    public abstract void newEvent(EventType<E> paramEventType, E paramE);
}