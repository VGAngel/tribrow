package mythruna.client.view;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import mythruna.es.Position;

public abstract class DragControl extends AbstractControl implements Cloneable {

    public DragControl() {
        setEnabled(true);
    }

    public abstract void setPosition(Position paramPosition);

    public abstract Position getPosition();

    public void setSpatial(Spatial s) {
        if (this.spatial == s) {
            return;
        }
        if ((isEnabled()) && (this.spatial != null)) {
            terminate();
        }

        super.setSpatial(s);

        if ((isEnabled()) && (this.spatial != null)) {
            initialize();
        }
    }

    protected void initialize() {
    }

    protected void terminate() {
    }

    public Control cloneForSpatial(Spatial spatial) {
        try {
            DragControl clone = (DragControl) super.clone();
            clone.spatial = null;
            clone.setSpatial(spatial);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Error cloning control", e);
        }
    }

    public void setEnabled(boolean b) {
        if (isEnabled() == b)
            return;
        super.setEnabled(b);

        if (this.spatial != null) {
            if (b)
                initialize();
            else
                terminate();
        }
    }

    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    protected void controlUpdate(float tpf) {
    }
}