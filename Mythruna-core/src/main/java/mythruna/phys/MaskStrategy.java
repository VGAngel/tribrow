package mythruna.phys;

import mythruna.BlockType;

public abstract interface MaskStrategy {

    public abstract boolean getMasks(int paramInt1, int paramInt2, BlockType paramBlockType, int[] paramArrayOfInt);
}