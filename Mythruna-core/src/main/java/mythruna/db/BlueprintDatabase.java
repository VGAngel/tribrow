package mythruna.db;

import java.io.IOException;
import java.util.List;

public abstract interface BlueprintDatabase {
    public abstract List<Long> getIds()
            throws IOException;

    public abstract BlueprintData getBlueprint(long paramLong)
            throws IOException;

    public abstract BlueprintData getBlueprint(long paramLong, boolean paramBoolean)
            throws IOException;

    public abstract BlueprintData createBlueprint(String paramString, int paramInt1, int paramInt2, int paramInt3, float paramFloat, int[][][] paramArrayOfInt)
            throws IOException;

    public abstract void close();
}