package org.progeeks.map;

public class MultiFractalElevations extends AbstractNoiseElevationGenerator {

    public MultiFractalElevations() {
        super("Multi-Fractal");
        heightScale = 15000;
        fractalIncrement = 0.5D;
        lacunarity = 3.02D;
        octaves = 6;
        offset = 0.31D;
        gain = 4D;
        maxOctaves = 128;
        exponents = new double[maxOctaves];
        setFrequency(512);
        setElevationOffset(0);
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
        double value = noise.getNoise(x, y, z) + offset;
        double weight = gain * value;
        x *= lacunarity;
        y *= lacunarity;
        z *= lacunarity;
        int i;
        for (i = 1; weight > 0.001D && i < octaves; i++) {
            if (weight > 1.0D)
                weight = 1.0D;
            double signal = (noise.getNoise(x, y, z) + offset) * exponents[i];
            value += weight * signal;
            weight *= gain * signal;
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
        }

        double remainder = octaves - octaves;
        if (remainder != 0.0D)
            value += remainder * noise.getNoise(x, y, z) * exponents[i];
        return value;
    }

    private int heightScale;
    private double fractalIncrement;
    private double lacunarity;
    private int octaves;
    private double offset;
    private double gain;
    private int maxOctaves;
    private double exponents[];
}
