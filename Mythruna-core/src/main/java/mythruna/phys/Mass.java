package mythruna.phys;

import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityComponent;
import mythruna.es.PersistentComponent;

@Serializable
public class Mass
        implements EntityComponent, PersistentComponent {
    private double inverseMass;

    public Mass() {
    }

    public Mass(double mass) {
        setMass(mass);
    }

    public final double getInverseMass() {
        return this.inverseMass;
    }

    protected void setMass(double mass) {
        if (mass == 0.0D)
            this.inverseMass = 0.0D;
        else
            this.inverseMass = (1.0D / mass);
    }

    public final double getMass() {
        if (this.inverseMass == 0.0D)
            return (1.0D / 0.0D);
        return 1.0D / this.inverseMass;
    }

    public Class<Mass> getType() {
        return Mass.class;
    }

    public String toString() {
        return "Mass[" + getMass() + "]";
    }
}