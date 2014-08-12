package org.progeeks.map;


public class ElevationData {

    public ElevationData(int mapSize) {
        scale = 15000;
        offset = -1500;
        this.mapSize = mapSize;
    }

    public void setBaseX(int x) {
        baseX = x;
    }

    public int getBaseX() {
        return baseX;
    }

    public void setBaseY(int y) {
        baseY = y;
    }

    public int getBaseY() {
        return baseY;
    }

    public int[][] getElevations() {
        if (map == null)
            map = new int[mapSize + 1][mapSize + 1];
        return map;
    }

    public void setMapSize(int mapSize) {
        if (this.mapSize == mapSize) {
            return;
        } else {
            this.mapSize = mapSize;
            map = (int[][]) null;
            return;
        }
    }

    public int getMapSize() {
        return mapSize;
    }

    public void setElevationScale(int scale) {
        this.scale = scale;
    }

    public int getElevationScale() {
        return scale;
    }

    public void setElevationOffset(int offset) {
        this.offset = offset;
    }

    public int getElevationOffset() {
        return offset;
    }

    private int map[][];
    private int mapSize;
    private int scale;
    private int offset;
    private int baseX;
    private int baseY;
}
