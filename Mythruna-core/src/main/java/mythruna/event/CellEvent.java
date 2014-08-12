package mythruna.event;

import mythruna.Vector3i;

public class CellEvent extends AbstractEvent<Object> {

    private Vector3i cell;
    private int oldType;
    private int newType;

    public CellEvent(Vector3i cell, int oldType, int newType) {
        super(null);
        this.cell = cell;
        this.oldType = oldType;
        this.newType = newType;
    }

    public Vector3i getCell() {
        return this.cell;
    }

    public int getOldType() {
        return this.oldType;
    }

    public int getNewType() {
        return this.newType;
    }

    public void setNewType(int t) {
        this.newType = t;
    }

    public void revert() {
        setNewType(getOldType());
    }

    public boolean isReverted() {
        return this.oldType == this.newType;
    }

    public String toString() {
        return "CellEvent[" + this.cell + ", " + this.oldType + " -> " + this.newType + "]";
    }
}