package mythruna.msg;

import com.jme3.network.serializing.Serializer;
import mythruna.es.EntityId;
import mythruna.server.GameServer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ServerEntityIdSerializer extends Serializer {

    private GameServer server;

    public ServerEntityIdSerializer(GameServer server) {
        this.server = server;
    }

    public static long clientIdToEntityId(int clientId) {
        if (clientId >= 0)
            return -42 - clientId;
        return clientId;
    }

    public EntityId readObject(ByteBuffer data, Class c)
            throws IOException {
        if (data.get() == 0) {
            return null;
        }
        long value = data.getLong();

        if (value < 0L) {
            int clientId = (int) (-42L - value);

            EntityId result = this.server.getPlayerId(clientId);

            return result;
        }

        return new EntityId(value);
    }

    public void writeObject(ByteBuffer buffer, Object object)
            throws IOException {
        buffer.put((byte) (object != null ? 1 : 0));
        if (object == null) {
            return;
        }

        EntityId id = (EntityId) object;
        long value = id.getId();

        int clientId = this.server.getClientId(id);
        if (clientId >= 0) {
            value = -42 - clientId;
        }

        buffer.putLong(value);
    }
}