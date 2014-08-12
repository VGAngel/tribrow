package mythruna.db;

import mythruna.BlockType;
import mythruna.Coordinates;
import mythruna.Vector3i;
import mythruna.util.LruCache;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractColumnWorldDatabase implements WorldDatabase {

    private static final int MAX_CACHE_SIZE = 169;
    private static final int COLUMN_INFO_CACHE_SIZE = 1024;
    private LruCache<Object, Column> cache = new LruCache("Column", 169);
    private LruCache<Object, ColumnInfo> columnCache = new LruCache("ColumnInfo", 1024);

    private List<CellChangeListener> listeners = new CopyOnWriteArrayList();
    private List<LeafChangeListener> leafListeners = new CopyOnWriteArrayList();

    protected AbstractColumnWorldDatabase() {
    }

    public abstract void setSeed(int paramInt);

    public abstract ColumnFactory getColumnFactory();

    public void close() {
    }

    public abstract boolean leafExists(int paramInt1, int paramInt2, int paramInt3);

    public ColumnInfo getColumnInfo(int x, int y, boolean load) {
        ColumnInfo info = (ColumnInfo) this.columnCache.get(toColumnKey(x, y));
        if (info != null) {
            return info;
        }
        Column col = getColumn(x, y, load);
        if (col == null)
            return null;
        return col.info;
    }

    public LeafData getLeaf(int x, int y, int z) {
        return getLeaf(x, y, z, true);
    }

    public LeafData getLeaf(int x, int y, int z, boolean load) {
        if ((z > 159) || (z < 0)) {
            return null;
        }
        Column col = getColumn(x, y, load);
        if (col == null) {
            return null;
        }
        return col.getLeaf(z);
    }

    public void setCellType(int x, int y, int z, int type) {
        setCellType(x, y, z, type, null);
    }

    public int setCellType(int x, int y, int z, int type, LeafData leaf) {
        if ((z >= 159) && (type != 0))
            return -1;
        if (leaf == null) {
            leaf = getLeaf(x, y, z);
        }
        int old = leaf.getWorld(x, y, z);

        LightPropagation lp = new LightPropagation(this);
        Set<LeafData> changed = lp.changeLighting(leaf, x, y, z, old, type);

        leaf.setWorldType(x, y, z, type);
        leaf.markChanged();

        fireCellChanged(leaf, x, y, z, old, type);
        fireLeafChanged(leaf);

        int i = x - leaf.getX();
        int j = y - leaf.getY();
        int k = z - leaf.getZ();

        if (i == 0)
            checkBorder(leaf, x, y, z, old, type, 3);
        if (j == 0)
            checkBorder(leaf, x, y, z, old, type, 0);
        if (k == 0)
            checkBorder(leaf, x, y, z, old, type, 5);
        if (i == 31)
            checkBorder(leaf, x, y, z, old, type, 2);
        if (j == 31)
            checkBorder(leaf, x, y, z, old, type, 1);
        if (k == 31) {
            checkBorder(leaf, x, y, z, old, type, 4);
        }
        for (LeafData l : changed) {
            if (l != leaf) {
                fireLeafChanged(l);
            }
        }

        Column col = getColumn(x, y, true);
        col.resetColumnInfo();

        return old;
    }

    protected void checkBorder(LeafData leaf, int x, int y, int z, int old, int type, int dir) {
        BlockType oldType = mythruna.BlockTypeIndex.types[old];
        BlockType newType = mythruna.BlockTypeIndex.types[type];

        if (((oldType == null) || (!oldType.isBoundary(dir))) && ((newType == null) || (!newType.isBoundary(dir)))) {
            return;
        }

        LeafData neighbor = getLeaf(x + mythruna.Direction.DIRS[dir][0], y + mythruna.Direction.DIRS[dir][1], z + mythruna.Direction.DIRS[dir][2]);

        if (neighbor == null) {
            return;
        }
        int adj = neighbor.getWorld(x + mythruna.Direction.DIRS[dir][0], y + mythruna.Direction.DIRS[dir][1], z + mythruna.Direction.DIRS[dir][2]);

        BlockType adjType = mythruna.BlockTypeIndex.types[adj];

        if ((adjType != null) && (adjType.isSolid(mythruna.Direction.INVERSE[dir]))) ;
        fireLeafChanged(neighbor);
    }

    protected abstract void writeLeaf(LeafData paramLeafData) throws IOException;

    protected abstract LeafData loadLeaf(int paramInt1, int paramInt2, int paramInt3) throws IOException;

    public void resetLeaf(LeafData leaf) {
        synchronized (this) {
            LeafInfo info = leaf.getInfo();

            Column col = getColumn(info.x, info.y, false);
            System.out.println("Resetting leaf:" + leaf + "  in column:" + col);

            long start = System.nanoTime();
            LightPropagation lp = new LightPropagation(this);
            Set<LeafData> changed = lp.refreshLights(col.leafs);
            long end = System.nanoTime();

            for (LeafData l : changed) {
                if (l != leaf) {
                    fireLeafChanged(l);
                }
            }
        }

        fireLeafReset(leaf);
    }

    public void markChanged(LeafData leaf) {
        try {
            synchronized (this) {
                if (leaf.isChanged()) {
                    leaf.getInfo().version += 1L;
                    leaf.clearChanged();
                }

                writeLeaf(leaf);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error storing leaf", e);
        }
    }

    public Vector3i findSafeLocation(int x, int y, int z) {
        Vector3i result = new Vector3i(x, y, z);
        return result;
    }

    public int getCellType(int x, int y, int z) {
        LeafData leaf = getLeaf(x, y, z);
        if (leaf == null)
            return 0;
        return leaf.getWorld(x, y, z);
    }

    public final int getLight(int lightType, int x, int y, int z) {
        LeafData leaf = getLeaf(x, y, z);
        if (leaf == null)
            return 15;
        return leaf.getWorldLight(lightType, x, y, z);
    }

    public void addCellChangeListener(CellChangeListener l) {
        this.listeners.add(l);
    }

    public void removeCellChangeListener(CellChangeListener l) {
        this.listeners.remove(l);
    }

    public void addLeafChangeListener(LeafChangeListener l) {
        this.leafListeners.add(l);
    }

    public void removeLeafChangeListener(LeafChangeListener l) {
        this.leafListeners.remove(l);
    }

    protected void fireCellChanged(LeafData leaf, int x, int y, int z, int oldType, int newType) {
        CellChangeEvent event = new CellChangeEvent(leaf, x, y, z, oldType, newType);
        for (CellChangeListener l : this.listeners)
            l.cellChanged(event);
    }

    protected void fireLeafChanged(LeafData leaf) {
        LeafChangeEvent event = new LeafChangeEvent(LeafChangeEvent.ChangeType.MODIFIED, leaf);
        for (LeafChangeListener l : this.leafListeners)
            l.leafChanged(event);
    }

    protected void fireLeafReset(LeafData leaf) {
        LeafChangeEvent event = new LeafChangeEvent(LeafChangeEvent.ChangeType.RESET, leaf);
        for (LeafChangeListener l : this.leafListeners)
            l.leafChanged(event);
    }

    protected void fireLeafCreated(LeafData leaf) {
        LeafChangeEvent event = new LeafChangeEvent(LeafChangeEvent.ChangeType.CREATED, leaf);
        for (LeafChangeListener l : this.leafListeners)
            l.leafChanged(event);
    }

    protected Object toColumnKey(int x, int y) {
        int i = Coordinates.worldToLeaf(x);
        int j = Coordinates.worldToLeaf(y);
        return i + "x" + j;
    }

    protected Column getColumn(int x, int y, boolean load) {
        Object key = toColumnKey(x, y);

        Column col = (Column) this.cache.get(key);
        if ((col != null) || (!load)) {
            return col;
        }

        synchronized (this) {
            col = (Column) this.cache.get(key);
            if (col != null) {
                return col;
            }
            long start = System.nanoTime();
            col = loadColumn(x, y);
            long end = System.nanoTime();

            System.out.println("Loaded column[" + key + "] in:" + (end - start / 1000000.0D) + " ms.");
            if (col != null) {
                this.cache.put(key, col);
            }
            return col;
        }
    }

    protected Column loadColumn(int x, int y) {
        x = Coordinates.leafToWorld(Coordinates.worldToLeaf(x));
        y = Coordinates.leafToWorld(Coordinates.worldToLeaf(y));

        Column col = new Column(x, y);
        try {
            boolean newLeafs = loadLeafs(col);
            if (newLeafs) {
                col.resetColumnInfo();
            } else {
                col.resetColumnInfo();
            }
            return col;
        } catch (IOException e) {
            throw new RuntimeException("Error loading column:" + x + ", " + y, e);
        }
    }

    protected abstract LeafData[] createLeafs(int paramInt1, int paramInt2);

    protected boolean loadLeafs(Column col)
            throws IOException {
        LeafData[] generated = null;
        boolean[] newLeaf = new boolean[5];
        boolean relight = false;

        int loadedCount = 0;
        int generatedCount = 0;
        for (int i = 0; i < col.leafs.length; i++) {
            col.leafs[i] = loadLeaf(col.x, col.y, Coordinates.leafToWorld(i));
            if (col.leafs[i] == null) {
                if (generated == null)
                    generated = createLeafs(col.x, col.y);
                if (generated != null) {
                    col.leafs[i] = generated[i];
                    newLeaf[i] = true;
                    generatedCount++;
                    if (!col.leafs[i].isLit())
                        relight = true;
                }
            } else {
                loadedCount++;
            }
        }

        if (generatedCount > 0) {
            System.out.println("loaded " + loadedCount + "  generate:" + generatedCount);
        }

        if (relight) {
            long start = System.nanoTime();
            LightPropagation lp = new LightPropagation(this);
            Set<LeafData> changed = lp.refreshLights(col.leafs);
            long end = System.nanoTime();

            for (int i = 0; i < col.leafs.length; i++) {
                if (newLeaf[i] != false) {
                //if (newLeaf[i] != 0) {
                    fireLeafCreated(col.leafs[i]);

                    changed.remove(col.leafs[i]);
                }

            }

            for (LeafData l : changed) {
                if ((l.getInfo().x == col.x) && (l.getInfo().y == col.y)) {
                    if (l.getInfo().version == 0L) {
                        fireLeafCreated(l);
                    }

                } else {
                    fireLeafChanged(l);
                }
            }
            return true;
        }

        return false;
    }

    protected class Column {
        protected ColumnInfo info;
        protected int x;
        protected int y;
        protected LeafData[] leafs = new LeafData[5];

        public Column(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public LeafData[] getLeafs() {
            return this.leafs;
        }

        public LeafData getLeaf(int z) {
            int k = Coordinates.worldToLeaf(z);
            if (k >= 5)
                return null;
            return this.leafs[k];
        }

        protected void resetColumnInfo() {
            ColumnInfo temp = this.info;
            if (temp == null) {
                temp = new ColumnInfo(this.x, this.y);
            }

            temp.recalculate(this.leafs);

            AbstractColumnWorldDatabase.this.columnCache.put(AbstractColumnWorldDatabase.this.toColumnKey(this.x, this.y), temp);
            this.info = temp;
        }
    }
}