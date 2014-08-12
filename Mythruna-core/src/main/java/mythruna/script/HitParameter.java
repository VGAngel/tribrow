package mythruna.script;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import mythruna.BlockType;
import mythruna.MaterialType;
import mythruna.es.EntityId;

@Serializable
public class HitParameter implements ActionParameter {

    private static final Vector3f NULL_VECTOR = new Vector3f((0.0F / 0.0F), (0.0F / 0.0F), (0.0F / 0.0F));
    private EntityId object;
    private Vector3f contact;
    private Vector3f normal;
    private int cellType;

    public HitParameter() {
    }

    public HitParameter(EntityId object, int cellType, Vector3f contact, Vector3f normal) {
        this.object = object;
        this.cellType = cellType;
        this.contact = (contact == null ? NULL_VECTOR : contact);
        this.normal = (normal == null ? NULL_VECTOR : normal);
    }

    public Vector3f getContact() {
        return this.contact;
    }

    public Vector3f getNormal() {
        return this.normal;
    }

    public EntityId getObject() {
        return this.object;
    }

    public int getCellType() {
        return this.cellType;
    }

    public MaterialType getMaterial() {
        BlockType type = mythruna.BlockTypeIndex.types[this.cellType];
        if (type == null)
            return MaterialType.EMPTY;
        return type.getMaterial();
    }

    public String toString() {
        return "HitParameter[" + this.object + ", " + this.cellType + ", " + this.contact + ", " + this.normal + "]";
    }
}