package org.progeeks.map;

public abstract class AbstractElevationGenerator implements ElevationGenerator {

    protected AbstractElevationGenerator(String name) {
        valid = false;
        enabled = true;
        elevationOffset = -1500;
        elevationScale = 15000;
        this.name = name;
    }

    public void invalidate() {
        valid = false;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public boolean acceptsSourceData() {
        return true;
    }

    public void setSourceData(ElevationData data) {
        if (sourceData == data) {
            return;
        } else {
            sourceData = data;
            invalidate();
            return;
        }
    }

    public void setElevationScale(int scale) {
        if (elevationScale == scale) {
            return;
        } else {
            elevationScale = scale;
            invalidate();
            return;
        }
    }

    public int getElevationScale() {
        return elevationScale;
    }

    public void setElevationOffset(int offset) {
        if (elevationOffset == offset) {
            return;
        } else {
            elevationOffset = offset;
            invalidate();
            return;
        }
    }

    public int getElevationOffset() {
        return elevationOffset;
    }

    protected abstract ElevationData generateElevations(ElevationData elevationdata);

    public ElevationData getSourceData() {
        return sourceData;
    }

    public ElevationData getGeneratedData() {
        if (!enabled)
            return getSourceData();
        if (!valid || result == null) {
            result = generateElevations(result);
            valid = true;
        }
        return result;
    }

    private String name;
    private boolean valid;
    private boolean enabled;
    private ElevationData sourceData;
    private ElevationData result;
    private int elevationOffset;
    private int elevationScale;
}
