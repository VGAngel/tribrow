package org.progeeks.map;

import java.util.Arrays;
import java.util.Random;

public class DiamondSquaresElevations extends AbstractElevationGenerator {

    private double roughness = 3.0D;

    private int roughnessBias = 5;
    private int seed;
    private int preseedFactor = 2;

    public DiamondSquaresElevations() {
        super("Diamond/Squares");
        setElevationScale(16500);
    }

    public void setSeed(int seed) {
        if (this.seed == seed)
            return;
        this.seed = seed;
        invalidate();
    }

    public int getSeed() {
        return this.seed;
    }

    public void setPreseedFrequency(int s) {
        int mapSize = getSourceData().getMapSize();
        int seedMax = (int) Math.round(Math.log(mapSize) / Math.log(2.0D));
        if (s > seedMax)
            s = seedMax;
        else if (s < 0) {
            s = 0;
        }
        if (this.preseedFactor == s)
            return;
        this.preseedFactor = s;
        invalidate();
    }

    public int getPreseedFrequency() {
        return this.preseedFactor;
    }

    public void setRoughnessScale(double r) {
        if (this.roughness == r)
            return;
        this.roughness = r;
        invalidate();
    }

    public double getRoughnessScale() {
        return this.roughness;
    }

    public void setRoughnessBias(int bias) {
        if (this.roughnessBias == bias)
            return;
        this.roughnessBias = bias;
        invalidate();
    }

    public int getRoughnessBias() {
        return this.roughnessBias;
    }

    protected ElevationData generateElevations(ElevationData result) {
        ElevationData source = getSourceData();
        int[][] sourceMap = source.getElevations();
        int mapSize = source.getMapSize();

        if (result == null) {
            result = new ElevationData(mapSize);
        }
        result.setElevationScale(getElevationScale());
        result.setElevationOffset(getElevationOffset());

        int sourceScale = source.getElevationScale();
        int sourceOffset = source.getElevationOffset();

        int scale = getElevationScale();
        int offset = getElevationOffset();

        int[][] map = result.getElevations();

        Random rand = new Random(getSeed());

        for (int y = 0; y <= mapSize; y++) {
            Arrays.fill(map[y], offset);
        }
        int heightRange = scale;
        int maxHeight = scale - offset;

        int seedMax = (int) Math.round(Math.log(mapSize) / Math.log(2.0D));
        int seedStep = seedMax - this.preseedFactor;
        seedStep = (int) Math.pow(2.0D, seedStep);

        for (int x = 0; x <= mapSize; x += seedStep) {
            for (int y = 0; y <= mapSize; y += seedStep) {
                if ((y != 0) && (x != 0) && (x != mapSize) && (y != mapSize)) {
                    if (sourceMap[y][x] != 0)
                        map[y][x] = sourceMap[y][x];
                    else
                        map[y][x] = (offset + rand.nextInt(scale));
                }
            }
        }
        double cos45 = Math.cos(Math.toRadians(45.0D));

        int step = seedStep;
        double displacement = scale / 2 * this.roughness;

        double count = Math.log(step) / Math.log(2.0D);

        count += this.roughnessBias;
        double displacementStep = Math.exp(Math.log(displacement) / count);

        boolean doElevFactor = false;

        while (step > 1) {
            int halfStep = step / 2;

            int squareDisp = (int) displacement;
            int halfSqDisp = squareDisp / 2;
            for (int x = 0; x < mapSize; x += step) {
                for (int y = 0; y < mapSize; y += step) {
                    int nw = map[y][x];
                    int ne = map[y][(x + step)];
                    int sw = map[(y + step)][x];
                    int se = map[(y + step)][(x + step)];

                    int average = (nw + ne + sw + se) / 4;
                    int d = squareDisp;

                    if (doElevFactor) {
                        int eFactor = maxHeight * 2 / 3 + average * 1 / 3;
                        d = d * eFactor / maxHeight;
                    }
                    int hd = d >> 1;

                    int rise = d > 0 ? rand.nextInt(d) - hd : 0;

                    map[(y + halfStep)][(x + halfStep)] = (average + rise);
                }

            }

            int diamondDisp = (int) Math.round(displacement * cos45);
            int halfDmdDisp = diamondDisp / 2;

            for (int x = 0; x < mapSize; x += step) {
                for (int y = 0; y < mapSize; y += step) {
                    if (y > 0) {
                        int previous = y - halfStep;
                        int n = map[previous][(x + halfStep)];
                        int s = map[(y + halfStep)][(x + halfStep)];
                        int e = map[y][(x + step)];
                        int w = map[y][x];

                        int average = (n + s + e + w) / 4;
                        int d = diamondDisp;

                        if (doElevFactor) {
                            int eFactor = maxHeight * 2 / 3 + average * 1 / 3;
                            d = d * eFactor / maxHeight;
                        }
                        int hd = d >> 1;

                        int rise = d > 0 ? rand.nextInt(d) - hd : 0;

                        map[y][(x + halfStep)] = (average + rise);
                    } else {
                        int s = map[(y + halfStep)][(x + halfStep)];
                        int e = map[y][(x + step)];
                        int w = map[y][x];

                        int average = (s + e + w) / 3;
                        int d = diamondDisp / 3;

                        int rise = d > 0 ? rand.nextInt(d) - halfDmdDisp : 0;
                        if (average + rise > 0)
                            rise = 0;
                        if (average > 0)
                            average = 0;
                        map[y][(x + halfStep)] = (average + rise);
                    }

                    if (x > 0) {
                        int previous = x - halfStep;
                        int n = map[y][x];
                        int s = map[(y + step)][x];
                        int e = map[(y + halfStep)][(x + halfStep)];
                        int w = map[(y + halfStep)][previous];

                        int average = (n + s + e + w) / 4;

                        int d = diamondDisp;

                        if (doElevFactor) {
                            int eFactor = maxHeight * 2 / 3 + average * 1 / 3;
                            d = d * eFactor / maxHeight;
                        }

                        int hd = d >> 1;

                        int rise = d > 0 ? rand.nextInt(d) - hd : 0;

                        map[(y + halfStep)][x] = (average + rise);
                    } else {
                        int n = map[y][x];
                        int s = map[(y + step)][x];
                        int e = map[(y + halfStep)][(x + halfStep)];

                        int average = (n + s + e) / 3;
                        int d = diamondDisp / 3;

                        int rise = d > 0 ? rand.nextInt(d) - halfDmdDisp : 0;
                        if (average + rise > 0)
                            rise = 0;
                        if (average > 0)
                            average = 0;
                        map[(y + halfStep)][x] = (average + rise);
                    }
                }

            }

            step = halfStep;
            displacement /= displacementStep;
        }

        return result;
    }
}