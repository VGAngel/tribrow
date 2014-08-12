package mythruna.msg;

import com.jme3.network.serializing.Serializer;
import com.jme3.network.serializing.SerializerException;
import com.jme3.network.serializing.SerializerRegistration;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ClassSerializer extends Serializer {

    public ClassSerializer() {
    }

    public Class readObject(ByteBuffer data, Class c)
            throws IOException {
        SerializerRegistration reg = readClass(data);
        if (reg == null)
            throw new SerializerException("Class not found for buffer data.");
        if (reg.getId() == -1) {
            return null;
        }
        return reg.getType();
    }

    public void writeObject(ByteBuffer buffer, Object object) throws IOException {
        if (object == null) {
            buffer.putShort((short) -1);
            return;
        }
        writeClass(buffer, (Class) object);
    }
}