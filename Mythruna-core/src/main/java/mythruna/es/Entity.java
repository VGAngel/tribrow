package mythruna.es;

public abstract interface Entity {

    public abstract EntityId getId();

    public abstract <T extends EntityComponent> T get(Class<T> paramClass);

    public abstract void set(EntityComponent paramEntityComponent);

    public abstract boolean isComplete();

    public abstract EntityComponent[] getComponents();
}