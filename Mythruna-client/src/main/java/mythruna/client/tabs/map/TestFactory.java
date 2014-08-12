package mythruna.client.tabs.map;

import mythruna.Coordinates;
import org.progeeks.map.*;

public class TestFactory {
    private int maxElevation = 128;
    private int seaLevel = 57;
    private int min = -15000;
    private int max = 18556;
    private ElevationGenerator[] generators;
    private ElevationData baseData = new ElevationData(2048);
    private ElevationData top;

    public TestFactory(ElevationGenerator[] generators) {
        this.generators = generators;
    }

    public TestFactory() {
        int baseFrequency = 256;
        int sphereFrequency = baseFrequency * 4;
        int ridgeScale = 2500;

        ridgeScale = 3500;
        baseFrequency = 512;
        sphereFrequency = 4192;

        SphericalElevations elev1 = new SphericalElevations();
        elev1.setFrequency(sphereFrequency);
        elev1.setElevationOffset(-1500);
        elev1.setElevationScale(5000);

        FractalSumElevations elev2 = new FractalSumElevations();
        elev2.setAdditive(true);
        elev2.setAffected(false);
        elev2.setFrequency(baseFrequency);
        elev2.setElevationOffset(-1500);
        elev2.setElevationScale(15000);
        elev2.setFrequencyIterations(8);
        elev2.setStartingFrequency(1.0D);

        RidgeFractalElevations elev3 = new RidgeFractalElevations();
        elev3.setAdditive(true);
        elev3.setAffected(true);
        elev3.setFrequency(baseFrequency * 2);
        elev3.setElevationOffset(-ridgeScale);
        elev3.setElevationScale(ridgeScale * 2);
        elev3.setFractalIncrement(0.5D);
        elev3.setGain(2.0D);
        elev3.setLacunarity(3.02D);
        elev3.setOctaves(5);
        elev3.setOffset(0.86D);
        elev3.setThreshold(1.5D);

        this.generators = new ElevationGenerator[]{elev1, elev2, elev3};
    }

    protected void invalidateGenerators() {
        this.top = this.baseData;
        for (ElevationGenerator g : this.generators) {
            g.setSourceData(this.top);
            g.invalidate();
            this.top = g.getGeneratedData();
        }
    }

    public int[][] createElevations(int x, int y) {
        long start = System.nanoTime();
        int x2 = Coordinates.worldToLeaf(x);
        int y2 = Coordinates.worldToLeaf(y);

        int xBase = Coordinates.leafToWorld(x2);
        int yBase = Coordinates.leafToWorld(y2);

        this.baseData.setElevationOffset(0);
        this.baseData.setElevationScale(1);
        this.baseData.setBaseX(yBase);
        this.baseData.setBaseY(xBase);

        invalidateGenerators();

        int width = 2048;

        int[][] elevations = this.top.getElevations();

        int denom = (this.max - this.min) / this.maxElevation;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                int h = elevations[i][j];
                h -= this.min;
                h /= denom;
                elevations[i][j] = h;
            }

        }

        return elevations;
    }
}