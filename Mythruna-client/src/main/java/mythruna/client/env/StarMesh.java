package mythruna.client.env;

import com.jme3.math.FastMath;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Random;

public class StarMesh extends Mesh {
    private Random random = new Random(0L);

    public StarMesh(float radius, int numStars) {
        setMode(Mesh.Mode.Points);

        FloatBuffer positions = BufferUtils.createVector3Buffer(numStars);
        VertexBuffer pvb = new VertexBuffer(VertexBuffer.Type.Position);
        pvb.setupData(VertexBuffer.Usage.Stream, 3, VertexBuffer.Format.Float, positions);
        setBuffer(pvb);

        ByteBuffer colors = BufferUtils.createByteBuffer(numStars * 4);
        VertexBuffer cvb = new VertexBuffer(VertexBuffer.Type.Color);
        cvb.setupData(VertexBuffer.Usage.Stream, 4, VertexBuffer.Format.UnsignedByte, colors);
        cvb.setNormalized(true);
        setBuffer(cvb);

        FloatBuffer sizes = BufferUtils.createFloatBuffer(numStars);
        VertexBuffer svb = new VertexBuffer(VertexBuffer.Type.Size);
        svb.setupData(VertexBuffer.Usage.Stream, 1, VertexBuffer.Format.Float, sizes);
        setBuffer(svb);

        FloatBuffer tb = BufferUtils.createFloatBuffer(numStars * 4);
        VertexBuffer tvb = new VertexBuffer(VertexBuffer.Type.TexCoord);
        tvb.setupData(VertexBuffer.Usage.Stream, 4, VertexBuffer.Format.Float, tb);
        setBuffer(tvb);

        createStars(radius, numStars);
    }

    protected void createStars(float radius, int numStars) {
        VertexBuffer pvb = getBuffer(VertexBuffer.Type.Position);
        FloatBuffer positions = (FloatBuffer) pvb.getData();

        VertexBuffer cvb = getBuffer(VertexBuffer.Type.Color);
        ByteBuffer colors = (ByteBuffer) cvb.getData();

        VertexBuffer svb = getBuffer(VertexBuffer.Type.Size);
        FloatBuffer sizes = (FloatBuffer) svb.getData();

        VertexBuffer tvb = getBuffer(VertexBuffer.Type.TexCoord);
        FloatBuffer texcoords = (FloatBuffer) tvb.getData();

        positions.rewind();
        colors.rewind();
        sizes.rewind();
        texcoords.rewind();

        for (int i = 0; i < numStars; i++) {
            float a1 = (float) (this.random.nextDouble() * 3.141592653589793D * 4.0D);
            float a2 = (float) (this.random.nextDouble() * 3.141592653589793D * 2.0D - 3.141592653589793D);

            float y = radius * FastMath.sin(a2);
            float scale = radius * FastMath.cos(a2);

            float x = scale * FastMath.cos(a1);
            float z = scale * FastMath.sin(a1);

            positions.put(x).put(y).put(z);

            sizes.put((float) this.random.nextDouble() * 5.0F);
            colors.putInt(-1);

            texcoords.put(0.0F).put(0.0F).put(0.0666667F).put(1.0F);
        }

        positions.flip();
        colors.flip();
        sizes.flip();
        texcoords.flip();

        pvb.updateData(positions);
        cvb.updateData(colors);
        svb.updateData(sizes);
        tvb.updateData(texcoords);
    }
}