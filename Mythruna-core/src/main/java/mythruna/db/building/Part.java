package mythruna.db.building;

import mythruna.Vector3i;
import mythruna.db.BlueprintData;
import mythruna.db.CellAccess;

public class Part {

    private Section section;
    private BlueprintData bp;
    private int rotation;
    private int mirrorFlags;
    private int xDir = 1;
    private int yDir = 1;
    private Vector3i offset;
    private Vector3i min;
    private Vector3i max;
    private double xAnchor;
    private double yAnchor;

    public Part(BlueprintData bp, int rotation, int mirrorFlags, Vector3i offset) {
        this(bp, rotation, mirrorFlags, offset, 0.0D, 0.0D);
    }

    public Part(BlueprintData bp, int rotation, int mirrorFlags, Vector3i offset, double xAnchor, double yAnchor) {
        this.bp = bp;
        this.rotation = rotation;
        this.mirrorFlags = mirrorFlags;
        this.offset = offset;
        this.xAnchor = xAnchor;
        this.yAnchor = yAnchor;

        setup();
    }

    private void setup() {
        int xd = 1;
        int yd = 1;
        if ((this.mirrorFlags & BlockTransforms.MIRROR_X) != 0)
            xd *= -1;
        if ((this.mirrorFlags & BlockTransforms.MIRROR_Y) != 0) {
            yd *= -1;
        }

        Vector3i v = new Vector3i();
        v.z = this.bp.zSize;

        Vector3i origin = new Vector3i(0, 0, this.offset.z);

        switch (this.rotation) {
            case 0:
                v.x = (this.bp.xSize * xd);
                v.y = (this.bp.ySize * yd);

                this.xDir = xd;
                this.yDir = yd;
                break;
            case 1:
                v.x = (this.bp.ySize * -yd);
                v.y = (this.bp.xSize * xd);

                this.xDir = (-yd);
                this.yDir = xd;
                break;
            case 2:
                v.x = (this.bp.xSize * -xd);
                v.y = (this.bp.ySize * -yd);

                this.xDir = (-xd);
                this.yDir = (-yd);
                break;
            case 3:
                v.x = (this.bp.ySize * yd);
                v.y = (this.bp.xSize * -xd);

                this.xDir = yd;
                this.yDir = (-xd);
        }

        this.min = new Vector3i();
        this.max = new Vector3i();

        this.min.minLocal(v.x, v.y, v.z);
        this.max.maxLocal(v.x, v.y, v.z);

        this.min.addLocal(origin);
        this.max.addLocal(origin);
    }

    private double reverseAnchor(double d) {
        return 1.0D - d;
    }

    public void rotate(int r) {
        if (r == 0) {
            return;
        }
        this.rotation = ((this.rotation + r) % 4);

        double xNew = this.xAnchor;
        double yNew = this.yAnchor;

        switch (r) {
            case 1:
                yNew = this.xAnchor;
                xNew = reverseAnchor(this.yAnchor);
                break;
            case 2:
                xNew = reverseAnchor(this.xAnchor);
                yNew = reverseAnchor(this.yAnchor);
                break;
            case 3:
                xNew = this.yAnchor;
                yNew = reverseAnchor(this.xAnchor);
        }

        this.xAnchor = xNew;
        this.yAnchor = yNew;

        setup();
    }

    public Vector3i getMin() {
        return this.min;
    }

    public Vector3i getMax() {
        return this.max;
    }

    public Vector3i getOffset() {
        return this.offset;
    }

    public int getSizeX() {
        return this.max.x - this.min.x;
    }

    public int getSizeY() {
        return this.max.y - this.min.y;
    }

    public int getSizeZ() {
        return this.max.z - this.min.z;
    }

    public void place(Vector3i loc, int cellWidth, int cellHeight, int maxHeight, CellAccess to) {
        Vector3i pos = new Vector3i(loc);

        loc.x += (int) (cellWidth * this.xAnchor - getSizeX() * this.xAnchor);
        loc.y += (int) (cellHeight * this.yAnchor - getSizeY() * this.yAnchor);

        BuildingUtils.placePart(pos, to, this.bp, this.rotation, this.mirrorFlags, maxHeight);
    }
}