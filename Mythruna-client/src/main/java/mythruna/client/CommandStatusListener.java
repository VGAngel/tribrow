package mythruna.client;

public abstract interface CommandStatusListener {
    public abstract void successful(String paramString);

    public abstract void failed(String paramString);
}