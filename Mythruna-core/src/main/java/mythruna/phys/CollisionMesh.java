package mythruna.phys;

import mythruna.BlockType;
import mythruna.db.BlueprintData;
import mythruna.es.EntityId;
import mythruna.mathd.Quatd;
import mythruna.mathd.Vec3d;

public class CollisionMesh {

    private EntityId id;
    private BlueprintData mesh;
    private double maxRadius;
    private double volume;
    private boolean isStatic;
    private int size;
    private byte[][][] clipMasks;
    public Vec3d cog = new Vec3d();
    public Vec3d boundsCenter = new Vec3d();

    public Vec3d position = new Vec3d();
    public Quatd orientation = new Quatd();

    public CollisionMesh(EntityId id, BlueprintData mesh) {
        this.id = id;
        setMesh(mesh);
    }

    public CollisionMesh(EntityId id, BlueprintData mesh, Vec3d cog) {
        this.id = id;
        setMesh(mesh);
        setCog(cog);
    }

    public EntityId getId() {
        return this.id;
    }

    public void setIsStatic(boolean f) {
        this.isStatic = f;
    }

    public boolean isStatic() {
        return this.isStatic;
    }

    public void setCog(Vec3d cog) {
        this.cog.set(cog);

        int xSize = this.mesh.xSize;
        int ySize = this.mesh.ySize;
        int zSize = this.mesh.zSize;

        double x = xSize * 0.5D * this.mesh.scale;
        double y = ySize * 0.5D * this.mesh.scale;
        double z = zSize * 0.5D * this.mesh.scale;

        this.boundsCenter.x = (x - cog.x);
        this.boundsCenter.y = (z - cog.y);
        this.boundsCenter.z = (y - cog.z);
    }

    public Vec3d getCenterBase() {
        double x = this.mesh.xSize * 0.5D;
        double y = this.mesh.ySize * 0.5D;

        return new Vec3d(x * this.mesh.scale, 0.0D, y * this.mesh.scale);
    }

    public void setBasePosition(double x, double y, double z) {
        this.position.set(x, y, z);

        double xOffset = this.mesh.xSize * 0.5D * this.mesh.scale;
        double zOffset = this.mesh.ySize * 0.5D * this.mesh.scale;
        Vec3d offset = new Vec3d(-xOffset, 0.0D, -zOffset);
        offset.addLocal(this.cog);

        offset = this.orientation.mult(offset, offset);

        this.position.addLocal(offset);
    }

    public Vec3d getBasePosition() {
        double x = this.mesh.xSize * 0.5D * this.mesh.scale;
        double y = this.mesh.ySize * 0.5D * this.mesh.scale;
        Vec3d result = this.cog.mult(-1.0D).addLocal(x, 0.0D, y);

        result = this.orientation.mult(result, result);

        result.addLocal(this.position);
        return result;
    }

    public void setMesh(BlueprintData bp) {
        this.mesh = bp;

        int xSize = bp.xSize;
        int ySize = bp.ySize;
        int zSize = bp.zSize;
        int[][][] cells = bp.cells;

        double x = xSize * 0.5D;
        double y = ySize * 0.5D;
        double z = zSize * 0.5D;

        this.cog.x = (x * this.mesh.scale);
        this.cog.y = 0.0D;
        this.cog.z = (y * this.mesh.scale);

        this.boundsCenter.y = (z * this.mesh.scale);

        double dist = x * x + y * y + z * z;
        this.maxRadius = (Math.sqrt(dist) * bp.scale);

        this.volume = bp.scale * xSize * (bp.scale * ySize) * (bp.scale * zSize);
        this.size = (xSize * ySize * zSize);

        this.clipMasks = new byte[xSize][ySize][zSize];
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                for (int k = 0; k < zSize; k++) {
                    int t = cells[i][j][k];
                    if (t != 0) {
                        BlockType type = mythruna.BlockTypeIndex.types[t];

                        if ((type != null) && (type.getGeomFactory().isClipped())) {
                            int mask = 0;

                            int bit = 1;
                            for (int d = 0; d < 6; ) {
                                int xa = i + mythruna.Direction.DIRS[d][0];
                                int ya = j + mythruna.Direction.DIRS[d][1];
                                int za = k + mythruna.Direction.DIRS[d][2];

                                if ((xa < 0) || (ya < 0) || (za < 0)) {
                                    mask |= bit;
                                } else if ((xa >= xSize) || (ya >= ySize) || (za >= zSize)) {
                                    mask |= bit;
                                } else {
                                    int a = cells[xa][ya][za];
                                    if (a == 0) {
                                        mask |= bit;
                                    } else {
                                        BlockType adjType = mythruna.BlockTypeIndex.types[a];
                                        if ((adjType == null) || (!adjType.getGeomFactory().isClipped())) {
                                            mask |= bit;
                                        } else if (type.getGroup() != adjType.getGroup()) {
                                            mask |= bit;
                                        } else {
                                            int back = mythruna.Direction.INVERSE[d];

                                            if (!type.getBoundary(d).isMatchingFace(adjType.getBoundary(back))) {
                                                mask |= bit;
                                            }
                                        }
                                    }
                                }
                                d++;
                                bit <<= 1;
                            }

                            this.clipMasks[i][j][k] = (byte) mask;
                        }
                    }
                }
            }
        }
    }

    public BlueprintData getMesh() {
        return this.mesh;
    }

    public byte[][][] getClipMasks() {
        return this.clipMasks;
    }

    public double getMaxRadius() {
        return this.maxRadius;
    }

    public double getVolume() {
        return this.volume;
    }

    public int getSize() {
        return this.size;
    }

    public String toString() {
        return "CollisionMesh[" + this.id + ", " + this.position + ", " + this.orientation + "]";
    }
}