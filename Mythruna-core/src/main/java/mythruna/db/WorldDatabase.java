package mythruna.db;

import mythruna.Vector3i;

public abstract interface WorldDatabase extends CellAccess {

    public abstract void setSeed(int paramInt);

    public abstract int getSeed();

    public abstract void close();

    public abstract ColumnInfo getColumnInfo(int paramInt1, int paramInt2, boolean paramBoolean);

    public abstract ColumnFactory getColumnFactory();

    public abstract boolean leafExists(int paramInt1, int paramInt2, int paramInt3);

    public abstract LeafData getLeaf(int paramInt1, int paramInt2, int paramInt3);

    public abstract LeafData getLeaf(int paramInt1, int paramInt2, int paramInt3, boolean paramBoolean);

    public abstract void resetLeaf(LeafData paramLeafData);

    public abstract void markChanged(LeafData paramLeafData);

    public abstract Vector3i findSafeLocation(int paramInt1, int paramInt2, int paramInt3);

    public abstract int setCellType(int paramInt1, int paramInt2, int paramInt3, int paramInt4, LeafData paramLeafData);

    public abstract int getCellType(int paramInt1, int paramInt2, int paramInt3);

    public abstract int getLight(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

    public abstract void addCellChangeListener(CellChangeListener paramCellChangeListener);

    public abstract void removeCellChangeListener(CellChangeListener paramCellChangeListener);

    public abstract void addLeafChangeListener(LeafChangeListener paramLeafChangeListener);

    public abstract void removeLeafChangeListener(LeafChangeListener paramLeafChangeListener);
}