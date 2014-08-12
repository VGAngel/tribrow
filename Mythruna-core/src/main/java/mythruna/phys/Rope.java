package mythruna.phys;

import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityComponent;

@Serializable
public class Rope implements EntityComponent {

    private float length = 5.0F;

    private float strength = 10000.0F;

    public Rope() {
    }

    public Rope(float length, float strength) {
        this.length = length;
        this.strength = strength;
    }

    public Class<Rope> getType() {
        return Rope.class;
    }

    public float getLength() {
        return this.length;
    }

    public float getStrength() {
        return this.strength;
    }

    public String toString() {
        return "Rope[" + this.length + ", " + this.strength + "]";
    }
}