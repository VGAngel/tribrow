package mythruna.client.tabs.map;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.system.AppSettings;
import com.jme3.system.NanoTimer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;
import mythruna.BlockTypeIndex;
import mythruna.DefaultWorld;
import mythruna.World;
import mythruna.client.ErrorHandler;
import mythruna.db.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

public class SkirtTest extends SimpleApplication {
    private Grid gridMesh;
    private int xBase = -512;
    private int yBase = -512;

    private static int[] colors = new int[13];

    private static int rockColor = color(160, 157, 144);
    private static int sandColor = color(225, 206, 181);
    private static int grassColor = color(35, 97, 36);
    private static int dirtColor = color(116, 93, 72);
    private static int waterColor = color(81, 133, 228);

    private static float fallOff = 0.01F;

    public SkirtTest() {
    }

    public static void main(String[] args)
            throws Exception {
        ErrorHandler.initialize();

        SkirtTest app = new SkirtTest();

        app.setPauseOnLostFocus(false);

        AppSettings settings = new AppSettings(true);
        settings.setTitle("Mythruna Map Viewer 3D Test");
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setSettingsDialogImage("/Interface/mythruna-title.png");
        app.setSettings(settings);

        app.start();
    }

    public void simpleInitApp() {
        setTimer(new NanoTimer());
        BlockTypeIndex.initialize();
        BufferedImage[] maps;
        try {
            maps = createImage();
        } catch (IOException e) {
            throw new RuntimeException("Error creating map", e);
        }

        this.flyCam.setMoveSpeed(10.0F);

        System.out.println("bottom:" + this.cam.getFrustumBottom() + " top:" + this.cam.getFrustumTop() + " left:" + this.cam.getFrustumLeft() + " rightt:" + this.cam.getFrustumRight() + " near:" + this.cam.getFrustumNear() + " far:" + this.cam.getFrustumFar());

        System.out.println("width:" + this.cam.getWidth() + " height:" + this.cam.getHeight());

        float aspect = this.cam.getWidth() / this.cam.getHeight();
        this.cam.setFrustumPerspective(70.0F, aspect, 0.1F, 2048.0F);

        System.out.println("bottom:" + this.cam.getFrustumBottom() + " top:" + this.cam.getFrustumTop() + " left:" + this.cam.getFrustumLeft() + " rightt:" + this.cam.getFrustumRight() + " near:" + this.cam.getFrustumNear() + " far:" + this.cam.getFrustumFar());

        System.out.println("width:" + this.cam.getWidth() + " height:" + this.cam.getHeight());

        AWTLoader loader = new AWTLoader();
        Image image = loader.load(maps[0], false);
        Texture texture = new Texture2D();
        texture.setImage(image);

        image = loader.load(maps[1], false);
        Texture normals = new Texture2D();
        normals.setImage(image);

        this.gridMesh = new Grid(256, 256, 8.0F);

        Geometry cube = new Geometry("grid", this.gridMesh);

        Material mat_stl = new Material(this.assetManager, "MatDefs/Grid.j3md");

        mat_stl.setTexture("ColorMap", texture);
        cube.setMaterial(mat_stl);
        int xPlayer = 512;
        int yPlayer = 512;

        cube.setLocalTranslation(-(xPlayer - this.xBase), -70.0F, -(yPlayer - this.yBase));

        this.rootNode.attachChild(cube);
    }

    private World createWorld() throws Exception {
        BlockTypeIndex.initialize();

        DefaultLeafDatabase leafDb = new DefaultLeafDatabase(new File("mythruna.db"), 0);
        DefaultBlueprintDatabase bpDb = new DefaultBlueprintDatabase(new File("mythruna.db/blueprints"));

        LeafFileLocator locator = new DefaultLeafFileLocator(new File("mythruna.db"));
        ColumnFactory colFactory = WorldUtils.createDefaultColumnFactory(locator, 0);
        WorldDatabase worldDb = new ColumnWorldDatabase(leafDb, colFactory);

        return new DefaultWorld(worldDb, bpDb, null);
    }

    public int getColor(int type) {
        switch (type) {
            case 1:
                return 33023;
            case 2:
                return 128;
            case 3:
            case 4:
                return 4227072;
            case 5:
            case 6:
                return 8421504;
        }
        return 16711680;
    }

    protected static int color(int r, int g, int b) {
        return r << 16 | g << 8 | b;
    }

    public int getColor2(int type) {
        if (type == 0)
            return 16776960;
        type &= 127;
        if (type < colors.length)
            return colors[type];
        return 16711680;
    }

    public int getColor3(int type) {
        switch (type) {
            case 4:
                return rockColor;
            case 3:
                return sandColor;
            case 2:
            case 82:
                return grassColor;
            case 1:
                return dirtColor;
        }
        return waterColor;
    }

    public BufferedImage[] createImage() throws IOException {
        int size = 2048;
        int leafCount = size / 32;
        BufferedImage map = new BufferedImage(size, size, 2);
        int[] pixels = ((DataBufferInt) map.getRaster().getDataBuffer()).getData();

        BufferedImage normalMap = new BufferedImage(size, size, 2);
        int[] normals = ((DataBufferInt) map.getRaster().getDataBuffer()).getData();

        System.out.println("Building image from types...");
        long start = System.nanoTime();

        TestFactory factory = new TestFactory();
        int[][] elevations = factory.createElevations(this.xBase, this.yBase);
        int[][] types = WorldUtils.generateTypes(elevations, 2048, 57);

        int pos = 0;
        for (int j = 0; j < 2048; j++) {
            for (int i = 0; i < 2048; i++) {
                int h = elevations[i][j];
                int t2 = types[i][j];
                int color = getColor3(t2);

                if (h < 56) {
                    h = 56;
                    color = waterColor;
                }
                int v = (h & 0xFF) << 24 | color;
                pixels[(pos++)] = v;
            }
        }

        long end = System.nanoTime();
        System.out.println("Image produced in " + (end - start / 1000000.0D) + " ms");

        return new BufferedImage[]{map, normalMap};
    }

    private void xAlphaSpan(float[][] alpha, int last, int current, int y) {
        if (last < 0) {
            for (float a = 1.0F; a > 0.0F; a -= fallOff) {
                if (current <= 0)
                    break;
                alpha[(--current)][y] = a;
            }
            return;
        }
        if (current < 0) {
            for (float a = 1.0F; a > 0.0F; a -= fallOff) {
                if (last >= 1023)
                    break;
                alpha[(++last)][y] = a;
            }
            return;
        }
        if (last - 1 == current) {
            return;
        }
        for (float a = 1.0F; a > 0.0F; a -= fallOff) {
            if (current > 0)
                alpha[(--current)][y] = a;
            if (last < 1023) {
                alpha[(++last)][y] = a;
            }
            if (current <= last)
                return;
        }
    }

    private int mix(int a1, int a2, float mix) {
        float f = a1 * (1.0F - mix) + a2 * mix;
        return (int) f;
    }

    static {
        colors[1] = color(86, 69, 55);
        colors[2] = color(42, 67, 39);
        colors[3] = color(200, 176, 149);
        colors[4] = color(89, 84, 72);
        colors[5] = color(89, 84, 72);
        colors[6] = color(89, 84, 72);
        colors[7] = color(37, 66, 118);
        colors[8] = color(118, 83, 67);
        colors[9] = color(200, 176, 149);
        colors[10] = color(200, 200, 240);
        colors[11] = color(0, 0, 0);
        colors[12] = color(58, 70, 51);
    }
}