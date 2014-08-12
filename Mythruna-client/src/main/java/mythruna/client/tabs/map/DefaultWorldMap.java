package mythruna.client.tabs.map;

import mythruna.db.WorldDatabase;

public class DefaultWorldMap implements WorldMap {

    private WorldDatabase worldDb;
    private boolean forceLoad;

    public DefaultWorldMap(WorldDatabase worldDb) {
        this(worldDb, false);
    }

    public DefaultWorldMap(WorldDatabase worldDb, boolean forceLoad) {
        this.worldDb = worldDb;
        this.forceLoad = forceLoad;
    }

    public MapTile getTile(int i, int j) {
        MapTile result = new MapTile(i * 256, j * 256);
        result.initialize(this.worldDb, this.forceLoad);
        return result;
    }

    public byte[][] getTypes(int x, int y, int size) {
        int arraySize = size * 32;
        byte[][] result = new byte[arraySize][arraySize];

        int xWorld = x * 32;
        int yWorld = y * 32;
        int xWorldMax = x * 32 + arraySize;
        int yWorldMin = y * 32 + arraySize;
        int xTile = MapTile.worldToTile(xWorld);
        int yTile = MapTile.worldToTile(yWorld);
        int xTileMax = MapTile.worldToTile(xWorld + arraySize - 1);
        int yTileMax = MapTile.worldToTile(yWorld + arraySize - 1);

        System.out.println("xTile:" + xTile + "  xTileMax:" + xTileMax);
        System.out.println("yTile:" + yTile + "  yTileMax:" + yTileMax);

        for (int i = xTile; i <= xTileMax; i++) {
            for (int j = yTile; j <= yTileMax; j++) {
                System.out.println("Processing tile:" + i + ", " + j);
                MapTile tile = getTile(i, j);

                copyTypes(tile, result, xWorld, yWorld, arraySize);
            }
        }

        return result;
    }

    protected void copyTypes(MapTile tile, byte[][] result, int xBase, int yBase, int arraySize) {
        int xStart = Math.max(tile.getX(), xBase);
        int yStart = Math.max(tile.getY(), yBase);
        int xEnd = Math.min(tile.getX() + 256, xBase + arraySize);
        int yEnd = Math.min(tile.getY() + 256, yBase + arraySize);

        int xSize = xEnd - xStart;
        int ySize = yEnd - yStart;

        for (int i = 0; i < xSize; i++) {
            int x = xStart + i;
            for (int j = 0; j < ySize; j++) {
                int y = yStart + j;

                byte t = tile.getType(x, y);
                result[(x - xBase)][(y - yBase)] = t;
            }
        }
    }
}