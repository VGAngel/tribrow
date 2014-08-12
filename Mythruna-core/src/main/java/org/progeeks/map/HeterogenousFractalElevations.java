package org.progeeks.map;

public class HeterogenousFractalElevations extends AbstractNoiseElevationGenerator {

    public HeterogenousFractalElevations() {
        super("Heterogenous Fractal");
        fractalIncrement = 0.75D;
        lacunarity = 3.0899999999999999D;
        octaves = 4;
        offset = 0.5D;
        maxOctaves = 128;
        exponents = new double[maxOctaves];
        setFrequency(128);
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

    protected double getElevation(double x, double y, double z) {
        double value = noise.getNoise(x, y) + offset;
        x *= lacunarity;
        y *= lacunarity;
        z *= lacunarity;
        int i;
        for (i = 1; i < octaves; i++) {
            double increment = noise.getNoise(x, y) + offset;
            increment *= exponents[i];
            increment *= value;
            value += increment;
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
        }

        double remainder = octaves - octaves;
        if (remainder != 0.0D) {
            double increment = noise.getNoise(x, y) + offset;
            increment *= exponents[i];
            value += remainder * increment * value;
        }
        return value;
    }

    private double fractalIncrement;
    private double lacunarity;
    private int octaves;
    private double offset;
    private int maxOctaves;
    private double exponents[];
}
