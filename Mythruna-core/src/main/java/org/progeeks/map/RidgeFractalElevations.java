package org.progeeks.map;

public class RidgeFractalElevations extends AbstractNoiseElevationGenerator {

    public RidgeFractalElevations() {
        super("Ridge Fractal");
        heightScale = 15000;
        fractalIncrement = 0.5D;
        lacunarity = 3.02D;
        octaves = 6;
        offset = 0.85999999999999999D;
        gain = 2D;
        threshold = 1.5D;
        maxOctaves = 128;
        exponents = new double[maxOctaves];
        setFrequency(512);
    }

    protected void reinitialize() {
        for (int i = 0; i < maxOctaves; i++)
            exponents[i] = Math.pow(lacunarity, (double) (-i) * fractalIncrement);

    }

    public void setFractalIncrement(double d) {
        if (fractalIncrement == d) {
            return;
        } else {
            fractalIncrement = d;
            invalidate();
            return;
        }
    }

    public double getFractalIncrement() {
        return fractalIncrement;
    }

    public void setLacunarity(double d) {
        if (lacunarity == d) {
            return;
        } else {
            lacunarity = d;
            invalidate();
            return;
        }
    }

    public double getLacunarity() {
        return lacunarity;
    }

    public void setOctaves(int d) {
        if (octaves == d) {
            return;
        } else {
            octaves = d;
            invalidate();
            return;
        }
    }

    public int getOctaves() {
        return octaves;
    }

    public void setOffset(double d) {
        if (offset == d) {
            return;
        } else {
            offset = d;
            invalidate();
            return;
        }
    }

    public double getOffset() {
        return offset;
    }

    public void setThreshold(double d) {
        if (threshold == d) {
            return;
        } else {
            threshold = d;
            invalidate();
            return;
        }
    }

    public double getThreshold() {
        return threshold;
    }

    public void setGain(double d) {
        if (gain == d) {
            return;
        } else {
            gain = d;
            invalidate();
            return;
        }
    }

    public double getGain() {
        return gain;
    }

    protected double getElevation(double x, double y, double z) {
        double signal = noise.getNoise(x, y, z);
        if (signal < 0.0D)
            signal = -signal;
        signal = offset - signal;
        signal *= signal;
        double result = signal;
        double weight = 1.0D;
        for (int i = 1; i < octaves; i++) {
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            weight = signal * threshold;
            if (weight > 1.0D)
                weight = 1.0D;
            if (weight < 0.0D)
                weight = 0.0D;
            signal = noise.getNoise(x, y, z);
            if (signal < 0.0D)
                signal = -signal;
            signal = offset - signal;
            signal *= signal;
            signal *= weight;
            result += signal * exponents[i];
        }

        return result;
    }

    private int heightScale;
    private double fractalIncrement;
    private double lacunarity;
    private int octaves;
    private double offset;
    private double gain;
    private double threshold;
    private int maxOctaves;
    private double exponents[];
}
