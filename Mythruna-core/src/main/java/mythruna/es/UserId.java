package mythruna.es;

import com.jme3.network.serializing.Serializable;

@Serializable
public class UserId implements EntityComponent, PersistentComponent {

    @StringType(maxLength = 80)
    private String id;

    public UserId() {
    }

    public UserId(String id) {
        this.id = id;
    }

    public Class<UserId> getType() {
        return UserId.class;
    }

    public String getUserId() {
        return this.id;
    }

    public String toString() {
        return "UserId[" + this.id + "]";
    }
}