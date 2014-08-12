package mythruna.client.view;

import com.jme3.math.Vector3f;

public abstract interface LeafPriority {
    public abstract int getPriority(int paramInt1, int paramInt2, int paramInt3, Vector3f paramVector3f, LeafReference paramLeafReference);
}