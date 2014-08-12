package mythruna.phys;

import mythruna.BlockType;
import mythruna.Vector3i;
import mythruna.World;
import mythruna.db.LeafData;
import mythruna.db.LeafInfo;

public class WorldLeaf {
    private World world;
    private LeafData leaf;
    private long version;
    private int[][][] cells;
    private byte[][][] masks;
    private Vector3i baseLoc;

    public WorldLeaf(World world, LeafData leaf) {
        this.world = world;

        LeafInfo info = leaf.getInfo();
        this.baseLoc = new Vector3i(info.x, info.y, info.z);
        update(leaf);
    }

    public void update(LeafData leaf) {
        this.leaf = leaf;
        update();
    }

    public final int getType(int x, int y, int z) {
        return LeafData.toType(this.cells[x][y][z]);
    }

    public final int getMask(int x, int y, int z) {
        return this.masks[x][y][z];
    }

    public final int getWorldType(int x, int y, int z) {
        x -= this.baseLoc.x;
        y -= this.baseLoc.y;
        z -= this.baseLoc.z;
        return LeafData.toType(this.cells[x][y][z]);
    }

    public final byte getWorldMask(int x, int y, int z) {
        x -= this.baseLoc.x;
        y -= this.baseLoc.y;
        z -= this.baseLoc.z;
        return this.masks[x][y][z];
    }

    private final int get(int x, int y, int z) {
        return this.world.getType(this.baseLoc.x + x, this.baseLoc.y + y, this.baseLoc.z + z, null);
    }

    protected void update() {
        this.version = this.leaf.getInfo().version;
        this.cells = this.leaf.getCells();

        int size = 32;
        this.masks = new byte[size][size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    int t = LeafData.toType(this.cells[i][j][k]);
                    if (t != 0) {
                        BlockType type = mythruna.BlockTypeIndex.types[t];
                        if ((type != null) && (type.getGeomFactory().isClipped())) {
                            int mask = 0;

                            int bit = 1;
                            for (int d = 0; d < 6; ) {
                                int xa = i + mythruna.Direction.DIRS[d][0];
                                int ya = j + mythruna.Direction.DIRS[d][1];
                                int za = k + mythruna.Direction.DIRS[d][2];
                                int a;
                                if ((xa < 0) || (ya < 0) || (za < 0)) {
                                    a = get(xa, ya, za);
                                } else {
                                    if ((xa >= size) || (ya >= size) || (za >= size)) {
                                        a = get(xa, ya, za);
                                    } else {
                                        a = LeafData.toType(this.cells[xa][ya][za]);
                                    }
                                }
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
                                d++;
                                bit <<= 1;
                            }

                            this.masks[i][j][k] = (byte) mask;
                        }
                    }
                }
            }
        }
    }
}