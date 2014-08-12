package mythruna.client.tabs.property;

public abstract interface Table<V> {
    public abstract void update();

    public abstract long getChangeVersion();

    public abstract int getColumnCount();

    public abstract int getSize();

    public abstract String getHeading(int paramInt);

    public abstract String getValue(int paramInt1, int paramInt2);

    public abstract V getRow(int paramInt);
}