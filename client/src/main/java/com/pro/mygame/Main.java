package com.pro.mygame;

import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.shape.Box;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ImageRaster;
import com.jme3.util.BufferUtils;
import com.jme3.water.WaterFilter;
import com.radans.TerrainTiler.TerrainTiler;
import com.radans.TerrainTiler.TerrainTilerAction;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * test
 *
 * @author Radan Vowles
 */
public class Main extends SimpleApplication {

    public static void main(String[] args) {
        Main app = new Main();
        BufferUtils.setTrackDirectMemoryEnabled(true);
        Logger.getLogger("").setLevel(Level.WARNING);
        app.start();
    }
    private BitmapText hudText;
    private TerrainTiler terrainTiler;
    private BitmapText hudText2;
    private FilterPostProcessor fpp;
    private WaterFilter water;
    private Vector3f lightDir = new Vector3f(0f, -1f, 1f); // same as light source
    private float initialWaterHeight = 96f; // choose a value for your scene
    private boolean haveTiles = false;
    private boolean haveTerrain = false;
    private String mapName;
    private String tileDir;
    private int genPass = 0;
    private int tileX, tileZ, mapTileSize, aSize, nTiles, imgH, imgW;
    private FloatBuffer mapArray;
    private MotionEvent cMC;
    private CameraNode camNode;
    private MotionPath path;
    private boolean uJars = false;
    private Spatial tree;
    private ConcurrentHashMap<String, Spatial> treeList;
    
    @Override
    public void simpleInitApp() {
        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
        geom.setLocalTranslation(10f, 1f, 10f);

        rootNode.attachChild(geom);
        treeList = new ConcurrentHashMap<String, Spatial>(200);
        
        System.out.println("Loading tree...");
        tree = assetManager.loadModel("mygame/assets/Models/tree2/t2.j3o");
        if (tree == null) {
            System.out.println("Error loading tree model!");
        }
        tree.setLocalScale(2f);
        
        AmbientLight amb = new AmbientLight();
        amb.setColor(ColorRGBA.White);
        rootNode.addLight(amb);

        DirectionalLight dirl = new DirectionalLight();
        dirl.setDirection(lightDir);
        dirl.setColor(ColorRGBA.White);
        rootNode.addLight(dirl);

        tileDir = System.getProperty("user.home") + "\\MyTerrainTest\\";
        tileDir = tileDir.replace("\\", "/");
        tileDir += "tileSet01/";
        if (!new File(tileDir).isDirectory()) {
            new File(tileDir).mkdir();
        }
        mapName = "mygame/assets/Scenes/TestMap.png";
        if (new File(tileDir + "DIR-0000/TILE-0000.j3o").isFile()) {
            haveTiles = true;
            System.out.println("Using Directories...");
        } else if (new File(tileDir + "DIR-0000.jar").isFile()) {
            uJars = true;
            haveTiles = true;
            System.out.println("Using Jars...");
        }

        guiNode.detachAllChildren();
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");

        hudText = new BitmapText(guiFont, false);
        hudText.setSize(guiFont.getCharSet().getRenderedSize());
        hudText.setText("Camera Position");
        hudText.setLocalTranslation(300, hudText.getLineHeight(), 0);
        guiNode.attachChild(hudText);

        hudText2 = new BitmapText(guiFont, false);
        hudText2.setSize(guiFont.getCharSet().getRenderedSize());
        hudText2.setText("Memory Stats");
        hudText2.setLocalTranslation(300, hudText2.getLineHeight() * 8, 0);
        guiNode.attachChild(hudText2);

        flyCam.setMoveSpeed(100f);
        cam.setLocation(new Vector3f(64f, 60f, 64f));
        cam.lookAt(new Vector3f(100f, 60f, 100f), Vector3f.UNIT_Y);
        //cam.setFrustumFar(2048f);

        fpp = new FilterPostProcessor(assetManager);
        water = new WaterFilter(rootNode, lightDir);
        water.setWaterHeight(initialWaterHeight);
        water.setDeepWaterColor(ColorRGBA.Cyan);
        fpp.addFilter(water);
        viewPort.addProcessor(fpp);

        viewPort.setBackgroundColor(new ColorRGBA(0.2f, 0.4f, 1f, 1f));
        
        camNode = new CameraNode("Motion", cam);
        camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        camNode.setEnabled(false);
        path = new MotionPath();
        path.setCycle(true);
        path.addWayPoint(new Vector3f(350, 110, 500));
        path.addWayPoint(new Vector3f(1394, 110, 322));
        path.addWayPoint(new Vector3f(2208, 110, 1180));
        path.addWayPoint(new Vector3f(2750, 199, 1468));
        path.addWayPoint(new Vector3f(4750, 181, 1480));
        path.addWayPoint(new Vector3f(5550, 118, 1350));
        path.addWayPoint(new Vector3f(6580, 110, 2150));
        path.addWayPoint(new Vector3f(6185, 120, 3350));
        path.addWayPoint(new Vector3f(5374, 262, 3859));
        path.addWayPoint(new Vector3f(4990, 387, 3520));
        path.addWayPoint(new Vector3f(4820, 408, 3520));
        path.addWayPoint(new Vector3f(4570, 380, 3717));
        path.addWayPoint(new Vector3f(3030, 186, 4070));
        path.addWayPoint(new Vector3f(2460, 110, 4180));
        path.addWayPoint(new Vector3f(2000, 175, 4000));
        path.addWayPoint(new Vector3f(1360, 110, 4238));
        path.addWayPoint(new Vector3f(1660, 320, 2960));
        path.setCurveTension(0.4f);
        //path.enableDebugShape(assetManager, rootNode);
        cMC = new MotionEvent(camNode, path);
        cMC.setLoopMode(LoopMode.Loop);
        cMC.setDirectionType(MotionEvent.Direction.PathAndRotation);
        cMC.setRotation(new Quaternion().fromAngleAxis(0.26f,   new Vector3f(1,0,0)));
        cMC.setInitialDuration(240);
        rootNode.attachChild(camNode);
        
        // Enable below line to record a video.
        //stateManager.attach(new VideoRecorderAppState()); //start recording

    }

    @Override
    public void simpleUpdate(float tpf) {
        if (!haveTiles) {
            if (genPass == 0) {
                hudText.setText("No Tiles! Generating tiles from: " + mapName + "...");
                //cam.setLocation(new Vector3f(-100f, 400f, -100f));
            } else if (genPass == 1) {
                loadMapArray();
            } else if (genPass == 2) {
                hudText.setText("Map Loaded, Generating Tiles...");
                tileX = 0;
                tileZ = 0;
            } else if (genPass > 2) {
                // GenerateTile(tileX, tileZ, tileRootDir, terrainSize, terrainPreScale, maxHeight, alphaMapScale) 
                TerrainQuad tile = GenerateTile(tileX, tileZ, tileDir, 256, 8, 400, 1);
                //Notes: Map is 1024 / (256/8) = 32x32 of 256x256 tiles 
                //       with a 256x256 AlphaMap and max height of 400;
                if (tile == null) {
                    hudText.setText("ERROR GENERATING TILE: " + tileX + ", " + tileZ + "!!!!");
                } else {
                    tileX++;
                    if (tileX == nTiles) {
                        tileX = 0;
                        tileZ++;
                        if (tileZ == nTiles) {
                            haveTiles = true;
                        }
                    }
                }
                System.gc();
            }
            if (genPass == -1) {
                hudText.setText("ERROR GENERATING TILES! CANNOT CONTINUE!!!");
            } else {
                genPass++;
            }
        }
        if (!haveTerrain & haveTiles) {
            // TerrainTiler(camera, numTiles, useJars, terrainPostScale, tileRootDir, app)
            terrainTiler = new TerrainTiler(cam, 32, uJars, 2, tileDir, this);
            terrainTiler.setGridSize(7);  // grid can be 3,5,7 or 9. Defaults to 3
            terrainTiler.addActionHandler(new TerrainTilerAction() {

                public void tileAttached(Vector3f center, TerrainQuad tile) {
                    System.out.println("Adding trees to tile: "+tile.getName());
                    float tx = tile.getLocalTranslation().x;
                    float tz = tile.getLocalTranslation().z;
                    int tsize = terrainTiler.getTileSize()*terrainTiler.getTileScale();
                    int num = new Random().nextInt(20)+20;
                    for (int i=0; i<num; i++) {
                        float x = tx - tsize/2 + new Random().nextFloat()*tsize;
                        float z = tz - tsize/2 + new Random().nextFloat()*tsize;
                        float h = terrainTiler.getHeight(new Vector2f(x,z));
                        if (h > 100f & h < 300f) {
                            String id = tile.getName()+i;
                            tree.setLocalTranslation(x, h-0.25f, z);
                            tree.setName(id);
                            treeList.put(id, tree.clone());
                            rootNode.attachChild(treeList.get(id));
                        }
                    }
                }

                public void tileDetached(Vector3f center, TerrainQuad tile) {
                    System.out.println("Removing trees from tile: "+tile.getName());
                    Iterator it = treeList.keySet().iterator();
                    while (it.hasNext()) {
                        String id = (String) it.next();
                        if (id.startsWith(tile.getName())) {
                            rootNode.detachChild(treeList.get(id));
                            treeList.remove(id);
                        }
                    }
                }
            });
            terrainTiler.setEnabled(true);
            rootNode.attachChild(terrainTiler);
            haveTerrain = true;
            // Enable the 3 lines below to have the camera follow the path
            //flyCam.setEnabled(false);
            //camNode.setEnabled(true);
            //cMC.play();
        }
        if (haveTerrain) {
            float py = terrainTiler.getHeight(new Vector2f(cam.getLocation().x, cam.getLocation().z));
            String gText = String.format("Camera X:%03.3f Y:%03.3f Z:%03.3f", cam.getLocation().x, cam.getLocation().y, cam.getLocation().z);
            hudText.setText(gText);
            StringBuilder mText = new StringBuilder(128);
            BufferUtils.printCurrentDirectMemory(mText);
            hudText2.setText(mText);
            // keep above-ground
            float x = cam.getLocation().x;
            float z = cam.getLocation().z;
            if (py < initialWaterHeight) {
                py = initialWaterHeight;
            }
            py += 1.8f;
            if (cam.getLocation().y < py) {
                cam.setLocation(new Vector3f(x, py, z));
            }
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    /**
     * Generates a new set of Tiles from a map picture file Map must be a single
     * png file of 16-bit greyscale heightmap Map must be square ie x size == y
     * size Map size must be evenly divisible by numTiles AND result in a power
     * of 2 - best to make both a power of 2 (eg 8192 / 256 = 32x32 tiles)
     * Resulting TerrainQuad will then be scaled by mapScale to final size (x &
     * z only) hScale will adjust the final height range as needed (y).
     *
     * Initial textures will be based on 16-bit height: 0-25% = Sand 25%-60% =
     * Grass 60%-100% = Rock, A single AlphaMap generated for above as R G B
     * with A unpainted.
     *
     * @param tx - tile to generate at x
     * @param tz - tile to generate at z
     * @param tileRootDir - root dir for the tile location
     * @param qSize - Required size of resulting terrainQuad
     * @param mapScale - Map scale for each tile (ie scale of 2 makes a 128 size
     * map segment into a 256 quad)
     * @param mHeight - Max height for full white from HeightMap image
     * @param aScale - Scale of AlphaMap as multiplier ie: a 256 size quad with
     * a x2 aScale equals an alphaMap of 512 size. (more detail)
     *
     * TODO - add a alphamap file param so user can supply a premade splat
     * mapping (new function?)
     */
    public TerrainQuad GenerateTile(int tx, int tz, String tileRootDir, int qSize, int mapScale, int mHeight, int aScale) {
        mapTileSize = qSize / mapScale;
        nTiles = imgH / mapTileSize;
        aSize = qSize * aScale;
        if ((qSize & (qSize - 1)) != 0) {
            System.err.print("Resulting tile size is not a power of 2!! (" + qSize + ")\n");
            genPass = -1;
            return null;
        }
        // Parameters look good, now start building tiles...
        System.out.println("Generating " + nTiles + "x" + nTiles + " of "
                + mapTileSize + "x" + mapTileSize + " scaled by " + mapScale
                + " = " + qSize + "x" + qSize);
        int qPz = 1;                    // +1 for overlaps unless...
        if (tz == nTiles - 1) {         // if last row then no overlap!
            qPz = 0;
        }
        int qPx = 1;                    // +1 for overlaps unless...
        if (tx == nTiles - 1) {         // if last tile then no overlap!
            qPx = 0;
        }
        // grab each tile chunk and put it into its own heightmap array
        float hMap[] = new float[(qSize + 1) * (qSize + 1)];
        float mH = 0;
        for (int z = 0; z < qSize + qPz; z++) {
            for (int x = 0; x < qSize + qPx; x++) {
                int ix = (x + tx * qSize) / mapScale; // image x
                int iy = (z + tz * qSize) / mapScale; // image y
                float imgV, imgVx, imgVy, imgVz;
                imgV = mapArray.get(ix + imgH * (imgH - iy - 1));
                imgVx = imgVy = imgVz = imgV;
                if (ix < imgW - 1) {
                    imgVx = mapArray.get(ix + 1 + imgH * (imgH - iy - 1));
                }
                if (iy < imgH - 1) {
                    imgVy = mapArray.get(ix + imgH * (imgH - iy - 2));
                }
                if (ix < imgW - 1 & iy < imgH - 1) {
                    imgVz = mapArray.get(ix + 1 + imgH * (imgH - iy - 2));
                }
                float sx = (x + tx * qSize) - (ix * mapScale);
                sx /= mapScale;
                float sy = (z + tz * qSize) - (iy * mapScale);
                sy /= mapScale;
                imgV = (imgV * (1 - sx) + imgVx * sx) * (1 - sy) + (imgVy * (1 - sx) + imgVz * sx) * sy;
                imgV *= mHeight;
                hMap[x + (z * (qSize + 1))] = imgV;
                if (imgV > mH) {
                    mH = imgV;
                }
            }
        }
        // Create the new TerrainQuad 
        String tileName = String.format("TILE-%02d%02d.j3o", (tx % 64), (tz % 64));
        String dirName = String.format("DIR-%02d%02d/", (tx / 64), (tz / 64));
        String tqName = String.format("%02d%02d%02d%02d", (tx / 64), (tx % 64), (tz / 64), (tz % 64));
        int pSize = qSize / 4;
        System.out.println("Generating Tile: " + dirName + tileName + " Size: "
                + qSize + " Patch: " + pSize + " Alpha: " + aSize);
        TerrainQuad tQuad = new TerrainQuad(tqName, pSize + 1, qSize + 1, hMap);

        // TODO Create a better alphamap for it
        // Create a new blank image as RGBA8 (4 bytes per pixel)
        Image imgAlphaMap = new Image(Image.Format.RGBA8, aSize,
                aSize, BufferUtils.createByteBuffer(aSize * aSize * 4));
        // Create a ImageRaster to modify it
        ImageRaster imgRaster = ImageRaster.create(imgAlphaMap);
        // change the pixels
        ColorRGBA red = new ColorRGBA(1f, 0f, 0f, 0f);
        ColorRGBA blu = new ColorRGBA(0f, 1f, 0f, 0f);
        ColorRGBA grn = new ColorRGBA(0f, 0f, 1f, 0f);
        ColorRGBA alp = new ColorRGBA(0f, 0f, 0f, 1f);
        for (int iy = 0; iy < aSize; iy++) {
            int iY = iy / aScale;
            for (int ix = 0; ix < aSize; ix++) {
                // TODO implement nicer paint ideas+
                int iX = ix / aScale;
                float h = hMap[iX + (iY * (qSize + 1))];
                if (h < (mHeight * 0.25f)) {
                    imgRaster.setPixel(ix, aSize - iy - 1, red); // DiffuseMap
                } else if (h < (mHeight * 0.6f)) {
                    imgRaster.setPixel(ix, aSize - iy - 1, blu); // DiffuseMap_1
                } else if (h < (mHeight * 0.7f)) {
                    imgRaster.setPixel(ix, aSize - iy - 1, alp); // DiffuseMap_1
                } else {
                    imgRaster.setPixel(ix, aSize - iy - 1, grn); // DiffuseMap_2
                }
                // (try painting alpha values in a paint program!!)
                // (or colours without the alpha!!)
            }
        }
        Material mat = new Material(assetManager,
                "Common/MatDefs/Terrain/TerrainLighting.j3md");
        float mScale = pSize;
        mScale = 1 / (128f / mScale);
        // load diffuse map 0  -  TODO Add Normal map
        Texture diffuse_0 = assetManager.loadTexture("mygame/assets/Textures/diffuse_0.png");
        diffuse_0.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("DiffuseMap", diffuse_0);
        mat.setFloat("DiffuseMap_0_scale", mScale);
        // load diffuse map 1  -  TODO Add Normal map
        Texture diffuse_1 = assetManager.loadTexture("mygame/assets/Textures/diffuse_1.png");
        diffuse_1.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("DiffuseMap_1", diffuse_1);
        mat.setFloat("DiffuseMap_1_scale", mScale);
        // load diffuse map 2  -  TODO Add Normal map
        Texture diffuse_2 = assetManager.loadTexture("mygame/assets/Textures/diffuse_2.png");
        diffuse_2.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("DiffuseMap_2", diffuse_2);
        mat.setFloat("DiffuseMap_2_scale", mScale);
        // load diffuse map 3  -  TODO Add Normal map
        Texture diffuse_3 = assetManager.loadTexture("mygame/assets/Textures/diffuse_3.png");
        diffuse_3.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("DiffuseMap_3", diffuse_3);
        mat.setFloat("DiffuseMap_3_scale", mScale);

        // Create a Texture for the AlphaMap image and load it
        Texture texAlpha = new Texture2D(imgAlphaMap);
        mat.setTexture("AlphaMap", texAlpha);

        mat.setBoolean("WardIso", true);
        mat.setBoolean("useTriPlanarMapping", true);

        //Material mat = new Material( assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
        // set new Material to the terrainQuad
        tQuad.setMaterial(mat);

        // Write it out to the file
        //tQuad.updateGeometricState();
        //tQuad.recalculateAllNormals();
        BinaryExporter bExp = BinaryExporter.getInstance();
        float pc = (tx + (tz * nTiles));
        pc = pc / (nTiles * nTiles) * 100f;
        File file = new File(tileRootDir + dirName + tileName);
        file.setWritable(true);
        file.canRead();
        file.canWrite();
        try {
            bExp.save(tQuad, file);
        } catch (IOException ex) {
            Logger.getLogger(TerrainTiler.class.getName()).log(Level.SEVERE,
                    "Error: Failed to save Tile: " + tileRootDir + tileName, ex);
        }
        hudText2.setText("Saved tile: " + tileRootDir + dirName + tileName + " completed:" + pc + "%");
        return tQuad;
    }

    private void loadMapArray() {
        Texture tex = (Texture) assetManager.loadTexture(mapName);
        Image img = tex.getImage();
        ImageRaster imgR = ImageRaster.create(img);
        if (!img.getFormat().equals(Image.Format.Luminance16)) {
            System.err.print("Map file is not 16-bit greyscale!!\n");
            genPass = -1;
            return;
        }
        imgH = imgR.getHeight();
        imgW = imgR.getWidth();
        if (imgH != imgW) {
            System.err.print("Map file is not square!!\n");
            genPass = -1;
            return;
        }
        mapArray = BufferUtils.createFloatBuffer(imgW * imgH);
        for (int z = 0; z < imgH; z++) {
            for (int x = 0; x < imgW; x++) {
                mapArray.put(imgR.getPixel(x, z).r);
            }
        }
    }
}
