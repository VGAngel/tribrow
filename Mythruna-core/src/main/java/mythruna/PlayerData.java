package mythruna;

import com.jme3.math.Vector3f;

public abstract interface PlayerData {

    public abstract void set(String paramString, Object paramObject);

    public abstract <T> T get(String paramString);

    public abstract int increment(String paramString);

    public abstract void addValue(String paramString, long paramLong);

    public abstract void setLocation(String paramString, Vector3f paramVector3f);

    public abstract Float getFloat(String paramString);

    public abstract Long getLong(String paramString);

    public abstract Vector3f getLocation(String paramString);

    public abstract void save();
}