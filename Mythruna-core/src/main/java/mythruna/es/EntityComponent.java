package mythruna.es;

public abstract interface EntityComponent {

    public abstract <T extends EntityComponent> Class<T> getType();
}