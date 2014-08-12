// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LightType.java

package mythruna.db;

import mythruna.BlockType;
import mythruna.BlockTypeIndex;
import mythruna.Direction;

public interface LightType {

    public static class Torchlight implements LightType {

        public final boolean isBlockType(int type) {
            return type == 29 || type == 211 || type == 212 || type == 213 || type == 214;
        }

        public final int getLightIndex() {
            return 1;
        }

        public final int getMaximumValue(int type) {
            switch (type) {
                case 29: // '\035'
                    return 10;

                case 211:
                    return 7;

                case 212:
                    return 8;

                case 213:
                    return 9;

                case 214:
                    return 4;
            }
            return 10;
        }

        public int getNextLightValue(int currentLight, int direction, int blockType) {
            int nextVal = currentLight - 1;
            if (blockType != 0) {
                BlockType type = BlockTypeIndex.types[blockType];
                if (type.isSolid(direction)) {
                    int l = Math.round(type.getTransparency(Direction.DIR_AXIS[direction]) * 15F);
                    nextVal = Math.min(l, nextVal);
                }
            }
            return nextVal;
        }

        public Torchlight() {
        }
    }

    public static class Sunlight
            implements LightType {

        public final boolean isBlockType(int type) {
            return false;
        }

        public final int getLightIndex() {
            return 0;
        }

        public final int getMaximumValue(int type) {
            return 15;
        }

        public int getNextLightValue(int currentLight, int direction, int blockType) {
            int nextVal = currentLight;
            if (currentLight == 15) {
                if (direction != 5)
                    nextVal -= 4;
            } else if (direction == 4) {
                if (currentLight > 10)
                    nextVal -= 4;
                else
                    nextVal -= 3;
            } else {
                nextVal--;
            }
            if (blockType != 0) {
                BlockType type = BlockTypeIndex.types[blockType];
                if (type.isSolid(direction)) {
                    int l = Math.round(type.getTransparency(Direction.DIR_AXIS[direction]) * 15F);
                    nextVal = Math.min(l, nextVal);
                }
            }
            return nextVal;
        }

        public Sunlight() {
        }
    }


    public abstract boolean isBlockType(int i);

    public abstract int getLightIndex();

    public abstract int getMaximumValue(int i);

    public abstract int getNextLightValue(int i, int j, int k);

    public static final LightType SUNLIGHT = new Sunlight();
    public static final LightType TORCHLIGHT = new Torchlight();
    public static final int LIGHT_SUN = 0;
    public static final int LIGHT_LOCAL = 1;

}
