package mythruna.client.env;

import com.jme3.asset.AssetManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.texture.Texture;

import java.util.Random;

public class CloudLayer {
    private int count;
    private float minCloudScale;
    private float maxCloudScale;
    private float radius;
    private Texture clouds;
    private Random random;

    public CloudLayer(long seed, int count, float minCloudScale, float maxCloudScale, float radius) {
        this.random = new Random(seed);
        this.count = count;
        this.minCloudScale = minCloudScale;
        this.maxCloudScale = maxCloudScale;
        this.radius = radius;
    }

    public Mesh createMesh() {
        float[][] baseQuad = {{-0.5F, -0.5F, 0.0F}, {0.5F, -0.5F, 0.0F}, {0.5F, 0.5F, 0.0F}, {-0.5F, 0.5F, 0.0F}};

        int vCount = this.count * 4;
        float[] coords = new float[vCount * 3];
        float[] colors = new float[vCount * 4];
        float[] norms = new float[vCount * 3];
        float[] texes = new float[vCount * 2];
        short[] indexes = new short[this.count * 2 * 3];

        int coordIndex = 0;
        int colorIndex = 0;
        int normIndex = 0;
        int texIndex = 0;
        int index = 0;

        Vector3f[] quad = {new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};

        int xImageCount = 3;
        int yImageCount = 3;

        int baseIndex = 0;

        float sizeRange = this.maxCloudScale - this.minCloudScale;

        for (int i = 0; i < this.count; i++) {
            float w = (float) this.random.nextDouble() * sizeRange + this.minCloudScale;
            float h = (float) this.random.nextDouble() * sizeRange + this.minCloudScale;

            float yaw = (float) this.random.nextDouble() * 3.141593F * 2.0F;
            float pitch = (float) (this.random.nextDouble() * 3.141592741012573D) - 1.570796F;
            pitch *= 0.8F;
            float roll = (float) (this.random.nextDouble() * 3.141592741012573D) - 1.570796F;

            Quaternion rotate = new Quaternion().fromAngles(pitch, yaw, roll);

            Vector3f normal = rotate.mult(new Vector3f(0.0F, 0.0F, -1.0F));

            for (int v = 0; v < 4; v++) {
                quad[v].x = (baseQuad[v][0] * w);
                quad[v].y = (baseQuad[v][1] * h);
                quad[v].z = this.radius;

                quad[v] = rotate.mult(quad[v]);

                coords[(coordIndex++)] = quad[v].x;
                coords[(coordIndex++)] = quad[v].y;
                coords[(coordIndex++)] = quad[v].z;

                norms[(normIndex++)] = normal.x;
                norms[(normIndex++)] = normal.y;
                norms[(normIndex++)] = normal.z;

                colors[(colorIndex++)] = 1.0F;
                colors[(colorIndex++)] = 1.0F;
                colors[(colorIndex++)] = 1.0F;
                colors[(colorIndex++)] = 1.0F;
            }

            int xIndex = (int) (this.random.nextDouble() * xImageCount);
            int yIndex = (int) (this.random.nextDouble() * yImageCount);

            float s1 = xIndex / xImageCount;
            float t1 = yIndex / yImageCount;
            float s2 = xIndex + 1 / xImageCount;
            float t2 = yIndex + 1 / yImageCount;

            texes[(texIndex++)] = s1;
            texes[(texIndex++)] = t1;
            texes[(texIndex++)] = s2;
            texes[(texIndex++)] = t1;
            texes[(texIndex++)] = s2;
            texes[(texIndex++)] = t2;
            texes[(texIndex++)] = s1;
            texes[(texIndex++)] = t2;

            indexes[(index++)] = (short) (baseIndex + 0);
            indexes[(index++)] = (short) (baseIndex + 2);
            indexes[(index++)] = (short) (baseIndex + 1);
            indexes[(index++)] = (short) (baseIndex + 0);
            indexes[(index++)] = (short) (baseIndex + 3);
            indexes[(index++)] = (short) (baseIndex + 2);
            baseIndex += 4;
        }

        Mesh mesh = new Mesh();

        mesh.setBuffer(VertexBuffer.Type.Position, 3, coords);
        mesh.setBuffer(VertexBuffer.Type.Normal, 3, norms);
        mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, texes);
        mesh.setBuffer(VertexBuffer.Type.Index, 3, indexes);
        mesh.setBuffer(VertexBuffer.Type.Color, 4, colors);

        mesh.updateBound();

        return mesh;
    }

    public void initialize(AssetManager assets) {
    }
}