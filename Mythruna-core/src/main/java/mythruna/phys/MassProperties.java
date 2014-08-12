package mythruna.phys;

import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityComponent;
import mythruna.es.PersistentComponent;
import mythruna.mathd.Matrix3d;
import mythruna.mathd.Vec3d;

@Serializable
public class MassProperties implements EntityComponent, PersistentComponent {

    private Vec3d cog;
    private Matrix3d inertia;

    public MassProperties() {
    }

    public MassProperties(Vec3d cog, Matrix3d inertia) {
        this.cog = cog;
        this.inertia = inertia;
    }

    public Vec3d getCog() {
        return this.cog;
    }

    public Matrix3d getInteria() {
        return this.inertia;
    }

    public Class<MassProperties> getType() {
        return MassProperties.class;
    }

    public String toString() {
        return "MassProperties[" + getCog() + ", " + getInteria() + "]";
    }
}