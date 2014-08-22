package com.pro.game;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;

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
        viewPort.setBackgroundColor(ColorRGBA.Blue);

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
        material.getAdditionalRenderState().setWireframe(true);

        quad = new Quad(tex1.getImage().getWidth(), tex1.getImage().getHeight());

        geo = new Geometry("Quad1", quad);
        geo.setLocalTranslation(0f, 35f, -200f);
        geo.setMaterial(material);

        rootNode.attachChild(geo);

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
}
