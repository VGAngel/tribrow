package mythruna.client;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.post.Filter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;

public class UnderwaterFilter extends Filter {
    private float focusDistance = 0.0F;
    private float focusRange = 30.0F;
    private float blurScale = 2.0F;
    private ColorRGBA waterColor = new ColorRGBA(0.0F, 0.2F, 0.5F, 1.0F);
    private float xScale;
    private float yScale;

    public UnderwaterFilter() {
        super("Underwater Filter");
    }

    public void setWaterColor(ColorRGBA c) {
        this.waterColor = c;
    }

    public ColorRGBA getWaterColor() {
        return this.waterColor;
    }

    public void setFocusDistance(float f) {
        this.focusDistance = f;
    }

    public float getFocusDistance() {
        return this.focusDistance;
    }

    public void setFocusRange(float f) {
        this.focusRange = f;
    }

    public float getFocusRange() {
        return this.focusRange;
    }

    public void setBlurScale(float f) {
        this.blurScale = f;
    }

    public float getBlurScale() {
        return this.blurScale;
    }

    public boolean isRequiresDepthTexture() {
        return true;
    }

    public Material getMaterial() {
        this.material.setFloat("FocusDistance", this.focusDistance);
        this.material.setFloat("FocusRange", this.focusRange);
        this.material.setFloat("XScale", this.blurScale * this.xScale);
        this.material.setFloat("YScale", this.blurScale * this.yScale);
        this.material.setColor("WaterColor", this.waterColor);

        return this.material;
    }

    public void initFilter(AssetManager assets, RenderManager renderManager, ViewPort vp, int w, int h) {
        this.material = new Material(assets, "MatDefs/Underwater.j3md");
        this.xScale = (1.0F / w);
        this.yScale = (1.0F / h);
    }

    public void cleanUpFilter(Renderer r) {
    }
}