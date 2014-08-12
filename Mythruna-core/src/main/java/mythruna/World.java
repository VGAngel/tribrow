package mythruna;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import mythruna.db.BlueprintData;
import mythruna.db.CellAccess;
import mythruna.db.LeafData;
import mythruna.db.WorldDatabase;
import mythruna.es.EntityData;

import java.util.List;

public interface World extends CellAccess {

    public abstract void setCellAccess(CellAccess cellaccess);

    public abstract void close();

    public abstract EntityData getEntityData();

    public abstract List getBlueprintIds();

    public abstract BlueprintData getBlueprint(long l);

    public abstract BlueprintData getBlueprint(long l, boolean flag);

    public abstract BlueprintData createBlueprint(String s, int i, int j, int k, float f, int ai[][][]);

    public abstract WorldDatabase getWorldDatabase();

    public abstract void setDefaultSpawnLocation(Vector3f vector3f);

    public abstract Vector3f getDefaultSpawnLocation();

    public abstract void setDefaultSpawnDirection(Quaternion quaternion);

    public abstract Quaternion getDefaultSpawnDirection();

    public abstract int findEmptySpace(float f, float f1, float f2, int i, LeafData leafdata);

    public abstract int findEmptySpace(int i, int j, int k, int l, LeafData leafdata);

    public abstract int getType(int i, int j, int k, LeafData leafdata);

    public abstract int getType(float f, float f1, float f2, LeafData leafdata);

    public abstract int getSunlight(int i, int j, int k, LeafData leafdata);

    public abstract int getLocalLight(int i, int j, int k, LeafData leafdata);

    public abstract int getType(int i, int j, int k, int l, LeafData leafdata);
}
