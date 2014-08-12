package mythruna.client.ui;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.scene.Spatial;

public class MouseAdapter implements MouseListener {

    private int xDown;
    private int yDown;

    public MouseAdapter() {
    }

    protected void click(MouseButtonEvent event) {
    }

    public void mouseButtonEvent(MouseButtonEvent event, Spatial capture) {
        event.setConsumed();

        if (event.isPressed()) {
            this.xDown = event.getX();
            this.yDown = event.getY();
        } else {
            int x = event.getX();
            int y = event.getY();
            if ((Math.abs(x - this.xDown) < 3) && (Math.abs(y - this.yDown) < 3)) {
                click(event);
            }
        }
    }

    public void mouseEntered(MouseMotionEvent event, Spatial capture) {
    }

    public void mouseExited(MouseMotionEvent event, Spatial capture) {
    }

    public void mouseMoved(MouseMotionEvent event, Spatial capture) {
    }
}