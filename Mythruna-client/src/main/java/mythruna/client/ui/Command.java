package mythruna.client.ui;

public abstract interface Command<T> {
    public abstract void execute(Object paramObject, T paramT);
}