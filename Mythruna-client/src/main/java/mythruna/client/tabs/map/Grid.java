package mythruna.client.tabs.map;

import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Grid extends Mesh {
    private int rows;
    private int columns;
    private float scale;

    public Grid(int rows, int columns, float scale) {
        updateGeometry(rows, columns, scale);
    }

    public void updateGeometry(int rows, int columns, float scale) {
        this.rows = rows;
        this.columns = columns;
        this.scale = scale;

        int vertCount = (rows + 1) * (columns + 1);
        FloatBuffer verts = BufferUtils.createFloatBuffer(vertCount * 3);

        for (int j = 0; j <= rows; j++) {
            for (int i = 0; i <= columns; i++) {
                verts.put(i * scale);
                verts.put(0.0F);
                verts.put(j * scale);
            }
        }

        setBuffer(VertexBuffer.Type.Position, 3, verts);

        int quadCount = rows * columns;
        IntBuffer indexes = BufferUtils.createIntBuffer(quadCount * 6);
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                int index0 = i * (rows + 1) + j;
                int index1 = i * (rows + 1) + j + 1;
                int index2 = (i + 1) * (rows + 1) + j + 1;
                int index3 = (i + 1) * (rows + 1) + j;

                indexes.put(index0).put(index2).put(index1);
                indexes.put(index0).put(index3).put(index2);
            }
        }

        setBuffer(VertexBuffer.Type.Index, 3, indexes);

        updateBound();
    }
}