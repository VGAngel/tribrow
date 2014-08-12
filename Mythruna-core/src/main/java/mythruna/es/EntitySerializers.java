package mythruna.es;

import com.jme3.network.serializing.Serializer;
import mythruna.es.action.*;
import mythruna.item.HeldEntities;
import mythruna.item.ReticleStyle;
import mythruna.script.*;

public class EntitySerializers {

    public EntitySerializers() {
    }

    public static void registerSerializers() {
        if (Serializer.getExactSerializerRegistration(EntityId.class) == null) {
            System.out.println("*************** Registering default EntityId serializer ********");
            Serializer.registerClass(EntityId.class);
        }

        Serializer.registerClasses(new Class[]{FieldFilter.class, OrFilter.class, Position.class, ModelInfo.class, InContainer.class, BlueprintReference.class, CreatedBy.class, Name.class, UserId.class, ActionReference.class, ContextActions.class, RadialActions.class, ToolActions.class, DialogOption.class, DialogPrompt.class, BlockParameter.class, ComponentParameter.class, EntityParameter.class, NumberParameter.class, ObjectParameter.class, HitParameter.class, ClaimType.class, ClaimArea.class, ClaimPermissions.class, OwnedBy.class, SymbolGroup.class});

        Serializer.registerClass(CreateBlueprintItemAction.class);
        Serializer.registerClass(CreateObjectAction.class);
        Serializer.registerClass(MoveObjectAction.class);
        Serializer.registerClass(RemoveBlueprintAction.class);
        Serializer.registerClass(SaveBlueprintAction.class);
        Serializer.registerClass(CreateLinkAction.class);
        Serializer.registerClass(RemoveLinkAction.class);

        Serializer.registerClasses(new Class[]{HeldEntities.class, ReticleStyle.class});
    }
}