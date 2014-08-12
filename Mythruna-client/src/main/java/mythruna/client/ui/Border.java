package mythruna.client.ui;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import mythruna.geom.Trifold;

public class Border extends Node {
    private Trifold borderMesh;
    private Geometry border;
    private Material borderMaterial;
    private float width = 128.0F;
    private float height = 128.0F;
    private float borderWidth = 12.0F;

    public Border(AssetManager assets) {
        this(128.0F, 128.0F, assets);
    }

    public Border(float width, float height, AssetManager assets) {
        this.width = width;
        this.height = height;

        this.borderMesh = new Trifold(width, height);

        resetBorders();

        this.borderMaterial = new Material(assets, "MatDefs/Composed.j3md");
        this.borderMaterial.setTexture("Map", assets.loadTexture("Interface/border.png"));
        Texture leather = assets.loadTexture("Interface/leather.jpg");
        leather.setWrap(Texture.WrapMode.Repeat);
        this.borderMaterial.setTexture("Mix", leather);
        this.borderMaterial.setVector3("MixParms", new Vector3f(0.01F, 0.01F, 1.0F));
        this.borderMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        this.border = new Geometry("Border", this.borderMesh);
        this.border.setMaterial(this.borderMaterial);

        attachChild(this.border);
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        this.borderMesh.setSize(width, height);
        resetBorders();
    }

    public float getHeight() {
        return this.height;
    }

    public float getWidth() {
        return this.width;
    }

    protected void resetBorders() {
        this.borderMesh.setFoldTextureCoordinates(new Vector2f(0.09375F, 0.09375F), new Vector2f(0.90625F, 0.90625F));

        this.borderMesh.setFoldCoordinates(new Vector2f(this.borderWidth, this.borderWidth), new Vector2f(this.width - this.borderWidth, this.height - this.borderWidth));

        this.borderMesh.updateGeometry();
    }
}