package mythruna.phys;

import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityComponent;

@Serializable
public class Spring implements EntityComponent {

    private float length = 5.0F;
    private float strength = 1.0F;

    public Spring() {
    }

    public Spring(float length, float strength) {
        this.length = length;
        this.strength = strength;
    }

    public Class<Spring> getType() {
        return Spring.class;
    }

    public float getLength() {
        return this.length;
    }

    public float getStrength() {
        return this.strength;
    }

    public String toString() {
        return "SpringInfo[" + this.length + ", " + this.strength + "]";
    }
}