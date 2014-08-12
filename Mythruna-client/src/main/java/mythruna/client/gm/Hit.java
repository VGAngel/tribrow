package mythruna.client.gm;

import com.jme3.math.Vector3f;
import mythruna.es.EntityId;
import mythruna.script.HitParameter;

public class Hit {
    private Vector3f contact;
    private Vector3f normal;
    private EntityId entity;
    private int cellType;

    public Hit(Vector3f contact, Vector3f normal, EntityId entity, int cellType) {
        this.contact = contact;
        this.normal = normal;
        this.entity = entity;
        this.cellType = cellType;
    }

    public HitParameter toHitParameter() {
        return new HitParameter(this.entity, this.cellType, this.contact, this.normal);
    }

    public EntityId getEntity() {
        return this.entity;
    }

    public Vector3f getContact() {
        return this.contact;
    }

    public Vector3f getNormal() {
        return this.normal;
    }

    public String toString() {
        return "Hit[" + this.contact + ", " + this.normal + ", " + this.entity + "]";
    }
}