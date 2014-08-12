package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class AccountStatusMessage extends AbstractMessage {

    private String userId;
    private boolean success;
    private String message;

    public AccountStatusMessage() {
    }

    public AccountStatusMessage(String userId, boolean success, String message) {
        this.userId = userId;
        this.message = message;
        this.success = success;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getMessage() {
        return this.message;
    }

    public boolean getSuccess() {
        return this.success;
    }

    public String toString() {
        return "AccountStatusMessage[" + this.userId + ", " + this.success + ", " + this.message + "]";
    }
}