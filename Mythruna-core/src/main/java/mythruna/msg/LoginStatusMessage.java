package mythruna.msg;

import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityId;

@Serializable
public class LoginStatusMessage extends AbstractMessage {

    private boolean connected;
    private String message;
    private Vector3f position;
    private EntityId playerEntity;

    public LoginStatusMessage() {
    }

    public LoginStatusMessage(boolean connected, String message, EntityId playerEntity, Vector3f position) {
        this.connected = connected;
        this.message = message;
        this.position = position;
        this.playerEntity = playerEntity;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public String getMessage() {
        return this.message;
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public EntityId getPlayer() {
        return this.playerEntity;
    }

    public String toString() {
        return "LoginStatusMessage[" + this.connected + ", " + this.message + "]";
    }
}