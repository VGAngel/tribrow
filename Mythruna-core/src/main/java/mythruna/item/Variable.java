package mythruna.item;

import mythruna.es.EntityComponent;
import mythruna.es.EntityId;

public abstract interface Variable extends EntityComponent {

    public abstract EntityId getHolder();

    public abstract int getNameId();
}