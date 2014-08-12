package mythruna.client;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.LightScatteringFilter;
import mythruna.client.ui.MessageLog;

public class PostProcessingState extends AbstractAppState {
    private Application app;
    private boolean active = true;
    private FilterPostProcessor fpp;
    private BloomFilter bloom;
    private ColorRGBA caveBloom = new ColorRGBA(2.3F, 4.57F, 0.189F, 3.2F);
    private ColorRGBA environmentBloom = null;
    private ColorRGBA bloomOverride = null;
    private LightScatteringFilter scatter;
    private RadialFadeFilter radialFade;
    private DepthBlurFilter depthFilter;
    private UnderwaterFilter underwaterFilter;

    public PostProcessingState(Application app) {
        this.app = app;

        setupFilters();
    }

    protected void setupFilters() {
        int numSamples = this.app.getContext().getSettings().getSamples();

        this.fpp = new FilterPostProcessor(this.app.getAssetManager());

        if (numSamples > 0) {
            this.fpp.setNumSamples(numSamples);
        }
        this.scatter = new LightScatteringFilter();

        this.bloom = new BloomFilter();
        this.bloom.setDownSamplingFactor(2.0F);
        this.bloom.setBlurScale(0.5F);
        this.bloom.setExposurePower(3.3F);
        this.bloom.setExposureCutOff(0.2F);
        this.bloom.setBloomIntensity(1.0F);

        this.fpp.addFilter(this.bloom);

        this.underwaterFilter = new UnderwaterFilter();
        this.underwaterFilter.setEnabled(false);
        this.fpp.addFilter(this.underwaterFilter);

        this.depthFilter = new DepthBlurFilter();
        this.depthFilter.setBlurScale(2.0F);
        this.depthFilter.setFocusRange(60.0F);
        this.depthFilter.setFocusDistance(0.0F);
        this.depthFilter.setEnabled(false);
        this.fpp.addFilter(this.depthFilter);

        this.radialFade = new RadialFadeFilter();
        this.radialFade.setFadeStrength(1.5F, 1.5F);
        this.radialFade.setColor(new ColorRGBA(0.0F, 0.0F, 0.0F, 0.85F));
        this.radialFade.setEnabled(false);
        this.fpp.addFilter(this.radialFade);

        KeyMethodAction toggle = new KeyMethodAction(this, "toggleEffects", 67);
        toggle.attach(this.app.getInputManager());

        this.app.getViewPort().addProcessor(this.fpp);
    }

    public void setBloom(ColorRGBA value) {
        if (this.bloom == null) {
            return;
        }
        this.bloom.setBlurScale(value.r);
        this.bloom.setExposurePower(value.g);
        this.bloom.setExposureCutOff(value.b);
        this.bloom.setBloomIntensity(value.a);
    }

    public void setLightPosition(Vector3f lightPos) {
        this.scatter.setLightPosition(lightPos);
    }

    public void setWaterColor(ColorRGBA color) {
        this.underwaterFilter.setWaterColor(color);
    }

    public void setUnderwater(boolean flag) {
        this.underwaterFilter.setEnabled(flag);
        this.depthFilter.setEnabled(flag);
    }

    public void setRadialFade(boolean flag) {
        this.radialFade.setEnabled(flag);
    }

    public void setRadialFadeOn(float xStrength, float yStrength) {
        this.radialFade.setFadeStrength(xStrength, yStrength);
        this.radialFade.setEnabled(true);
    }

    public void toggleEffects() {
        if (this.active) {
            System.out.println("Post-processing effects off.");
            this.active = false;
            this.app.getViewPort().removeProcessor(this.fpp);
            MessageLog.addMessage("Post-processing effects off.");
        } else {
            System.out.println("Post-processing effects on.");
            this.active = true;
            this.app.getViewPort().addProcessor(this.fpp);
            MessageLog.addMessage("Post-processing effects on.");
        }
    }

    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
    }
}