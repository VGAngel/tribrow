package mythruna.db.building;

import mythruna.*;
import mythruna.db.BlueprintData;
import mythruna.db.CellAccess;
import mythruna.geom.GeomFactory;

public class BuildingUtils {
    public BuildingUtils() {
    }

    public static boolean isGround(int t) {
        BlockType type = BlockTypeIndex.types[t];
        if (type == null)
            return false;
        MaterialType material = type.getMaterial();
        return (material == MaterialType.DIRT) || (material == MaterialType.SAND) || (material == MaterialType.STONE);
    }

    public static boolean isPlant(int t) {
        BlockType type = BlockTypeIndex.types[t];
        if (type == null)
            return false;
        MaterialType material = type.getMaterial();
        return (material == MaterialType.FLORA) || (material == MaterialType.LEAVES);
    }

    public static boolean isFlora(int t) {
        BlockType type = BlockTypeIndex.types[t];
        if (type == null)
            return false;
        MaterialType material = type.getMaterial();
        return material == MaterialType.FLORA;
    }

    public static void placeBuilding(Building b, Vector3i pos, World world) {
        Vector3i size = b.getSize();

        System.out.println("Building size:" + b.getSize());

        size = new Vector3i(16, 16, 20);

        ArrayCellAccess cellAccess = new ArrayCellAccess(size.x, size.y, size.z);
        b.place(new Vector3i(), cellAccess);

        int[][][] cells = cellAccess.cells;

        for (int i = 0; i < size.x; i++) {
            for (int j = 0; j < size.y; j++) {
                boolean empty = true;
                boolean isBottomOnly = false;
                int val;
                for (int k = 0; k < size.z; k++) {
                    val = cells[i][j][k];
                    if (val != 0) {
                        empty = false;
                        if (k == 0) {
                            isBottomOnly = val == 2;
                        } else if (k > 0) {
                            isBottomOnly = false;
                            break;
                        }
                    }
                }

                if (!empty) {
                    boolean skipBottom = false;

                    int bottom = cells[i][j][0];
                    int under = world.getCellType(pos.x + i, pos.y + j, pos.z);
                    if ((!isGround(under)) && (!isBottomOnly) && (bottom != 0)) {
                        BlockType bottomType = BlockTypeIndex.types[bottom];
                        if ((bottomType != null) && (bottomType.isSolid())) {
                            MaterialType bottomMaterial = bottomType.getMaterial();
                            bottomType = BlockTypeIndex.getDefaultType(bottomMaterial);

                            for (int z = pos.z; z > 0; z--) {
                                under = world.getCellType(pos.x + i, pos.y + j, z);

                                if (isGround(under))
                                    break;
                                world.setCellType(pos.x + i, pos.y + j, z, bottomType.getId());
                            }
                        }

                    } else if ((!isGround(under)) && (isBottomOnly) && (bottom != 0)) {
                        skipBottom = true;
                    }

                    for (int k = 0; k < 20; k++) {
                        int t = cells[i][j][k];
                        if ((k != 0) || (t == 0) || (!skipBottom)) {
                            if ((k == 0) && (t == 0)) {
                                int v = world.getCellType(pos.x + i, pos.y + j, pos.z + k + 1);
                                BlockType existing = BlockTypeIndex.types[v];
                                if ((existing == null) || ((existing.getMaterial() != MaterialType.WOOD) && (existing.getMaterial() != MaterialType.LEAVES)))
                                    ;
                            } else {
                                if ((t == 0) && (k == 1)) {
                                    under = cells[i][j][0];
                                    if (under == 0) {
                                        int v = world.getCellType(pos.x + i, pos.y + j, pos.z + k);
                                        if (v != 0) {
                                            BlockType existing = BlockTypeIndex.types[v];
                                            if ((existing != null) && (existing.getMaterial() == MaterialType.DIRT)) {
                                                world.setCellType(pos.x + i, pos.y + j, pos.z + k, 2);
                                            }

                                        }

                                    }

                                    if ((under == 0) || (under == 2)) {
                                        int v = world.getCellType(pos.x + i, pos.y + j, pos.z + k + 1);
                                        if (isFlora(v)) {
                                            continue;
                                        }
                                    }
                                }
                                if (t == 7) {
                                    t = 0;
                                }
                                world.setCellType(pos.x + i, pos.y + j, pos.z + k + 1, t);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void placePart(Vector3i pos, CellAccess cells, BlueprintData data, int rotation, int mirrorFlags, int maxHeight) {
        int cardinal = rotation + 1;
        if (cardinal >= 4)
            cardinal -= 4;
        int dir = mythruna.Direction.CARDINAL_TO_DIR[cardinal];

        int xDir = dir;
        int yDir = mythruna.Direction.LEFT[xDir];
        yDir = mythruna.Direction.INVERSE[yDir];
        int zDir = 4;

        System.out.println("placePart:" + pos + "  size:" + data.xSize + ", " + data.ySize + ", " + data.zSize);
        Vector3i xVec = mythruna.Direction.VECS[xDir];
        if ((mirrorFlags & BlockTransforms.MIRROR_X) != 0)
            xVec = xVec.mult(-1);
        Vector3i yVec = mythruna.Direction.VECS[yDir];
        if ((mirrorFlags & BlockTransforms.MIRROR_Y) != 0)
            yVec = yVec.mult(-1);
        Vector3i zVec = mythruna.Direction.VECS[zDir];

        System.out.println("xVec:" + xVec + "   yVec:" + yVec);

        pos = pos.clone();
        System.out.println("pos before:" + pos);
        if (xVec.x < 0) {
            pos.x += data.xSize - 1;
        } else if (yVec.x < 0) {
            pos.x += data.ySize - 1;
        }

        if (xVec.y < 0) {
            pos.y += data.xSize - 1;
        } else if (yVec.y < 0) {
            pos.y += data.ySize - 1;
        }
        System.out.println("pos after:" + pos);

        Vector3i xPos = pos.clone();
        for (int i = 0; i < data.xSize; ) {
            Vector3i yPos = xPos.clone();

            for (int j = 0; j < data.ySize; ) {
                Vector3i zPos = yPos.clone();

                for (int k = 0; k < data.zSize; ) {
                    int val = data.cells[i][j][k];
                    int existing = cells.getCellType(zPos.x, zPos.y, zPos.z);

                    if (val != 0) {
                        boolean topOverride = false;
                        topOverride = cells.getCellType(zPos.x, zPos.y, zPos.z + 1) == 7;

                        BlockType type = BlockTypeIndex.types[val];
                        type = BlockTransforms.mirror(type, mirrorFlags);
                        type = BlockTransforms.rotate(type, rotation);

                        if (existing != 0) {
                            type = merge(type, existing, topOverride);
                            if (type == null) ;
                        } else {
                            cells.setCellType(zPos.x, zPos.y, zPos.z, type.getId());
                        }
                    }
                    k++;
                    zPos.addLocal(zVec);
                }

                if (data.zSize == 10) {
                    pos.z += 9;
                    int repeat = data.cells[i][j][8];
                    int cap = data.cells[i][j][9];
                    System.out.println("Repeat:" + repeat + "  cap:" + cap);
                    if (cap != 0) {
                        BlockType type = BlockTypeIndex.types[repeat];
                        type = BlockTransforms.mirror(type, mirrorFlags);
                        type = BlockTransforms.rotate(type, rotation);

                        int count = maxHeight - zPos.z;
                        System.out.println("Extending " + count + " blocks up from:" + zPos.z + "  num floors:" + maxHeight);
                        for (int k = 0; k < count; ) {
                            cells.setCellType(zPos.x, zPos.y, zPos.z, type.getId());

                            k++;
                            zPos.addLocal(zVec);
                        }

                        type = BlockTypeIndex.types[cap];
                        type = BlockTransforms.mirror(type, mirrorFlags);
                        type = BlockTransforms.rotate(type, rotation);

                        cells.setCellType(zPos.x, zPos.y, zPos.z, type.getId());
                    }
                }
                j++;
                yPos.addLocal(yVec);
            }
            i++;
            xPos.addLocal(xVec);
        }
    }

    public static MaterialType bestType(MaterialType type1, MaterialType type2, boolean shapesAreSame) {
        if ((type1 == type2) && (shapesAreSame)) {
            return null;
        }
        if (type1 == type2) {
            return type1;
        }

        if ((type1 == MaterialType.WATER) || (type2 == MaterialType.WATER)) {
            return MaterialType.WATER;
        }

        if ((type1 == MaterialType.STONE) || (type2 == MaterialType.STONE)) {
            return MaterialType.STONE;
        }

        if ((type1 == MaterialType.MARBLE) || (type2 == MaterialType.MARBLE)) {
            return MaterialType.MARBLE;
        }

        if ((type1 == MaterialType.ROCK) || (type2 == MaterialType.ROCK)) {
            return MaterialType.ROCK;
        }

        if (shapesAreSame) {
            return type2;
        }

        if ((type1 == MaterialType.WOOD) || (type2 == MaterialType.WOOD)) {
            MaterialType other = type1 == MaterialType.WOOD ? type2 : type1;
            if ((other == MaterialType.WADDLE) || (other == MaterialType.SHINGLES)) {
                return MaterialType.WOOD;
            }

        }

        if ((type1 == MaterialType.WADDLE) || (type2 == MaterialType.WADDLE)) {
            MaterialType other = type1 == MaterialType.WADDLE ? type2 : type1;
            if ((other == MaterialType.WOOD) || (other == MaterialType.SHINGLES)) {
                return MaterialType.WOOD;
            }

        }

        return null;
    }

    public static BlockType merge(BlockType type, int existing, boolean topOverride) {
        System.out.println("merge(" + type + ", " + existing + ")");
        if (existing == 0) {
            return type;
        }

        if (type.getId() == existing) {
            return type;
        }
        BlockType e = BlockTypeIndex.types[existing];

        System.out.println("existing type:" + e);

        GeomFactory f1 = type.getGeomFactory();
        GeomFactory f2 = e.getGeomFactory();
        System.out.println("Checking factories:" + f1 + "  and  " + f2);
        boolean shapesAreSame = f1.isSameShape(f2);
        System.out.println("Shapes are same:" + shapesAreSame);

        MaterialType newType = type.getMaterial();
        MaterialType currentType = e.getMaterial();

        MaterialType best = bestType(currentType, newType, shapesAreSame);
        System.out.println("best type:" + best);
        if (best == null) {
            return null;
        }

        if (shapesAreSame) {
            if (best == newType) {
                return type;
            }
            return e;
        }

        if ((topOverride) && (!e.isSolid(4))) {
            return null;
        }

        if (best == MaterialType.SHINGLES) {
            if (f1.getMassPortion() < f2.getMassPortion()) {
                return e;
            }

            return type;
        }

        return BlockTypeIndex.getDefaultType(best);
    }
}