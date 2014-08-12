package mythruna.client.sound;

public abstract class AbstractAmbientSound implements AmbientSound {
    private float masterVolume = 1.0F;
    private float volume = 1.0F;
    private float effectiveVolume = 1.0F;
    private boolean enabled;

    protected AbstractAmbientSound() {
    }

    public void setEnabled(boolean f) {
        if (f == this.enabled)
            return;
        this.enabled = f;
        adjustEnabled(f);
        if (!this.enabled)
            stop();
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    protected void setEffectiveVolume(float v) {
        if (this.effectiveVolume == v)
            return;
        this.effectiveVolume = v;
        adjustVolume(v);
    }

    protected float getEffectiveVolume() {
        return this.effectiveVolume;
    }

    protected abstract void adjustEnabled(boolean paramBoolean);

    protected abstract void adjustVolume(float paramFloat);

    public void setMasterVolume(float v) {
        if (this.masterVolume == v)
            return;
        this.masterVolume = v;
        setEffectiveVolume(this.volume * this.masterVolume);
    }

    public void setVolume(float volume) {
        if (this.volume == volume) {
            return;
        }
        this.volume = volume;
        setEffectiveVolume(volume * this.masterVolume);
    }
}