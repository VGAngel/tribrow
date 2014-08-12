package mythruna.client.sound;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.Listener;
import com.jme3.math.Vector3f;
import mythruna.client.env.Environment;
import mythruna.client.env.FloatRangeEffect;

import java.util.ArrayList;
import java.util.List;

public class AmbientEffects extends AbstractAmbientSound {
    private Application app;
    private AudioRenderer audioRenderer;
    private AssetManager assetManager;
    private List<TimedSound> sounds = new ArrayList();
    private Listener audioListener;

    public AmbientEffects(Application app, Listener listener) {
        this.app = app;
        this.audioRenderer = app.getAudioRenderer();
        this.assetManager = app.getAssetManager();
        this.audioListener = listener;

        String[] foleys = {"Sounds/foley/Crickets,_Night,_Chirp,_Single_1-low.ogg", "Sounds/foley/Crickets,_Night,_Chirp,_Single_2-low.ogg", "Sounds/foley/Single_Cricket_Short-low.ogg", "Sounds/foley/Owls_Loop_2-low.ogg", "Sounds/foley/Snowy_owl,_one_year_old,_crying-low.ogg", "Sounds/foley/Snowy_owl,_one_year_old,_crying_while_twittering_and_chattering-low.ogg", "Sounds/foley/dove-low.ogg", "Sounds/foley/Mockingbird_Call_1-low.ogg", "Sounds/foley/Mockingbird_Call_2-low.ogg", "Sounds/foley/Mockingbird_Call_7-low.ogg", "Sounds/foley/Mockingbird_Two_Calls_3-low.ogg"};

        addFoley(foleys[0], 0.7F, 0.1F, 2.0F, 10.0F, new Vector3f(20.0F, 10.0F, 20.0F), new Float[]{Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(0.5F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.25F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F)});

        addFoley(foleys[1], 0.7F, 0.1F, 2.0F, 10.0F, new Vector3f(20.0F, 10.0F, 20.0F), new Float[]{Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(0.5F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.25F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F)});

        addFoley(foleys[2], 0.4F, 0.1F, 2.0F, 10.0F, new Vector3f(40.0F, 10.0F, 40.0F), new Float[]{Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(0.5F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.25F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F)});

        addFoley(foleys[3], 0.5F, 0.1F, 2.0F, 10.0F, new Vector3f(40.0F, 10.0F, 40.0F), new Float[]{Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F)});

        addFoley(foleys[4], 0.7F, 0.1F, 2.0F, 20.0F, new Vector3f(40.0F, 10.0F, 40.0F), new Float[]{Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F)});

        addFoley(foleys[5], 0.7F, 0.1F, 2.0F, 20.0F, new Vector3f(40.0F, 10.0F, 40.0F), new Float[]{Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F)});

        addFoley(foleys[6], 1.0F, 0.25F, 2.0F, 10.0F, new Vector3f(40.0F, 10.0F, 40.0F), new Float[]{Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.5F), Float.valueOf(0.5F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(1.0F), Float.valueOf(0.5F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F)});

        addFoley(foleys[7], 0.4F, 0.1F, 2.0F, 20.0F, new Vector3f(40.0F, 10.0F, 40.0F), new Float[]{Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F)});

        addFoley(foleys[8], 0.4F, 0.1F, 2.0F, 20.0F, new Vector3f(40.0F, 10.0F, 40.0F), new Float[]{Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F)});

        addFoley(foleys[9], 0.4F, 0.1F, 2.0F, 20.0F, new Vector3f(40.0F, 10.0F, 40.0F), new Float[]{Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(1.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F), Float.valueOf(0.0F)});
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

    public void addFoley(String sound, float gain, float probability, float minTime, float maxTime, Vector3f range, Float[] values) {
        RandomFoley foley = new RandomFoley(this.app, sound, gain, this.audioListener, range, probability, minTime, maxTime);

        TimedSound ts = new TimedSound(foley, values);
        this.sounds.add(ts);
        Environment env = Environment.getInstance();
        env.addTimeEffect(ts);
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
