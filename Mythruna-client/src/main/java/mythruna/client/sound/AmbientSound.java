package mythruna.client.sound;

public abstract interface AmbientSound {
    public abstract void setEnabled(boolean paramBoolean);

    public abstract boolean isEnabled();

    public abstract void setMasterVolume(float paramFloat);

    public abstract void setVolume(float paramFloat);

    public abstract void update(float paramFloat);

    public abstract void stop();
}