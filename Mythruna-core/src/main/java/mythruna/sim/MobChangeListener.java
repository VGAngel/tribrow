package mythruna.sim;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

public interface MobChangeListener {

    public abstract void mobChanged(Mob mob, Vector3f vector3f, Quaternion quaternion);
}
