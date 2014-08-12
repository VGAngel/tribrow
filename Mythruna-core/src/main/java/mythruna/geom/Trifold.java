package mythruna.geom;

import com.jme3.math.Vector2f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Trifold extends Mesh {

    private Vector2f size;
    private Vector2f fold1;
    private Vector2f fold2;
    private Vector2f[] texCoords = new Vector2f[4];

    public Trifold(float width, float height) {
        this.size = new Vector2f(width, height);
        this.fold1 = this.size.mult(0.3333333F);
        this.fold2 = this.size.mult(0.6666667F);
        this.texCoords[0] = new Vector2f(0.0F, 0.0F);
        this.texCoords[3] = new Vector2f(1.0F, 1.0F);
        this.texCoords[1] = this.texCoords[3].mult(0.3333333F);
        this.texCoords[2] = this.texCoords[3].mult(0.6666667F);

        createGeometry();
    }

    public Trifold(float width, float height, float imageWidth, float imageHeight, float xBorder, float yBorder) {
        this.size = new Vector2f(width, height);
        this.fold1 = new Vector2f(xBorder, yBorder);
        this.fold2 = new Vector2f(width - xBorder, height - yBorder);
        this.texCoords[0] = new Vector2f(0.0F, 0.0F);
        this.texCoords[3] = new Vector2f(1.0F, 1.0F);
        this.texCoords[1] = new Vector2f(xBorder / imageWidth, yBorder / imageWidth);
        this.texCoords[2] = new Vector2f(1.0F - this.texCoords[1].x, 1.0F - this.texCoords[1].y);

        createGeometry();
    }

    public void setFoldTextureCoordinates(Vector2f fold1, Vector2f fold2) {
        this.texCoords[1] = fold1;
        this.texCoords[2] = fold2;
    }

    public void setFoldCoordinates(Vector2f fold1, Vector2f fold2) {
        this.fold1 = fold1;
        this.fold2 = fold2;
    }

    public void setSize(float width, float height) {
        float xDelta = this.size.x - width;
        float yDelta = this.size.y - height;
        this.size.x = width;
        this.size.y = height;

        if (this.fold2.x != this.fold1.x)
            this.fold2.x -= xDelta;
        if (this.fold2.y != this.fold1.y)
            this.fold2.y -= yDelta;
    }

    public Vector2f getSize() {
        return this.size;
    }

    private int index(int x, int y) {
        return y * 4 + x;
    }

    private void createGeometry() {
        FloatBuffer pb = BufferUtils.createVector3Buffer(16);
        setPositions(pb);

        setBuffer(VertexBuffer.Type.Position, 3, pb);

        FloatBuffer tb = BufferUtils.createVector2Buffer(16);

        setTexCoords(tb);

        setBuffer(VertexBuffer.Type.TexCoord, 2, tb);

        FloatBuffer normals = BufferUtils.createVector3Buffer(16);
        for (int i = 0; i < 16; i++) {
            normals.put(0.0F);
            normals.put(0.0F);
            normals.put(1.0F);
        }
        setBuffer(VertexBuffer.Type.Normal, 3, normals);

        IntBuffer indexes = BufferUtils.createIntBuffer(54);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                indexes.put(index(i, j)).put(index(i + 1, j)).put(index(i + 1, j + 1));
                indexes.put(index(i, j)).put(index(i + 1, j + 1)).put(index(i, j + 1));
            }

        }

        setBuffer(VertexBuffer.Type.Index, 3, indexes);
    }

    protected void setPositions(FloatBuffer pb) {
        pb.put(0.0F).put(0.0F).put(0.0F);
        pb.put(this.fold1.x).put(0.0F).put(0.0F);
        pb.put(this.fold2.x).put(0.0F).put(0.0F);
        pb.put(this.size.x).put(0.0F).put(0.0F);

        pb.put(0.0F).put(this.fold1.y).put(0.0F);
        pb.put(this.fold1.x).put(this.fold1.y).put(0.0F);
        pb.put(this.fold2.x).put(this.fold1.y).put(0.0F);
        pb.put(this.size.x).put(this.fold1.y).put(0.0F);

        pb.put(0.0F).put(this.fold2.y).put(0.0F);
        pb.put(this.fold1.x).put(this.fold2.y).put(0.0F);
        pb.put(this.fold2.x).put(this.fold2.y).put(0.0F);
        pb.put(this.size.x).put(this.fold2.y).put(0.0F);

        pb.put(0.0F).put(this.size.y).put(0.0F);
        pb.put(this.fold1.x).put(this.size.y).put(0.0F);
        pb.put(this.fold2.x).put(this.size.y).put(0.0F);
        pb.put(this.size.x).put(this.size.y).put(0.0F);
    }

    protected void setTexCoords(FloatBuffer tb) {
        tb.put(this.texCoords[0].x).put(this.texCoords[0].y);
        tb.put(this.texCoords[1].x).put(this.texCoords[0].y);
        tb.put(this.texCoords[2].x).put(this.texCoords[0].y);
        tb.put(this.texCoords[3].x).put(this.texCoords[0].y);

        tb.put(this.texCoords[0].x).put(this.texCoords[1].y);
        tb.put(this.texCoords[1].x).put(this.texCoords[1].y);
        tb.put(this.texCoords[2].x).put(this.texCoords[1].y);
        tb.put(this.texCoords[3].x).put(this.texCoords[1].y);

        tb.put(this.texCoords[0].x).put(this.texCoords[2].y);
        tb.put(this.texCoords[1].x).put(this.texCoords[2].y);
        tb.put(this.texCoords[2].x).put(this.texCoords[2].y);
        tb.put(this.texCoords[3].x).put(this.texCoords[2].y);

        tb.put(this.texCoords[0].x).put(this.texCoords[3].y);
        tb.put(this.texCoords[1].x).put(this.texCoords[3].y);
        tb.put(this.texCoords[2].x).put(this.texCoords[3].y);
        tb.put(this.texCoords[3].x).put(this.texCoords[3].y);

        updateBound();
    }

    public void updateGeometry() {
        FloatBuffer pb = getFloatBuffer(VertexBuffer.Type.Position);
        pb.clear();
        FloatBuffer tb = getFloatBuffer(VertexBuffer.Type.TexCoord);
        tb.clear();

        setPositions(pb);

        setTexCoords(tb);

        VertexBuffer pBuff = getBuffer(VertexBuffer.Type.Position);
        pBuff.updateData(pb);
        VertexBuffer tBuff = getBuffer(VertexBuffer.Type.TexCoord);
        tBuff.updateData(tb);

        updateBound();
    }
}