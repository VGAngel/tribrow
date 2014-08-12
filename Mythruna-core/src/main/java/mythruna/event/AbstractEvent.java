package mythruna.event;

public abstract class AbstractEvent<C> {

    private C context;

    protected AbstractEvent(C context) {
        this.context = context;
    }

    public C getContext() {
        return this.context;
    }

    public String toString() {
        return getClass().getSimpleName() + "[" + this.context + "]";
    }
}