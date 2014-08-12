package mythruna.phys;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityComponent;

@Serializable
public class Impulse implements EntityComponent {

    private Vector3f velocity;

    public Impulse() {
    }

    public Impulse(Vector3f velocity) {
        this.velocity = velocity;
    }

    public Class<Impulse> getType() {
        return Impulse.class;
    }

    public Vector3f getVelocity() {
        return this.velocity;
    }

    public String toString() {
        return "Impulse[" + this.velocity + "]";
    }
}