package mythruna.client.gm;

import mythruna.db.BlueprintData;

public abstract interface Tool {
    public abstract void toolAttached(ItemToolState paramItemToolState);

    public abstract void toolDetached();

    public abstract String getName();

    public abstract BlueprintData getIcon();

    public abstract void updateModel();

    public abstract ControlSlot[] getSlots();

    public abstract void mainClick(ControlSlot paramControlSlot);

    public abstract void alternateClick(ControlSlot paramControlSlot);

    public abstract void mainButton(boolean paramBoolean, ControlSlot paramControlSlot);

    public abstract void alternateButton(boolean paramBoolean, ControlSlot paramControlSlot);

    public abstract boolean mainDrag(int paramInt1, int paramInt2, int paramInt3, int paramInt4, ControlSlot paramControlSlot);

    public abstract boolean alternateDrag(int paramInt1, int paramInt2, int paramInt3, int paramInt4, ControlSlot paramControlSlot);

    public abstract boolean hover(boolean paramBoolean, ControlSlot paramControlSlot);

    public abstract void roll(int paramInt, ControlSlot paramControlSlot);
}