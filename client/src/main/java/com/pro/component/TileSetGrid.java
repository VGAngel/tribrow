package com.pro.component;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.util.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

/**
 * Created by Valentyn.Polishchuk on 8/22/2014
 */
public class TileSetGrid extends Node {

    private int xWidth;
    private int yHeight;
    private int tileWidth;
    private int tileHeight;
    private int border = 1;
    private int spacing = 1;

    private ArrayList<Geometry> tiles;

    private AssetManager assetManager;

    public TileSetGrid(int xWidth, int yHeight, int tileWidth, int tileHeight, AssetManager assetManager) {
        System.out.println("xWidth=" + xWidth + " yHeight=" + yHeight + " tileWidth=" + tileWidth + " tileHeight=" + tileHeight);

        this.xWidth = xWidth;
        this.yHeight = yHeight;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.assetManager = assetManager;

        tiles = new ArrayList<>(xWidth * yHeight);

        Quad rootQuad = new Quad(2 * border + spacing * (xWidth - 1) + xWidth * tileWidth, 2 * border + spacing * (yHeight - 1) + yHeight * tileHeight);
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.getAdditionalRenderState().setWireframe(true);
        material.setColor("Color", ColorRGBA.Blue);

        Geometry geo = new Geometry("RootQuad", rootQuad);
        geo.setMaterial(material);
        geo.setLocalTranslation(0, 0, 0);

        this.attachChild(geo);

        Geometry grid = new Geometry("grid", drawGrid(yHeight + 1, xWidth + 1, tileWidth, tileHeight));
        Material gridMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        //gridMat.getAdditionalRenderState().setWireframe(true);
        gridMat.setColor("Color", ColorRGBA.Yellow);

        grid.setMaterial(gridMat);
        grid.setLocalTranslation(0, 0, 0);

        Quaternion pitch90 = new Quaternion();
        pitch90.fromAngleAxis(-FastMath.PI / 2, new Vector3f(1, 0, 0));
        grid.setLocalRotation(pitch90);

        this.attachChild(grid);
    }

    public Mesh drawGrid(int xLines, int yLines, float lineDistX, float lineDistY) {
        Mesh gridMesh = new Mesh();

        xLines -= 2;
        yLines -= 2;
        int lineCount = xLines + yLines + 4;

        FloatBuffer fpb = BufferUtils.createFloatBuffer(6 * lineCount);
        ShortBuffer sib = BufferUtils.createShortBuffer(2 * lineCount);

        float xLineLen = (yLines + 1) * lineDistX;
        float yLineLen = (xLines + 1) * lineDistY;
        int curIndex = 0;

        // add lines along X
        for (int i = 0; i < xLines + 2; i++) {
            float y = (i) * lineDistY;

            // positions
            fpb.put(0).put(0).put(y);
            fpb.put(xLineLen).put(0).put(y);

            // indices
            sib.put((short) (curIndex++));
            sib.put((short) (curIndex++));
        }

        // add lines along Y
        for (int i = 0; i < yLines + 2; i++) {
            float x = (i) * lineDistX;

            // positions
            fpb.put(x).put(0).put(0);
            fpb.put(x).put(0).put(yLineLen);

            // indices
            sib.put((short) (curIndex++));
            sib.put((short) (curIndex++));
        }

        fpb.flip();
        sib.flip();

        gridMesh.setBuffer(VertexBuffer.Type.Position, 3, fpb);
        gridMesh.setBuffer(VertexBuffer.Type.Index, 2, sib);

        gridMesh.setMode(Mesh.Mode.Lines);

        gridMesh.updateBound();
        gridMesh.updateCounts();

        return gridMesh;
    }

    public void addTiles(Texture[] textures) {
        for(Texture texture : textures) {
            Quad quad = new Quad(texture.getImage().getWidth(), texture.getImage().getHeight());

            Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            material.setTexture("ColorMap", texture);

            Geometry geo = new Geometry("Tile" + tiles.size() + 1, quad);
            System.out.println("xLoc=" + (tileWidth * tiles.size()));
            geo.setLocalTranslation(tileWidth * tiles.size(), 0, 0);
            geo.setMaterial(material);

            this.attachChild(geo);
            System.out.println("this.getLocalTranslation=" + this.getLocalTranslation().toString());
            System.out.println("this.getWorldTranslation=" + this.getWorldTranslation().toString());
            tiles.add(geo);
        }

    }

}
