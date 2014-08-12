package mythruna.geom;

import com.jme3.bounding.BoundingBox;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import mythruna.MaterialIndex;
import org.progeeks.util.log.Log;

import java.util.*;

public class GeomPartBuffer {
    static Log log = Log.getLog();

    private Set<Object> visited = new HashSet();
    private Map<Integer, PartList> parts = new HashMap();
    private Random random = new Random(0L);
    private int partCount = 0;
    private BoundingBox bounds;

    public GeomPartBuffer() {
    }

    public GeomPartBuffer(BoundingBox bounds) {
        this.bounds = bounds;
    }

    public void setRandomSeed(long num) {
        this.random.setSeed(num);
    }

    public double nextRandom() {
        return this.random.nextDouble();
    }

    public int size() {
        return this.partCount;
    }

    protected PartList getList(int materialType, int dir, boolean create) {
        int key = materialType;
        PartList list = (PartList) this.parts.get(Integer.valueOf(key));
        if ((list == null) && (create)) {
            list = new PartList(materialType, dir);
            this.parts.put(Integer.valueOf(key), list);
        }
        return list;
    }

    public boolean visit(Object key) {
        return this.visited.add(key);
    }

    public boolean isVisited(Object key) {
        return this.visited.contains(key);
    }

    public boolean unvisit(Object key) {
        return this.visited.remove(key);
    }

    public void add(GeomPart part) {
        getList(part.getMaterialType(), part.getDirection(), true).add(part);
        this.partCount += 1;
    }

    protected int append(float[] src, float[] dest, int start) {
        System.arraycopy(src, 0, dest, start, src.length);
        return src.length;
    }

    public Node createNode(String id) {
        return createNode(id, 0.0F, 0.0F, 0.0F);
    }

    public Node createNode(String id, float xOffset, float yOffset, float zOffset) {
        Node node = new Node(id);

        return createGeometry(node, xOffset, yOffset, zOffset);
    }

    public Node createGeometry(Node node, float xOffset, float yOffset, float zOffset) {
        for (PartList list : this.parts.values()) {
            Mesh mesh = createMesh(list);
            mesh.setStatic();
            if (mesh != null) {
                Geometry geom = new Geometry("geom:" + list.materialType, mesh);
                geom.setLocalTranslation(xOffset, zOffset, yOffset);
                Material mat = MaterialIndex.getMaterial(list.materialType);
                geom.setMaterial(mat);

                if (mat.getAdditionalRenderState().getBlendMode() == RenderState.BlendMode.Alpha) {
                    geom.setQueueBucket(RenderQueue.Bucket.Transparent);
                }
                if (mat.getAdditionalRenderState().getBlendMode() == RenderState.BlendMode.AlphaAdditive) {
                    geom.setQueueBucket(RenderQueue.Bucket.Transparent);
                }
                if (mat.getAdditionalRenderState().getBlendMode() == RenderState.BlendMode.Color) {
                    geom.setQueueBucket(RenderQueue.Bucket.Transparent);
                }

                if (list.materialType == 22) {
                    geom.addControl(new CullDistanceControl(64));
                } else if (list.materialType == 33) {
                    geom.addControl(new CullDistanceControl(64));
                } else if (list.materialType == 500) {
                    geom.addControl(new CullDistanceControl(64));
                }

                node.attachChild(geom);
            }
        }
        return node;
    }

    public Mesh createMesh(int materialType, int dir) {
        PartList list = getList(materialType, dir, false);
        if ((list == null) || (list.size() == 0))
            return null;
        return createMesh(list);
    }

    protected Mesh createMesh(PartList list) {
        if ((list == null) || (list.size() == 0)) {
            return null;
        }
        if (list.getPrimitiveType() == 1) {
            return createPointMesh(list);
        }
        boolean hasTangents = list.hasTangents();
        boolean hasNormals = list.hasNormals();

        int vc = list.getVertexCount();
        float[] coords = new float[vc * 3];
        float[] colors = new float[vc * 4];
        float[] norms;
        if (hasNormals)
            norms = new float[vc * 3];
        else
            norms = null;
        float[] tangents;
        if (hasTangents)
            tangents = new float[vc * 3];
        else {
            tangents = null;
        }

        float[] texes = new float[vc * 2];
        short[] indexes = new short[list.getTriangleCount() * list.getPrimitiveSize()];

        int coordIndex = 0;
        int normIndex = 0;
        int binormIndex = 0;
        int tanIndex = 0;
        int texIndex = 0;
        int indexIndex = 0;
        int clr = 0;
        int baseIndex = 0;

        for (GeomPart part : list) {
            coordIndex += append(part.getCoords(), coords, coordIndex);
            texIndex += append(part.getTexCoords(), texes, texIndex);

            if (hasNormals) {
                normIndex += append(part.getNormals(), norms, normIndex);
            }

            if (hasTangents) {
                tanIndex += append(part.getTangents(), tangents, tanIndex);
            }

            int size = part.getVertexCount();
            float sun = part.getSun();
            float light = part.getLight();
            float dir = part.getDirection();

            for (int index = 0; index < size; index++) {
                colors[(clr++)] = light;
                colors[(clr++)] = dir;
                colors[(clr++)] = 1.0F;
                colors[(clr++)] = sun;
            }

            short[] temp = part.getIndexes();

            for (int index = 0; index < temp.length; index++) {
                indexes[(indexIndex++)] = (short) (baseIndex + temp[index]);
            }

            baseIndex += part.getVertexCount();
        }

        Mesh mesh = new LeafMesh(this.bounds);

        if (list.getPrimitiveType() == 1) {
            mesh.setMode(Mesh.Mode.Points);
        }

        try {
            mesh.setBuffer(VertexBuffer.Type.Position, 3, coords);
            mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, texes);
            mesh.setBuffer(VertexBuffer.Type.Index, 3, indexes);
            mesh.setBuffer(VertexBuffer.Type.Color, 4, colors);

            if (hasNormals) {
                mesh.setBuffer(VertexBuffer.Type.Normal, 3, norms);
            }

            if (hasTangents) {
                mesh.setBuffer(VertexBuffer.Type.Tangent, 3, tangents);
            }
        } catch (IndexOutOfBoundsException e) {
            log.error("Buffer indexing problem:" + list, e);
            throw e;
        }

        mesh.setStatic();
        mesh.updateBound();

        return mesh;
    }

    protected Mesh createPointMesh(PartList list) {
        if ((list == null) || (list.size() == 0)) {
            return null;
        }

        int vc = list.getVertexCount();
        float[] coords = new float[vc * 3];
        float[] colors = new float[vc * 4];
        float[] sizes = new float[vc];
        float[] texes = new float[vc * 4];

        int coordIndex = 0;
        int texIndex = 0;
        int sizeIndex = 0;
        int clr = 0;

        for (GeomPart part : list) {
            coordIndex += append(part.getCoords(), coords, coordIndex);
            texIndex += append(part.getTexCoords(), texes, texIndex);
            clr += append(part.getColors(), colors, clr);
            sizeIndex += append(part.getSizes(), sizes, sizeIndex);
        }

        Mesh mesh = new LeafMesh(this.bounds);
        mesh.setMode(Mesh.Mode.Points);
        try {
            mesh.setBuffer(VertexBuffer.Type.Position, 3, coords);
            mesh.setBuffer(VertexBuffer.Type.TexCoord, 4, texes);
            mesh.setBuffer(VertexBuffer.Type.Size, 1, sizes);
            mesh.setBuffer(VertexBuffer.Type.Color, 4, colors);
        } catch (IndexOutOfBoundsException e) {
            log.error("Buffer indexing problem:" + list, e);
            throw e;
        }

        mesh.setStatic();
        mesh.updateBound();

        return mesh;
    }

    protected class PartList implements Iterable<GeomPart> {
        private int materialType;
        private int dir;
        private int primitiveType;
        private int primitiveSize;
        private List<GeomPart> list = new ArrayList();
        private int vertCount;
        private int triCount;
        private boolean hasTangents;
        private boolean hasNormals;

        public PartList(int materialType, int dir) {
            this.materialType = materialType;
            this.dir = dir;
        }

        public boolean hasTangents() {
            return this.hasTangents;
        }

        public boolean hasNormals() {
            return this.hasNormals;
        }

        public void add(GeomPart part) {
            if (part.getTangents() != null) {
                if ((!this.list.isEmpty()) && (!this.hasTangents))
                    throw new RuntimeException("Error, part has tangents but the rest of part list does not, type:" + part.getMaterialType());
                this.hasTangents = true;
            } else if (this.hasTangents) {
                throw new RuntimeException("Error, part does not have tangents but the rest of part list does, type:" + part.getMaterialType());
            }
            if (part.getNormals() != null) {
                if ((!this.list.isEmpty()) && (!this.hasNormals))
                    throw new RuntimeException("Error, part has normals but the rest of part list does not, type:" + part.getMaterialType());
                this.hasNormals = true;
            } else if (this.hasNormals) {
                throw new RuntimeException("Error, part does not have normals but the rest of part list does, type:" + part.getMaterialType());
            }
            if (part.getPrimitiveSize() != this.primitiveSize) {
                if (!this.list.isEmpty())
                    throw new RuntimeException("Error, part has different primitive size than the rest of the list:" + this.primitiveSize);
                this.primitiveSize = part.getPrimitiveSize();
            }
            if (part.getPrimitiveType() != this.primitiveType) {
                if (!this.list.isEmpty())
                    throw new RuntimeException("Error, part has different primitive type than the rest of the list:" + this.primitiveType);
                this.primitiveType = part.getPrimitiveType();
            }

            this.list.add(part);

            this.vertCount += part.getVertexCount();
            this.triCount += part.getTriangleCount();
        }

        public int getVertexCount() {
            return this.vertCount;
        }

        public int getTriangleCount() {
            return this.triCount;
        }

        public int getPrimitiveType() {
            return this.primitiveType;
        }

        public int getPrimitiveSize() {
            return this.primitiveSize;
        }

        public Iterator<GeomPart> iterator() {
            return this.list.iterator();
        }

        public int size() {
            return this.list.size();
        }

        public String toString() {
            return "PartList[ material=" + this.materialType + ", vertCount=" + this.vertCount + ", triCount=" + this.triCount + ", hasTangents=" + this.hasTangents + ", hasNormals=" + this.hasNormals + "]";
        }
    }
}