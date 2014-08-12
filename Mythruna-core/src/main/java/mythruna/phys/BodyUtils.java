package mythruna.phys;

import mythruna.BlockType;
import mythruna.db.BlueprintData;
import mythruna.es.EntityData;
import mythruna.es.EntityId;
import mythruna.mathd.Matrix3d;
import mythruna.mathd.Vec3d;

public class BodyUtils {

    public BodyUtils() {
    }

    public static void addMassProperties(EntityData ed, EntityId id, BlueprintData data) {
        int[][][] cells = data.cells;
        int xSize = data.xSize;
        int ySize = data.ySize;
        int zSize = data.zSize;

        double massScale = data.scale * data.scale * data.scale;
        System.out.println("************* massScale:" + massScale);
        double totalMass = 0.0D;
        double totalVolume = 0.0D;

        Vec3d center = new Vec3d();
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                for (int k = 0; k < zSize; k++) {
                    int t = cells[i][j][k];
                    if (t != 0) {
                        BlockType type = mythruna.BlockTypeIndex.types[t];
                        if (type != null) {
                            double m = type.getMaterial().getMass();

                            System.out.println("t:" + t + " @ " + i + ", " + j + ", " + k + "  mass/cu.m.:" + m);
                            System.out.println("  portion:" + type.getGeomFactory().getMassPortion());
                            System.out.println("  min:" + type.getGeomFactory().getMin() + "  max:" + type.getGeomFactory().getMax());

                            double vol = type.getGeomFactory().getMassPortion();
                            m = m * vol * massScale;

                            totalMass += m;
                            totalVolume += vol;

                            center.addLocal((i + 0.5D) * m, (k + 0.5D) * m, (j + 0.5D) * m);
                        }
                    }
                }
            }
        }
        System.out.println("Total mass:" + totalMass + "  Total volume:" + totalVolume);
        System.out.println("Center of mass before:" + center);
        center.multLocal(1.0D / totalMass);
        System.out.println("Center of mass:" + center);

        center.multLocal(data.scale);

        System.out.println("Center of mass:" + center);

        Vec3d halfExtents = new Vec3d(xSize, zSize, ySize);

        halfExtents.multLocal(0.5D * data.scale);

        double mass = totalMass;

        Matrix3d tensor = new Matrix3d();
        System.out.println("halfExtents:" + halfExtents);
        Vec3d squares = halfExtents.mult(halfExtents);

        tensor.m00 = (0.300000011920929D * mass * (squares.y + squares.z));
        tensor.m11 = (0.300000011920929D * mass * (squares.x + squares.z));
        tensor.m22 = (0.300000011920929D * mass * (squares.x + squares.y));

        System.out.println("Tensor:" + tensor);

        ed.setComponent(id, new Mass(totalMass));
        ed.setComponent(id, new MassProperties(center, tensor));
        ed.setComponent(id, new Volume(totalVolume, xSize * ySize * zSize));
    }
}