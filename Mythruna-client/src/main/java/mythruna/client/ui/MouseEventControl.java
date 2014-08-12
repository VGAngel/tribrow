package mythruna.client.ui;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MouseEventControl extends AbstractControl {
    private List<MouseListener> listeners = new CopyOnWriteArrayList();
    private boolean filterOnHit = true;

    public MouseEventControl() {
    }

    public MouseEventControl(MouseListener[] listeners) {
        this.listeners.addAll(Arrays.asList(listeners));
    }

    public MouseEventControl(boolean filterOnHit) {
        this.filterOnHit = filterOnHit;
    }

    public <T extends MouseListener> T getMouseListener(Class<T> type) {
        for (MouseListener l : this.listeners) {
            if (l.getClass() == type)
                return (T) l;
        }
        return null;
    }

    public void addMouseListener(MouseListener l) {
        this.listeners.add(l);
    }

    public void removeMouseListener(MouseListener l) {
        this.listeners.remove(l);
    }

    public void mouseButtonEvent(MouseButtonEvent event, Spatial capture) {
        for (MouseListener l : this.listeners)
            l.mouseButtonEvent(event, capture);
    }

    public void mouseEntered(MouseMotionEvent event, Spatial capture) {
        for (MouseListener l : this.listeners)
            l.mouseEntered(event, capture);
    }

    public void mouseExited(MouseMotionEvent event, Spatial capture) {
        for (MouseListener l : this.listeners)
            l.mouseExited(event, capture);
    }

    public void mouseMoved(MouseMotionEvent event, Spatial capture) {
        for (MouseListener l : this.listeners)
            l.mouseMoved(event, capture);
    }

    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    protected void controlUpdate(float tpf) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        return this;
    }
}
