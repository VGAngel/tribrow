package mythruna.db.building;

import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.BlockTypeIndex;
import mythruna.BoundaryShape;
import mythruna.MaterialType;
import mythruna.geom.*;

import java.util.*;

public class BlockTransforms {
    public static int MIRROR_X = 1;

    public static int MIRROR_Y = 2;

    private static Map<String, BlockGroup> groups = new HashMap();
    private static Map<Integer, BlockGroup> transforms = new HashMap();

    public BlockTransforms() {
    }

    public static void initialize() {
        addType("Wood Planks Block", BlockTypeIndex.types[32]);
        addType("Wood Planks-90 Block", BlockTypeIndex.types[87]);
        addType("Wood Planks Top", BlockTypeIndex.types[31]);
        addType("Wood Planks Top-90", BlockTypeIndex.types[88]);
        addType("Wood Planks Bottom", BlockTypeIndex.types[12]);
        addType("Wood Planks Bottom-90", BlockTypeIndex.types[89]);

        for (BlockType t : BlockTypeIndex.types) {
            addType(t);
        }

        for (Map.Entry e : groups.entrySet()) {
            ((BlockGroup) e.getValue()).compile();
        }
    }

    private static float epsilon(float f) {
        long i = Math.round(f * 10000.0F);
        f = (float) i / 10000.0F;
        return f;
    }

    protected static String getGroup(BlockType type) {
        MaterialType mat = type.getMaterial();
        if (mat == MaterialType.FLORA)
            return new StringBuilder().append("Flora:").append(type.getId()).toString();
        if (mat == MaterialType.FIRE) {
            return new StringBuilder().append("Fire:").append(type.getId()).toString();
        }
        GeomFactory geom = type.getGeomFactory();

        String group = mat.getName();

        if (mat == MaterialType.MARBLE) {
            if ((type.getName().startsWith("Blk")) || (type.getName().startsWith("Black")))
                group = new StringBuilder().append(group).append("-blk").toString();
            else if ((type.getName().startsWith("Wht")) || (type.getName().startsWith("White")))
                group = new StringBuilder().append(group).append("-wht").toString();
            else
                throw new RuntimeException(new StringBuilder().append("Unknown marble sub-type:").append(type).toString());
        } else if (mat == MaterialType.ROCK) {
            if (type.getName().indexOf("Capped") >= 0)
                group = new StringBuilder().append(group).append("-capped").toString();
        } else if (mat == MaterialType.STONE) {
            if (type.getName().indexOf("Mineral") >= 0)
                group = new StringBuilder().append(group).append("-mineral").toString();
        } else if (mat == MaterialType.LEAVES) {
            if (type.getName().indexOf("2") >= 0)
                group = new StringBuilder().append(group).append("-dark").toString();
        } else if (mat == MaterialType.WOOD) {
            if (type.getName().indexOf("Trunk") >= 0)
                group = new StringBuilder().append(group).append("-trunk").toString();
            else if (type.getName().indexOf("Log") >= 0) {
                group = new StringBuilder().append(group).append("-log").toString();
            }
        }
        if ((geom instanceof WedgeFactory)) {
            WedgeFactory wf = (WedgeFactory) geom;
            group = new StringBuilder().append(group).append(":wedge").append(wf.isFacingUp() ? "" : "-up").toString();
        } else if ((geom instanceof ThatchFactory)) {
            ThatchFactory f = (ThatchFactory) geom;
            group = new StringBuilder().append(group).append(":thatch").toString();
        } else if ((geom instanceof AngleFactory)) {
            AngleFactory af = (AngleFactory) geom;
            group = new StringBuilder().append(group).append(":angle").toString();
        } else if ((geom instanceof CylinderFactory)) {
            CylinderFactory f = (CylinderFactory) geom;
            group = new StringBuilder().append(group).append(":cylinder-").append(epsilon(f.getRadius())).toString();
        } else if ((geom instanceof CylinderFactory2)) {
            CylinderFactory2 f = (CylinderFactory2) geom;
            if (f.getDir() < 0) {
                group = new StringBuilder().append(group).append(":cylinder-horz-").append(epsilon(f.getRadius())).toString();
            } else {
                group = new StringBuilder().append(group).append(":cylinder2").toString();
            }

            Vector3f offset = f.getOffset();
            if ((offset.x != 0.0F) || (offset.y != 0.0F) || (offset.z != 0.0F)) {
                group = new StringBuilder().append(group).append("[").append(offset).append("]").toString();
            }
        } else if ((geom instanceof ConeFactory)) {
            ConeFactory f = (ConeFactory) geom;
            group = new StringBuilder().append(group).append(":cone-").append(epsilon(f.getRadius())).append(":").append(epsilon(f.getHeight())).toString();
            if (f.getDirection() == 4)
                group = new StringBuilder().append(group).append("-up").toString();
        } else if ((geom instanceof PineBranchFactory)) {
            PineBranchFactory f = (PineBranchFactory) geom;
            group = new StringBuilder().append(group).append(":pine-branch").toString();

            if (Math.abs(epsilon(f.getYaw()) % epsilon(1.570796F)) > 0.001D)
                group = new StringBuilder().append(group).append("-45").toString();
        } else if ((geom instanceof ThatchCornerFactory)) {
            ThatchCornerFactory f = (ThatchCornerFactory) geom;
            group = new StringBuilder().append(group).append(":thatch-corner").toString();
        } else if ((geom instanceof DefaultGeomFactory)) {
            DefaultGeomFactory f = (DefaultGeomFactory) geom;
            if (f.getCollider() != null) {
                group = new StringBuilder().append(group).append(":").append(f.getCollider().getName()).toString();
            } else {
                group = new StringBuilder().append(group).append(":default").toString();
            }

            Vector3f min = geom.getMin();
            Vector3f max = geom.getMax();
            float volume = (float) geom.getMassPortion();

            group = new StringBuilder().append(group).append("-").append(epsilon(volume)).append("[").append(epsilon(min.z)).append(":").append(epsilon(max.z)).append("]").toString();
        } else {
            Vector3f min = geom.getMin();
            Vector3f max = geom.getMax();
            float volume = (float) geom.getMassPortion();
            if ((min.x == 0.0F) && (min.y == 0.0F) && (min.z == 0.0F) && (max.x == 1.0F) && (max.y == 1.0F) && (max.z == 1.0F))
                group = new StringBuilder().append(group).append(":cube").toString();
            else {
                group = new StringBuilder().append(group).append(":box-").append(epsilon(volume)).append("[").append(epsilon(min.z)).append(":").append(epsilon(max.z)).append("]").toString();
            }
        }

        return group;
    }

    protected static void addType(BlockType t) {
        if ((t == null) || (t.getId() >= 32767)) {
            return;
        }

        if (transforms.containsKey(Integer.valueOf(t.getId()))) {
            return;
        }
        String group = getGroup(t);

        addType(group, t);
    }

    protected static void addType(String group, BlockType t) {
        BlockGroup g = getGroup(group);
        g.add(t);

        System.out.println(new StringBuilder().append("type:").append(t).append("  group:").append(group).toString());

        transforms.put(Integer.valueOf(t.getId()), g);
    }

    protected static BlockGroup getGroup(String group) {
        BlockGroup g = (BlockGroup) groups.get(group);
        if (g == null) {
            g = new BlockGroup();
            groups.put(group, g);
        }
        return g;
    }

    public static BlockType rotate(BlockType type, int amount) {
        BlockGroup g = (BlockGroup) transforms.get(Integer.valueOf(type.getId()));
        if (g == null)
            return type;
        return g.rotate(type, amount);
    }

    public static BlockType mirror(BlockType type, int mirrorFlags) {
        BlockGroup g = (BlockGroup) transforms.get(Integer.valueOf(type.getId()));
        if (g == null) {
            return type;
        }
        if ((mirrorFlags & MIRROR_Y) != 0) {
            type = g.mirror(type, 0);
            g = (BlockGroup) transforms.get(Integer.valueOf(type.getId()));
            if (g == null) {
                return type;
            }
        }
        if ((mirrorFlags & MIRROR_X) != 0) {
            type = g.mirror(type, 1);
        }

        return type;
    }

    protected static boolean isSymmetric(GeomFactory geom1, GeomFactory geom2) {
        if ((geom1 instanceof WedgeFactory))
            return true;
        if ((geom1 instanceof ThatchFactory))
            return true;
        if ((geom1 instanceof CylinderFactory2))
            return true;
        if ((geom1 instanceof PineBranchFactory)) {
            PineBranchFactory f = (PineBranchFactory) geom1;

            return Math.abs(epsilon(f.getYaw()) % epsilon(1.570796F)) <= 0.001D;
        }

        Vector3f min = geom1.getMin();
        Vector3f max = geom1.getMax();

        Vector3f center1 = min.add(max).mult(0.5F);
        center1.x = epsilon(center1.x);
        center1.y = epsilon(center1.y);

        min = geom2.getMin();
        max = geom2.getMax();

        Vector3f center2 = min.add(max).mult(0.5F);
        center2.x = epsilon(center2.x);
        center2.y = epsilon(center2.y);

        if ((center1.x == 0.5F) && (center2.x == 0.5F)) {
            BoundaryShape east = geom1.getBoundaryShape(2);
            BoundaryShape west = geom1.getBoundaryShape(3);

            return east == west;
        }

        if ((center1.y == 0.5F) && (center2.y == 0.5F)) {
            BoundaryShape north = geom1.getBoundaryShape(0);
            BoundaryShape south = geom1.getBoundaryShape(1);

            return north == south;
        }

        return false;
    }

    protected static BlockTransform[] createSingleSymmetricTransforms(List<BlockType> list) {
        System.out.println(new StringBuilder().append("createSingleSymmetricTransforms(").append(list).append(")").toString());

        BlockType[] rotations = {(BlockType) list.get(0), (BlockType) list.get(2), (BlockType) list.get(1), (BlockType) list.get(3)};
        BlockTransform[] array = new BlockTransform[4];
        System.out.println(new StringBuilder().append("Sorted to:").append(Arrays.asList(rotations)).toString());

        array[0] = new SingleSymmetricTransform(1, rotations);
        rotations[0].transformIndex = 0;

        array[1] = new SingleSymmetricTransform(0, rotations);
        rotations[1].transformIndex = 1;

        array[2] = new SingleSymmetricTransform(1, rotations);
        rotations[2].transformIndex = 2;
        array[3] = new SingleSymmetricTransform(0, rotations);
        rotations[3].transformIndex = 3;

        return array;
    }

    protected static BlockTransform[] createRotationTransforms(List<BlockType> list) {
        Collections.sort(list, new SortOrderComparator());

        GeomFactory geom1 = ((BlockType) list.get(0)).getGeomFactory();
        GeomFactory geom2 = ((BlockType) list.get(1)).getGeomFactory();

        if (isSymmetric(geom1, geom2)) {
            return createSingleSymmetricTransforms(list);
        }

        BlockType[] rotations = {(BlockType) list.get(3), (BlockType) list.get(2), (BlockType) list.get(1), (BlockType) list.get(0)};
        BlockTransform[] array = new BlockTransform[4];

        array[0] = new RotationTransform(rotations[0], rotations[3], rotations[1], rotations);
        rotations[0].transformIndex = 0;

        array[1] = new RotationTransform(rotations[1], rotations[2], rotations[0], rotations);
        rotations[1].transformIndex = 1;

        array[2] = new RotationTransform(rotations[2], rotations[1], rotations[3], rotations);
        rotations[2].transformIndex = 2;

        array[3] = new RotationTransform(rotations[3], rotations[0], rotations[2], rotations);
        rotations[3].transformIndex = 3;

        return array;
    }

    protected static class SortOrderComparator
            implements Comparator<BlockType> {
        protected SortOrderComparator() {
        }

        public int compare(BlockType o1, BlockType o2) {
            int i1 = o1.getSortingIndex();
            int i2 = o2.getSortingIndex();

            return i1 - i2;
        }
    }

    protected static class BlockGroup {
        private List<BlockType> list = new ArrayList();
        private BlockTransforms.BlockTransform[] transforms;

        public BlockGroup() {
        }

        public BlockType mirror(BlockType type, int axis) {
            return this.transforms[type.transformIndex].mirror(axis, type);
        }

        public BlockType rotate(BlockType type, int amount) {
            return this.transforms[type.transformIndex].rotate(amount, type);
        }

        public void add(BlockType t) {
            this.list.add(t);
        }

        public void compile() {
            if (this.list.size() == 1) {
                BlockType type = (BlockType) this.list.get(0);
                type.transformIndex = 0;
                this.transforms = new BlockTransforms.BlockTransform[]{new BlockTransforms.IdentityTransform(type)};
                return;
            }

            if (this.list.size() == 2) {
                BlockType type1 = (BlockType) this.list.get(0);
                type1.transformIndex = 0;
                BlockType type2 = (BlockType) this.list.get(1);
                type2.transformIndex = 1;
                BlockTransforms.DualSymmetricTransform xform = new BlockTransforms.DualSymmetricTransform(type1, type2);
                this.transforms = new BlockTransforms.BlockTransform[]{xform, xform};
                return;
            }

            if (this.list.size() == 4) {
                this.transforms = BlockTransforms.createRotationTransforms(this.list);
                return;
            }

            throw new RuntimeException("Unable to form transformation for block types:" + this.list);
        }
    }

    protected static class RotationTransform
            implements BlockTransforms.BlockTransform {
        private BlockType root;
        private BlockType[] axes;
        private BlockType[] rotations;

        public RotationTransform(BlockType root, BlockType axis1, BlockType axis2, BlockType[] rotations) {
            this.root = root;
            this.axes = new BlockType[]{axis1, axis2};
            this.rotations = rotations;
        }

        public BlockType mirror(int axis, BlockType start) {
            if (start != this.root)
                throw new IllegalArgumentException("Using wrong transform to mirror:" + start);
            return this.axes[axis];
        }

        public BlockType rotate(int amount, BlockType start) {
            int base = start.transformIndex;
            base += amount;
            base %= 4;

            return this.rotations[base];
        }
    }

    protected static class SingleSymmetricTransform
            implements BlockTransforms.BlockTransform {
        private int symAxis;
        private BlockType[] rotations;

        public SingleSymmetricTransform(int symAxis, BlockType[] rotations) {
            this.symAxis = symAxis;
            this.rotations = rotations;
        }

        public BlockType mirror(int axis, BlockType start) {
            if (axis == this.symAxis)
                return start;
            return rotate(2, start);
        }

        public BlockType rotate(int amount, BlockType start) {
            int base = start.transformIndex;
            base += amount;
            base %= 4;

            return this.rotations[base];
        }
    }

    protected static class DualSymmetricTransform
            implements BlockTransforms.BlockTransform {
        private BlockType type1;
        private BlockType type2;

        public DualSymmetricTransform(BlockType type1, BlockType type2) {
            this.type1 = type1;
            this.type2 = type2;
        }

        public BlockType mirror(int axis, BlockType start) {
            return start;
        }

        public BlockType rotate(int amount, BlockType start) {
            if (amount % 2 == 0)
                return start;
            if (start == this.type1)
                return this.type2;
            return this.type1;
        }
    }

    protected static class IdentityTransform
            implements BlockTransforms.BlockTransform {
        private BlockType type;

        public IdentityTransform(BlockType type) {
            this.type = type;
        }

        public BlockType mirror(int axis, BlockType start) {
            return this.type;
        }

        public BlockType rotate(int amount, BlockType start) {
            return this.type;
        }
    }

    protected static abstract interface BlockTransform {
        public abstract BlockType mirror(int paramInt, BlockType paramBlockType);

        public abstract BlockType rotate(int paramInt, BlockType paramBlockType);
    }
}