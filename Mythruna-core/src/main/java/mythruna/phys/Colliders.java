package mythruna.phys;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.BlockTypeIndex;
import mythruna.Direction;
import mythruna.geom.*;
import mythruna.mathd.Vec3d;
import mythruna.phys.collision.AngleCollider;
import mythruna.phys.collision.CubeCollider;
import mythruna.phys.collision.CylinderCollider;
import mythruna.phys.collision.WedgeCollider;

public class Colliders {

    public static byte[] inverseMask = new byte[64];

    public static Collider UNIT_CUBE = new CubeCollider();
    public static Collider[] colliders;

    public Colliders() {
    }

    public static void initialize() {
        if (colliders == null) {
            if (!BlockTypeIndex.isInitialized()) {
                throw new RuntimeException("Block types have not been initialized.");
            }
            colliders = new Collider[BlockTypeIndex.types.length];
            for (int i = 0; i < colliders.length; i++) {
                BlockType type = BlockTypeIndex.types[i];
                if (type != null) {
                    GeomFactory geom = type.getGeomFactory();
                    if ((geom.isClipped()) && (i != 7) && (i != 8)) {
                        if ((geom instanceof WedgeFactory)) {
                            WedgeFactory wf = (WedgeFactory) geom;
                            colliders[i] = new WedgeCollider(wf.getSolidDir(), wf.isFacingUp());
                        } else if ((geom instanceof ThatchFactory)) {
                            ThatchFactory f = (ThatchFactory) geom;
                            colliders[i] = new WedgeCollider(Direction.INVERSE[f.getDirection()], true);
                        } else if ((geom instanceof AngleFactory)) {
                            AngleFactory af = (AngleFactory) geom;
                            colliders[i] = new AngleCollider(af.getDirection());
                        } else if ((geom instanceof CylinderFactory)) {
                            CylinderFactory f = (CylinderFactory) geom;
                            colliders[i] = new CylinderCollider(f.getRadius());
                        } else if ((geom instanceof CylinderFactory2)) {
                            CylinderFactory2 f = (CylinderFactory2) geom;
                            if (f.getDir() < 0) {
                                float radius = f.getRadius();
                                float length = f.getHeight();

                                radius *= 0.8F;

                                Vector3f v1 = new Vector3f(0.0F, length, 0.0F);
                                Vector3f v2 = new Vector3f(radius, 0.0F, 0.0F);
                                Vector3f v3 = new Vector3f(0.0F, 0.0F, radius);

                                Quaternion q = f.getRotation();
                                v1 = q.mult(v1);
                                v2 = q.mult(v2);
                                v3 = q.mult(v3);

                                Vector3f min = new Vector3f(0.0F, 0.0F, 0.0F);
                                Vector3f max = new Vector3f(0.0F, 0.0F, 0.0F);
                                min.minLocal(v1);
                                min.minLocal(v2);
                                min.minLocal(v3);
                                min.minLocal(v1.negate());
                                min.minLocal(v2.negate());
                                min.minLocal(v3.negate());
                                max.maxLocal(v1);
                                max.maxLocal(v2);
                                max.maxLocal(v3);
                                max.maxLocal(v1.negate());
                                max.maxLocal(v2.negate());
                                max.maxLocal(v3.negate());

                                min.addLocal(0.5F, 0.5F, 0.5F);
                                max.addLocal(0.5F, 0.5F, 0.5F);
                                min.addLocal(f.getOffset());
                                max.addLocal(f.getOffset());

                                if (Math.abs(min.x) < 0.001D)
                                    min.x = 0.0F;
                                if (Math.abs(min.y) < 0.001D)
                                    min.y = 0.0F;
                                if (Math.abs(min.z) < 0.001D)
                                    min.z = 0.0F;
                                if (Math.abs(max.x) < 0.001D)
                                    max.x = 0.0F;
                                if (Math.abs(max.y) < 0.001D)
                                    max.y = 0.0F;
                                if (Math.abs(max.z) < 0.001D) {
                                    max.z = 0.0F;
                                }

                                Vec3d low = new Vec3d(min.x, min.y, min.z);
                                Vec3d hi = new Vec3d(max.x, max.y, max.z);

                                colliders[i] = new CubeCollider(low, hi);
                            } else {
                                int dir = f.getDir();
                                float radius = f.getRadius();
                                double length = Math.sqrt(2.0D);
                                Vector3f offset = f.getOffset();
                                double xOffset = offset.x > 0.0F ? offset.x + 1.0F : offset.x == 0.0F ? 0.5D : offset.x;
                                double yOffset = -0.25D;
                                double zOffset = offset.z > 0.0F ? offset.z + 1.0F : offset.z == 0.0F ? 0.5D : offset.z;

                                yOffset -= 0.1D;
                                length += 0.25D;

                                colliders[i] = new CylinderCollider(dir, radius, length, xOffset, yOffset, zOffset);
                            }
                        } else if ((geom instanceof DefaultGeomFactory)) {
                            DefaultGeomFactory f = (DefaultGeomFactory) geom;
                            if (f.getCollider() != null)
                                colliders[i] = f.getCollider();
                            else
                                colliders[i] = new CubeCollider(geom.getMin(), geom.getMax());
                        } else {
                            colliders[i] = new CubeCollider(geom.getMin(), geom.getMax());
                        }
                    }
                }
            }
        }
    }

    static {
        for (int i = 0; i < 64; i++) {
            int mask = 0;
            if (Direction.hasNorth(i))
                mask |= 2;
            if (Direction.hasSouth(i))
                mask |= 1;
            if (Direction.hasEast(i))
                mask |= 8;
            if (Direction.hasWest(i))
                mask |= 4;
            if (Direction.hasUp(i))
                mask |= 32;
            if (Direction.hasDown(i)) {
                mask |= 16;
            }
            inverseMask[i] = (byte) mask;
        }
    }
}