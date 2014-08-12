package mythruna.client.ui;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.scene.Spatial;

public abstract interface MouseListener {
    public abstract void mouseButtonEvent(MouseButtonEvent paramMouseButtonEvent, Spatial paramSpatial);

    public abstract void mouseEntered(MouseMotionEvent paramMouseMotionEvent, Spatial paramSpatial);

    public abstract void mouseExited(MouseMotionEvent paramMouseMotionEvent, Spatial paramSpatial);

    public abstract void mouseMoved(MouseMotionEvent paramMouseMotionEvent, Spatial paramSpatial);
}