package mythruna.db;

public interface ColumnFactory {

    public abstract void setSeed(int i);

    public abstract LeafData[] createLeafs(int i, int j);
}
