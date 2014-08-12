package org.progeeks.map;

public class Erosion extends AbstractElevationGenerator {

    public Erosion() {
        super("Erosion Operator");
        filter = 0.5D;
    }

    public void setSeed(int i) {
    }

    public void setFilter(double filter) {
        if (this.filter == filter) {
            return;
        } else {
            this.filter = filter;
            invalidate();
            return;
        }
    }

    public double getFilter() {
        return filter;
    }

    protected ElevationData generateElevations(ElevationData result) {
        ElevationData source = getSourceData();
        int sourceMap[][] = source.getElevations();
        int mapSize = source.getMapSize();
        if (result == null)
            result = new ElevationData(mapSize);
        int map[][] = result.getElevations();
        for (int y = 0; y < mapSize; y++) {
            double d = map[y][0] = sourceMap[y][0];
            for (int x = 1; x < mapSize; x++) {
                d = filter * d + (1.0D - filter) * (double) sourceMap[y][x];
                map[y][x] = (int) Math.round(d);
            }

        }

        for (int y = 0; y < mapSize; y++) {
            double d = sourceMap[y][mapSize - 1];
            for (int x = mapSize - 1; x >= 0; x--) {
                d = filter * d + (1.0D - filter) * (double) map[y][x];
                map[y][x] = (int) Math.round(d);
            }

        }

        for (int x = 0; x < mapSize; x++) {
            double d = sourceMap[0][x];
            for (int y = 1; y < mapSize; y++) {
                d = filter * d + (1.0D - filter) * (double) map[y][x];
                map[y][x] = (int) Math.round(d);
            }

        }

        for (int x = 0; x < mapSize; x++) {
            double d = sourceMap[mapSize - 1][x];
            for (int y = mapSize - 1; y >= 0; y--) {
                d = filter * d + (1.0D - filter) * (double) map[y][x];
                map[y][x] = (int) Math.round(d);
            }

        }

        return result;
    }

    private double filter;
}
