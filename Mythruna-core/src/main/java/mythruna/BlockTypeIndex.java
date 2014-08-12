package mythruna;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import mythruna.db.building.BlockTransforms;
import mythruna.geom.*;
import mythruna.geom.script.BlockScripts;

import java.util.*;

public class BlockTypeIndex {
    public static BlockType INVALID = new BlockType(32767, "Invalid", MaterialType.EMPTY, true, 0, new CubeFactory(0));

    private static boolean initialized = false;
    private static int maxId = 0;
    public static BlockType[] types = new BlockType[80];

    private static Map<String, BlockGroup> groups = new LinkedHashMap();

    private static Map<MaterialType, BlockType> defaultTypes = new HashMap();

    public BlockTypeIndex() {
    }

    public static int getMaxId() {
        return maxId;
    }

    public static Set<String> groupNames() {
        return groups.keySet();
    }

    public static BlockGroup getGroup(String name) {
        return (BlockGroup) groups.get(name);
    }

    public static Collection<BlockGroup> groups() {
        return groups.values();
    }

    public static BlockType getDefaultType(MaterialType type) {
        return (BlockType) defaultTypes.get(type);
    }

    protected static void expand(int size) {
        BlockType[] newTypes = new BlockType[size];
        System.arraycopy(types, 0, newTypes, 0, types.length);
        types = newTypes;

        for (int i = 1; i < types.length; i++) {
            if (types[i] == null)
                types[i] = INVALID;
        }
    }

    public static void set(int index, BlockType type) {
        if (types.length <= index)
            expand(index + 20);
        if ((types[index] != null) && (types[index] != INVALID) && (type != null))
            throw new RuntimeException("Type already exists at index:" + index + "  type:" + types[index]);
        types[index] = type;
        if ((type != null) && (index > maxId)) {
            maxId = index;
        }
        if (type != null) {
            for (BlockGroup bg : groups.values()) {
                bg.reset(index, type);
            }

            MaterialType mt = type.getMaterial();
            System.out.println("type:" + type + "  material:" + mt + "  already indexed:" + defaultTypes.containsKey(mt));
            if (!defaultTypes.containsKey(mt))
                defaultTypes.put(mt, type);
        } else {
            types[index] = INVALID;
        }
    }

    public static BlockGroup setGroup(String name, List<BlockType> types) {
        BlockGroup bg = (BlockGroup) groups.get(name);
        if (bg == null) {
            bg = new BlockGroup(name, types);
            groups.put(name, bg);
            return bg;
        }

        bg.update(types);
        return bg;
    }

    public static BlockGroup addGroup(String name, BlockType[] types) {
        BlockGroup bg = new BlockGroup(name, types);
        if (groups.containsKey(name))
            throw new RuntimeException("Group already defined for name:" + name);
        groups.put(name, bg);
        return bg;
    }

    public static BlockType addType(int id, String name, MaterialType material, int group, GeomFactory factory) {
        boolean opaque = true;
        if (group > 0)
            opaque = false;
        BlockType bt = new BlockType(id, name, material, opaque, group, factory);
        set(id, bt);
        return bt;
    }

    public static BlockType addBlockType(int id, String name, MaterialType material, int group, int materialType) {
        boolean opaque = true;
        if (group > 0)
            opaque = false;
        BlockType bt = new BlockType(id, name, material, opaque, group, new CubeFactory(materialType));
        set(id, bt);
        return bt;
    }

    protected static BlockType addRampType(int id, String name, MaterialType material, int group, int solidXY, int solidZ, int materialType) {
        return addRampType(id, name, material, group, solidXY, solidZ, materialType, materialType, materialType);
    }

    protected static BlockType addRampType(int id, String name, MaterialType material, int group, int solidXY, int solidZ, int sideType, int topType, int bottomType) {
        boolean opaque = true;
        if (group > 0)
            opaque = false;
        BlockType bt = new BlockType(id, name, material, opaque, group, new WedgeFactory(solidXY, solidZ, sideType, topType, bottomType));

        set(id, bt);
        return bt;
    }

    protected static BlockType addRampType(int id, String name, MaterialType material, int group, int solidXY, int solidZ, int sideType, int topType, int bottomType, boolean yFlip) {
        boolean opaque = true;
        if (group > 0)
            opaque = false;
        BlockType bt = new BlockType(id, name, material, opaque, group, new WedgeFactory(solidXY, solidZ, sideType, topType, bottomType, yFlip));

        set(id, bt);
        return bt;
    }

    protected static BlockType addBlockTypeW(int id, String name, MaterialType material, int group, float light, int[] materialTypes) {
        boolean opaque = true;
        if (group > 0)
            opaque = false;
        float[] dirs = {light, light, light};
        BlockType bt = new BlockType(id, name, material, opaque, group, new CubeFactory(dirs, materialTypes[0], materialTypes[0], materialTypes));
        set(id, bt);
        return bt;
    }

    protected static BlockType addBlockType(int id, String name, MaterialType material, int group, int materialType, float yOffset) {
        boolean opaque = true;
        if (group > 0) {
            opaque = false;
        }
        boolean clip = material != MaterialType.EMPTY;
        Vector3f min = Vector3f.ZERO;
        Vector3f max = CubeFactory.UNIT_CUBE.clone();
        max.z = (0.5F + yOffset);
        BlockType bt = new BlockType(id, name, material, opaque, group, min, max, new PartialCubeFactory(min, max, clip, false, null, materialType, materialType, materialType, new int[0]));

        set(id, bt);
        return bt;
    }

    protected static BlockType addBlockTypeX(int id, String name, MaterialType material, int group, float light, float yOffset, int[] materialTypes) {
        boolean opaque = true;
        if (group > 0)
            opaque = false;
        boolean clip = material != MaterialType.EMPTY;
        float[] dirs = {light, light, light};
        Vector3f min = Vector3f.ZERO;
        Vector3f max = CubeFactory.UNIT_CUBE.clone();
        max.z = (0.5F + yOffset);
        BlockType bt = new BlockType(id, name, material, opaque, group, min, max, new PartialCubeFactory(min, max, clip, false, dirs, materialTypes[0], materialTypes[0], materialTypes[0], materialTypes));

        set(id, bt);
        return bt;
    }

    protected static BlockType addBlockType(int id, String name, MaterialType material, int group, int materialType, float x1, float y1, float z1, float x2, float y2, float z2) {
        return addBlockType(id, name, material, group, materialType, materialType, materialType, x1, y1, z1, x2, y2, z2);
    }

    protected static BlockType addBlockType(int id, String name, MaterialType material, int group, int sideType, int topType, int bottomType, float x1, float y1, float z1, float x2, float y2, float z2) {
        boolean opaque = true;
        if (group > 0) {
            opaque = false;
        }

        float x = x2 - x1;
        float y = y2 - y1;
        float z = z2 - z1;

        boolean clip = material != MaterialType.EMPTY;

        Vector3f min = new Vector3f(x1, y1, z1);
        Vector3f max = new Vector3f(x2, y2, z2);

        BlockType bt = new BlockType(id, name, material, opaque, group, min, max, new PartialCubeFactory(min, max, clip, false, null, sideType, topType, bottomType, new int[0]));

        set(id, bt);
        return bt;
    }

    protected static BlockType addBlockType(int id, String name, MaterialType material, int group, int materialType, float x1, float y1, float z1, float x2, float y2, float z2, float light) {
        boolean opaque = true;
        if (group > 0) {
            opaque = false;
        }

        float x = x2 - x1;
        float y = y2 - y1;
        float z = z2 - z1;

        boolean clip = material != MaterialType.EMPTY;

        float[] dirs = {light, light, light};

        Vector3f min = new Vector3f(x1, y1, z1);
        Vector3f max = new Vector3f(x2, y2, z2);

        BlockType bt = new BlockType(id, name, material, opaque, group, min, max, new PartialCubeFactory(min, max, clip, false, dirs, materialType, materialType, materialType, new int[0]));

        set(id, bt);
        return bt;
    }

    protected static BlockType addBlockType(int id, String name, MaterialType material, int group, int sideType, int topType, int bottomType) {
        boolean opaque = true;
        if (group != 0)
            opaque = false;
        BlockType bt = new BlockType(id, name, material, opaque, group, new CubeFactory(sideType, topType, bottomType));
        set(id, bt);
        return bt;
    }

    protected static BlockType addBlockType(int id, String name, MaterialType material, int group, int topType, int bottomType, int[] sides) {
        boolean opaque = true;
        if (group != 0)
            opaque = false;
        BlockType bt = new BlockType(id, name, material, opaque, group, new CubeFactory(topType, bottomType, sides));
        set(id, bt);
        return bt;
    }

    protected static BlockType addGrassBlockType(int id, String name, MaterialType material, int group, float height, int materialType) {
        GeomFactory factory = new GrassFactory(height, materialType);

        BlockType bt = new BlockType(id, name, material, false, group, new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(1.0F, 1.0F, height * 0.5F), factory);

        set(id, bt);
        return bt;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        addGroup("Dirt", new BlockType[]{addBlockType(1, "Dirt", MaterialType.DIRT, 0, 0), addBlockType(2, "Grass", MaterialType.GRASS, 0, 1, 2, 0), addRampType(48, "Dirt Slope", MaterialType.DIRT, 0, 0, 5, 0, 100, 0), addRampType(49, "Dirt Slope", MaterialType.DIRT, 0, 1, 5, 0, 100, 0), addRampType(50, "Dirt Slope", MaterialType.DIRT, 0, 2, 5, 0, 100, 0), addRampType(51, "Dirt Slope", MaterialType.DIRT, 0, 3, 5, 0, 100, 0)});
        addGroup("Sand", new BlockType[]{addBlockType(3, "Sand", MaterialType.SAND, 0, 3)});
        addGroup("Stone", new BlockType[0]);
        addGroup("Stone Slopes", new BlockType[0]);
        addGroup("Rock", new BlockType[]{addBlockType(6, "Rock", MaterialType.ROCK, 0, 6, 16, 6), addType(143, "Rock Angle", MaterialType.ROCK, 0, new AngleFactory(2, 0, 6, 106)), addType(140, "Rock Angle", MaterialType.ROCK, 0, new AngleFactory(0, 3, 6, 106)), addType(141, "Rock Angle", MaterialType.ROCK, 0, new AngleFactory(3, 1, 6, 106)), addType(142, "Rock Angle", MaterialType.ROCK, 0, new AngleFactory(1, 2, 6, 106)), addBlockType(5, "Rock Capped", MaterialType.ROCK, 0, 14, 15, 6), addType(147, "Rock Capped Angle", MaterialType.ROCK, 0, new AngleFactory(2, 0, 14, 15, 6, 106)), addType(144, "Rock Capped Angle", MaterialType.ROCK, 0, new AngleFactory(0, 3, 14, 15, 6, 106)), addType(145, "Rock Capped Angle", MaterialType.ROCK, 0, new AngleFactory(3, 1, 14, 15, 6, 106)), addType(146, "Rock Capped Angle", MaterialType.ROCK, 0, new AngleFactory(1, 2, 14, 15, 6, 106))});
        addGroup("Cobble", new BlockType[]{addBlockType(43, "Cobble", MaterialType.COBBLE, 0, 5), addBlockType(23, "Cobble Wall", MaterialType.COBBLE, 0, 5, 0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F), addBlockType(24, "Cobble Wall", MaterialType.COBBLE, 0, 5, 0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F), addBlockType(22, "Cobble Wall", MaterialType.COBBLE, 0, 5, 0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F), addBlockType(21, "Cobble Wall", MaterialType.COBBLE, 0, 5, 0.0F, 0.0F, 0.0F, 0.5F, 1.0F, 1.0F), addType(151, "Cobble Angle", MaterialType.COBBLE, 0, new AngleFactory(2, 0, 5, 105)), addType(148, "Cobble Angle", MaterialType.COBBLE, 0, new AngleFactory(0, 3, 5, 105)), addType(149, "Cobble Angle", MaterialType.COBBLE, 0, new AngleFactory(3, 1, 5, 105)), addType(150, "Cobble Angle", MaterialType.COBBLE, 0, new AngleFactory(1, 2, 5, 105))});
        addGroup("Mortared Rock", new BlockType[0]);
        addGroup("Water", new BlockType[]{addBlockTypeW(7, "Water", MaterialType.WATER, 1, 0.99F, new int[]{24, 24, 24, 24, 7, 24}), addBlockTypeX(8, "Water Top", MaterialType.WATER, 1, 0.99F, 0.3F, new int[]{24, 24, 24, 24, 7, 24})});
        addGroup("Trees", new BlockType[]{addType(161, "Trunk", MaterialType.WOOD, 0, new CylinderFactory(108, 113, 0.5F)), addType(168, "Trunk", MaterialType.WOOD, 0, new CylinderFactory(108, 113, 0.25F)), addType(170, "Branch", MaterialType.WOOD, 0, new CylinderFactory2(108, 113, 0.25F, 0)), addType(172, "Branch", MaterialType.WOOD, 0, new CylinderFactory2(108, 113, 0.25F, 1)), addType(169, "Branch", MaterialType.WOOD, 0, new CylinderFactory2(108, 113, 0.25F, 2)), addType(171, "Branch", MaterialType.WOOD, 0, new CylinderFactory2(108, 113, 0.25F, 3)), addType(308, "Log", MaterialType.WOOD, 0, new CylinderFactory2(108, 113, 0.25F, 0.501F, new Quaternion().fromAngles(1.570796F, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, -0.4F))), addType(309, "Log", MaterialType.WOOD, 0, new CylinderFactory2(108, 113, 0.25F, 0.501F, new Quaternion().fromAngles(0.0F, 0.0F, 1.570796F), new Vector3f(0.0F, 0.0F, -0.4F))), addType(310, "Log", MaterialType.WOOD, 0, new CylinderFactory2(108, 113, 0.55F, 0.501F, new Quaternion().fromAngles(1.570796F, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, -0.15F))), addType(311, "Log", MaterialType.WOOD, 0, new CylinderFactory2(108, 113, 0.55F, 0.501F, new Quaternion().fromAngles(0.0F, 0.0F, 1.570796F), new Vector3f(0.0F, 0.0F, -0.15F))), addType(160, "Leaves", MaterialType.LEAVES, 3, new TreeLeafFactory(25)), addType(173, "Leaves 2", MaterialType.LEAVES, 3, new TreeLeafFactory(26)), addType(238, "Spike", MaterialType.WOOD, 0, new ConeFactory(134, 113, 4, 1.2F, 0.5F)), addType(239, "Spike", MaterialType.WOOD, 0, new ConeFactory(134, 113, 5, 1.2F, 0.5F)), addType(240, "Spike", MaterialType.WOOD, 0, new ConeFactory(134, 113, 4, 1.1F, 0.25F)), addType(241, "Spike", MaterialType.WOOD, 0, new ConeFactory(134, 113, 5, 1.1F, 0.25F)), addType(223, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, 0.0F)), addType(227, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, 0.7853982F)), addType(224, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, 1.570796F)), addType(228, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, 2.356195F)), addType(225, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, 3.141593F)), addType(229, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, 3.926991F)), addType(226, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, 4.712389F)), addType(230, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, 5.497787F)), addType(231, "Pine Sapling", MaterialType.LEAVES, 4, new PineTopFactory(108, 32))});
        addGroup("Flora", new BlockType[]{addGrassBlockType(82, "Tall Grass", MaterialType.FLORA, 0, 0.99F, 22), addGrassBlockType(83, "Medium Grass", MaterialType.FLORA, 0, 0.75F, 22), addGrassBlockType(84, "Short Grass", MaterialType.FLORA, 0, 0.5F, 22), addType(232, "Wild Flowers", MaterialType.FLORA, -1, new StaticParticleFactory(33, new Vector3f(0.0F, 0.0F, 0.2F), new Vector3f(1.0F, 1.0F, 0.5F), types[84].getGeomFactory(), new int[]{2, 3, 3, 3}).setHeight(0, 0.25F)), addType(233, "Flower 1", MaterialType.FLORA, -1, new StaticParticleFactory(33, new Vector3f(0.0F, 0.0F, 0.2F), new Vector3f(1.0F, 1.0F, 0.5F), types[84].getGeomFactory(), new int[]{3, 5, 0, 0}).setHeight(0, 0.25F)), addType(234, "Flower 2", MaterialType.FLORA, -1, new StaticParticleFactory(33, new Vector3f(0.0F, 0.0F, 0.2F), new Vector3f(1.0F, 1.0F, 0.5F), types[84].getGeomFactory(), new int[]{3, 0, 5, 0}).setHeight(0, 0.25F)), addType(235, "Flower 3", MaterialType.FLORA, -1, new StaticParticleFactory(33, new Vector3f(0.0F, 0.0F, 0.2F), new Vector3f(1.0F, 1.0F, 0.5F), types[84].getGeomFactory(), new int[]{3, 0, 0, 5}).setHeight(0, 0.25F)), addType(236, "Brush Short", MaterialType.FLORA, -1, new StaticParticleFactory(33, new Vector3f(0.0F, 0.0F, 0.1F), new Vector3f(1.0F, 1.0F, 0.25F), types[84].getGeomFactory(), new int[]{8, 0, 0, 0})), addType(237, "Brush Tall", MaterialType.FLORA, -1, new StaticParticleFactory(33, new Vector3f(0.0F, 0.0F, 0.1F), new Vector3f(1.0F, 1.0F, 0.5F), types[82].getGeomFactory(), new int[]{10, 0, 0, 0}))});
        addGroup("Old Vegetation", new BlockType[]{addBlockType(9, "Tree Trunk", MaterialType.WOOD, 0, 8, 13, 13), addBlockType(85, "Log", MaterialType.WOOD, 0, 8, 8, new int[]{13, 13, 23, 23}), addBlockType(86, "Log", MaterialType.WOOD, 0, 23, 23, new int[]{23, 23, 13, 13}), addRampType(90, "Trunk Slope", MaterialType.WOOD, 0, 0, 5, 23, 108, 8), addRampType(91, "Trunk Slope", MaterialType.WOOD, 0, 1, 5, 23, 108, 8), addRampType(92, "Trunk Slope", MaterialType.WOOD, 0, 2, 5, 23, 123, 23), addRampType(93, "Trunk Slope", MaterialType.WOOD, 0, 3, 5, 23, 123, 23), addRampType(94, "Trunk Slope Bottom", MaterialType.WOOD, 0, 0, 4, 23, 8, 108), addRampType(95, "Trunk Slope Bottom", MaterialType.WOOD, 0, 1, 4, 23, 8, 108), addRampType(96, "Trunk Slope Bottom", MaterialType.WOOD, 0, 2, 4, 23, 23, 123), addRampType(97, "Trunk Slope Bottom", MaterialType.WOOD, 0, 3, 4, 23, 23, 123), addBlockType(10, "Leaves", MaterialType.LEAVES, 2, 9), addRampType(102, "Leaves Slope", MaterialType.LEAVES, 2, 0, 5, 9, 109, 9), addRampType(103, "Leaves Slope", MaterialType.LEAVES, 2, 1, 5, 9, 109, 9), addRampType(104, "Leaves Slope", MaterialType.LEAVES, 2, 2, 5, 9, 109, 9), addRampType(105, "Leaves Slope", MaterialType.LEAVES, 2, 3, 5, 9, 109, 9), addRampType(98, "Leaves Slope Bottom", MaterialType.LEAVES, 2, 0, 4, 9, 9, 109), addRampType(99, "Leaves Slope Bottom", MaterialType.LEAVES, 2, 1, 4, 9, 9, 109), addRampType(100, "Leaves Slope Bottom", MaterialType.LEAVES, 2, 2, 4, 9, 9, 109), addRampType(101, "Leaves Slope Bottom", MaterialType.LEAVES, 2, 3, 4, 9, 9, 109), addType(155, "Leaves Angle", MaterialType.LEAVES, 2, new AngleFactory(2, 0, 9, 109)), addType(152, "Leaves Angle", MaterialType.LEAVES, 2, new AngleFactory(0, 3, 9, 109)), addType(153, "Leaves Angle", MaterialType.LEAVES, 2, new AngleFactory(3, 1, 9, 109)), addType(154, "Leaves Angle", MaterialType.LEAVES, 2, new AngleFactory(1, 2, 9, 109))});
        addGroup("Wood", new BlockType[]{addBlockType(32, "Wood Planks", MaterialType.WOOD, 0, 11, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F), addBlockType(87, "Wood Planks-90", MaterialType.WOOD, 0, 21, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F), addBlockType(31, "Wood Planks Top", MaterialType.WOOD, 0, 11, 0.0F, 0.0F, 0.7F, 1.0F, 1.0F, 1.0F), addBlockType(88, "Wood Planks Top-90", MaterialType.WOOD, 0, 21, 0.0F, 0.0F, 0.7F, 1.0F, 1.0F, 1.0F), addBlockType(12, "Wood Planks Bottom", MaterialType.WOOD, 0, 11, -0.3F), addBlockType(89, "Wood Planks Bottom-90", MaterialType.WOOD, 0, 21, -0.3F), addBlockType(54, "Wood Wall", MaterialType.WOOD, 0, 21, 0.0F, 0.0F, 0.0F, 1.0F, 0.3F, 1.0F), addBlockType(55, "Wood Wall", MaterialType.WOOD, 0, 21, 0.0F, 0.7F, 0.0F, 1.0F, 1.0F, 1.0F), addBlockType(53, "Wood Wall", MaterialType.WOOD, 0, 21, 0.7F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F), addBlockType(52, "Wood Wall", MaterialType.WOOD, 0, 21, 0.0F, 0.0F, 0.0F, 0.3F, 1.0F, 1.0F), addBlockType(18, "Wood Pillar", MaterialType.WOOD, 0, 21, 0.5F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F), addBlockType(17, "Wood Pillar", MaterialType.WOOD, 0, 21, 0.0F, 0.0F, 0.0F, 0.5F, 0.5F, 1.0F), addBlockType(19, "Wood Pillar", MaterialType.WOOD, 0, 21, 0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 1.0F), addBlockType(20, "Wood Pillar", MaterialType.WOOD, 0, 21, 0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F), addBlockType(62, "Wood Beam Bottom", MaterialType.WOOD, 0, 11, 0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 0.5F), addBlockType(63, "Wood Beam Bottom", MaterialType.WOOD, 0, 11, 0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F), addBlockType(61, "Wood Beam Bottom", MaterialType.WOOD, 0, 11, 21, 21, 0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 0.5F), addBlockType(60, "Wood Beam Bottom", MaterialType.WOOD, 0, 11, 21, 21, 0.0F, 0.0F, 0.0F, 0.5F, 1.0F, 0.5F), addBlockType(66, "Wood Beam Top", MaterialType.WOOD, 0, 11, 0.0F, 0.0F, 0.5F, 1.0F, 0.5F, 1.0F), addBlockType(67, "Wood Beam Top", MaterialType.WOOD, 0, 11, 0.0F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F), addBlockType(65, "Wood Beam Top", MaterialType.WOOD, 0, 11, 21, 21, 0.5F, 0.0F, 0.5F, 1.0F, 1.0F, 1.0F), addBlockType(64, "Wood Beam Top", MaterialType.WOOD, 0, 11, 21, 21, 0.0F, 0.0F, 0.5F, 0.5F, 1.0F, 1.0F), addType(162, "Wood Column", MaterialType.WOOD, 0, new CylinderFactory(128, 128, 0.5F)), addType(166, "Wood Post", MaterialType.WOOD, 0, new CylinderFactory(128, 128, 0.25F)), addType(306, "Spar", MaterialType.WOOD, 0, new CylinderFactory2(121, 121, 0.25F, 0.5F, new Quaternion().fromAngles(1.570796F, 0.0F, 0.0F))), addType(307, "Spar", MaterialType.WOOD, 0, new CylinderFactory2(121, 121, 0.25F, 0.5F, new Quaternion().fromAngles(0.0F, 0.0F, 1.570796F)))});
        addGroup("Wood Slopes", new BlockType[0]);
        addGroup("W & D", new BlockType[]{addBlockType(11, "W&D Block", MaterialType.WADDLE, 0, 10), addBlockType(15, "W&D Wall", MaterialType.WADDLE, 0, 10, 0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F), addBlockType(16, "W&D Wall", MaterialType.WADDLE, 0, 10, 0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F), addBlockType(14, "W&D Wall", MaterialType.WADDLE, 0, 10, 0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F), addBlockType(13, "W&D Wall", MaterialType.WADDLE, 0, 10, 0.0F, 0.0F, 0.0F, 0.5F, 1.0F, 1.0F)});
        addGroup("Shingles", new BlockType[]{addRampType(254, "Shingles", MaterialType.WOOD, 0, 0, 5, 11, 135, 21), addRampType(255, "Shingles", MaterialType.WOOD, 0, 1, 5, 11, 135, 21, true), addRampType(256, "Shingles", MaterialType.WOOD, 0, 2, 5, 11, 136, 11), addRampType(257, "Shingles", MaterialType.WOOD, 0, 3, 5, 11, 136, 11, true), addType(258, "Shingles Crnr", MaterialType.WOOD, 0, new OuterCornerFactory(2, 136, 135, 11, 21)), addType(259, "Shingles Crnr", MaterialType.WOOD, 0, new OuterCornerFactory(0, 136, 135, 11, 21)), addType(260, "Shingles Crnr", MaterialType.WOOD, 0, new OuterCornerFactory(3, 136, 135, 11, 21)), addType(261, "Shingles Crnr", MaterialType.WOOD, 0, new OuterCornerFactory(1, 136, 135, 11, 21))});
        addGroup("Thatch", new BlockType[]{addType(209, "Thatch", MaterialType.LEAVES, 0, new ThatchFactory(29, 30, 0)), addType(210, "Thatch", MaterialType.LEAVES, 0, new ThatchFactory(29, 30, 1)), addType(207, "Thatch", MaterialType.LEAVES, 0, new ThatchFactory(29, 30, 2)), addType(208, "Thatch", MaterialType.LEAVES, 0, new ThatchFactory(29, 30, 3)), addType(215, "Thatch Corner", MaterialType.LEAVES, 0, new ThatchCornerFactory(29, 30, 2)), addType(216, "Thatch Corner", MaterialType.LEAVES, 0, new ThatchCornerFactory(29, 30, 0)), addType(217, "Thatch Corner", MaterialType.LEAVES, 0, new ThatchCornerFactory(29, 30, 3)), addType(218, "Thatch Corner", MaterialType.LEAVES, 0, new ThatchCornerFactory(29, 30, 1))});
        addGroup("Glass", new BlockType[]{addBlockTypeW(30, "Glass", MaterialType.GLASS, 4, 0.99F, new int[]{18}), addBlockType(58, "Glass Panel", MaterialType.GLASS, 0, 18, 0.0F, 0.2F, 0.0F, 1.0F, 0.3F, 1.0F, 0.99F), addBlockType(59, "Glass Panel", MaterialType.GLASS, 0, 18, 0.0F, 0.7F, 0.0F, 1.0F, 0.8F, 1.0F, 0.99F), addBlockType(57, "Glass Panel", MaterialType.GLASS, 0, 18, 0.7F, 0.0F, 0.0F, 0.8F, 1.0F, 1.0F, 0.99F), addBlockType(56, "Glass Panel", MaterialType.GLASS, 0, 18, 0.2F, 0.0F, 0.0F, 0.3F, 1.0F, 1.0F, 0.99F)});
        addGroup("Black Marble", new BlockType[]{addBlockType(42, "Black Marble", MaterialType.MARBLE, 0, 20), addBlockType(110, "Blk Mrbl Slab Top", MaterialType.MARBLE, 0, 20, 0.0F, 0.0F, 0.5F, 1.0F, 1.0F, 1.0F), addBlockType(111, "Blk Mrbl Slab Bottom", MaterialType.MARBLE, 0, 20, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.5F), addBlockType(114, "Blk Mrbl Wall", MaterialType.MARBLE, 0, 20, 0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F), addBlockType(115, "Blk Mrbl Wall", MaterialType.MARBLE, 0, 20, 0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F), addBlockType(113, "Blk Mrbl Wall", MaterialType.MARBLE, 0, 20, 0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F), addBlockType(112, "Blk Mrbl Wall", MaterialType.MARBLE, 0, 20, 0.0F, 0.0F, 0.0F, 0.5F, 1.0F, 1.0F), addBlockType(117, "Blk Mrbl Pillar", MaterialType.MARBLE, 0, 20, 0.5F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F), addBlockType(116, "Blk Mrbl Pillar", MaterialType.MARBLE, 0, 20, 0.0F, 0.0F, 0.0F, 0.5F, 0.5F, 1.0F), addBlockType(118, "Blk Mrbl Pillar", MaterialType.MARBLE, 0, 20, 0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 1.0F), addBlockType(119, "Blk Mrbl Pillar", MaterialType.MARBLE, 0, 20, 0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F), addBlockType(122, "Blk Mrbl Beam Bottom", MaterialType.MARBLE, 0, 20, 0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 0.5F), addBlockType(123, "Blk Mrbl Beam Bottom", MaterialType.MARBLE, 0, 20, 0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F), addBlockType(121, "Blk Mrbl Beam Bottom", MaterialType.MARBLE, 0, 20, 0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 0.5F), addBlockType(120, "Blk Mrbl Beam Bottom", MaterialType.MARBLE, 0, 20, 0.0F, 0.0F, 0.0F, 0.5F, 1.0F, 0.5F), addBlockType(126, "Blk Mrbl Beam Top", MaterialType.MARBLE, 0, 20, 0.0F, 0.0F, 0.5F, 1.0F, 0.5F, 1.0F), addBlockType(127, "Blk Mrbl Beam Top", MaterialType.MARBLE, 0, 20, 0.0F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F), addBlockType(125, "Blk Mrbl Beam Top", MaterialType.MARBLE, 0, 20, 0.5F, 0.0F, 0.5F, 1.0F, 1.0F, 1.0F), addBlockType(124, "Blk Mrbl Beam Top", MaterialType.MARBLE, 0, 20, 0.0F, 0.0F, 0.5F, 0.5F, 1.0F, 1.0F), addType(159, "Blk Mrbl Angle", MaterialType.MARBLE, 0, new AngleFactory(2, 0, 20, 120)), addType(156, "Blk Mrbl Angle", MaterialType.MARBLE, 0, new AngleFactory(0, 3, 20, 120)), addType(157, "Blk Mrbl Angle", MaterialType.MARBLE, 0, new AngleFactory(3, 1, 20, 120)), addType(158, "Blk Mrbl Angle", MaterialType.MARBLE, 0, new AngleFactory(1, 2, 20, 120)), addType(163, "Blk Marble Column", MaterialType.MARBLE, 0, new CylinderFactory(120, 120, 0.5F)), addType(167, "Blk Marble Post", MaterialType.MARBLE, 0, new CylinderFactory(120, 120, 0.25F)), addType(246, "Blk Marble Cone", MaterialType.MARBLE, 0, new ConeFactory(120, 120, 4, 1.2F, 0.5F)), addType(247, "Blk Marble Cone", MaterialType.MARBLE, 0, new ConeFactory(120, 120, 5, 1.2F, 0.5F)), addType(248, "Blk Marble Spike", MaterialType.MARBLE, 0, new ConeFactory(120, 120, 4, 1.1F, 0.25F)), addType(249, "Blk Marble Spike", MaterialType.MARBLE, 0, new ConeFactory(120, 120, 5, 1.1F, 0.25F))});
        addGroup("Blackk Marble Slopes", new BlockType[]{addRampType(128, "Blk Mrbl Slope", MaterialType.MARBLE, 0, 0, 5, 20, 120, 20), addRampType(129, "Blk Mrbl Slope", MaterialType.MARBLE, 0, 1, 5, 20, 120, 20), addRampType(130, "Blk Mrbl Slope", MaterialType.MARBLE, 0, 2, 5, 20, 120, 20), addRampType(131, "Blk Mrbl Slope", MaterialType.MARBLE, 0, 3, 5, 20, 120, 20), addRampType(132, "Blk Mrbl Slope Bottom", MaterialType.MARBLE, 0, 0, 4, 20, 20, 120), addRampType(133, "Blk Mrbl Slope Bottom", MaterialType.MARBLE, 0, 1, 4, 20, 20, 120), addRampType(134, "Blk Mrbl Slope Bottom", MaterialType.MARBLE, 0, 2, 4, 20, 20, 120), addRampType(135, "Blk Mrbl Slope Bottom", MaterialType.MARBLE, 0, 3, 4, 20, 20, 120)});
        addGroup("White Marble", new BlockType[]{addBlockType(174, "White Marble", MaterialType.MARBLE, 0, 27), addBlockType(175, "Wht Mrbl Slab Top", MaterialType.MARBLE, 0, 27, 0.0F, 0.0F, 0.5F, 1.0F, 1.0F, 1.0F), addBlockType(176, "Wht Mrbl Slab Bottom", MaterialType.MARBLE, 0, 27, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.5F), addBlockType(179, "Wht Mrbl Wall", MaterialType.MARBLE, 0, 27, 0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F), addBlockType(180, "Wht Mrbl Wall", MaterialType.MARBLE, 0, 27, 0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F), addBlockType(178, "Wht Mrbl Wall", MaterialType.MARBLE, 0, 27, 0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F), addBlockType(177, "Wht Mrbl Wall", MaterialType.MARBLE, 0, 27, 0.0F, 0.0F, 0.0F, 0.5F, 1.0F, 1.0F), addBlockType(182, "Wht Mrbl Pillar", MaterialType.MARBLE, 0, 27, 0.5F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F), addBlockType(181, "Wht Mrbl Pillar", MaterialType.MARBLE, 0, 27, 0.0F, 0.0F, 0.0F, 0.5F, 0.5F, 1.0F), addBlockType(183, "Wht Mrbl Pillar", MaterialType.MARBLE, 0, 27, 0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 1.0F), addBlockType(184, "Wht Mrbl Pillar", MaterialType.MARBLE, 0, 27, 0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F), addBlockType(187, "Wht Mrbl Beam Bottom", MaterialType.MARBLE, 0, 27, 0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 0.5F), addBlockType(188, "Wht Mrbl Beam Bottom", MaterialType.MARBLE, 0, 27, 0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F), addBlockType(186, "Wht Mrbl Beam Bottom", MaterialType.MARBLE, 0, 27, 0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 0.5F), addBlockType(185, "Wht Mrbl Beam Bottom", MaterialType.MARBLE, 0, 27, 0.0F, 0.0F, 0.0F, 0.5F, 1.0F, 0.5F), addBlockType(191, "Wht Mrbl Beam Top", MaterialType.MARBLE, 0, 27, 0.0F, 0.0F, 0.5F, 1.0F, 0.5F, 1.0F), addBlockType(192, "Wht Mrbl Beam Top", MaterialType.MARBLE, 0, 27, 0.0F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F), addBlockType(190, "Wht Mrbl Beam Top", MaterialType.MARBLE, 0, 27, 0.5F, 0.0F, 0.5F, 1.0F, 1.0F, 1.0F), addBlockType(189, "Wht Mrbl Beam Top", MaterialType.MARBLE, 0, 27, 0.0F, 0.0F, 0.5F, 0.5F, 1.0F, 1.0F), addType(204, "Wht Mrbl Angle", MaterialType.MARBLE, 0, new AngleFactory(2, 0, 27, 127)), addType(201, "Wht Mrbl Angle", MaterialType.MARBLE, 0, new AngleFactory(0, 3, 27, 127)), addType(202, "Wht Mrbl Angle", MaterialType.MARBLE, 0, new AngleFactory(3, 1, 27, 127)), addType(203, "Wht Mrbl Angle", MaterialType.MARBLE, 0, new AngleFactory(1, 2, 27, 127)), addType(205, "Wht Marble Column", MaterialType.MARBLE, 0, new CylinderFactory(127, 127, 0.5F)), addType(206, "Wht Marble Post", MaterialType.MARBLE, 0, new CylinderFactory(127, 127, 0.25F)), addType(250, "Wht Marble Cone", MaterialType.MARBLE, 0, new ConeFactory(127, 127, 4, 1.2F, 0.5F)), addType(251, "Wht Marble Cone", MaterialType.MARBLE, 0, new ConeFactory(127, 127, 5, 1.2F, 0.5F)), addType(252, "Wht Marble Spike", MaterialType.MARBLE, 0, new ConeFactory(127, 127, 4, 1.1F, 0.25F)), addType(253, "Wht Marble Spike", MaterialType.MARBLE, 0, new ConeFactory(127, 127, 5, 1.1F, 0.25F))});
        addGroup("White Marble Slopes", new BlockType[]{addRampType(193, "Wht Mrbl Slope", MaterialType.MARBLE, 0, 0, 5, 27, 127, 27), addRampType(194, "Wht Mrbl Slope", MaterialType.MARBLE, 0, 1, 5, 27, 127, 27), addRampType(195, "Wht Mrbl Slope", MaterialType.MARBLE, 0, 2, 5, 27, 127, 27), addRampType(196, "Wht Mrbl Slope", MaterialType.MARBLE, 0, 3, 5, 27, 127, 27), addRampType(197, "Wht Mrbl Slope Bottom", MaterialType.MARBLE, 0, 0, 4, 27, 27, 127), addRampType(198, "Wht Mrbl Slope Bottom", MaterialType.MARBLE, 0, 1, 4, 27, 27, 127), addRampType(199, "Wht Mrbl Slope Bottom", MaterialType.MARBLE, 0, 2, 4, 27, 27, 127), addRampType(200, "Wht Mrbl Slope Bottom", MaterialType.MARBLE, 0, 3, 4, 27, 27, 127)});
        addGroup("Minerals", new BlockType[]{addBlockType(41, "Mineral Vein", MaterialType.STONE, 0, 19)});
        addGroup("Lights", new BlockType[]{addType(29, "Magic Light", MaterialType.EMPTY, -1, new PartialCubeFactory(new Vector3f(0.4F, 0.4F, 0.4F), new Vector3f(0.6F, 0.6F, 0.6F), false, true, null, 12, 12, 12, new int[0])), addType(214, "Small Flame", MaterialType.FIRE, -1, new FlameFactory(31, 0.5F)), addType(211, "Large Flame", MaterialType.FIRE, -1, new FlameFactory(31)), addType(212, "Fire", MaterialType.FIRE, -1, new FlameFactory(31, 3)), addType(213, "Inferno", MaterialType.FIRE, -1, new FlameFactory(31, 5))});

        System.out.println("Running block scripts...");
        long start = System.nanoTime();
        BlockScripts.initialize();
        long end = System.nanoTime();
        System.out.println("Block scripts run in:" + (end - start / 1000000.0D) + " ms");

        start = System.nanoTime();
        BlockTransforms.initialize();
        end = System.nanoTime();
        System.out.println("Block transforms initialized in:" + (end - start / 1000000.0D) + " ms");

        defaultTypes.put(MaterialType.STONE, types[4]);
        defaultTypes.put(MaterialType.WOOD, types[32]);
    }

    public static void setFloraQualityLow(boolean uglyGrass) {
        set(232, null);
        set(233, null);
        set(234, null);
        set(235, null);
        set(236, null);
        set(237, null);

        if (uglyGrass) {
            addBlockType(232, "Wild Flowers", MaterialType.FLORA, 0, 2, 0.1F, 0.1F, 0.0F, 0.9F, 0.9F, 0.1F);
            addBlockType(233, "Flower 1", MaterialType.FLORA, 0, 2, 0.1F, 0.1F, 0.0F, 0.9F, 0.9F, 0.1F);
            addBlockType(234, "Flower 2", MaterialType.FLORA, 0, 2, 0.1F, 0.1F, 0.0F, 0.9F, 0.9F, 0.1F);
            addBlockType(235, "Flower 3", MaterialType.FLORA, 0, 2, 0.1F, 0.1F, 0.0F, 0.9F, 0.9F, 0.1F);
            addBlockType(236, "Brush Short", MaterialType.FLORA, 0, 2, 0.1F, 0.1F, 0.0F, 0.9F, 0.9F, 0.1F);
            addBlockType(237, "Brush Tall", MaterialType.FLORA, 0, 2, 0.1F, 0.1F, 0.0F, 0.9F, 0.9F, 0.2F);
        } else {
            addType(232, "Wild Flowers", MaterialType.FLORA, -1, new StaticParticleFactory(33, new Vector3f(0.0F, 0.0F, 0.2F), new Vector3f(1.0F, 1.0F, 0.5F), types[84].getGeomFactory(), new int[]{2, 3, 3, 3}).setHeight(0, 0.25F));
            addType(233, "Flower 1", MaterialType.FLORA, -1, new StaticParticleFactory(33, new Vector3f(0.0F, 0.0F, 0.2F), new Vector3f(1.0F, 1.0F, 0.5F), types[84].getGeomFactory(), new int[]{3, 5, 0, 0}).setHeight(0, 0.25F));
            addType(234, "Flower 2", MaterialType.FLORA, -1, new StaticParticleFactory(33, new Vector3f(0.0F, 0.0F, 0.2F), new Vector3f(1.0F, 1.0F, 0.5F), types[84].getGeomFactory(), new int[]{3, 0, 5, 0}).setHeight(0, 0.25F));
            addType(235, "Flower 3", MaterialType.FLORA, -1, new StaticParticleFactory(33, new Vector3f(0.0F, 0.0F, 0.2F), new Vector3f(1.0F, 1.0F, 0.5F), types[84].getGeomFactory(), new int[]{3, 0, 0, 5}).setHeight(0, 0.25F));
            addType(236, "Brush Short", MaterialType.FLORA, -1, new StaticParticleFactory(33, new Vector3f(0.0F, 0.0F, 0.1F), new Vector3f(1.0F, 1.0F, 0.25F), types[84].getGeomFactory(), new int[]{8, 0, 0, 0}));
            addType(237, "Brush Tall", MaterialType.FLORA, -1, new StaticParticleFactory(33, new Vector3f(0.0F, 0.0F, 0.1F), new Vector3f(1.0F, 1.0F, 0.5F), types[82].getGeomFactory(), new int[]{10, 0, 0, 0}));
        }
    }

    public static void setGrassQuality(int level) {
        set(82, null);
        set(83, null);
        set(84, null);

        if (level == 0) {
            addBlockType(82, "Tall Grass", MaterialType.FLORA, 0, 2, 0.1F, 0.1F, 0.0F, 0.9F, 0.9F, 0.3F);
            addBlockType(83, "Medium Grass", MaterialType.FLORA, 0, 2, 0.1F, 0.1F, 0.0F, 0.9F, 0.9F, 0.2F);
            addBlockType(84, "Short Grass", MaterialType.FLORA, 0, 2, 0.1F, 0.1F, 0.0F, 0.9F, 0.9F, 0.1F);
        } else if (level == 1) {
            addGrassBlockType(82, "Tall Grass", MaterialType.FLORA, 0, 0.99F, 102);
            addGrassBlockType(83, "Medium Grass", MaterialType.FLORA, 0, 0.75F, 102);
            addGrassBlockType(84, "Short Grass", MaterialType.FLORA, 0, 0.5F, 102);
        } else if (level == 2) {
            addGrassBlockType(82, "Tall Grass", MaterialType.FLORA, 0, 0.99F, 500);
            addGrassBlockType(83, "Medium Grass", MaterialType.FLORA, 0, 0.75F, 500);
            addGrassBlockType(84, "Short Grass", MaterialType.FLORA, 0, 0.5F, 500);
        } else {
            addGrassBlockType(82, "Tall Grass", MaterialType.FLORA, 0, 0.99F, 22);
            addGrassBlockType(83, "Medium Grass", MaterialType.FLORA, 0, 0.75F, 22);
            addGrassBlockType(84, "Short Grass", MaterialType.FLORA, 0, 0.5F, 22);
        }
    }

    public static void setTreeQualityLow(boolean uglyTrees) {
        set(160, null);
        set(173, null);

        set(223, null);
        set(227, null);
        set(224, null);
        set(228, null);
        set(225, null);
        set(229, null);
        set(226, null);
        set(230, null);
        set(231, null);

        if (uglyTrees) {
            addBlockType(160, "Leaves", MaterialType.LEAVES, 2, 9);
            addBlockType(173, "Leaves", MaterialType.LEAVES, 2, 9);

            addBlockType(223, "Leaves", MaterialType.LEAVES, 2, 9);
            addBlockType(227, "Leaves", MaterialType.LEAVES, 2, 9);
            addBlockType(224, "Leaves", MaterialType.LEAVES, 2, 9);
            addBlockType(228, "Leaves", MaterialType.LEAVES, 2, 9);
            addBlockType(225, "Leaves", MaterialType.LEAVES, 2, 9);
            addBlockType(229, "Leaves", MaterialType.LEAVES, 2, 9);
            addBlockType(226, "Leaves", MaterialType.LEAVES, 2, 9);
            addBlockType(230, "Leaves", MaterialType.LEAVES, 2, 9);
            addBlockType(231, "Leaves", MaterialType.LEAVES, 2, 9);
        } else {
            addType(160, "Leaves", MaterialType.LEAVES, 3, new TreeLeafFactory(25));
            addType(173, "Leaves 2", MaterialType.LEAVES, 3, new TreeLeafFactory(26));

            addType(223, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, 0.0F));
            addType(227, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, 0.7853982F));
            addType(224, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, 1.570796F));
            addType(228, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, 2.356195F));
            addType(225, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, 3.141593F));
            addType(229, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, 3.926991F));
            addType(226, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, 4.712389F));
            addType(230, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, 5.497787F));
            addType(231, "Pine Sapling", MaterialType.LEAVES, 4, new PineTopFactory(108, 32));
        }
    }

    public static class BlockGroup {
        private String name;
        private List<BlockType> types = new ArrayList();

        public BlockGroup(String name, BlockType[] types) {
            this.name = name;
            this.types.addAll(Arrays.asList(types));
        }

        public BlockGroup(String name, List<BlockType> types) {
            this.name = name;
            this.types.addAll(types);
        }

        public void reset(int id, BlockType type) {
            for (int i = 0; i < this.types.size(); i++) {
                if (((BlockType) this.types.get(i)).getId() == id)
                    this.types.set(i, type);
            }
        }

        public void update(List<BlockType> newTypes) {
            this.types.clear();
            this.types.addAll(newTypes);
        }

        public String getName() {
            return this.name;
        }

        public List<BlockType> getTypes() {
            return this.types;
        }

        public String toString() {
            return "BlockGrou[" + this.name + "]";
        }
    }
}