package mythruna.client.sound;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.Listener;
import mythruna.client.env.Environment;
import mythruna.client.env.FloatRangeEffect;

import java.util.ArrayList;
import java.util.List;

public class AmbientEnvironment extends AbstractAmbientSound {
    private Application app;
    private AudioRenderer audioRenderer;
    private AssetManager assetManager;
    private float effectsVolume;
    private boolean effectsEnabled;
    private List<TimedSound> sounds = new ArrayList();
    private Listener audioListener;

    public AmbientEnvironment(Application app, Listener listener) {
        this.app = app;
        this.audioRenderer = app.getAudioRenderer();
        this.assetManager = app.getAssetManager();
        this.audioListener = listener;

        AmbientLoop quietForest = new AmbientLoop(app, "Sounds/ambient/Quiet_Forest_Morning_Loop-low.ogg");
        AmbientLoop nightForest = new AmbientLoop(app, "Sounds/ambient/Crickets,_Night,_Forest_Edge,_Chirpy,_Steady-low.ogg");
        AmbientLoop meadow = new AmbientLoop(app, "Sounds/ambient/Meadow___Crickets___Birds___Crow-low.ogg");
        AmbientLoop morningBirds = new AmbientLoop(app, "Sounds/ambient/Pretty_Forest_Birds_Loop_01-low.ogg");
        AmbientLoop windInLeaves = new AmbientLoop(app, "Sounds/ambient/wind-low.ogg");

        addSound(nightForest, new Float[]{Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F)});

        addSound(windInLeaves, new Float[]{Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.4F), Float.valueOf(0.8F), Float.valueOf(0.4F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F)});

        addSound(quietForest, new Float[]{Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F)});

        addSound(morningBirds, new Float[]{Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(-5.0F), Float.valueOf(1.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F)});

        addSound(meadow, new Float[]{Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(1.0F), Float.valueOf(0.5F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(1.0F), Float.valueOf(-2.0F), Float.valueOf(0.0F), Float.valueOf(0.0F)});
    }

    protected void adjustVolume(float v) {
        for (TimedSound t : this.sounds) {
            t.loop.setMasterVolume(v);
        }
    }

    protected void adjustEnabled(boolean enabled) {
        for (TimedSound t : this.sounds) {
            t.setEnabled(enabled);
        }
    }

    public void addSound(AmbientSound sound, Float[] values) {
        Environment env = Environment.getInstance();
        TimedSound sound1 = new TimedSound(sound, values);
        this.sounds.add(sound1);
        env.addTimeEffect(sound1);
    }

    public void update(float tpf) {
        for (TimedSound t : this.sounds) {
            t.updateSound(tpf);
        }
    }

    public void stop() {
        for (TimedSound t : this.sounds) {
            t.stop();
        }
    }

    private class TimedSound extends FloatRangeEffect {
        private AmbientSound loop;
        private float volume = 0.0F;

        public TimedSound(AmbientSound loop, Float[] values) {
            super(values);
            this.loop = loop;
        }

        public void setEnabled(boolean f) {
            this.loop.setEnabled(f);
        }

        protected void update(Float f) {
            if ((f == null) || (f.floatValue() < 0.0F)) {
                this.volume = 0.0F;
                return;
            }
            this.volume = f.floatValue();
        }

        public void stop() {
            this.loop.stop();
        }

        public void updateSound(float tpf) {
            if (this.volume > 0.0F) {
                this.loop.setVolume(this.volume);
                this.loop.update(tpf);
            } else {
                this.loop.stop();
            }
        }
    }
}
