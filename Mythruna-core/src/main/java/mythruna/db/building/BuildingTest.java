package mythruna.db.building;

import mythruna.*;
import mythruna.db.BlueprintData;
import mythruna.db.CellAccess;
import mythruna.db.DefaultBlueprintDatabase;
import mythruna.geom.GeomFactory;
import org.progeeks.util.log.Log;

import java.io.File;
import java.io.IOException;

public class BuildingTest {

    static Log log = Log.getLog();

    private static String[] groundCornerNames = {"gl-c-kitchen1.bp", "gl-c-room1.bp", "gl-c-door1.bp", "gl-c-stairs.bp", "gl-c-window-open.bp", "gl-c-window2.bp", "gl-c-stairs2.bp", "gl-c-stall.bp", "gl-c-forge-lg.bp", "gl-c-room-door1.bp"};

    private static String[] upperCornerNames = {"ul-ce-open.bp", "ul-ce-open2.bp", "ul-ce-room.bp", "ul-ce-open.bp", "ul-ce-open2.bp", "ul-ce-room.bp", "ul-ce-open.bp", "ul-ce-open2.bp", "ul-ce-room.bp", "ul-ce-open.bp", "ul-ce-open2.bp", "ul-ce-room.bp", "ul-ce-balcony2.bp"};

    private static String[] roofCornerNames = {"roof-ce-floor.bp", "roof-ce-open1.bp", "roof-ce-floor.bp", "roof-ce-open1.bp", "roof-ce-gable.bp"};

    private static String[] hutFrontNames = {"gl-cap-hut1-door.bp", "gl-cap-hut1-door2.bp"};

    private static String[] hutBackNames = {"gl-cap-hut1-chimney.bp", "gl-cap-hut1-window.bp"};

    private static String[] thatchFrontNames = {"gl-cap-thatch-front1.bp", "gl-cap-thatch-front2.bp"};

    private static String[] thatchBackNames = {"gl-cap-thatch-back1.bp", "gl-cap-thatch-back2.bp", "gl-cap-thatch-back3.bp"};
    private static BlueprintData cKitchen;
    private static BlueprintData cBackroom;
    private static BlueprintData[] groundCorners;
    private static BlueprintData[] upperCorners;
    private static BlueprintData[] roofCorners;
    private static BlueprintData[] hutFronts = loadParts("mods/scripts/building/hut1/", hutFrontNames);
    private static BlueprintData[] hutBacks = loadParts("mods/scripts/building/hut1/", hutBackNames);
    private static BlueprintData[] thatchFronts = loadParts("mods/scripts/building/thatch1/", thatchFrontNames);
    private static BlueprintData[] thatchBacks = loadParts("mods/scripts/building/thatch1/", thatchBackNames);

    public BuildingTest() {
    }

    private static BlueprintData[] loadParts(String base, String[] names) {
        BlueprintData[] result = new BlueprintData[names.length];
        for (int i = 0; i < names.length; i++) {
            try {
                result[i] = DefaultBlueprintDatabase.importBlueprint(new File(base + names[i]));
            } catch (IOException e) {
                log.error("Error loading:" + names[i], e);
                result[i] = new BlueprintData();
                result[i].cells = new int[0][0][0];
            }
        }
        return result;
    }

    public static Building createTestBuilding() {
        int numFloors = (int) (Math.random() * 4.0D);

        numFloors += 2;

        DefaultBuilding building = new DefaultBuilding(2, 2, numFloors);

        Vector3i grid11 = new Vector3i(8, 8, 0);
        Vector3i grid00 = grid11.add(-1, -1, 0);
        Vector3i grid10 = grid11.add(0, -1, 0);
        Vector3i grid01 = grid11.add(-1, 0, 0);

        Vector3i[][] grid = {{grid00, grid01}, {grid10, grid11}};
        int[][] rotations = {{2, 1}, {3, 0}};

        Vector3i[][] anchors = {{new Vector3i(1, 1, 0), new Vector3i(1, 0, 0)}, {new Vector3i(0, 1, 0), new Vector3i(0, 0, 0)}};

        int floorLevel = 0;

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                grid[i][j].z = floorLevel;
                int which = (int) (Math.random() * groundCorners.length);

                BlueprintData bp = groundCorners[which];

                int r = 0;
                int mirrorFlags = 0;

                boolean mirrorX = Math.random() < 0.5D;
                boolean mirrorY = Math.random() < 0.5D;
                System.out.println("mirrorX:" + mirrorX + "  mirrorY:" + mirrorY);
                if ((mirrorX) && (mirrorY)) {
                    mirrorFlags = BlockTransforms.MIRROR_X | BlockTransforms.MIRROR_Y;
                    r = 2;
                } else if (mirrorX) {
                    mirrorFlags = BlockTransforms.MIRROR_X;
                    r = 1;
                } else if (mirrorY) {
                    mirrorFlags = BlockTransforms.MIRROR_Y;
                    r = 3;
                }

                int desiredRotation = rotations[i][j];
                int needToRotate = desiredRotation - r;
                if (needToRotate < 0) {
                    needToRotate += 4;
                }
                System.out.println("Desired rotation:" + desiredRotation + "  rotation:" + r + "  need to rotate:" + needToRotate);

                Part p = new Part(bp, needToRotate, mirrorFlags, grid[i][j].clone(), anchors[i][j].x, anchors[i][j].y);

                building.setPart(i, j, 0, p);
            }
        }

        for (int f = 1; f < numFloors - 1; f++) {
            floorLevel += 3;

            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    System.out.println("grid[" + i + "][" + j + "]   loc:" + grid[i][j]);
                    grid[i][j].z = floorLevel;

                    int which = (int) (Math.random() * upperCorners.length);
                    BlueprintData bp = upperCorners[which];

                    int desiredRotation = rotations[i][j];
                    int mirrorFlags = 0;

                    switch (desiredRotation) {
                        case 0:
                            break;
                        case 1:
                            mirrorFlags = BlockTransforms.MIRROR_X;
                            break;
                        case 2:
                            mirrorFlags = BlockTransforms.MIRROR_X | BlockTransforms.MIRROR_Y;
                            break;
                        case 3:
                            mirrorFlags = BlockTransforms.MIRROR_Y;
                    }

                    Part p = new Part(bp, 0, mirrorFlags, grid[i][j].clone(), anchors[i][j].x, anchors[i][j].y);

                    building.setPart(i, j, f, p);
                }
            }
        }

        floorLevel += 3;

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                System.out.println("grid[" + i + "][" + j + "]   loc:" + grid[i][j]);
                grid[i][j].z = floorLevel;

                int which = (int) (Math.random() * roofCorners.length);
                BlueprintData bp = roofCorners[which];

                int desiredRotation = rotations[i][j];
                int mirrorFlags = 0;

                switch (desiredRotation) {
                    case 0:
                        break;
                    case 1:
                        mirrorFlags = BlockTransforms.MIRROR_X;
                        break;
                    case 2:
                        mirrorFlags = BlockTransforms.MIRROR_X | BlockTransforms.MIRROR_Y;
                        break;
                    case 3:
                        mirrorFlags = BlockTransforms.MIRROR_Y;
                }

                Part p = new Part(bp, 0, mirrorFlags, grid[i][j].clone(), anchors[i][j].x, anchors[i][j].y);

                building.setPart(i, j, numFloors - 1, p);
            }
        }

        return building;
    }

    public static Building createTestHut() {
        DefaultBuilding building = new DefaultBuilding(1, 2, 1);

        int[] rotations = {2, 0};
        int[] partNums = {(int) (Math.random() * hutBacks.length), (int) (Math.random() * hutFronts.length)};
        BlueprintData[] parts = {hutBacks[partNums[0]], hutFronts[partNums[1]]};

        for (int j = 0; j < 2; j++) {
            BlueprintData bp = parts[j];

            int r = 0;
            int mirrorFlags = 0;

            boolean mirrorX = Math.random() < 0.5D;
            System.out.println("mirrorX:" + mirrorX);
            if (mirrorX) {
                mirrorFlags = BlockTransforms.MIRROR_X;
            }

            int desiredRotation = rotations[j];
            int needToRotate = desiredRotation - r;
            if (needToRotate < 0) {
                needToRotate += 4;
            }
            System.out.println("Desired rotation:" + desiredRotation + "  rotation:" + r + "  need to rotate:" + needToRotate);

            Part p = new Part(bp, needToRotate, mirrorFlags, new Vector3i(), 0.5D, j == 0 ? 1 : 0);
            building.setPart(0, j, 0, p);
        }

        return building;
    }

    public static Building createTestCottage() {
        DefaultBuilding building = new DefaultBuilding(1, 2, 1);

        int[] rotations = {2, 0};
        int[] partNums = {(int) (Math.random() * thatchBacks.length), (int) (Math.random() * thatchFronts.length)};
        BlueprintData[] parts = {thatchBacks[partNums[0]], thatchFronts[partNums[1]]};

        for (int j = 0; j < 2; j++) {
            BlueprintData bp = parts[j];

            int r = 0;
            int mirrorFlags = 0;

            double val = Math.random();
            boolean mirrorX = val < 0.5D;
            System.out.println("mirrorX:" + mirrorX + "   val:" + val);
            if (mirrorX) {
                mirrorFlags = BlockTransforms.MIRROR_X;
            }

            int desiredRotation = rotations[j];
            int needToRotate = desiredRotation - r;
            if (needToRotate < 0) {
                needToRotate += 4;
            }
            System.out.println("Desired rotation:" + desiredRotation + "  rotation:" + r + "  need to rotate:" + needToRotate);

            Part p = new Part(bp, needToRotate, mirrorFlags, new Vector3i(), 0.5D, j == 0 ? 1 : 0);
            building.setPart(0, j, 0, p);
        }

        return building;
    }

    public static void placeTestBuilding2(Vector3i pos, World world, int r) {
        Building b = createTestBuilding();
        b.rotate(r);

        BuildingUtils.placeBuilding(b, pos, world);
    }

    public static void placeTestBuilding3(Vector3i pos, World world, int rotate) {
        Building b = createTestHut();
        b.rotate(rotate);

        BuildingUtils.placeBuilding(b, pos, world);
    }

    public static void placeTestBuilding4(Vector3i pos, World world, int dir) {
        Building b = createTestCottage();

        BuildingUtils.placeBuilding(b, pos, world);
    }

    public static void placeTestBuilding(Vector3i pos, World world, int dir) {
        System.out.println("placeTestBuilding(" + pos + ", " + world + ", " + dir + ")");

        Vector3i grid11 = new Vector3i(8, 8, 0);
        Vector3i grid00 = grid11.add(-1, -1, 0);
        Vector3i grid10 = grid11.add(0, -1, 0);
        Vector3i grid01 = grid11.add(-1, 0, 0);

        int[][][] cells = new int[16][16][20];

        BlueprintData[] tiles = {cBackroom, cKitchen};
        Vector3i[][] grid = {{grid00, grid10}, {grid01, grid11}};
        int[][] rotations = {{2, 3}, {1, 0}};

        int floorLevel = 0;

        int numFloors = (int) (Math.random() * 4.0D);

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                System.out.println("grid[" + i + "][" + j + "]   loc:" + grid[i][j]);
                grid[i][j].z = floorLevel;

                int which = (int) (Math.random() * groundCorners.length);
                BlueprintData bp = groundCorners[which];

                int r = 0;
                int mirrorFlags = 0;

                boolean mirrorX = Math.random() < 0.5D;
                boolean mirrorY = Math.random() < 0.5D;
                System.out.println("mirrorX:" + mirrorX + "  mirrorY:" + mirrorY);
                if ((mirrorX) && (mirrorY)) {
                    mirrorFlags = BlockTransforms.MIRROR_X | BlockTransforms.MIRROR_Y;
                    r = 2;
                } else if (mirrorX) {
                    mirrorFlags = BlockTransforms.MIRROR_X;
                    r = 1;
                } else if (mirrorY) {
                    mirrorFlags = BlockTransforms.MIRROR_Y;
                    r = 3;
                }

                int desiredRotation = rotations[i][j];
                int needToRotate = desiredRotation - r;
                if (needToRotate < 0) {
                    needToRotate += 4;
                }
                System.out.println("Desired rotation:" + desiredRotation + "  rotation:" + r + "  need to rotate:" + needToRotate);

                placePart(grid[i][j], cells, bp, needToRotate, mirrorFlags, numFloors);
            }
        }

        for (int f = 0; f < numFloors; f++) {
            floorLevel += 3;

            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    System.out.println("grid[" + i + "][" + j + "]   loc:" + grid[i][j]);
                    grid[i][j].z = floorLevel;

                    int which = (int) (Math.random() * upperCorners.length);
                    BlueprintData bp = upperCorners[which];

                    int desiredRotation = rotations[i][j];
                    int mirrorFlags = 0;

                    switch (desiredRotation) {
                        case 0:
                            break;
                        case 1:
                            mirrorFlags = BlockTransforms.MIRROR_X;
                            break;
                        case 2:
                            mirrorFlags = BlockTransforms.MIRROR_X | BlockTransforms.MIRROR_Y;
                            break;
                        case 3:
                            mirrorFlags = BlockTransforms.MIRROR_Y;
                    }

                    placePart(grid[i][j], cells, bp, 0, mirrorFlags, numFloors);
                }
            }
        }

        floorLevel += 3;

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                System.out.println("grid[" + i + "][" + j + "]   loc:" + grid[i][j]);
                grid[i][j].z = floorLevel;

                int which = (int) (Math.random() * roofCorners.length);
                BlueprintData bp = roofCorners[which];

                int desiredRotation = rotations[i][j];
                int mirrorFlags = 0;

                switch (desiredRotation) {
                    case 0:
                        break;
                    case 1:
                        mirrorFlags = BlockTransforms.MIRROR_X;
                        break;
                    case 2:
                        mirrorFlags = BlockTransforms.MIRROR_X | BlockTransforms.MIRROR_Y;
                        break;
                    case 3:
                        mirrorFlags = BlockTransforms.MIRROR_Y;
                }

                placePart(grid[i][j], cells, bp, 0, mirrorFlags, numFloors);
            }

        }

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                boolean empty = true;
                boolean isBottomOnly = false;
                int val;
                for (int k = 0; k < 20; k++) {
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

    public static void placePart(Vector3i pos, int[][][] cells, BlueprintData data, int rotation, int mirrorFlags, int numFloors) {
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

        Vector3i xPos = pos.clone();
        for (int i = 0; i < data.xSize; ) {
            Vector3i yPos = xPos.clone();

            for (int j = 0; j < data.ySize; ) {
                Vector3i zPos = yPos.clone();

                for (int k = 0; k < data.zSize; ) {
                    int val = data.cells[i][j][k];
                    int existing = cells[zPos.x][zPos.y][zPos.z];

                    if (val != 0) {
                        boolean topOverride = false;
                        topOverride = cells[zPos.x][zPos.y][(zPos.z + 1)] == 7;

                        BlockType type = BlockTypeIndex.types[val];
                        type = BlockTransforms.mirror(type, mirrorFlags);
                        type = BlockTransforms.rotate(type, rotation);

                        if (existing != 0) {
                            type = merge(type, existing, topOverride);
                            if (type == null) ;
                        } else {
                            System.out.println("Setting type at:" + zPos + " to:" + type.getId() + "  existing:" + existing);
                            cells[zPos.x][zPos.y][zPos.z] = type.getId();

                            existing = existing = cells[zPos.x][zPos.y][zPos.z];
                            System.out.println("Type is now:" + existing);
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

                        int count = (numFloors + 2) * 3 + 3 - zPos.z;

                        System.out.println("Extending " + count + " blocks up from:" + zPos.z + "  num floors:" + numFloors);
                        for (int k = 0; k < count; ) {
                            cells[zPos.x][zPos.y][zPos.z] = type.getId();

                            k++;
                            zPos.addLocal(zVec);
                        }

                        type = BlockTypeIndex.types[cap];
                        type = BlockTransforms.mirror(type, mirrorFlags);
                        type = BlockTransforms.rotate(type, rotation);
                        cells[zPos.x][zPos.y][zPos.z] = type.getId();
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

    public static void placePart(Vector3i pos, World world, BlueprintData data, int dir) {
        System.out.println("placePart(" + pos + ", " + world + ", " + data + ", " + dir + ")");

        pos.z += 1;

        int xDir = dir;
        int yDir = mythruna.Direction.LEFT[xDir];
        yDir = mythruna.Direction.INVERSE[yDir];
        int zDir = 4;

        int rotation = mythruna.Direction.DIR_TO_CARDINAL[dir];

        rotation--;
        if (rotation < 0) {
            rotation += 4;
        }
        Vector3i xVec = mythruna.Direction.VECS[xDir];
        Vector3i yVec = mythruna.Direction.VECS[yDir];
        Vector3i zVec = mythruna.Direction.VECS[zDir];

        Vector3i xPos = pos.clone();
        for (int i = 0; i < data.xSize; ) {
            Vector3i yPos = xPos.clone();

            for (int j = 0; j < data.ySize; ) {
                Vector3i zPos = yPos.clone();

                for (int k = 0; k < data.zSize; ) {
                    int val = data.cells[i][j][k];
                    if (val != 0) {
                        BlockType type = BlockTypeIndex.types[val];
                        type = BlockTransforms.rotate(type, rotation);
                        world.setCellType(zPos.x, zPos.y, zPos.z, type.getId());
                    }
                    k++;
                    zPos.addLocal(zVec);
                }
                j++;
                yPos.addLocal(yVec);
            }
            i++;
            xPos.addLocal(xVec);
        }
    }

    public static void placeMirroredPart(Vector3i pos, World world, BlueprintData data, int mirrorFlags) {
        System.out.println("placeMirroredPart(" + pos + ", " + world + ", " + data + ", " + mirrorFlags + ")");

        pos.z += 1;

        int dir = 2;
        int xDir = dir;
        int yDir = mythruna.Direction.LEFT[xDir];
        yDir = mythruna.Direction.INVERSE[yDir];
        int zDir = 4;

        int rotation = mythruna.Direction.DIR_TO_CARDINAL[dir];

        rotation--;
        if (rotation < 0) {
            rotation += 4;
        }
        Vector3i xVec = mythruna.Direction.VECS[xDir];
        if ((mirrorFlags & BlockTransforms.MIRROR_X) != 0)
            xVec = xVec.mult(-1);
        Vector3i yVec = mythruna.Direction.VECS[yDir];
        if ((mirrorFlags & BlockTransforms.MIRROR_Y) != 0)
            yVec = yVec.mult(-1);
        Vector3i zVec = mythruna.Direction.VECS[zDir];

        Vector3i xPos = pos.clone();
        for (int i = 0; i < data.xSize; ) {
            Vector3i yPos = xPos.clone();

            for (int j = 0; j < data.ySize; ) {
                Vector3i zPos = yPos.clone();

                for (int k = 0; k < data.zSize; ) {
                    int val = data.cells[i][j][k];
                    if (val != 0) {
                        BlockType type = BlockTypeIndex.types[val];
                        type = BlockTransforms.mirror(type, mirrorFlags);
                        world.setCellType(zPos.x, zPos.y, zPos.z, type.getId());
                    }
                    k++;
                    zPos.addLocal(zVec);
                }
                j++;
                yPos.addLocal(yVec);
            }
            i++;
            xPos.addLocal(xVec);
        }
    }

    static {
        try {
            cKitchen = DefaultBlueprintDatabase.importBlueprint(new File("mods/scripts/gl-c-kitchen1.bp"));
            cBackroom = DefaultBlueprintDatabase.importBlueprint(new File("mods/scripts/gl-c-room1.bp"));

            groundCorners = new BlueprintData[groundCornerNames.length];
            for (int i = 0; i < groundCornerNames.length; i++) {
                groundCorners[i] = DefaultBlueprintDatabase.importBlueprint(new File("mods/scripts/" + groundCornerNames[i]));
            }

            upperCorners = new BlueprintData[upperCornerNames.length];
            for (int i = 0; i < upperCornerNames.length; i++) {
                upperCorners[i] = DefaultBlueprintDatabase.importBlueprint(new File("mods/scripts/" + upperCornerNames[i]));
            }

            roofCorners = new BlueprintData[roofCornerNames.length];
            for (int i = 0; i < roofCornerNames.length; i++) {
                roofCorners[i] = DefaultBlueprintDatabase.importBlueprint(new File("mods/scripts/" + roofCornerNames[i]));
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not find file", e);
        }
    }
}