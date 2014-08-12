package mythruna.client.ui;

import com.jme3.input.InputManager;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.post.filters.BloomFilter;

public class BloomUI {

    public BloomUI(InputManager inputManager, final BloomFilter filter) {
        System.out.println("----------------- Bloom UI Debugger --------------------");
        System.out.println("-- blur Scale : press Y to increase, H to decrease");
        System.out.println("-- exposure Power : press U to increase, J to decrease");
        System.out.println("-- exposure CutOff : press I to increase, K to decrease");
        System.out.println("-- bloom Intensity : press O to increase, P to decrease");
        System.out.println("-------------------------------------------------------");

        inputManager.addMapping("blurScaleUp", new Trigger[]{new KeyTrigger(21)});
        inputManager.addMapping("blurScaleDown", new Trigger[]{new KeyTrigger(35)});
        inputManager.addMapping("exposurePowerUp", new Trigger[]{new KeyTrigger(22)});
        inputManager.addMapping("exposurePowerDown", new Trigger[]{new KeyTrigger(36)});
        inputManager.addMapping("exposureCutOffUp", new Trigger[]{new KeyTrigger(23)});
        inputManager.addMapping("exposureCutOffDown", new Trigger[]{new KeyTrigger(37)});
        inputManager.addMapping("bloomIntensityUp", new Trigger[]{new KeyTrigger(24)});
        inputManager.addMapping("bloomIntensityDown", new Trigger[]{new KeyTrigger(38)});

        AnalogListener anl = new AnalogListener() {
            public void onAnalog(String name, float value, float tpf) {
                if (name.equals("blurScaleUp")) {
                    filter.setBlurScale(filter.getBlurScale() + 0.01F);
                    System.out.println("blurScale : " + filter.getBlurScale());
                }
                if (name.equals("blurScaleDown")) {
                    filter.setBlurScale(filter.getBlurScale() - 0.01F);
                    System.out.println("blurScale : " + filter.getBlurScale());
                }
                if (name.equals("exposurePowerUp")) {
                    filter.setExposurePower(filter.getExposurePower() + 0.01F);
                    System.out.println("exposurePower : " + filter.getExposurePower());
                }
                if (name.equals("exposurePowerDown")) {
                    filter.setExposurePower(filter.getExposurePower() - 0.01F);
                    System.out.println("exposurePower : " + filter.getExposurePower());
                }
                if (name.equals("exposureCutOffUp")) {
                    filter.setExposureCutOff(Math.min(1.0F, Math.max(0.0F, filter.getExposureCutOff() + 0.001F)));
                    System.out.println("exposure CutOff : " + filter.getExposureCutOff());
                }
                if (name.equals("exposureCutOffDown")) {
                    filter.setExposureCutOff(Math.min(1.0F, Math.max(0.0F, filter.getExposureCutOff() - 0.001F)));
                    System.out.println("exposure CutOff : " + filter.getExposureCutOff());
                }
                if (name.equals("bloomIntensityUp")) {
                    filter.setBloomIntensity(filter.getBloomIntensity() + 0.01F);
                    System.out.println("bloom Intensity : " + filter.getBloomIntensity());
                }
                if (name.equals("bloomIntensityDown")) {
                    filter.setBloomIntensity(filter.getBloomIntensity() - 0.01F);
                    System.out.println("bloom Intensity : " + filter.getBloomIntensity());
                }
            }
        };
        inputManager.addListener(anl, new String[]{"blurScaleUp", "blurScaleDown", "exposurePowerUp", "exposurePowerDown", "exposureCutOffUp", "exposureCutOffDown", "bloomIntensityUp", "bloomIntensityDown"});
    }
}