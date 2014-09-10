package com.pro.game;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;
import com.pro.component.TileSetGrid;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by Valentyn.Polishchuk on 8/22/2014
 */
public class LoadTileSet extends SimpleApplication {

    public static void main(String[] args) {
        LoadTileSet app = new LoadTileSet();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Vector3f centerPos = new Vector3f(0f, 0f, 0f);

        attachCoordinateAxes(centerPos);
        //attachGrid(centerPos, 1, ColorRGBA.Gray);

        RtsCamNew.applyCamera(this);
        createDebugGrid();

        viewPort.setBackgroundColor(ColorRGBA.Gray);

        Texture[][] textures = null;
        try {
            textures = loadAlphaMap();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex1 = assetManager.loadTexture("tiles/examples/tmw_desert_spacing.png");
        tex1 = textures[0][0];
        System.out.println(tex1.getImage().getWidth());
        System.out.println(tex1.getImage().getHeight());
        material.setTexture("ColorMap", tex1);
        material.getAdditionalRenderState().setWireframe(true);

        Quad quad = new Quad(tex1.getImage().getWidth(), tex1.getImage().getHeight());

        Geometry geo = new Geometry("Quad", quad);
        geo.setLocalTranslation(0f, 0f, -200f);
        geo.setMaterial(material);

        rootNode.attachChild(geo);

        //----------------------------
        material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        tex1 = textures[0][1];
        System.out.println(tex1.getImage().getWidth());
        System.out.println(tex1.getImage().getHeight());
        material.setTexture("ColorMap", tex1);
        //material.getAdditionalRenderState().setWireframe(true);

        quad = new Quad(tex1.getImage().getWidth(), tex1.getImage().getHeight());

        geo = new Geometry("Quad1", quad);
        geo.setLocalTranslation(0f, 35f, -200f);
        geo.setMaterial(material);

        rootNode.attachChild(geo);

        TileSetGrid tileSetGrid = new TileSetGrid(5, 3, 29, 33, assetManager);
        rootNode.attachChild(tileSetGrid);

        tileSetGrid.addTiles(new Texture[]{textures[0][0], textures[0][1], textures[0][2]});

    }

    private Texture[][] loadAlphaMap() throws IOException {
        int chunkCountX = 9;
        int chunkCountZ = 6;
        Texture[][] groundAlpha = new Texture[chunkCountX][chunkCountZ];

        BufferedImage alphaImage = ImageIO.read(LoadTileSet.class.getResource("/tiles/examples/tmw_desert_spacing.png"));
        BufferedImage[][] alphaArray = new BufferedImage[chunkCountX][chunkCountZ];
        BufferedImage alphaChunk;

        int arrayX = alphaImage.getWidth() / chunkCountX;
        int arrayY = alphaImage.getHeight() / chunkCountZ;

        for (int cX = 0; cX < chunkCountX; cX++) {
            for (int cZ = 0; cZ < chunkCountZ; cZ++) {

                alphaChunk = new BufferedImage(arrayX, arrayY, BufferedImage.TYPE_INT_ARGB);
                alphaChunk.setData(alphaImage.getSubimage(cX * arrayX, cZ * arrayY, arrayX, arrayY).getData());
                alphaArray[cX][cZ] = alphaChunk;

            }
        }

        AWTLoader loader = new AWTLoader();

        for (int cX = 0; cX < chunkCountX; cX++) {
            for (int cZ = 0; cZ < chunkCountZ; cZ++) {
                groundAlpha[cX][cZ] = new Texture2D();
                groundAlpha[cX][cZ].setImage(loader.load(alphaArray[cX][cZ], false));
            }
        }

        return groundAlpha;
    }

    private void attachCoordinateAxes(Vector3f pos) {
        int length = 20;

        Arrow arrow = new Arrow(Vector3f.UNIT_X);
        arrow.setLineWidth(length); // make arrow thicker
        putShape(arrow, ColorRGBA.Red).setLocalTranslation(pos);

        arrow = new Arrow(Vector3f.UNIT_Y);
        arrow.setLineWidth(length); // make arrow thicker
        putShape(arrow, ColorRGBA.Green).setLocalTranslation(pos);

        arrow = new Arrow(Vector3f.UNIT_Z);
        arrow.setLineWidth(length); // make arrow thicker
        putShape(arrow, ColorRGBA.Blue).setLocalTranslation(pos);
    }


    private Geometry putShape(Mesh shape, ColorRGBA color){
        Geometry geo = new Geometry("coordinate axis", shape);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", color);
        geo.setMaterial(mat);
        rootNode.attachChild(geo);
        return geo;
    }

    public void createDebugGrid() {
        //Create a grid plane
        Geometry g = new Geometry("GRID", new Grid(201, 201, 10f));
        Material floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        floor_mat.getAdditionalRenderState().setWireframe(true);
        floor_mat.setColor("Color", new ColorRGBA(0.4f, 0.4f, 0.4f, 0.15f));
        floor_mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        floor_mat.getAdditionalRenderState().setDepthWrite(false);
//        g.setCullHint(Spatial.CullHint.Never);
        g.setShadowMode(RenderQueue.ShadowMode.Off);
        g.setQueueBucket(RenderQueue.Bucket.Transparent);
        g.setMaterial(floor_mat);
        g.center().move(new Vector3f(0f, 0f, 0f));
        rootNode.attachChild(g);
    }
}
