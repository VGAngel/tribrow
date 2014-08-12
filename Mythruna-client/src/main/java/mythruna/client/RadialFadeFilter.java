package mythruna.client;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector4f;
import com.jme3.post.Filter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;

public class RadialFadeFilter extends Filter {
    private Vector4f radial = new Vector4f(0.5F, 0.5F, 2.0F, 1.0F);
    private ColorRGBA color = ColorRGBA.Black;
    private float xStrength = 1.0F;
    private float yStrength = 1.0F;
    private float w;
    private float h;

    public RadialFadeFilter() {
        super("Radial Fade");
    }

    public void setRadialCenter(float x, float y) {
        this.radial.x = x;
        this.radial.y = y;
    }

    public void setFadeStrength(float x, float y) {
        this.xStrength = x;
        this.yStrength = y;
    }

    public void setColor(ColorRGBA color) {
        this.color = color;
    }

    public boolean isRequiresDepthTexture() {
        return false;
    }

    public Material getMaterial() {
        this.radial.z = (this.w / this.h * this.xStrength);
        this.radial.w = this.yStrength;

        this.material.setVector4("Radial", this.radial);
        this.material.setColor("FadeColor", this.color);

        return this.material;
    }

    public void initFilter(AssetManager assets, RenderManager renderManager, ViewPort vp, int w, int h) {
        this.material = new Material(assets, "MatDefs/RadialFade.j3md");
        this.w = w;
        this.h = h;
    }

    public void cleanUpFilter(Renderer r) {
    }
}