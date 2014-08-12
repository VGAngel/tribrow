package mythruna.client.sound;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.Listener;
import com.jme3.math.Vector3f;

public class RandomFoley extends AbstractAmbientSound {
    private AudioRenderer audioRenderer;
    private AssetManager assetManager;
    private String soundName;
    private SoundNode sound;
    private boolean stream = false;
    private boolean active = false;
    private float gain = 1.0F;
    private Listener audioListener;
    private Vector3f range;
    private float probability = 0.25F;
    private float minTimeDelta = 1.0F;
    private float maxTimeDelta = 2.0F;
    private float nextPlay = 0.0F;
    private float time;

    public RandomFoley(Application app, String soundName, float gain, Listener listener, Vector3f range, float probability, float minTime, float maxTime) {
        this.audioRenderer = app.getAudioRenderer();
        this.assetManager = app.getAssetManager();
        this.soundName = soundName;
        this.gain = gain;
        this.audioListener = listener;
        this.range = range;
        this.probability = probability;
        this.minTimeDelta = minTime;
        this.maxTimeDelta = maxTime;
    }

    protected void adjustVolume(float v) {
        if ((this.sound != null) && (this.sound.isPlaying()))
            this.sound.setVolume(v * this.gain);
    }

    protected void adjustEnabled(boolean enabled) {
    }

    public void update(float tpf) {
        this.time += tpf;
        if ((isEnabled()) && (this.time > this.nextPlay) && ((this.sound == null) || (!this.active)) && (getEffectiveVolume() > 0.0F)) {
            double dice = Math.random();

            if (dice > this.probability) {
                this.nextPlay = (this.time + this.minTimeDelta + (float) (Math.random() * this.maxTimeDelta - this.minTimeDelta));

                return;
            }

            if (this.sound == null)
                this.sound = new SoundNode(this.audioRenderer, this.assetManager, this.soundName, this.stream);
            this.active = true;
            this.sound.setPositional(true);
            this.sound.setReverbEnabled(false);
            this.sound.setVolume(getEffectiveVolume() * this.gain);
            float x = (float) (this.range.x * 2.0F * Math.random() - this.range.x);
            float y = (float) (this.range.y * 2.0F * Math.random() - this.range.y);
            float z = (float) (this.range.z * 2.0F * Math.random() - this.range.z);
            Vector3f loc = this.audioListener.getLocation();
            this.sound.setLocalTranslation(loc.x + x, loc.y + y, loc.z + z);
            System.out.println("Play:" + this.soundName + "  at:" + this.sound.getLocalTranslation());
            this.sound.play();
            return;
        }

        if ((this.active) && (this.sound != null) && (!this.sound.isPlaying())) {
            if (this.stream)
                this.sound = null;
            this.active = false;
            this.nextPlay = (this.time + this.minTimeDelta + (float) (Math.random() * this.maxTimeDelta - this.minTimeDelta));
        }
    }

    public void stop() {
        if (this.sound != null) {
            this.sound.stop();
            if (this.stream)
                this.sound = null;
            this.active = false;
        }
    }

    public String toString() {
        return "RandomFoley[" + this.soundName + ", " + getEffectiveVolume() + "]";
    }
}