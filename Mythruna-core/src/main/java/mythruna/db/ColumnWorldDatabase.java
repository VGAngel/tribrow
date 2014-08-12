package mythruna.db;

import java.io.IOException;

public class ColumnWorldDatabase extends AbstractColumnWorldDatabase {

    private int seed;
    private LeafDatabase leafDb;
    private ColumnFactory factory;

    public ColumnWorldDatabase(LeafDatabase leafDb, ColumnFactory factory) {
        this.leafDb = leafDb;
        this.factory = factory;
    }

    public LeafDatabase getLeafDb() {
        return this.leafDb;
    }

    public ColumnFactory getColumnFactory() {
        return this.factory;
    }

    public void setSeed(int seed) {
        this.seed = seed;
        this.factory.setSeed(seed);
    }

    public int getSeed() {
        return this.seed;
    }

    public void close() {
    }

    public boolean leafExists(int x, int y, int z) {
        if ((z > 159) || (z < 0)) {
            return false;
        }
        try {
            return this.leafDb.exists(x, y, z);
        } catch (IOException e) {
        }
        throw new RuntimeException("Error checking leaf existence.");
    }

    protected void writeLeaf(LeafData leaf)
            throws IOException {
        this.leafDb.writeData(leaf);
    }

    protected LeafData loadLeaf(int x, int y, int z) throws IOException {
        return this.leafDb.readData(x, y, z);
    }

    protected LeafData[] createLeafs(int x, int y) {
        return this.factory.createLeafs(x, y);
    }
}