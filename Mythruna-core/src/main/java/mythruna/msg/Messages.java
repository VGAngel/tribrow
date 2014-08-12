package mythruna.msg;

import com.jme3.network.serializing.Serializer;
import mythruna.Vector3i;
import mythruna.es.EntitySerializers;
import mythruna.phys.PhysSerializers;

import java.lang.reflect.Field;

public class Messages {

    public Messages() {
    }

    public static void initialize() {
        Serializer.registerClass(Class.class, new ClassSerializer());
        Serializer.registerClass(Field.class, new ClassFieldSerializer());

        EntitySerializers.registerSerializers();
        PhysSerializers.registerSerializers();

        Serializer.registerClasses(new Class[]{Vector3i.class});

        Serializer.registerClasses(new Class[]{UserStateMessage.class, EntityStateMessage.class, WarpPlayerMessage.class, EntityListUpdateMessage.class, SetBlockMessage.class, GetLeafDataMessage.class, ReturnLeafDataMessage.class, ResetLeafMessage.class, TimeMessage.class, ConsoleMessage.class, LoginMessage.class, LoginStatusMessage.class, CreateAccountMessage.class, AccountStatusMessage.class, GetEntitySetMessage.class, EntityDataMessage.class, EntityDataMessage.ComponentData.class, GetBlueprintMessage.class, BlueprintDataMessage.class, EntityActionMessage.class, RunActionMessage.class, RunNamedActionMessage.class, GetComponentsMessage.class, EntityComponentsMessage.class, ComponentChangeMessage.class, ReleaseEntitySetMessage.class, ResetEntitySetFilterMessage.class, FindEntityMessage.class, FindEntitiesMessage.class, EntityIdsMessage.class, ObserveChangesMessage.class, ReleaseObserveChangesMessage.class});
    }
}