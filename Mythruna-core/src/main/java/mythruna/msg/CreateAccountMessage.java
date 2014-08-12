package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class CreateAccountMessage extends AbstractMessage {

    private String userId;
    private String password;
    private String email;
    private String name;

    public CreateAccountMessage() {
    }

    public CreateAccountMessage(String userId, String password, String email, String name) {
        this.userId = userId;
        this.password = password;
        this.email = email;
        this.name = name;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getPassword() {
        return this.password;
    }

    public String getEmail() {
        return this.email;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return "CreateAccountMessage[" + this.userId + ", " + this.name + "]";
    }
}