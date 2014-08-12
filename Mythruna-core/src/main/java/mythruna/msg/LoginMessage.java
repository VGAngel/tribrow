package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mythruna.GameConstants;

@Serializable
public class LoginMessage extends AbstractMessage {

    private String userId;
    private String password;
    private String buildVersion;

    public LoginMessage() {
    }

    public LoginMessage(String userId, String password) {
        this.userId = userId;
        this.password = password;
        this.buildVersion = GameConstants.buildVersion();
    }

    public String getUserId() {
        return this.userId;
    }

    public String getPassword() {
        return this.password;
    }

    public String getBuildVersion() {
        return this.buildVersion;
    }

    public String toString() {
        return "LoginMessage[" + this.userId + "]";
    }
}