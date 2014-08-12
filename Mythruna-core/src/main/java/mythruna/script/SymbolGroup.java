package mythruna.script;

import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityComponent;

@Serializable
public class SymbolGroup implements EntityComponent {

    private String name;
    private String[] strings;

    public SymbolGroup() {
    }

    public SymbolGroup(String name, String[] strings) {
        this.name = name;
        this.strings = strings;
    }

    public Class<SymbolGroup> getType() {
        return SymbolGroup.class;
    }

    public String getName() {
        return this.name;
    }

    public String[] getStrings() {
        return this.strings;
    }

    public String toString() {
        return new StringBuilder().append("SymbolGroup[").append(this.name).append(", count:").append(this.strings != null ? this.strings.length : 0).append("]").toString();
    }
}