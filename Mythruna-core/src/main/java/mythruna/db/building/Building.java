package mythruna.db.building;

import mythruna.Vector3i;
import mythruna.db.CellAccess;

public interface Building {

    public abstract void place(Vector3i vector3i, CellAccess cellaccess);

    public abstract Vector3i getSize();

    public abstract void rotate(int i);
}
