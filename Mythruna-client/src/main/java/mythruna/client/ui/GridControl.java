package mythruna.client.ui;

import com.jme3.bounding.BoundingVolume;
import com.jme3.math.Quaternion;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import mythruna.client.anim.AnimationState;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GridControl extends AbstractControl {
    private AnimationState anim;
    private Node node;
    private int rows;
    private int cols;
    private float gridSpacing = 1.0F;
    private float padding;
    private float minSize = 1.7F;

    private List<Spatial> children = new CopyOnWriteArrayList();
    private boolean changed = false;

    public GridControl(AnimationState anim, int rows, int cols) {
        this.anim = anim;
        this.rows = rows;
        this.cols = cols;
    }

    public void setSpatial(Spatial spatial) {
        this.node = ((Node) spatial);
        super.setSpatial(spatial);
    }

    public List<Spatial> children() {
        return this.children;
    }

    public void clear() {
        this.children.clear();
    }

    public void add(Spatial[] s) {
        this.children.addAll(Arrays.asList(s));
        this.changed = true;
    }

    public void addAll(Collection<Spatial> list) {
        this.children.addAll(list);
        this.changed = true;
    }

    public void remove(Spatial[] s) {
        this.children.removeAll(Arrays.asList(s));
        this.changed = true;
    }

    protected float getSize(BoundingVolume bounds) {
        float size = 1.0F;

        return size < this.minSize ? this.minSize : size;
    }

    public float getHeight() {
        if (this.cols <= 1)
            return this.children.size() * this.gridSpacing;
        return this.rows * this.gridSpacing;
    }

    public float getWidth() {
        return this.cols * this.gridSpacing;
    }

    public void setChildRotation(Quaternion rot) {
        for (Spatial s : this.children)
            s.setLocalRotation(rot);
    }

    public void setRows(int rows) {
        this.rows = rows;
        this.changed = true;
    }

    public void setMinimumCellSize(float minSize) {
        this.minSize = minSize;
    }

    public float getMinimumCellSize() {
        return this.minSize;
    }

    public void refreshChildren() {
        this.node.detachAllChildren();

        float maxSize = 0.0F;
        for (Spatial s : this.children) {
            s.setLocalTranslation(0.0F, 0.0F, 0.0F);
            BoundingVolume bounds = s.getWorldBound();
            float size = getSize(bounds);
            if (size > maxSize) {
                maxSize = size;
            }
        }
        this.gridSpacing = (maxSize + this.padding);

        this.cols = (this.children.size() / this.rows);
        if (this.cols * this.rows != this.children.size()) {
            this.cols += 1;
        }
        int index = 0;
        for (int i = 0; i < this.cols; i++) {
            for (int j = 0; j < this.rows; j++) {
                if (index >= this.children.size())
                    break;
                Spatial s = (Spatial) this.children.get(index++);
                s.setLocalTranslation(-i * this.gridSpacing, -j * this.gridSpacing, 0.0F);
                this.node.attachChild(s);
            }
        }

        this.changed = false;
    }

    protected void controlUpdate(float tpf) {
        if (this.changed)
            refreshChildren();
    }

    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        return this;
    }
}