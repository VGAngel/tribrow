package org.progeeks.map;

public abstract class AbstractNoiseElevationGenerator extends AbstractElevationGenerator {

    protected AbstractNoiseElevationGenerator(String name) {
        super(name);
        noise = new PerlinNoise();
        frequency = 256;
        additive = false;
        affected = true;
    }

    public void setSeed(int seed) {
        if (this.seed == seed) {
            return;
        } else {
            this.seed = seed;
            invalidate();
            return;
        }
    }

    public int getSeed() {
        return seed;
    }

    public void setFrequency(int f) {
        if (frequency == f) {
            return;
        } else {
            frequency = f;
            invalidate();
            return;
        }
    }

    public int getFrequency() {
        return frequency;
    }

    public void setAdditive(boolean additive) {
        if (this.additive == additive) {
            return;
        } else {
            this.additive = additive;
            invalidate();
            return;
        }
    }

    public boolean isAdditive() {
        return additive;
    }

    public void setAffected(boolean affected) {
        if (this.affected == affected) {
            return;
        } else {
            this.affected = affected;
            invalidate();
            return;
        }
    }

    public boolean isAffected() {
        return affected;
    }

    protected void reinitialize() {
    }

    public int calculateElevation(int x, int y, int z) {
        ElevationData source = getSourceData();
        double scale = 1.0D / (double) frequency;
        int sourceScale = source.getElevationScale();
        int sourceOffset = source.getElevationOffset();
        double elevationScale = getElevationScale();
        double elevationOffset = getElevationOffset();
        double tx = (double) x * scale;
        double ty = (double) y * scale;
        double tz = z;
        if (affected)
            tz = (double) (z - sourceOffset) / (double) sourceScale;
        else
            tz = 0.0D;
        if (!additive)
            return (int) Math.round(getElevation(tx, ty, tz) * elevationScale + elevationOffset);
        else
            return z + (int) Math.round(getElevation(tx, ty, tz) * elevationScale + elevationOffset);
    }

    protected abstract double getElevation(double d, double d1, double d2);

    protected ElevationData generateElevations(ElevationData result) {
        ElevationData source = getSourceData();
        int sourceMap[][] = source.getElevations();
        int mapSize = source.getMapSize();
        if (result == null)
            result = new ElevationData(mapSize);
        result.setBaseX(source.getBaseX());
        result.setBaseY(source.getBaseY());
        int sourceScale = source.getElevationScale();
        int sourceOffset = source.getElevationOffset();
        double elevationScale = getElevationScale();
        double elevationOffset = getElevationOffset();
        int adjustedScale = sourceScale + (int) (elevationScale + elevationOffset);
        int adjustedOffset = sourceOffset - (int) elevationOffset;
        result.setElevationScale(adjustedScale);
        result.setElevationOffset(adjustedOffset);
        if (noise.getSeed() != getSeed())
            noise.setSeed(getSeed());
        int baseX = source.getBaseX();
        int baseY = source.getBaseY();
        int map[][] = result.getElevations();
        double scale = 1.0D / (double) frequency;
        reinitialize();
        for (int x = 0; x < mapSize; x++) {
            for (int y = 0; y < mapSize; y++) {
                double tx = ((double) baseX + (double) x) * scale;
                double ty = ((double) baseY + (double) y) * scale;
                double tz = 0.0D;
                if (affected)
                    tz = (double) (sourceMap[y][x] - sourceOffset) / (double) sourceScale;
                if (!additive)
                    map[y][x] = (int) Math.round(getElevation(tx, ty, tz) * elevationScale + elevationOffset);
                else
                    map[y][x] = sourceMap[y][x] + (int) Math.round(getElevation(tx, ty, tz) * elevationScale + elevationOffset);
            }

        }

        return result;
    }

    protected PerlinNoise noise;
    private int frequency;
    private int seed;
    private boolean additive;
    private boolean affected;
}
