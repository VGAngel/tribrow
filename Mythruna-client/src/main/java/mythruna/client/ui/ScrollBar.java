package mythruna.client.ui;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import mythruna.geom.Trifold;

import javax.swing.*;

public class ScrollBar extends Node {
    private BoundedRangeModel model;
    private float margin = 5.0F;
    private float width;
    private float height;
    private Trifold background;
    private Button up;
    private Button down;
    private Trifold thumbMesh;
    private Geometry thumb;
    private ColorRGBA arrowsColor = new ColorRGBA(1.0F, 0.9333333F, 0.6235294F, 0.5F);
    private ColorRGBA backgroundColor = new ColorRGBA(1.0F, 1.0F, 1.0F, 0.5F);
    private int value;
    private int min;
    private int max;
    private int extent;

    public ScrollBar(AssetManager assets) {
        this(assets, new DefaultBoundedRangeModel(0, 60, 0, 100));
    }

    public ScrollBar(AssetManager assets, BoundedRangeModel model) {
        setModel(model);

        this.value = model.getValue();
        this.min = model.getMinimum();
        this.max = model.getMaximum();
        this.extent = model.getExtent();

        this.width = (32.0F + this.margin * 2.0F);
        this.height = (200.0F + this.margin * 2.0F + 64.0F);

        TextureKey key = new TextureKey("Interface/arrow-32.png");
        key.setGenerateMips(false);
        Texture tex = assets.loadTexture(key);
        tex.setWrap(Texture.WrapMode.Repeat);
        tex.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        Material mat = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", this.arrowsColor);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        mat.setTexture("ColorMap", tex);

        Trifold tri = new Trifold(32.0F, 32.0F, 32.0F, 32.0F, 1.0F, 1.0F);
        this.up = new Button(assets, tri, tex);
        this.up.setBackgroundColor(this.arrowsColor);
        this.up.setLocalTranslation(this.margin, this.height - this.margin - 32.0F, 0.1F);
        this.up.addCommand(new IncrementScrollCommand(-1));
        attachChild(this.up);

        tri = new Trifold(32.0F, 32.0F, 32.0F, 32.0F, 1.0F, 1.0F);
        tri.scaleTextureCoordinates(new Vector2f(-1.0F, -1.0F));
        this.down = new Button(assets, tri, tex);
        this.down.setBackgroundColor(this.arrowsColor);
        this.down.setLocalTranslation(this.margin, this.margin, 0.1F);
        this.down.addCommand(new IncrementScrollCommand(1));
        attachChild(this.down);

        this.background = new Trifold(this.width, this.height, 64.0F, 64.0F, 16.0F, 16.0F);
        Geometry geom = new Geometry("bg", this.background);
        geom.setLocalTranslation(0.0F, 0.0F, -0.5F);

        key = new TextureKey("Interface/brown-cell-64.png");
        key.setGenerateMips(false);
        tex = assets.loadTexture(key);
        tex.setWrap(Texture.WrapMode.Repeat);
        tex.setMinFilter(Texture.MinFilter.NearestNoMipMaps);
        mat = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", this.backgroundColor);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        mat.setTexture("ColorMap", tex);
        geom.setMaterial(mat);
        attachChild(geom);

        this.thumbMesh = new Trifold(32.0F, getThumbSize(), 32.0F, 32.0F, 4.0F, 4.0F);
        this.thumb = new Geometry("Thumb", this.thumbMesh);
        mat = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", this.arrowsColor);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        this.thumb.setMaterial(mat);
        this.thumb.setLocalTranslation(this.margin, getThumbPosition(), 0.0F);

        attachChild(this.thumb);
    }

    public void updateLogicalState(float tpf) {
        super.updateLogicalState(tpf);

        boolean change = false;
        if (this.value != this.model.getValue()) {
            this.value = this.model.getValue();
            change = true;
        }
        if (this.min != this.model.getMinimum()) {
            this.min = this.model.getMinimum();
            change = true;
        }
        if (this.max != this.model.getMaximum()) {
            this.max = this.model.getMaximum();
            change = true;
        }
        if (this.extent != this.model.getExtent()) {
            this.extent = this.model.getExtent();
            change = true;
        }

        if (change)
            resetThumb();
    }

    protected float getThumbPosition() {
        float part = this.height - (this.margin * 2.0F + 64.0F);
        float range = this.model.getMaximum() - this.model.getMinimum();
        float val;
        if (range == 0.0F) {
            val = 0.0F;
        } else {
            val = this.model.getValue() - this.model.getMinimum();
            val = val / range * part;
        }

        val = part - getThumbSize() - val;

        return this.margin + 32.0F + val;
    }

    protected float getThumbSize() {
        float part = this.height - (this.margin * 2.0F + 64.0F);

        float range = this.model.getMaximum() - this.model.getMinimum();
        if (range == 0.0F) {
            return part;
        }
        float size = this.model.getExtent() / range;

        return size * part;
    }

    protected void resetThumb() {
        this.thumbMesh.setSize(32.0F, getThumbSize());
        this.thumbMesh.updateGeometry();
        this.thumb.setLocalTranslation(this.margin, getThumbPosition(), 0.0F);
    }

    public void setHeight(float height) {
        if (this.height == height) {
            return;
        }
        this.height = height;
        this.background.setSize(this.width, height);
        this.background.updateGeometry();

        this.up.setLocalTranslation(this.margin, height - this.margin - 32.0F, 0.1F);

        resetThumb();
    }

    public float getHeight() {
        return this.height;
    }

    public float getWidth() {
        return this.width;
    }

    public void setModel(BoundedRangeModel model) {
        if (this.model == model) {
            return;
        }

        this.model = model;
    }

    public BoundedRangeModel getModel() {
        return this.model;
    }

    public class IncrementScrollCommand implements Command {
        private int delta;

        public IncrementScrollCommand(int delta) {
            this.delta = delta;
        }

        public void execute(Object source, Object action) {
            System.out.println("moving by delta:" + this.delta);
            ScrollBar.this.model.setValue(ScrollBar.this.model.getValue() + this.delta);
        }
    }
}