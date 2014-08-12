package mythruna.es;

import com.jme3.network.serializing.Serializable;

@Serializable
public class Name implements EntityComponent, PersistentComponent {

    @StringType(maxLength = 80)
    private String name;

    public Name() {
    }

    public Name(String name) {
        this.name = name;
    }

    public Class<Name> getType() {
        return Name.class;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return "Name[" + this.name + "]";
    }
}