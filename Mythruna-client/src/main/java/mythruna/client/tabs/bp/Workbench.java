package mythruna.client.tabs.bp;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import mythruna.geom.CubeFactory;
import mythruna.geom.GeomFactory;
import mythruna.geom.GeomPartBuffer;

public class Workbench {
    private int size;
    private int borderSize = 2;
    private int totalSize;
    private int wallType = 10;
    private int floorType = 10;
    private int borderType = 11;

    private GeomFactory wallFactory = new CubeFactory(this.wallType);
    private GeomFactory borderFactory = new CubeFactory(this.borderType);

    public Workbench(int size) {
        this.size = size;
        this.totalSize = (size + this.borderSize * 2);
    }

    public int getSize() {
        return this.size;
    }

    public boolean contains(Vector3f cell) {
        if ((cell.x < 0.0F) || (cell.y < 0.0F) || (cell.z < 0.0F))
            return false;
        if ((cell.x >= this.size) || (cell.y >= this.size) || (cell.z >= this.size))
            return false;
        return true;
    }

    public Vector3f cellToModel(Vector3f cell) {
        Vector3f result = new Vector3f();
        cell.x -= this.size / 2.0F;
        result.z = (cell.y - this.size / 2.0F);
        result.y = cell.z;

        return result;
    }

    public GeomPartBuffer generateParts(float sun, float localLight) {
        GeomPartBuffer parts = new GeomPartBuffer();

        for (int i = 0; i < this.totalSize; i++) {
            for (int j = 0; j < this.totalSize; j++) {
                if ((i < this.borderSize) || (i >= this.totalSize - this.borderSize) || (j < this.borderSize) || (j >= this.totalSize - this.borderSize)) {
                    this.borderFactory.createGeometry(parts, i, j, -1, i, j, -1, sun, localLight, null, 4);
                } else {
                    this.wallFactory.createGeometry(parts, i, j, -1, i, j, -1, sun, localLight, null, 4);

                    this.wallFactory.createGeometry(parts, i, j, this.size, i, j, this.size, sun, localLight, null, 5);
                }
            }
        }

        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                this.wallFactory.createGeometry(parts, i + this.borderSize, 1, j, i + this.borderSize, 1, j, sun, localLight, null, 1);

                this.wallFactory.createGeometry(parts, i + this.borderSize, this.size + this.borderSize, j, i + this.borderSize, this.size + this.borderSize, j, sun, localLight, null, 0);
            }

        }

        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                this.wallFactory.createGeometry(parts, 1, i + this.borderSize, j, 1, i + this.borderSize, j, sun, localLight, null, 2);

                this.wallFactory.createGeometry(parts, this.size + this.borderSize, i + this.borderSize, j, this.size + this.borderSize, i + this.borderSize, j, sun, localLight, null, 3);
            }

        }

        return parts;
    }

    protected Node generateNode(float sun, float localLight) {
        GeomPartBuffer parts = generateParts(sun, localLight);
        Node node = parts.createNode(String.valueOf(this), -this.totalSize / 2.0F, -this.totalSize / 2.0F, 0.0F);

        return node;
    }
}