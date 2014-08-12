package org.progeeks.map;

import java.util.Arrays;
import java.util.Random;

public class AdaptiveDiamondSquaresElevations extends AbstractElevationGenerator {

    public AdaptiveDiamondSquaresElevations() {
        super("Adaptive Diamond/Squares");
        roughness = 3D;
        roughnessBias = 5;
        preseedFactor = 2;
        attenuation = 1.0D;
        curveFactor = 1;
        setElevationScale(16500);
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

    public void setAttenuation(double d) {
        if (attenuation == d) {
            return;
        } else {
            attenuation = d;
            invalidate();
            return;
        }
    }

    public double getAttenuation() {
        return attenuation;
    }

    public void setCurveFactor(int i) {
        if (curveFactor == i) {
            return;
        } else {
            curveFactor = i;
            invalidate();
            return;
        }
    }

    public int getCurveFactor() {
        return curveFactor;
    }

    private int getMax(int a, int b, int c, int d) {
        int max = a;
        if (max < b)
            max = b;
        if (max < c)
            max = c;
        if (max < d)
            max = d;
        return max;
    }

    private int getMin(int a, int b, int c, int d) {
        int min = a;
        if (min > b)
            min = b;
        if (min > c)
            min = c;
        if (min > d)
            min = d;
        return min;
    }

    private double getRoughnessScalar(int min, int max, int step, int displacement, int rise) {
        double elevationFactor = (double) (max - getElevationOffset()) / (double) getElevationScale();
        if (elevationFactor < 0.01D)
            elevationFactor = 0.01D;
        elevationFactor = Math.pow(elevationFactor, curveFactor);
        elevationFactor *= attenuation;
        double roughFactor = (double) (max - min) / (double) displacement;
        elevationFactor = Math.pow(elevationFactor, curveFactor);
        roughFactor *= attenuation;
        double stepRatio = 256D / (double) step;
        double stepFactor = 1.0D - (double) step / 256D;
        double result = Math.min(1.0D, elevationFactor * roughFactor + stepFactor);
        if (step == 128 || step == 64) {
            System.out.println((new StringBuilder()).append("min:").append(min).append(" max:").append(max).append(" step:").append(step).append(" disp:").append(displacement).append(" rise:").append(rise).toString());
            System.out.println((new StringBuilder()).append("  e factor:").append(elevationFactor).toString());
            System.out.println((new StringBuilder()).append("  r factor:").append(roughFactor).toString());
            System.out.println((new StringBuilder()).append("  s ratio:").append(stepRatio).toString());
            System.out.println((new StringBuilder()).append("  s factor:").append(stepFactor).toString());
            System.out.println((new StringBuilder()).append("    :").append(elevationFactor * roughFactor).toString());
            System.out.println((new StringBuilder()).append("    :").append(elevationFactor * roughFactor * stepFactor).toString());
            System.out.println((new StringBuilder()).append("    :").append(result).toString());
        }
        return result;
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
        int maxHeight = scale - offset;
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
        while (step > 1) {
            int halfStep = step / 2;
            int squareDisp = (int) displacement;
            int halfSqDisp = squareDisp / 2;
            for (int x = 0; x < mapSize; x += step) {
                for (int y = 0; y < mapSize; y += step) {
                    int nw = map[y][x];
                    int ne = map[y][x + step];
                    int sw = map[y + step][x];
                    int se = map[y + step][x + step];
                    int max = getMax(nw, ne, sw, se);
                    int min = getMin(nw, ne, sw, se);
                    int average = (nw + ne + sw + se) / 4;
                    int rise = squareDisp <= 0 ? 0 : rand.nextInt(squareDisp) - halfSqDisp;
                    double scalar = getRoughnessScalar(min, max, step, squareDisp, rise);
                    rise = (int) Math.round((double) rise * scalar);
                    map[y + halfStep][x + halfStep] = average + rise;
                }

            }

            int diamondDisp = (int) Math.round(displacement * cos45);
            int halfDmdDisp = diamondDisp / 2;
            for (int x = 0; x < mapSize; x += step) {
                for (int y = 0; y < mapSize; y += step) {
                    int d = diamondDisp;
                    int hd = halfDmdDisp;
                    int n;
                    int s;
                    int e;
                    int w;
                    int sampleCount;
                    int max;
                    int min;
                    if (y > 0) {
                        int previous = y - halfStep;
                        n = map[previous][x + halfStep];
                        s = map[y + halfStep][x + halfStep];
                        e = map[y][x + step];
                        w = map[y][x];
                        sampleCount = 4;
                        max = getMax(n, s, e, w);
                        min = getMin(n, s, e, w);
                    } else {
                        n = 0;
                        s = map[y + halfStep][x + halfStep];
                        e = map[y][x + step];
                        w = map[y][x];
                        sampleCount = 3;
                        max = getMax(s, s, e, w);
                        min = getMin(s, s, e, w);
                        d /= 3;
                    }
                    int average = (n + s + e + w) / sampleCount;
                    int rise = d <= 0 ? 0 : rand.nextInt(d) - hd;
                    double scalar = getRoughnessScalar(min, max, step, d, rise);
                    rise = (int) Math.round((double) rise * scalar);
                    if (y == 0) {
                        if (average + rise > 0)
                            rise = 0;
                        if (average > 0)
                            average = 0;
                    }
                    map[y][x + halfStep] = average + rise;
                    d = diamondDisp;
                    hd = halfDmdDisp;
                    if (x > 0) {
                        int previous = x - halfStep;
                        n = map[y][x];
                        s = map[y + step][x];
                        e = map[y + halfStep][x + halfStep];
                        w = map[y + halfStep][previous];
                        sampleCount = 4;
                        max = getMax(n, s, e, w);
                        min = getMin(n, s, e, w);
                    } else {
                        n = map[y][x];
                        s = map[y + step][x];
                        e = map[y + halfStep][x + halfStep];
                        w = 0;
                        sampleCount = 3;
                        max = getMax(n, s, e, e);
                        min = getMin(n, s, e, e);
                        d /= 3;
                    }
                    average = (n + s + e + w) / sampleCount;
                    rise = d <= 0 ? 0 : rand.nextInt(d) - hd;
                    scalar = getRoughnessScalar(min, max, step, d, rise);
                    rise = (int) Math.round((double) rise * scalar);
                    if (x == 0) {
                        if (average + rise > 0)
                            rise = 0;
                        if (average > 0)
                            average = 0;
                    }
                    map[y + halfStep][x] = average + rise;
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
    private double attenuation;
    private int curveFactor;
}
