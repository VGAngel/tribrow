package mythruna.msg;

import com.jme3.network.serializing.Serializer;
import com.jme3.network.serializing.SerializerRegistration;
import com.jme3.network.serializing.serializers.StringSerializer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public class ClassFieldSerializer extends Serializer {
    private StringSerializer delegate = new StringSerializer();

    public ClassFieldSerializer() {
    }

    public Field readObject(ByteBuffer data, Class c) throws IOException {
        SerializerRegistration reg = readClass(data);
        if ((reg == null) || (reg.getType() == Void.class)) {
            return null;
        }
        Class type = reg.getType();
        String name = (String) this.delegate.readObject(data, String.class);
        try {
            return type.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new IOException("Error resolving field:" + name + " on:" + type, e);
        }
    }

    public void writeObject(ByteBuffer buffer, Object object) throws IOException {
        if (object == null) {
            buffer.putShort((short) -1);
            return;
        }
        Field field = (Field) object;

        writeClass(buffer, field.getDeclaringClass());
        this.delegate.writeObject(buffer, field.getName());
    }
}