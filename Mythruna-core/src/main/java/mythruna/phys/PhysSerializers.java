package mythruna.phys;

import com.jme3.network.serializing.Serializer;
import mythruna.phys.proto.ContactDebug;
import mythruna.phys.proto.PhysicsDebug;

public class PhysSerializers {

    public PhysSerializers() {
    }

    public static void registerSerializers() {
        Serializer.registerClasses(new Class[]{Mass.class, MassProperties.class, Volume.class, BodyTemplate.class, BodyState.class, Impulse.class, ContactDebug.class, PhysicsDebug.class, PhysicalLink.class, Spring.class, Rope.class});
    }
}