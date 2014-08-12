package mythruna.client.tabs.bp;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import java.util.ArrayList;
import java.util.List;
import mythruna.BlockType;
import mythruna.Coordinates;
import mythruna.db.BlueprintData;
import mythruna.db.building.BlockTransforms;
import mythruna.geom.GeomPartBuffer;

public class BlueprintObject implements Cloneable {
    private static long instanceCount = 0L;
    private long id = instanceCount++;

    private Vector3f position = new Vector3f();

    private Quaternion rotation = new Quaternion();
    private int xSize;
    private int ySize;
    private int zSize;
    private int[][][] cells;
    private float sunlight = 0.8F;
    private float localLight = 0.8F;
    private float scale = 1.0F;
    private Vector3f offset;
    private Node node;
    private long changeCount = 0L;

    public BlueprintObject(int xSize, int ySize, int zSize)
    {
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;

        this.cells = new int[xSize][ySize][zSize];

        this.offset = new Vector3f(xSize * 0.5F, ySize * 0.5F, 0.0F);
    }

    public BlueprintObject(BlueprintData data)
    {
        this.xSize = data.xSize;
        this.ySize = data.ySize;
        this.zSize = data.zSize;
        this.scale = data.scale;

        this.cells = data.cells;
        this.offset = new Vector3f(this.xSize * 0.5F, this.ySize * 0.5F, 0.0F);
    }

    public long getId()
    {
        return this.id;
    }

    public boolean isEmpty()
    {
        for (int i = 0; i < this.xSize; i++)
        {
            for (int j = 0; j < this.ySize; j++)
            {
                for (int k = 0; k < this.zSize; k++)
                {
                    if (this.cells[i][j][k] != 0)
                        return false;
                }
            }
        }
        return true;
    }

    public boolean isChanged()
    {
        return this.changeCount > 0L;
    }

    public void resetChanged()
    {
        this.changeCount = 0L;
    }

    public void rotate()
    {
        int x = 0;
        int y = this.ySize - 1;
        int z = 0;

        int[][][] newCells = new int[this.ySize][this.xSize][this.zSize];

        for (int i = 0; i < this.xSize; )
        {
            y = this.ySize - 1;
            for (int j = 0; j < this.ySize; )
            {
                z = 0;
                for (int k = 0; k < this.zSize; )
                {
                    int v = this.cells[i][j][k];
                    if (v == 0)
                    {
                        newCells[y][x][z] = v;
                    }
                    else {
                        BlockType type = mythruna.BlockTypeIndex.types[v];
                        type = BlockTransforms.rotate(type, 1);
                        newCells[y][x][z] = type.getId();
                    }
                    k++; z++;
                }
                j++; y--;
            }
            i++; x++;
        }

        this.cells = newCells;
        this.changeCount += 1L;
    }

    public void mirror()
    {
        int x = this.xSize - 1;
        int y = 0;
        int z = 0;

        int[][][] newCells = new int[this.xSize][this.ySize][this.zSize];

        for (int i = 0; i < this.xSize; )
        {
            y = 0;
            for (int j = 0; j < this.ySize; )
            {
                z = 0;
                for (int k = 0; k < this.zSize; )
                {
                    int v = this.cells[i][j][k];
                    if (v == 0)
                    {
                        newCells[x][y][z] = v;
                    }
                    else {
                        BlockType type = mythruna.BlockTypeIndex.types[v];
                        type = BlockTransforms.mirror(type, BlockTransforms.MIRROR_X);
                        newCells[x][y][z] = type.getId();
                    }
                    k++; z++;
                }
                j++; y++;
            }
            i++; x--;
        }

        this.cells = newCells;
        this.changeCount += 1L;
    }

    public void copyData(BlueprintData data)
    {
        clear();

        int x = (this.xSize - data.xSize) / 2;
        int y = (this.ySize - data.ySize) / 2;

        for (int i = 0; i < data.xSize; i++)
        {
            for (int j = 0; j < data.ySize; j++)
            {
                for (int k = 0; k < data.zSize; k++)
                {
                    this.cells[(x + i)][(y + j)][k] = data.cells[i][j][k];
                }
            }
        }
    }

    public void resetSize(int x, int y, int z)
    {
        this.xSize = x;
        this.ySize = y;
        this.zSize = z;
        clear();
    }

    public void clear()
    {
        this.cells = new int[this.xSize][this.ySize][this.zSize];
        this.changeCount = 0L;
    }

    public int[][][] getCells()
    {
        return this.cells;
    }

    public void setOffset(Vector3f offset)
    {
        this.offset = offset;
    }

    public void pack()
    {
        int xMin = this.xSize;
        int xMax = 0;
        int yMin = this.ySize;
        int yMax = 0;
        int zMin = this.zSize;
        int zMax = 0;

        for (int i = 0; i < this.xSize; i++)
        {
            for (int j = 0; j < this.ySize; j++)
            {
                for (int k = 0; k < this.zSize; k++)
                {
                    if (this.cells[i][j][k] != 0)
                    {
                        if (i < xMin)
                            xMin = i;
                        if (i > xMax)
                            xMax = i;
                        if (j < yMin)
                            yMin = j;
                        if (j > yMax)
                            yMax = j;
                        if (k < zMin)
                            zMin = k;
                        if (k > zMax)
                            zMax = k;
                    }
                }
            }
        }
        int xNew = xMax - xMin + 1;
        int yNew = yMax - yMin + 1;
        int zNew = zMax - zMin + 1;
        if ((xNew == this.xSize) && (yNew == this.ySize) && (zNew == this.zSize)) {
            return;
        }
        int[][][] newCells = new int[xNew][yNew][zNew];

        for (int i = 0; i < xNew; i++)
        {
            for (int j = 0; j < yNew; j++)
            {
                for (int k = 0; k < zNew; k++)
                {
                    newCells[i][j][k] = this.cells[(xMin + i)][(yMin + j)][(zMin + k)];
                }
            }
        }

        this.xSize = xNew;
        this.ySize = yNew;
        this.zSize = zNew;
        this.cells = newCells;
    }

    public void setPosition(float x, float y, float z)
    {
        this.position.set(x, y, z);
    }

    public void setPosition(Vector3f v)
    {
        this.position.set(v);
    }

    public Vector3f getPosition()
    {
        return this.position;
    }

    public void setRotation(Quaternion rot)
    {
        this.rotation.set(rot);
    }

    public Quaternion getRotation()
    {
        return this.rotation;
    }

    public int getSizeX()
    {
        return this.xSize;
    }

    public int getSizeY()
    {
        return this.ySize;
    }

    public int getSizeZ()
    {
        return this.zSize;
    }

    public void setScale(float scale)
    {
        this.scale = scale;
        if (this.node != null)
            this.node.setLocalScale(scale);
    }

    public float getScale()
    {
        return this.scale;
    }

    public boolean setType(float i, float j, float k, int type)
    {
        return setType(Coordinates.worldToCell(i), Coordinates.worldToCell(j), Coordinates.worldToCell(k), type);
    }

    public boolean setType(int i, int j, int k, int type)
    {
        if ((i < 0) || (j < 0) || (k < 0))
            return false;
        if ((i >= this.xSize) || (j >= this.ySize) || (k >= this.zSize))
            return false;
        if (this.cells[i][j][k] != type)
        {
            this.changeCount += 1L;
            this.cells[i][j][k] = type;
        }
        return true;
    }

    public int getType(float i, float j, float k)
    {
        return getType(Coordinates.worldToCell(i), Coordinates.worldToCell(j), Coordinates.worldToCell(k));
    }

    public int getType(int i, int j, int k)
    {
        if ((i < 0) || (j < 0) || (k < 0))
            return -1;
        if ((i >= this.xSize) || (j >= this.ySize) || (k >= this.zSize))
            return -1;
        return this.cells[i][j][k];
    }

    public BlueprintObject clone()
    {
        try
        {
            BlueprintObject clone = (BlueprintObject)super.clone();
            clone.position = this.position.clone();
            clone.rotation = this.rotation.clone();

            clone.cells = new int[this.xSize][this.ySize][this.zSize];
            for (int i = 0; i < this.xSize; i++)
            {
                for (int j = 0; j < this.ySize; j++)
                {
                    for (int k = 0; k < this.zSize; k++)
                    {
                        clone.cells[i][j][k] = this.cells[i][j][k];
                    }
                }
            }
            clone.node = null;
            clone.id = (instanceCount++);

            return clone;
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException("This should never happen", e);
        }
    }

    protected void genTest()
    {
        for (int i = 1; i < this.xSize - 1; i++)
        {
            for (int j = 1; j < this.ySize - 1; j++)
            {
                for (int k = 1; k < this.zSize - 1; k++)
                {
                    this.cells[i][j][k] = (k + 1);
                }
            }
        }
        this.cells[0][2][2] = 6;
        this.cells[2][0][2] = 6;
        this.cells[2][2][0] = 6;
        this.cells[3][2][2] = 6;
        this.cells[2][3][2] = 6;
        this.cells[2][2][3] = 6;
    }

    protected int getAdjacent(int x, int y, int z, int d)
    {
        x += mythruna.Direction.DIRS[d][0];
        y += mythruna.Direction.DIRS[d][1];
        z += mythruna.Direction.DIRS[d][2];

        if ((x < 0) || (y < 0) || (z < 0))
            return 0;
        if ((x >= this.xSize) || (y >= this.ySize) || (z >= this.zSize))
            return 0;
        return this.cells[x][y][z];
    }

    public static Long nodeToObjectId(Node n)
    {
        return (Long)n.getUserData("BlockObjectId");
    }

    public Node getNode()
    {
        if (this.node == null)
        {
            this.node = generateNode(this.sunlight, this.localLight);
            this.node.setLocalScale(this.scale);
            this.node.setUserData("BlockObjectId", Long.valueOf(this.id));
        }
        return this.node;
    }

    public void regenerateGeometry()
    {
        System.out.println("************ regenerate geometry *************");
        if (this.node == null) {
            return;
        }

        Node newNode = generateNode(this.sunlight, this.localLight);

        this.node.detachAllChildren();

        List<Spatial> children = new ArrayList<Spatial>(newNode.getChildren());
        newNode.detachAllChildren();

        for (Spatial s : children) {
            this.node.attachChild(s);
        }
        System.out.println("    world location:" + this.node.getWorldTranslation());
    }

    public GeomPartBuffer generateParts(float sun, float localLight)
    {
        GeomPartBuffer parts = new GeomPartBuffer();

        for (int k = 0; k < this.zSize; k++)
        {
            for (int i = 0; i < this.xSize; i++)
            {
                for (int j = 0; j < this.ySize; j++)
                {
                    int t = this.cells[i][j][k];
                    BlockType type = mythruna.BlockTypeIndex.types[t];

                    if (t != 0)
                    {
                        int d = -1;

                        if (k == 0)
                        {
                            d = 5;
                            type.getGeomFactory().createGeometry(parts, i, j, k, i, j, k, sun, localLight, type, d);
                        }

                        if (i == 0)
                        {
                            d = 3;
                            type.getGeomFactory().createGeometry(parts, i, j, k, i, j, k, sun, localLight, type, d);
                        }

                        if (j == 0)
                        {
                            d = 0;
                            type.getGeomFactory().createGeometry(parts, i, j, k, i, j, k, sun, localLight, type, d);
                        }

                        if (k == this.zSize - 1)
                        {
                            d = 4;
                            type.getGeomFactory().createGeometry(parts, i, j, k, i, j, k, sun, localLight, type, d);
                        }

                        if (i == this.xSize - 1)
                        {
                            d = 2;
                            type.getGeomFactory().createGeometry(parts, i, j, k, i, j, k, sun, localLight, type, d);
                        }

                        if (j == this.ySize - 1)
                        {
                            d = 1;
                            type.getGeomFactory().createGeometry(parts, i, j, k, i, j, k, sun, localLight, type, d);
                        }

                        if ((type.isSolid()) && (type.getGroup() == 0));
                    }
                    else
                    {
                        for (int d = 0; d < 6; d++)
                        {
                            int adj = getAdjacent(i, j, k, d);
                            if (adj != 0)
                            {
                                BlockType adjType = mythruna.BlockTypeIndex.types[adj];

                                if ((type == null) ||
                                        (adjType.getGroup() != type.getGroup()) || (
                                        (adjType.getGroup() == 0) && (!type.isSolid(d))))
                                {
                                    int x = i + mythruna.Direction.DIRS[d][0];
                                    int y = j + mythruna.Direction.DIRS[d][1];
                                    int z = k + mythruna.Direction.DIRS[d][2];
                                    int back = mythruna.Direction.INVERSE[d];

                                    adjType.getGeomFactory().createGeometry(parts, x, y, z, x, y, z, sun, localLight, adjType, back);
                                }
                            }
                        }

                        if ((t != 0) && (!type.isSolid()))
                        {
                            type.getGeomFactory().createInternalGeometry(parts, i, j, k, i, j, k, sun, localLight, type);
                        }
                    }
                }
            }

        }

        return parts;
    }

    protected Node generateNode(float sun, float localLight)
    {
        GeomPartBuffer parts = generateParts(sun, localLight);
        Node node = parts.createNode(String.valueOf(this), -this.offset.x, -this.offset.y, -this.offset.z);

        return node;
    }
}