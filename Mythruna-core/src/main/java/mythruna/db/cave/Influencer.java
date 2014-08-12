package mythruna.db.cave;

import com.jme3.math.Vector3f;

public interface Influencer {

    public abstract void setWet(boolean flag);

    public abstract boolean isWet();

    public abstract float getStrength(Vector3f vector3f);

    public abstract Vector3f getCenter();

    public abstract Vector3f getMin();

    public abstract Vector3f getMax();

    public abstract boolean canInfluence(float f, float f1, float f2, float f3);
}
