package org.progeeks.map;

import java.util.Arrays;
import java.util.Random;

public class MidpointDisplacementElevations extends AbstractElevationGenerator {

    public MidpointDisplacementElevations() {
        super("Midpoint Displacement");
        roughness = 3D;
        roughnessBias = 5;
        preseedFactor = 2;
        drift = 4;
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

    public void setPreseedFrequency(int s) {
        int mapSize = getSourceData().getMapSize();
        int seedMax = (int) Math.round(Math.log(mapSize) / Math.log(2D));
        if (s > seedMax)
            s = seedMax;
        else if (s < 0)
            s = 0;
        if (preseedFactor == s) {
            return;
        } else {
            preseedFactor = s;
            invalidate();
            return;
        }
    }

    public int getPreseedFrequency() {
        return preseedFactor;
    }

    public void setRoughnessScale(double r) {
        if (roughness == r) {
            return;
        } else {
            roughness = r;
            invalidate();
            return;
        }
    }

    public double getRoughnessScale() {
        return roughness;
    }

    public void setRoughnessBias(int bias) {
        if (roughnessBias == bias) {
            return;
        } else {
            roughnessBias = bias;
            invalidate();
            return;
        }
    }

    public int getRoughnessBias() {
        return roughnessBias;
    }

    public void setDrift(int drift) {
        if (this.drift == drift) {
            return;
        } else {
            this.drift = drift;
            invalidate();
            return;
        }
    }

    public int getDrift() {
        return drift;
    }

    protected int driftedAverage(int v1, int v2, int drift, Random rand) {
        int r = rand.nextInt(drift) + 1;
        int count = r;
        int total = v1 * r;
        r = rand.nextInt(drift) + 1;
        count += r;
        total += v2 * r;
        return total / count;
    }

    protected int driftedAverage(int v1, int v2, int v3, int v4, int drift, Random rand) {
        int r = rand.nextInt(drift) + 1;
        int count = r;
        int total = v1 * r;
        r = rand.nextInt(drift) + 1;
        count += r;
        total += v2 * r;
        r = rand.nextInt(drift) + 1;
        count += r;
        total += v3 * r;
        r = rand.nextInt(drift) + 1;
        count += r;
        total += v4 * r;
        return total / count;
    }

    protected ElevationData generateElevations(ElevationData result) {
        ElevationData source = getSourceData();
        int sourceMap[][] = source.getElevations();
        int mapSize = source.getMapSize();
        if (result == null)
            result = new ElevationData(mapSize);
        result.setElevationScale(getElevationScale());
        result.setElevationOffset(getElevationOffset());
        int sourceScale = source.getElevationScale();
        int sourceOffset = source.getElevationOffset();
        int scale = getElevationScale();
        int offset = getElevationOffset();
        int map[][] = result.getElevations();
        Random rand = new Random(getSeed());
        for (int y = 0; y <= mapSize; y++)
            Arrays.fill(map[y], offset);

        int heightRange = scale;
        int seedMax = (int) Math.round(Math.log(mapSize) / Math.log(2D));
        int seedStep = seedMax - preseedFactor;
        seedStep = (int) Math.pow(2D, seedStep);
        for (int x = 0; x <= mapSize; x += seedStep) {
            for (int y = 0; y <= mapSize; y += seedStep) {
                if (y == 0 || x == 0 || x == mapSize || y == mapSize)
                    continue;
                if (sourceMap[y][x] != 0)
                    map[y][x] = sourceMap[y][x];
                else
                    map[y][x] = offset + rand.nextInt(scale);
            }

        }

        double cos45 = Math.cos(Math.toRadians(45D));
        int step = seedStep;
        double displacement = (double) (scale / 2) * roughness;
        double count = Math.log(step) / Math.log(2D);
        count += roughnessBias;
        double displacementStep = Math.exp(Math.log(displacement) / count);
        boolean doElevFactor = false;
        while (step > 1) {
            int halfStep = step / 2;
            int disp = (int) displacement;
            int halfDisp = disp / 2;
            for (int x = 0; x < mapSize; x += step) {
                for (int y = 0; y < mapSize; y += step) {
                    int nw = map[y][x];
                    int ne = map[y][x + step];
                    int sw = map[y + step][x];
                    int se = map[y + step][x + step];
                    int w = map[y + halfStep][x];
                    int n = map[y][x + halfStep];
                    int average = driftedAverage(ne, se, drift, rand);
                    int rise = disp <= 0 ? 0 : rand.nextInt(disp) - halfDisp;
                    int e = average + rise;
                    average = driftedAverage(sw, se, drift, rand);
                    rise = disp <= 0 ? 0 : rand.nextInt(disp) - halfDisp;
                    int s = average + rise;
                    average = driftedAverage(n, s, e, w, drift, rand);
                    rise = disp <= 0 ? 0 : rand.nextInt(disp) - halfDisp;
                    int center = average + rise;
                    map[y + step][x + halfStep] = s;
                    map[y + halfStep][x + step] = e;
                    map[y + halfStep][x + halfStep] = center;
                }

            }

            step = halfStep;
            displacement /= displacementStep;
        }
        return result;
    }

    private double roughness;
    private int roughnessBias;
    private int seed;
    private int preseedFactor;
    private int drift;
}
