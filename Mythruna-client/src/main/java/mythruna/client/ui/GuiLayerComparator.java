package mythruna.client.ui;

import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.GeometryComparator;
import com.jme3.renderer.queue.GuiComparator;
import com.jme3.scene.Geometry;

public class GuiLayerComparator implements GeometryComparator {

    public static final String LAYER = "guiLayer";
    private GeometryComparator delegate = new GuiComparator();

    public GuiLayerComparator() {
    }

    public void setCamera(Camera cam) {
        this.delegate.setCamera(cam);
    }

    public int getLayer(Geometry g) {
        Integer layer = (Integer) g.getUserData("guiLayer");
        if (layer == null)
            return 0;
        return layer.intValue();
    }

    public int compare(Geometry o1, Geometry o2) {
        int p1 = getLayer(o1);
        int p2 = getLayer(o2);

        if (p1 < p2)
            return -1;
        if (p1 > p2) {
            return 1;
        }
        return this.delegate.compare(o1, o2);
    }
}