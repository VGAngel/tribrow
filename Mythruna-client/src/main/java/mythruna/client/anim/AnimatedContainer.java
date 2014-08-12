package mythruna.client.anim;

public abstract interface AnimatedContainer {
    public abstract void attach();

    public abstract void detach();

    public abstract AnimationTask[] animateOpen();

    public abstract AnimationTask[] animateClose();
}