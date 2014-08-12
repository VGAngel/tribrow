package mythruna.client.tabs.map;

import mythruna.Coordinates;
import mythruna.Vector3i;
import mythruna.db.*;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapBuilder {
    private static final int LEAF_COUNT = 32;
    private static final int SIZE = 1024;
    private static final ColumnInfo NULL_COLUMN = new ColumnInfo(2147483647, 2147483647);
    private WorldDatabase worldDb;
    private ColumnInfo[][] infos;
    private byte[][] types;
    private int xLast;
    private int yLast;
    private long lastRefreshTime = 0L;
    private long refreshDelta = 1000L;
    private BufferedImage map;
    private BufferedImage working;
    private ConcurrentHashMap<Vector3i, ColumnInfo> updates = new ConcurrentHashMap();
    private LeafObserver leafObserver = new LeafObserver();

    public MapBuilder(WorldDatabase worldDb) {
        this.worldDb = worldDb;

        worldDb.addLeafChangeListener(this.leafObserver);
    }

    public int getImageSize() {
        return 1024;
    }

    protected boolean containsColumn(int x, int y) {
        if ((x < this.xLast) || (y < this.yLast))
            return false;
        x -= this.xLast;
        y -= this.yLast;
        if ((x >= 32) || (y >= 32))
            return false;
        return true;
    }

    protected ColumnInfo getInfo(int x, int y) {
        if (this.infos == null)
            return null;
        if ((x < this.xLast) || (y < this.yLast))
            return null;
        x -= this.xLast;
        y -= this.yLast;
        if ((x >= 32) || (y >= 32))
            return null;
        return this.infos[x][y];
    }

    protected void resetLocation(int x, int y) {
        ColumnInfo[][] arrays = new ColumnInfo[32][32];
        this.types = new byte[1024][1024];

        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                arrays[i][j] = getInfo(x + i, y + j);
                if (arrays[i][j] != null) {
                    copyTypes(x, y, arrays[i][j]);
                } else {
                    Vector3i loc = new Vector3i(Coordinates.leafToWorld(x + i), Coordinates.leafToWorld(y + j), 0);

                    arrays[i][j] = this.worldDb.getColumnInfo(loc.x, loc.y, false);

                    if (arrays[i][j] != null) {
                        copyTypes(x, y, arrays[i][j]);
                    }

                }

            }

        }

        this.xLast = x;
        this.yLast = y;
        this.infos = arrays;
    }

    protected void copyTypes(int xBase, int yBase, ColumnInfo info) {
        xBase = info.getX() - Coordinates.leafToWorld(xBase);
        yBase = info.getY() - Coordinates.leafToWorld(yBase);

        byte[][] colTypes = info.getTypes();
        byte[][] elevations = info.getElevations();

        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                byte t = MapTile.typeToTerrain(colTypes[i][j], elevations[i][j] & 0xFF);
                this.types[(xBase + i)][(yBase + j)] = t;
            }
        }
    }

    protected void applyChanges() {
        for (Iterator i = this.updates.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry) i.next();
            i.remove();
            Vector3i v = (Vector3i) e.getKey();
            ColumnInfo info = (ColumnInfo) e.getValue();
            if (info == NULL_COLUMN)
                info = this.worldDb.getColumnInfo(v.x, v.y, false);
            if (info != null) {
                int x = Coordinates.worldToLeaf(info.getX());
                int y = Coordinates.worldToLeaf(info.getY());

                if (containsColumn(x, y)) {
                    x -= this.xLast;
                    y -= this.yLast;
                    this.infos[x][y] = info;
                    copyTypes(this.xLast, this.yLast, info);
                }
            }
        }
    }

    protected void generateMap() {
        if (this.map == null) {
            System.out.println("Creating map images...");
            int imageSize = 1024;
            this.map = new BufferedImage(imageSize, imageSize, 2);
            this.working = new BufferedImage(imageSize, imageSize, 2);
        }

        this.map = MapUtils.drawImage(this.working, this.map, this.types, this.xLast, this.yLast, 32);
    }

    public BufferedImage getLastMap() {
        return this.map;
    }

    protected boolean needsRefresh(int x, int y) {
        if (this.infos == null) {
            return true;
        }

        if ((this.xLast == x) && (this.yLast == y) && (this.updates.size() == 0)) {
            return false;
        }

        long time = System.currentTimeMillis();
        if (time < this.lastRefreshTime + this.refreshDelta) {
            return false;
        }
        return true;
    }

    public BufferedImage getMapImage(int x, int y) {
        if (!needsRefresh(x, y)) {
            return null;
        }

        resetLocation(x, y);

        applyChanges();

        generateMap();

        this.lastRefreshTime = System.currentTimeMillis();

        return this.map;
    }

    private class LeafObserver implements LeafChangeListener {
        private LeafObserver() {
        }

        public void leafChanged(LeafChangeEvent event) {
            LeafInfo info = event.getLeaf().getInfo();
            Vector3i column = new Vector3i(info.x, info.y, 0);
            MapBuilder.this.updates.put(column, MapBuilder.NULL_COLUMN);
        }
    }
}
