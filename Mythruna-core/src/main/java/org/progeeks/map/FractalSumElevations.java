package org.progeeks.map;

public class FractalSumElevations extends AbstractNoiseElevationGenerator {

    public FractalSumElevations() {
        super("Fractal Sum");
        minFreq = 1.0D;
        frequencyIterations = 5;
    }

    public void setStartingFrequency(double d) {
        if (minFreq == d) {
            return;
        } else {
            minFreq = d;
            invalidate();
            return;
        }
    }

    public double getStartingFrequency() {
        return minFreq;
    }

    public void setFrequencyIterations(int i) {
        if (frequencyIterations == i) {
            return;
        } else {
            frequencyIterations = i;
            invalidate();
            return;
        }
    }

    public int getFrequencyIterations() {
        return frequencyIterations;
    }

    protected double getElevation(double x, double y, double z) {
        double value = 0.0D;
        double f = minFreq;
        for (int i = 0; i < frequencyIterations; i++) {
            double sample = noise.getNoise(x * f, y * f, z * f) / f;
            value += sample;
            f *= 2D;
        }

        return value;
    }

    private double minFreq;
    private int frequencyIterations;
}
