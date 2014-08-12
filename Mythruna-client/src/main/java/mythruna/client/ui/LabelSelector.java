package mythruna.client.ui;

import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;

public class LabelSelector extends MouseAdapter {
    private Label label;
    private ColorRGBA color;
    private ColorRGBA shadow;
    private ColorRGBA selected;
    private int xDown;
    private int yDown;

    public LabelSelector(Label label, ColorRGBA color, ColorRGBA shadow, ColorRGBA selected) {
        this.label = label;
        this.color = color;
        this.shadow = shadow;
        this.selected = selected;
    }

    protected void click() {
    }

    public void mouseEntered(MouseMotionEvent event, Spatial capture) {
        this.label.setColor(this.selected);
    }

    public void mouseExited(MouseMotionEvent event, Spatial capture) {
        this.label.setColor(this.color);
        this.label.setShadowColor(this.shadow);
    }
}