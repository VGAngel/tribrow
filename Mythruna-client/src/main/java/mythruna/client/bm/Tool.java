package mythruna.client.bm;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public abstract interface Tool {
    public abstract void initialize(ObjectSelector paramObjectSelector);

    public abstract void update();

    public abstract Spatial getIcon();

    public abstract String getName();

    public abstract void select(Vector3f paramVector3f1, Vector3f paramVector3f2, Quaternion paramQuaternion, boolean paramBoolean);

    public abstract void place(Vector3f paramVector3f1, Vector3f paramVector3f2, Quaternion paramQuaternion, boolean paramBoolean);

    public abstract boolean isCapturingView();

    public abstract boolean showBlockSelection();

    public abstract void viewMoved(Vector3f paramVector3f1, Vector3f paramVector3f2, Quaternion paramQuaternion);
}