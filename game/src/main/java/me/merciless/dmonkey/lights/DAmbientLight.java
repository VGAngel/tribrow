package me.merciless.dmonkey.lights;

import me.merciless.dmonkey.DMonkey;

import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.Vector2f;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.FrameBuffer;

public class DAmbientLight implements SceneProcessor {

    private RenderManager rm;
    private Geometry quad;

    @Override
    public void initialize(final RenderManager rm, final ViewPort vp) {
        this.rm = rm;
        this.quad = new Geometry("DistantLight", new Quad(1, 1));

        final Material material = new Material(DMonkey.getInstance().getAssetManager(), "assets/DMonkey/AmbientLight.j3md");
        material.setTexture("DiffuseBuffer", DMonkey.getInstance().getDiffuseBuffer());
        material.setTexture("DepthBuffer", DMonkey.getInstance().getZBuffer());
        material.setTexture("NormalBuffer", DMonkey.getInstance().getNormalBuffer());
        material.setVector2("Resolution", new Vector2f(DMonkey.getInstance().getCamera().getWidth(), DMonkey.getInstance().getCamera().getHeight()));
        final RenderState rs = material.getAdditionalRenderState();
        rs.setBlendMode(RenderState.BlendMode.Additive);
        rs.setDepthTest(false);
        rs.setDepthWrite(false);
        this.quad.setMaterial(material);
        this.quad.updateGeometricState();

        System.out.println(vp.getName());
        System.out.println(vp.getOutputFrameBuffer());
    }

    @Override
    public void reshape(final ViewPort vp, final int w, final int h) {
    }

    @Override
    public boolean isInitialized() {
        return this.rm != null;
    }

    @Override
    public void preFrame(final float tpf) {
        this.quad.updateLogicalState(tpf);
    }

    @Override
    public void postQueue(final RenderQueue rq) {
    }

    @Override
    public void postFrame(final FrameBuffer out) {
        this.rm.renderGeometry(this.quad);
    }

    @Override
    public void cleanup() {
    }

}
