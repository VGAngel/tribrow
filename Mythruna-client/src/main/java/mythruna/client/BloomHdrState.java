package mythruna.client;

import com.jme3.app.Application;
import com.jme3.math.ColorRGBA;
import mythruna.client.env.ColorRangeEffect;
import mythruna.client.env.Environment;
import mythruna.client.ui.ObservableState;
import mythruna.client.view.LocalArea;

public class BloomHdrState extends ObservableState {
    private LocalArea localArea;
    private ColorRGBA caveBloom = new ColorRGBA(2.3F, 4.57F, 0.189F, 3.2F);
    private ColorRGBA environmentBloom = null;
    private ColorRGBA bloomOverride = null;

    private int lastSunLevel = -1;
    private int lastLightLevel = -1;
    private float bloomTransition = 0.0F;

    public BloomHdrState(LocalArea localArea) {
        super("Bloom HDR", true);

        this.localArea = localArea;
    }

    protected void setBloom(ColorRGBA value) {
        ((PostProcessingState) getState(PostProcessingState.class)).setBloom(value);
    }

    protected void initialize(Application app) {
        super.initialize(app);

        Environment environment = Environment.getInstance();
        environment.addTimeEffect(new ColorRangeEffect(new ColorRGBA[]{new ColorRGBA(2.7F, 4.57F, 0.189F, 1.75F), new ColorRGBA(2.7F, 4.57F, 0.189F, 1.75F), new ColorRGBA(2.7F, 4.57F, 0.189F, 1.75F), new ColorRGBA(2.7F, 4.57F, 0.189F, 1.75F), new ColorRGBA(2.7F, 4.57F, 0.189F, 1.75F), new ColorRGBA(2.7F, 4.57F, 0.189F, 1.75F), new ColorRGBA(2.7F, 4.57F, 0.189F, 1.75F), new ColorRGBA(0.5F, 3.3F, 0.2F, 1.0F), new ColorRGBA(0.5F, 3.3F, 0.2F, 1.0F), new ColorRGBA(1.0F, 3.3F, 0.2F, 0.75F), new ColorRGBA(1.0F, 3.3F, 0.2F, 0.5F), new ColorRGBA(1.0F, 3.3F, 0.2F, 0.5F), new ColorRGBA(1.0F, 3.3F, 0.2F, 0.5F), new ColorRGBA(1.0F, 3.3F, 0.2F, 0.5F), new ColorRGBA(1.0F, 3.3F, 0.2F, 0.5F), new ColorRGBA(1.0F, 3.3F, 0.2F, 0.75F), new ColorRGBA(0.5F, 3.3F, 0.2F, 1.0F), new ColorRGBA(0.5F, 3.3F, 0.2F, 1.0F), new ColorRGBA(0.5F, 3.3F, 0.2F, 1.0F), new ColorRGBA(2.7F, 4.57F, 0.189F, 1.75F), new ColorRGBA(2.7F, 4.57F, 0.189F, 1.75F), new ColorRGBA(2.7F, 4.57F, 0.189F, 1.75F), new ColorRGBA(2.7F, 4.57F, 0.189F, 1.75F), new ColorRGBA(2.7F, 4.57F, 0.189F, 1.75F)}) {
            protected void update(ColorRGBA value) {
                BloomHdrState.this.environmentBloom = value;
                if (BloomHdrState.this.bloomOverride == null) {
                    BloomHdrState.this.setBloom(value);
                }
            }
        });
    }

    protected void enable() {
    }

    protected void disable() {
    }

    public void update(float tpf) {
        int sunLevel = this.localArea.getCenterSunlightValue();
        int lightLevel = this.localArea.getCenterLocalLightValue();
        if ((this.lastSunLevel != sunLevel) || (this.lastLightLevel != lightLevel)) {
            this.lastSunLevel = sunLevel;
            this.lastLightLevel = lightLevel;
        }

        if (this.lastSunLevel <= 3) {
            this.bloomTransition += 1.0F * tpf;
            if (this.bloomOverride == null) {
                this.bloomOverride = new ColorRGBA();
            }
            if (this.bloomTransition >= 1.0F) {
                this.bloomOverride.set(this.caveBloom);
                this.bloomTransition = 1.0F;
            } else {
                float d1 = this.caveBloom.r - this.environmentBloom.r;
                float d2 = this.caveBloom.g - this.environmentBloom.g;
                float d3 = this.caveBloom.b - this.environmentBloom.b;
                float d4 = this.caveBloom.a - this.environmentBloom.a;
                this.bloomOverride.set(this.environmentBloom.r + this.bloomTransition * d1, this.environmentBloom.g + this.bloomTransition * d2, this.environmentBloom.b + this.bloomTransition * d3, this.environmentBloom.a + this.bloomTransition * d4);
            }

            setBloom(this.bloomOverride);
        } else if (this.bloomOverride != null) {
            this.bloomTransition -= 1.0F * tpf;
            if (this.bloomTransition <= 0.0F) {
                this.bloomOverride = null;
                this.bloomTransition = 0.0F;
                setBloom(this.environmentBloom);
            } else {
                float d1 = this.caveBloom.r - this.environmentBloom.r;
                float d2 = this.caveBloom.g - this.environmentBloom.g;
                float d3 = this.caveBloom.b - this.environmentBloom.b;
                float d4 = this.caveBloom.a - this.environmentBloom.a;
                this.bloomOverride.set(this.environmentBloom.r + this.bloomTransition * d1, this.environmentBloom.g + this.bloomTransition * d2, this.environmentBloom.b + this.bloomTransition * d3, this.environmentBloom.a + this.bloomTransition * d4);

                setBloom(this.bloomOverride);
            }
        }
    }
}