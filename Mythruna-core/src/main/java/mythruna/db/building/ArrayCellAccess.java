package mythruna.db.building;

import mythruna.db.CellAccess;

public class ArrayCellAccess implements CellAccess {

    int[][][] cells;

    public ArrayCellAccess(int x, int y, int z) {
        this.cells = new int[x][y][z];
    }

    public final int getCellType(int x, int y, int z) {
        return this.cells[x][y][z];
    }

    public final void setCellType(int x, int y, int z, int type) {
        this.cells[x][y][z] = type;
    }

    public final int getLight(int lightType, int x, int y, int z) {
        return 0;
    }
}