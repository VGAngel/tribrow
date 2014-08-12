package org.progeeks.map;

public class PerlinElevations extends AbstractNoiseElevationGenerator {

    public PerlinElevations() {
        super("Perlin Noise");
        setFrequency(256);
        setElevationOffset(7500);
        setElevationScale(15000);
    }

    protected double getElevation(double x, double y, double z) {
        return noise.getNoise(x, y, z);
    }
}
