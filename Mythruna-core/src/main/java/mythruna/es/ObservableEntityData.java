package mythruna.es;

public abstract interface ObservableEntityData extends EntityData {

    public abstract void addEntityComponentListener(EntityComponentListener paramEntityComponentListener);

    public abstract void removeEntityComponentListener(EntityComponentListener paramEntityComponentListener);

    public abstract ChangeQueue getChangeQueue(Class[] paramArrayOfClass);

    public abstract void releaseChangeQueue(ChangeQueue paramChangeQueue);
}