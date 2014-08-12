package mythruna.client.sound;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioRenderer;

public class AmbientLoop extends AbstractAmbientSound {
    private AudioRenderer audioRenderer;
    private AssetManager assetManager;
    private String soundName;
    private SoundNode sound;

    public AmbientLoop(Application app, String soundName) {
        this.audioRenderer = app.getAudioRenderer();
        this.assetManager = app.getAssetManager();
        this.soundName = soundName;
    }

    protected void adjustVolume(float v) {
        if ((this.sound != null) && (this.sound.isPlaying())) {
            this.sound.setVolume(v);
            if (v <= 0.0F) {
                this.sound.stop();
                this.sound = null;
            }
        }
    }

    protected void adjustEnabled(boolean enabled) {
    }

    public void update(float tpf) {
        if ((isEnabled()) && ((this.sound == null) || (!this.sound.isPlaying())) && (getEffectiveVolume() > 0.0F)) {
            System.out.println("Restarting:" + this.soundName);
            this.sound = new SoundNode(this.audioRenderer, this.assetManager, this.soundName, true);
            this.sound.setPositional(false);
            this.sound.setReverbEnabled(false);
            this.sound.setVolume(getEffectiveVolume());
            this.sound.play();
        }
    }

    public void stop() {
        if (this.sound != null) {
            this.sound.stop();
            this.sound = null;
        }
    }

    public String toString() {
        return "AmbientLoop[" + this.soundName + ", " + getEffectiveVolume() + "]";
    }
}