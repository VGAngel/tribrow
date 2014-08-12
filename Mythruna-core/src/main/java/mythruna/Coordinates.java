package mythruna;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import mythruna.mathd.Vec3d;

public class Coordinates {
    public static final int LEAF_SIZE = 32;
    public static final int NODE_SIZE = 1024;
    public static final int MAX_ELEVATION = 159;
    public static final int MAX_K = 5;

    public Coordinates() {
    }

    public static Quaternion flipAxes(Quaternion q) {
        return q;
    }

    public static long worldToNodeId(int wx, int wy) {
        long x = worldToNode(wx) & 0xFFFFFFFF;
        long y = worldToNode(wy) & 0xFFFFFFFF;

        return x << 32 | y;
    }

    public static long worldToNodeId(float wx, float wy) {
        long x = worldToNode(worldToCell(wx)) & 0xFFFFFFFF;
        long y = worldToNode(worldToCell(wy)) & 0xFFFFFFFF;

        return x << 32 | y;
    }

    public static long leafToColumnId(int lx, int ly) {
        long x = lx & 0xFFFFFFFF;
        long y = ly & 0xFFFFFFFF;

        return x << 32 | y;
    }

    public static long worldToColumnId(Vector3f world) {
        long x = worldToLeaf(world.x) & 0xFFFFFFFF;
        long y = worldToLeaf(world.y) & 0xFFFFFFFF;

        return x << 32 | y;
    }

    public static long worldToColumnId(float wx, float wy) {
        long x = worldToLeaf(wx) & 0xFFFFFFFF;
        long y = worldToLeaf(wy) & 0xFFFFFFFF;

        return x << 32 | y;
    }

    public static long worldToColumnId(double wx, double wy) {
        long x = worldToLeaf(wx) & 0xFFFFFFFF;
        long y = worldToLeaf(wy) & 0xFFFFFFFF;

        return x << 32 | y;
    }

    public static int worldToCell(float f) {
        return (int) Math.floor(f);
    }

    public static int worldToCell(double d) {
        return (int) Math.floor(d);
    }

    public static Vector3i worldToCell(Vec3d world) {
        return new Vector3i(worldToCell(world.x), worldToCell(world.y), worldToCell(world.z));
    }

    public static Vector3i worldToCell(Vector3f world) {
        return new Vector3i(worldToCell(world.x), worldToCell(world.y), worldToCell(world.z));
    }

    public static Vector3i physToCell(Vec3d world) {
        return new Vector3i(worldToCell(world.x), worldToCell(world.z), worldToCell(world.y));
    }

    public static int worldToLeaf(double d) {
        return worldToLeaf((float) d);
    }

    public static int worldToLeaf(float f) {
        return worldToLeaf(worldToCell(f));
    }

    public static int leafToWorld(int i) {
        return i * 32;
    }

    public static Vector3i leafToWorld(Vector3i leaf) {
        return new Vector3i(leafToWorld(leaf.x), leafToWorld(leaf.y), leafToWorld(leaf.z));
    }

    public static Vector3i worldToLeaf(Vector3f world) {
        return new Vector3i(worldToLeaf(world.x), worldToLeaf(world.y), worldToLeaf(world.z));
    }

    public static Vector3i worldToLeaf(Vector3i world) {
        return new Vector3i(worldToLeaf(world.x), worldToLeaf(world.y), worldToLeaf(world.z));
    }

    public static int worldToLeaf(int i) {
        if (i < 0) {
            i = (i + 1) / 32;
            return i - 1;
        }

        return i / 32;
    }

    public static int worldToNode(int i) {
        if (i < 0) {
            i = (i + 1) / 1024;
            return i - 1;
        }

        return i / 1024;
    }

    public static int nodeToWorld(int i) {
        return i * 1024;
    }

    public static void main(String[] args) {
        System.out.println("512, 512 = " + worldToColumnId(512.0F, 512.0F));
        System.out.println("-512, 512 = " + worldToColumnId(-512.0F, 512.0F));
        System.out.println("512, -512 = " + worldToColumnId(512.0F, -512.0F));
        System.out.println("-512, -512 = " + worldToColumnId(-512.0F, -512.0F));

        System.out.println("1024, 1024 = " + worldToColumnId(1024.0F, 1024.0F));
        System.out.println("-1024, 1024 = " + worldToColumnId(-1024.0F, 1024.0F));
        System.out.println("1024, -1024 = " + worldToColumnId(1024.0F, -1024.0F));
        System.out.println("-1024, -1024 = " + worldToColumnId(-1024.0F, -1024.0F));

        System.out.println("-1, -1 = " + worldToColumnId(-1.0F, -1.0F));
    }
}