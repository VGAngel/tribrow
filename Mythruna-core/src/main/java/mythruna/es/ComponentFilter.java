package mythruna.es;

public abstract interface ComponentFilter<T extends EntityComponent> {

    public abstract Class<T> getComponentType();

    public abstract boolean evaluate(EntityComponent paramEntityComponent);
}