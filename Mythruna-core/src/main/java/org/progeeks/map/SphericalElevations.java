package org.progeeks.map;

public class SphericalElevations extends AbstractNoiseElevationGenerator {

    public SphericalElevations() {
        super("Spherical");
        setFrequency(1024);
    }

    protected double getElevation(double x, double y, double z) {
        x *= 3.1415926535897931D;
        y *= 3.1415926535897931D;
        return Math.sin(y) * Math.sin(x);
    }
}
