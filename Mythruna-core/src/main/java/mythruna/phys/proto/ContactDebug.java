package mythruna.phys.proto;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import mythruna.Coordinates;
import mythruna.es.EntityComponent;

@Serializable
public class ContactDebug implements EntityComponent {

    private long columnId;
    private Vector3f contactPoint;
    private Vector3f contactNormal;
    private double penetration;
    private boolean activated;

    public ContactDebug() {
    }

    public ContactDebug(boolean inActive) {
        this.activated = inActive;
        this.contactPoint = new Vector3f();
        this.contactNormal = new Vector3f();
        this.columnId = 9223372036854775807L;
    }

    public ContactDebug(Contact contact) {
        this.activated = true;
        this.contactPoint = new Vector3f((float) contact.contactPoint.x, (float) contact.contactPoint.y, (float) contact.contactPoint.z);

        this.contactNormal = new Vector3f((float) contact.contactNormal.x, (float) contact.contactNormal.y, (float) contact.contactNormal.z);

        this.penetration = contact.penetration;

        this.columnId = Coordinates.worldToColumnId(this.contactPoint.x, this.contactPoint.z);
    }

    public ContactDebug(mythruna.phys.Contact contact) {
        this.activated = true;
        this.contactPoint = new Vector3f((float) contact.contactPoint.x, (float) contact.contactPoint.y, (float) contact.contactPoint.z);

        this.contactNormal = new Vector3f((float) contact.contactNormal.x, (float) contact.contactNormal.y, (float) contact.contactNormal.z);

        this.penetration = contact.penetration;

        this.columnId = Coordinates.worldToColumnId(this.contactPoint.x, this.contactPoint.z);
    }

    public Class<ContactDebug> getType() {
        return ContactDebug.class;
    }

    public long getColumnId() {
        return this.columnId;
    }

    public boolean isActivated() {
        return this.activated;
    }

    public Vector3f getContactPoint() {
        return this.contactPoint;
    }

    public Vector3f getContactNormal() {
        return this.contactNormal;
    }

    public double getPenetration() {
        return this.penetration;
    }

    public String toString() {
        return "ContactDebug[" + this.columnId + ", " + this.contactPoint + ", " + this.contactNormal + ", " + this.penetration + "]";
    }
}