package mythruna.db.building;

import mythruna.Vector3i;
import mythruna.db.CellAccess;

public class DefaultBuilding implements Building {

    private Vector3i size = new Vector3i();
    private Vector3i arraySize;
    private Part[][][] parts;
    private int[] colSizes;
    private int[] rowSizes;
    private Vector3i min;
    private Vector3i max;

    public DefaultBuilding(int xSize, int ySize, int zSize) {
        this.arraySize = new Vector3i(xSize, ySize, zSize);
        this.parts = new Part[xSize][ySize][zSize];
    }

    public void setPart(int x, int y, int z, Part p) {
        this.parts[x][y][z] = p;
        this.size = null;
    }

    public Part getPart(int x, int y, int z) {
        return this.parts[x][y][z];
    }

    public void rotate(int r) {
        if (r == 0) {
            return;
        }

        int xCardinal = (1 + r) % 4;
        int yCardinal = (xCardinal + 1) % 4;

        int xDir = mythruna.Direction.CARDINAL_TO_DIR[xCardinal];
        int yDir = mythruna.Direction.CARDINAL_TO_DIR[yCardinal];

        Vector3i xVec = mythruna.Direction.VECS[xDir];
        Vector3i yVec = mythruna.Direction.VECS[yDir];

        Vector3i newArraySize = this.arraySize.clone();
        if ((xDir == 0) || (xDir == 1)) {
            newArraySize.x = this.arraySize.y;
            newArraySize.y = this.arraySize.x;
        }

        System.out.println("Original size:" + this.arraySize + "  new Size:" + newArraySize);

        Part[][][] newParts = new Part[newArraySize.x][newArraySize.y][newArraySize.z];

        Vector3i xPos = new Vector3i(0, 0, 0);

        if (xVec.x < 0)
            xPos.x += newArraySize.x - 1;
        if (xVec.y < 0)
            xPos.y += newArraySize.y - 1;
        if (yVec.x < 0)
            xPos.x += newArraySize.x - 1;
        if (yVec.y < 0) {
            xPos.y += newArraySize.y - 1;
        }
        for (int i = 0; i < this.arraySize.x; ) {
            Vector3i yPos = xPos.clone();

            for (int j = 0; j < this.arraySize.y; ) {
                System.out.println("source[" + i + "][" + j + "]  becomes:" + yPos);
                for (int k = 0; k < this.arraySize.z; k++) {
                    newParts[yPos.x][yPos.y][k] = this.parts[i][j][k];
                    this.parts[i][j][k].rotate(r);
                }
                j++;
                yPos.addLocal(yVec);
            }
            i++;
            xPos.addLocal(xVec);
        }

        this.parts = newParts;
        this.arraySize = newArraySize;
        calculateSize();
    }

    protected void calculateSize() {
        this.min = new Vector3i(64, 64, 64);
        this.max = new Vector3i(0, 0, 0);

        this.colSizes = new int[this.arraySize.x];
        this.rowSizes = new int[this.arraySize.y];

        for (int i = 0; i < this.arraySize.x; i++) {
            for (int j = 0; j < this.arraySize.y; j++) {
                for (int k = 0; k < this.arraySize.z; k++) {
                    if (this.parts[i][j][k] != null) {
                        Part p = this.parts[i][j][k];

                        Vector3i pMin = p.getMin();
                        Vector3i pMax = p.getMax();
                        Vector3i offset = p.getOffset();
                        System.out.println("[" + i + "][" + j + "][" + k + "] offset:" + offset);

                        this.colSizes[i] = Math.max(this.colSizes[i], pMax.x - pMin.x);
                        this.rowSizes[j] = Math.max(this.rowSizes[j], pMax.y - pMin.y);

                        this.min.minLocal(pMin);
                        this.max.maxLocal(pMax);
                    }
                }
            }
        }
        this.size = new Vector3i(this.max.x - this.min.x, this.max.y - this.min.x, this.max.z - this.min.z);
    }

    public Vector3i getSize() {
        if (this.size == null)
            calculateSize();
        return this.size;
    }

    public void place(Vector3i pos, CellAccess to) {
        getSize();

        System.out.println("Building size:" + this.size + "  min:" + this.min + "  max:" + this.max);

        int cellWidth = 8;
        int cellHeight = 8;

        System.out.println("Column sizes:");
        for (int c : this.colSizes)
            System.out.print("[" + c + "]");
        System.out.println();

        System.out.println("Row sizes:");
        for (int c : this.rowSizes)
            System.out.print("[" + c + "]");
        System.out.println();

        Vector3i v = new Vector3i();
        for (int k = 0; k < this.arraySize.z; k++) {
            v.x = pos.x;
            for (int i = 0; i < this.arraySize.x; ) {
                v.y = pos.y;
                for (int j = 0; j < this.arraySize.y; ) {
                    System.out.println("[" + i + "][" + j + "]  pos:" + v);
                    if (this.parts[i][j][k] != null) {
                        Part p = this.parts[i][j][k];

                        p.place(v, this.colSizes[i], this.rowSizes[j], this.size.z, to);
                    }
                    v.y += this.rowSizes[j];
                    j++;
                }
                v.x += this.colSizes[i];
                i++;
            }

            v.z += 3;
        }
    }
}