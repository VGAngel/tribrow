package com.pro.component;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.shape.Quad;

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

    private ArrayList<Quad> tiles;

    public TileSetGrid(int xWidth, int yHeight, int tileWidth, int tileHeight, AssetManager assetManager) {
        System.out.println("xWidth=" + xWidth + " yHeight=" + yHeight + " tileWidth=" + tileWidth + " tileHeight=" + tileHeight);

        this.xWidth = xWidth;
        this.yHeight = yHeight;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;

        tiles = new ArrayList<>(xWidth * yHeight);

        Quad rootQuad = new Quad(2 * border + spacing * (xWidth - 1) + xWidth * tileWidth, 2 * border + spacing * (yHeight - 1) + yHeight * tileHeight);
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.getAdditionalRenderState().setWireframe(true);
        material.setColor("Color", ColorRGBA.Blue);

        Geometry geo = new Geometry("RootQuad", rootQuad);
        geo.setMaterial(material);
        geo.setLocalTranslation(0, 0, 0);

        this.attachChild(geo);

        Geometry grid = new Geometry("grid", new Grid(yHeight, xWidth, tileWidth));
        Material gridMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        gridMat.getAdditionalRenderState().setWireframe(true);
        gridMat.setColor("Color", ColorRGBA.Yellow);

        grid.setMaterial(gridMat);
        grid.setLocalTranslation(0, 0, 0);

        Quaternion pitch90 = new Quaternion();
        pitch90.fromAngleAxis(-FastMath.PI/2, new Vector3f(1,0,0));
        grid.setLocalRotation(pitch90);

        this.attachChild(grid);
    }

    public void addTiles() {

    }

}
