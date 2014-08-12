package mythruna.client.view;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import mythruna.Vector3i;

public class CameraLeafPriority
        implements LeafPriority {
    private Camera camera;

    public CameraLeafPriority(Camera camera) {
        this.camera = camera;
    }

    public int getPriority(int i, int j, int k, Vector3f areaPos, LeafReference leaf) {
        int priority = i * i + j * j + k * k;

        Vector3i loc = leaf.getLeafLocation();
        Vector3f pos = new Vector3f(i * 32, k * 32, j * 32);

        BoundingBox box = new BoundingBox(pos, pos.add(32.0F, 32.0F, 32.0F));

        int planeState = this.camera.getPlaneState();
        Camera.FrustumIntersect result = this.camera.contains(box);

        this.camera.setPlaneState(planeState);

        if (result != Camera.FrustumIntersect.Inside) {
            if (result != Camera.FrustumIntersect.Intersects) {
                priority *= 2;
            }
        }
        return priority;
    }
}