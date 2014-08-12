package mythruna.phys.proto;

import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityComponent;

@Serializable
public class PhysicsDebug implements EntityComponent {

    private PhysicsState state;
    private double temperature;

    public PhysicsDebug() {
        this(PhysicsState.UNKNOWN, 0.0D);
    }

    public PhysicsDebug(PhysicsState state, double temperature) {
        this.state = state;
        this.temperature = temperature;
    }

    public Class<PhysicsDebug> getType() {
        return PhysicsDebug.class;
    }

    public PhysicsState getState() {
        return this.state;
    }

    public double getTemperature() {
        return this.temperature;
    }

    public String toString() {
        return "PhysicsDebug[" + this.state + "]";
    }

    public static enum PhysicsState {
        STATIC, SLEEPING, AWAKE, HALTED, UNKNOWN;
    }
}