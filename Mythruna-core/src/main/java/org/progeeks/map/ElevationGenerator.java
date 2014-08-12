package org.progeeks.map;

public abstract interface ElevationGenerator {

    public abstract void invalidate();

    public abstract void setEnabled(boolean paramBoolean);

    public abstract boolean isEnabled();

    public abstract String getName();

    public abstract boolean acceptsSourceData();

    public abstract void setSourceData(ElevationData paramElevationData);

    public abstract ElevationData getSourceData();

    public abstract ElevationData getGeneratedData();

    public abstract void setSeed(int paramInt);
}