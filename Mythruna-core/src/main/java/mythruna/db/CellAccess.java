package mythruna.db;

public interface CellAccess {

    public abstract int getCellType(int i, int j, int k);

    public abstract int getLight(int i, int j, int k, int l);

    public abstract void setCellType(int i, int j, int k, int l);
}
