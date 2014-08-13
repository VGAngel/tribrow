package me.merciless.dmonkey.lights;

import me.merciless.dmonkey.DMonkey;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.shader.VarType;

public class DPointLight {
    private final Material material;

    public DPointLight() {
        final AssetManager assetManager = DMonkey.getInstance().getAssetManager();
        final Spatial model = assetManager.loadModel("assets/Models/PointLight.j3o");
        this.material = new Material(assetManager, "assets/DMonkey/PointLight.j3md");
        this.material.setTexture("DiffuseBuffer", DMonkey.getInstance().getDiffuseBuffer());
        this.material.setTexture("DepthBuffer", DMonkey.getInstance().getZBuffer());
        this.material.setTexture("NormalBuffer", DMonkey.getInstance().getNormalBuffer());
        this.material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Back);
        this.material.getAdditionalRenderState().setDepthTest(true);
        this.material.getAdditionalRenderState().setDepthWrite(false);
        this.material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Additive);

        this.material.setVector3("LightPosition", model.getLocalTranslation());
        this.setLightColor(new ColorRGBA(1.0f, 0.1f, 0.1f, 8.6f));
        this.setLightRadius(0.3f);
        this.setLightIntensity(0);

        this.material.setParam("LightPositions", VarType.Vector3Array, new Vector3f[]{model.getLocalTranslation()});

        model.addControl(new LightControl());
        model.addControl(new LightLocationSynchControll(this.material));
        model.addControl(new LightQualityControl(this.material, DMonkey.getInstance().getCamera()));

        model.setMaterial(this.material);
        model.setLocalScale(15f);
        DMonkey.getInstance().getLightNode().attachChild(model);
    }

    public void setLightColor(final ColorRGBA color) {
        this.material.setColor("LightColor", color);
    }

    public void setLightRadius(final float radius) {
        this.material.setFloat("LightRadius", radius);
    }

    public void setLightIntensity(final float intensity) {
        this.material.setFloat("LightIntensity", intensity);
    }

    static class LightControl extends AbstractControl {
        private float time;
        private final float period = 10;

        @Override
        protected void controlUpdate(final float tpf) {
            this.time += tpf;
            if (this.time > this.period) {
                this.time -= this.period;
            }
            this.spatial.setLocalTranslation(0, FastMath.sin(this.time / this.period * FastMath.TWO_PI) * 1 + 1.2f, 0);
        }

        @Override
        protected void controlRender(final RenderManager rm, final ViewPort vp) {
        }

        @Override
        public Control cloneForSpatial(final Spatial spatial) {
            final Control control = new LightControl();
            control.setSpatial(spatial);
            return control;
        }
    }

    static class LightQualityControl extends AbstractControl {

        private final Material material;
        private final Camera cam;
        private boolean inside = true;

        public LightQualityControl(final Material material, final Camera cam) {
            this.material = material;
            this.cam = cam;
        }

        @Override
        protected void controlUpdate(final float tpf) {
            final Vector3f pos = this.spatial.getWorldTranslation();
            final float d = this.cam.getLocation().distance(pos);
            final float lightRadius = this.spatial.getLocalScale().x / 2f;
            if (this.inside && d > lightRadius + this.cam.getFrustumNear()) {
                this.inside = false;
                this.material.getAdditionalRenderState().setDepthTest(true);
                this.material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Back);
                this.material.setBoolean("specular", false);
                System.out.println("enable depthtest");
                return;
            }
            if (!this.inside && d < lightRadius + this.cam.getFrustumNear()) {
                System.out.println("disable depthtest");
                this.inside = true;
                this.material.setBoolean("specular", true);
                this.material.getAdditionalRenderState().setDepthTest(false);
                this.material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Front);
                return;
            }
        }

        @Override
        protected void controlRender(final RenderManager rm, final ViewPort vp) {
        }

        @Override
        public Control cloneForSpatial(final Spatial spatial) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
