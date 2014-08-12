package mythruna.phys;

import mythruna.BlockType;
import mythruna.MaterialType;

public class MaskStrategies {

    public static final MaskStrategy ALL = constantAdd(16);
    public static final MaskStrategy REPTILIAN = materialFilter(16, new MaterialType[]{MaterialType.DIRT, MaterialType.GRASS, MaterialType.STONE, MaterialType.COBBLE, MaterialType.ROCK, MaterialType.WOOD, MaterialType.WADDLE, MaterialType.LEAVES});

    public static final MaskStrategy SIMIAN = materialFilter(16, new MaterialType[]{MaterialType.WOOD, MaterialType.WADDLE, MaterialType.LEAVES});

    public MaskStrategies() {
    }

    public static MaskStrategy constantAdd(int addMask) {
        return new ConstantAdd(addMask);
    }

    public static MaskStrategy materialFilter(int addMask, MaterialType[] materialTypes) {
        return new MaterialFilter(addMask, materialTypes);
    }

    private static class MaterialFilter
            implements MaskStrategy {
        private boolean[] add;
        private int addMask;

        public MaterialFilter(int addMask, MaterialType[] materialTypes) {
            this.addMask = addMask;
            this.add = new boolean[MaterialType.types.length];
            for (MaterialType t : materialTypes) {
                this.add[t.getId()] = true;
            }
        }

        public final boolean getMasks(int worldMask, int invSrcMask, BlockType worldType, int[] store) {
            if (worldType == null) {
                return false;
            }
            MaterialType matType = worldType.getMaterial();
            if (matType == null) {
                return false;
            }
            int mat = matType.getId();

            //if (this.add[mat] == 0) {
            if (this.add[mat] == false) {
                return false;
            }
            store[0] = (worldMask | this.addMask);
            store[1] = (invSrcMask | this.addMask);
            return true;
        }
    }

    private static class ConstantAdd
            implements MaskStrategy {
        private int addMask;

        public ConstantAdd(int addMask) {
            this.addMask = addMask;
        }

        public final boolean getMasks(int worldMask, int invSrcMask, BlockType worldType, int[] store) {
            store[0] = (worldMask | this.addMask);
            store[1] = (invSrcMask | this.addMask);
            return true;
        }
    }
}