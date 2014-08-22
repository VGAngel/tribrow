package com.pro.radans.tteditor;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.niftygui.RenderImageJme;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.terrain.geomipmap.TerrainGrid;
import com.jme3.terrain.geomipmap.TerrainGridLodControl;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.grid.ImageTileLoader;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.heightmap.Namer;
import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ImageRaster;
import com.jme3.util.BufferUtils;
import com.jme3.util.MemoryUtils;
import com.radans.TerrainTiler.TerrainTiler;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.*;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.ImageRenderer;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.spi.render.RenderImage;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.xml.xpp3.Attributes;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import jme3tools.converters.ImageToAwt;

/**
 * Tiled Terrain Editor
 *
 * @author Radan Vowles
 * @version a14.06.01
 */
public class Main extends SimpleApplication implements ScreenController,Controller {

    private Nifty nifty;
    private BitmapText statusText;
    private boolean loadInit;
    private String mapFile;
    private volatile String heightMapFile;
    private volatile String imageFile;
    private volatile String fSep;
    private String loadType;
    private volatile String saveFile = null;
    private volatile Texture imageBuffer = null;
    private volatile int imageFileX = 0;
    private volatile int imageFileY = 0;
    private volatile float loadProgress = 0f;
    private volatile Format imageFileType;
    private volatile String loadFileFor;
    private Texture[] texColor;
    private Texture[] texNormal;
    private Texture[] texSpecular;
    private volatile String loadDirRoot;
    private volatile int heightMapFileX;
    private volatile int heightMapFileY;
    private Future myThread = null;
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(4);
    private volatile int mapTilesX;
    private volatile int mapTilesZ;
    private volatile int mapTileSize;
    private volatile String saveDirRoot;
    private boolean saveinit;
    private String saveFileFor;
    private volatile float mapScaleX;
    private volatile float mapScaleZ;
    private boolean haveTerrain = false;
    private String terrainDirRoot;
    private TerrainGrid terrainGrid;
    private Integer maxHeight;
    private Integer mapRevision;
    private TerrainTiler tiledTerrain;
    private boolean useJars;

    @Override
    public void onFocus(boolean getFocus) {
        
    }

    @Override
    public boolean inputEvent(NiftyInputEvent inputEvent) {
        return false;
    }

    private enum TType {

        GRID, TILED
    }
    private TType terrainType;
    
    private enum TTStates {

        START, LOAD, GENERATE, EDITOR, IMAGELOAD, FLAT, NOISE,
        IMAGE, IMAGEGENPNG, IMAGEGENMAP, FLATGEN, NOISEGEN, TGRID
    }
    private TTStates ttState;

    public static void main(String[] args) {
        Main app = new Main();
        BufferUtils.setTrackDirectMemoryEnabled(true);
        app.setPauseOnLostFocus(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        ttState = TTStates.START;

        // setup Start menu
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, viewPort);
        nifty = niftyDisplay.getNifty();
        nifty.fromXml("radans/tteditor/assets/Interface/StartGui.xml", "start", this);
        guiViewPort.addProcessor(niftyDisplay);
        flyCam.setDragToRotate(true);
        //nifty.setDebugOptionPanelColors(true);
        texColor = new Texture[16];
        texNormal = new Texture[16];
        texSpecular = new Texture[16];

        fSep = System.getProperty("file.separator");
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        statusText = new BitmapText(guiFont, false);
        statusText.setSize(guiFont.getCharSet().getRenderedSize());
        statusText.setText("Hello World");
        statusText.setLocalTranslation(300, statusText.getLineHeight(), 0);
        guiNode.attachChild(statusText);

        Box b = new Box(1, 1, 1);
        Geometry geom = new Geometry("Box", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        Texture tex = assetManager.loadTexture("Interface/Logo/Monkey.jpg");
        mat.setTexture("ColorMap", tex);
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
        
        DirectionalLight dLight = new DirectionalLight();
        dLight.setDirection(Vector3f.UNIT_XYZ.negate());
        rootNode.addLight(dLight);
            /** A white ambient light source. */ 
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White);
        rootNode.addLight(ambient); 

        cam.setLocation(new Vector3f(-10f, 10f, -10f));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        rootNode.attachChild(geom);
    }

    @Override
    public void simpleUpdate(float tpf) {
        long dMemTot = MemoryUtils.getDirectMemoryCount();
        long dMemUse = MemoryUtils.getDirectMemoryUsage();
        switch (ttState) {
            case START:
                statusText.setText("START ("+dMemUse+"/"+dMemTot+")");
                break;
            case LOAD:
                statusText.setText("LOAD ("+dMemUse+"/"+dMemTot+")");
                if (!loadInit && nifty.getCurrentScreen().getScreenId().contentEquals("loadfile")) {
                    loadType = ".map";
                    mapFile = null;
                    initLoadFile(System.getProperty("user.dir"));
                    loadInit = true;
                }
                if (mapFile != null && nifty.getCurrentScreen().getScreenId().contentEquals("loadingScreen")) {
                    // we have selected a map file: load it and goto editor
                    Logger.getLogger(Main.class.getName()).log(Level.INFO, "Loading Map....");
                    haveTerrain = false;
                    terrainDirRoot = loadDirRoot;
                    if (mapFile.contentEquals("TerrainGrid.map")) {
                        nifty.gotoScreen("terraingrid");
                        ttState = TTStates.TGRID;
                        terrainType = TType.GRID;
                    } else if (loadMap()) {
                        nifty.gotoScreen("editorhud");
                        ttState = TTStates.EDITOR;
                        terrainType = TType.TILED;
                    } else {
                        nifty.gotoScreen("start");
                        mapFile = null;
                    }
                }
                break;
            case GENERATE:
                statusText.setText("GENERATE ("+dMemUse+"/"+dMemTot+")");
                break;
            case EDITOR:
                statusText.setText("EDITOR ("+dMemUse+"/"+dMemTot+")");
                // todo setup the editorhud fields and textures.
                if (haveTerrain) {
                    int x = (int) cam.getLocation().x/tiledTerrain.getTerrainSize();
                    int z = (int) cam.getLocation().z/tiledTerrain.getTerrainSize();
                    Element location = nifty.getCurrentScreen().findElementByName("GridLocation");
                    location.getRenderer(TextRenderer.class).setText("Grid Location: "+x+","+z);
                }
                break;
            case TGRID:
                statusText.setText("TGRID ("+dMemUse+"/"+dMemTot+")");
                if (!haveTerrain) {
                    terrainType = TType.GRID;
                    createTerrainGrid(terrainDirRoot);
                } else {
                    int x = (int) terrainGrid.getCurrentCell().x;
                    int z = (int) terrainGrid.getCurrentCell().z;
                    Element location = nifty.getCurrentScreen().findElementByName("gridLocation");
                    location.getRenderer(TextRenderer.class).setText("Grid Location: "+x+","+z);
                }
                break;
            case IMAGELOAD:
                statusText.setText("IMAGELOAD ("+dMemUse+"/"+dMemTot+")");
                if (!loadInit && nifty.getCurrentScreen().getScreenId().contentEquals("loadfile")) {
                    loadType = ".png";
                    imageFile = null;
                    initLoadFile(System.getProperty("user.dir"));
                    loadInit = true;
                }
                if (imageFile != null) {
                    // we have selected an image file: load it and goto IMAGE
                    if (myThread == null) {
                        // start the callable to load the image
                        myThread = executor.submit(loadImage);
                    } else if (myThread.isDone()) {
                        try {
                            Object result = myThread.get();
                            if (result.equals(true)) {
                                nifty.gotoScreen("image");
                                ttState = TTStates.IMAGE;
                                if (loadFileFor.contentEquals("heightmap")) {
                                    loadInit = false;
                                    heightMapFile = loadDirRoot.concat(fSep).concat(imageFile);
                                    heightMapFileX = imageFileX;
                                    heightMapFileY = imageFileY;
                                }   // else we just popped out for a texture
                            } else {
                                if (loadFileFor.contentEquals("heightmap")) {
                                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE,
                                            "Image load failure - exiting to start menu!");
                                    nifty.gotoScreen("start");
                                    ttState = TTStates.START;
                                    mapFile = null;
                                } else {
                                    nifty.gotoScreen("image");
                                    ttState = TTStates.IMAGE;
                                    loadFileFor = "";
                                }
                            }
                        } catch (InterruptedException  ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ExecutionException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        myThread = null;
                    } else if (myThread.isCancelled()) {
                        myThread = null;   // retry
                    } else {
                        // myThread callable is active and not done or cancelled
                        // update the progress 
                        Element element = nifty.getScreen(
                                "loadingScreen").findElementByName("loadingText");
                        int val = (int) (100 * loadProgress);
                        element.getRenderer(TextRenderer.class).setText(
                                "Loading... Please Wait..." + val + "%");
                        element = nifty.getScreen("loadingScreen").findElementByName("progressBar");
                        int px = (int) (32 + (element.getParent().getWidth() - 32) * loadProgress);
                        element.setConstraintWidth(new SizeValue(px + "px"));
                        element.getParent().layoutElements();
                    }
                }
                break;
            case FLAT:
                statusText.setText("FLAT ("+dMemUse+"/"+dMemTot+")");
                break;
            case NOISE:
                statusText.setText("NOISE ("+dMemUse+"/"+dMemTot+")");
                break;
            case IMAGEGENPNG:
                statusText.setText("IMAGEGENPNG ("+dMemUse+"/"+dMemTot+")");
                if (!saveinit && nifty.getCurrentScreen().getScreenId().contentEquals("savefile")) {
                    initSaveFile(saveDirRoot);
                    saveinit = true;
                }
                if (myThread != null) {
                    // tile generation is active
                    if (myThread.isDone()) {
                        // png tile generation complete - Start up TerrainGrid
                        nifty.gotoScreen("terraingrid");
                        ttState = TTStates.TGRID;
                        myThread = null;
                        haveTerrain = false;
                    } else if (myThread.isCancelled()) {
                        // cancelled = return to generator??
                        nifty.gotoScreen("image");
                        ttState = TTStates.IMAGE;
                        myThread = null;
                    } else {
                        // in progress - update progress
                        Element element = nifty.getScreen(
                                "loadingScreen").findElementByName("loadingText");
                        int val = (int) (100 * loadProgress);
                        element.getRenderer(TextRenderer.class).setText(
                                "Generating Tiles ... " + val + "% complete");
                        element = nifty.getScreen("loadingScreen").findElementByName("progressBar");
                        int px = (int) (32 + (element.getParent().getWidth() - 32) * loadProgress);
                        element.setConstraintWidth(new SizeValue(px + "px"));
                        element.getParent().layoutElements();
                    }
                }
                break;
            case IMAGEGENMAP:
                statusText.setText("IMAGEGENMAP ("+dMemUse+"/"+dMemTot+")");
                if (!saveinit && nifty.getCurrentScreen().getScreenId().contentEquals("savefile")) {
                    initSaveFile(saveDirRoot);
                    saveinit = true;
                }
                if (myThread != null) {
                    // tile generation is active
                    if (myThread.isDone()) {
                        // map tile generation complete - Start up Editor
                        myThread = null;
                        haveTerrain = false;
                        if (loadMap()) {
                            nifty.gotoScreen("editorhud");
                            ttState = TTStates.EDITOR;
                        } else {
                            nifty.gotoScreen("start");
                            mapFile = null;
                        }
                    } else if (myThread.isCancelled()) {
                        // cancelled = return to generator??
                        nifty.gotoScreen("image");
                        ttState = TTStates.IMAGE;
                        myThread = null;
                    } else {
                        // in progress - update progress
                        Element element = nifty.getScreen(
                                "loadingScreen").findElementByName("loadingText");
                        int val = (int) (100 * loadProgress);
                        element.getRenderer(TextRenderer.class).setText(
                                "Generating Tiles ... " + val + "% complete");
                        element = nifty.getScreen("loadingScreen").findElementByName("progressBar");
                        int px = (int) (32 + (element.getParent().getWidth() - 32) * loadProgress);
                        element.setConstraintWidth(new SizeValue(px + "px"));
                        element.getParent().layoutElements();
                    }
                }
                break;
            case FLATGEN:
                statusText.setText("FLATGEN ("+dMemUse+"/"+dMemTot+")");
                break;
            case NOISEGEN:
                statusText.setText("NOISEGEN ("+dMemUse+"/"+dMemTot+")");
                break;
            case IMAGE:
                statusText.setText("IMAGE ("+dMemUse+"/"+dMemTot+")");
                if (!loadInit && nifty.getCurrentScreen().getScreenId().contentEquals("image")) {
                    RenderImage rImg = new RenderImageJme((Texture2D) imageBuffer);
                    NiftyImage img = new NiftyImage(nifty.getRenderEngine(), rImg);
                    Element element = nifty.getCurrentScreen().findElementByName("miniMapGen");
                    element.getRenderer(ImageRenderer.class).setImage(img);
                    element = nifty.getCurrentScreen().findElementByName("imgSize");
                    element.getRenderer(TextRenderer.class).setText("Image Size: " + heightMapFileX + " x " + heightMapFileY);
                    element = nifty.getCurrentScreen().findElementByName("imgType");
                    element.getRenderer(TextRenderer.class).setText("Image Type: " + imageFileType);
                    element = nifty.getCurrentScreen().findElementByName("tileSize");
                    element.getNiftyControl(DropDown.class).clear();
                    element.getNiftyControl(DropDown.class).addItem(256);
                    element.getNiftyControl(DropDown.class).addItem(512);
                    element.getNiftyControl(DropDown.class).addItem(1024);
                    element.getNiftyControl(DropDown.class).addItem(2048);
                    element = nifty.getCurrentScreen().findElementByName("maxHeight");
                    element.getNiftyControl(DropDown.class).clear();
                    int i = 512;
                    while (i <= 32768) {
                        element.getNiftyControl(DropDown.class).addItem(i);
                        i *= 2;
                    }
                    maxHeight = 512;
                    int xTiles = heightMapFileX / 256;
                    int zTiles = heightMapFileY / 256;
                    element = nifty.getCurrentScreen().findElementByName("numTilesX");
                    element.getNiftyControl(Slider.class).setValue(xTiles);
                    element = nifty.getCurrentScreen().findElementByName("numTilesZ");
                    element.getNiftyControl(Slider.class).setValue(zTiles);
                    loadInit = true;
                    onNumTilesChangeX("numTilesX", null);
                    onNumTilesChangeZ("numTilesZ", null);
                }
                if (loadFileFor.startsWith("color")) {
                    int idx = Integer.parseInt(loadFileFor.substring(5));
                    Logger.getLogger(Main.class.getName()).log(Level.INFO,
                            "Setting Color Texture at {0}", idx);
                    texColor[idx - 1] = imageBuffer;
                    RenderImage rImg = new RenderImageJme((Texture2D) imageBuffer);
                    NiftyImage img = new NiftyImage(nifty.getRenderEngine(), rImg);
                    Element element = nifty.getScreen("image").findElementByName(loadFileFor);
                    element.getRenderer(ImageRenderer.class).setImage(img);
                    loadFileFor = "";
                }
                if (loadFileFor.startsWith("normal")) {
                    int idx = Integer.parseInt(loadFileFor.substring(6));
                    Logger.getLogger(Main.class.getName()).log(Level.INFO,
                            "Setting Normal Texture at {0}", idx);
                    texNormal[idx - 1] = imageBuffer;
                    RenderImage rImg = new RenderImageJme((Texture2D) imageBuffer);
                    NiftyImage img = new NiftyImage(nifty.getRenderEngine(), rImg);
                    Element element = nifty.getScreen("image").findElementByName(loadFileFor);
                    element.getRenderer(ImageRenderer.class).setImage(img);
                    loadFileFor = "";
                }
                break;
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    @Override
    public void destroy() {
        super.destroy();
        executor.shutdown();
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        // 
    }

    @Override
    public void bind(Nifty nifty, Screen screen, Element element, Properties properties, Attributes attributes) {
        this.nifty = nifty;
    }

    @Override
    public void init(Properties properties, Attributes attributes) {

    }

    @Override
    public void onStartScreen() {
        // 
    }

    @Override
    public void onEndScreen() {
        // 
    }

    public void quitApp() {
        this.stop();
    }

    public void guiAction(String value) {
        if (value.contentEquals("LOAD")) {
                ttState = TTStates.LOAD;
                nifty.gotoScreen("loadfile");
                loadFileFor = "map";
                loadInit = false;
        } else if (value.contentEquals("GENERATE")) {
                ttState = TTStates.GENERATE;
                nifty.gotoScreen("generate");
        } else if (value.contentEquals("IMAGELOAD")) {
                ttState = TTStates.IMAGELOAD;
                nifty.gotoScreen("loadfile");
                loadFileFor = "heightmap";
                loadInit = false;
        } else if (value.contentEquals("FLAT")) {
                ttState = TTStates.FLAT;
                //TODO:
        } else if (value.contentEquals("NOISE")) {
                ttState = TTStates.NOISE;
                //TODO:
        } else if (value.contentEquals("START")) {
                ttState = TTStates.START;
                nifty.gotoScreen("start");
        }
    }

    public void loadAction(String value) {
        TextField label = nifty.getScreen("loadfile").findNiftyControl("loadRoot", TextField.class);
        ListBox listbox = nifty.getScreen("loadfile").findNiftyControl("loadListBox", ListBox.class);
        String chosen = (String) listbox.getFocusItem();
        if (value.contentEquals("LOAD")) {
                Logger.getLogger(Main.class.getName()).log(Level.INFO, "Chose: {0}{1}{2}",
                        new Object[]{label.getRealText(), fSep, chosen});
                if (chosen.toLowerCase().endsWith(".map")) {
                    mapFile = chosen;
                    nifty.gotoScreen("loadingScreen");
                } else if (chosen.toLowerCase().endsWith(".png")) {
                    imageFile = chosen;
                    nifty.gotoScreen("loadingScreen");
                } else {
                    String dir = label.getRealText().concat(fSep).concat(chosen);
                    initLoadFile(dir);
                }
        } else if (value.contentEquals("CANCEL")) {
            if (loadFileFor.contentEquals("heightmap")) {
                ttState = TTStates.GENERATE;
                nifty.gotoScreen("generate");
            } else if (loadFileFor.contentEquals("map")) {
                ttState = TTStates.START;
                nifty.gotoScreen("start");
            } else {
                ttState = TTStates.IMAGE;
                nifty.gotoScreen("image");
            }
            loadFileFor = "";
        }
    }

    public void genAction(String value) {
        if (value.contentEquals("GENERATE")) {
                int numTextures = 0;
                for (int i = 0; i < 4; i++) {
                    if (texColor[i] != null) {
                        numTextures++;
                    }
                }
                if (numTextures == 0) {
                    Logger.getLogger(Main.class.getName()).log(Level.INFO, "No Textures set so generating Image tiles only!");
                    nifty.gotoScreen("pngGenConfirm");
                    ttState = TTStates.IMAGEGENPNG;
                } else {
                    Logger.getLogger(Main.class.getName()).log(Level.INFO, "One or more Textures set so generating full map tiles!");
                    nifty.gotoScreen("mapGenConfirm");
                    ttState = TTStates.IMAGEGENMAP;
                }
        } else if (value.contentEquals("CANCEL")) {
                ttState = TTStates.START;
                nifty.gotoScreen("start");
        }
    }

    public void imageAction(String value) {
        if (value.startsWith("del")) {
            value = value.substring(3);
            if (value.startsWith("color")) {
                int idx = Integer.parseInt(value.substring(5));
                texColor[idx - 1] = null;
                Element element = nifty.getScreen("image").findElementByName(value);
                RenderImageJme rImg = new RenderImageJme((Texture2D) assetManager.loadTexture("radans/tteditor/assets/Textures/blank.png"));
                NiftyImage image = new NiftyImage(nifty.getRenderEngine(), rImg);
                element.getRenderer(ImageRenderer.class).setImage(image);
            } else if (value.startsWith("normal")) {
                int idx = Integer.parseInt(value.substring(6));
                texNormal[idx - 1] = null;
                Element element = nifty.getScreen("image").findElementByName(value);
                RenderImageJme rImg = new RenderImageJme((Texture2D) assetManager.loadTexture("radans/tteditor/assets/Textures/blank.png"));
                NiftyImage image = new NiftyImage(nifty.getRenderEngine(), rImg);
                element.getRenderer(ImageRenderer.class).setImage(image);
            }
        } else {
            loadFileFor = value;
            loadType = "png";
            imageFile = null;
            ttState = TTStates.IMAGELOAD;
            nifty.gotoScreen("loadfile");
            TextField label = nifty.getScreen("loadfile").findNiftyControl("loadRoot", TextField.class);
            initLoadFile(label.getRealText());
        }
    }

    public void saveAction(String value) {
        if (value.contentEquals("PNG")) {
                    File file = new File(heightMapFile);
                    saveDirRoot = file.getParent();
                    saveFile = "";
                    saveinit = false;
                    saveFileFor = "PNG";
                    nifty.gotoScreen("savefile");
        } else if (value.contentEquals("MAP")) {
                    File file = new File(heightMapFile);
                    saveDirRoot = file.getParent();
                    saveFile = "";
                    saveinit = false;
                    saveFileFor = "MAP";
                    nifty.gotoScreen("savefile");
        } else if (value.contentEquals("GEN")) {
            // got location and name now generate with progress
            TextField label = nifty.getScreen("savefile").findNiftyControl("saveName", TextField.class);
            saveFile = label.getRealText();
            label = nifty.getScreen("savefile").findNiftyControl("saveRoot", TextField.class);
            saveDirRoot = label.getRealText();
            if (saveFileFor.contentEquals("PNG")) {
                nifty.gotoScreen("loadingScreen");
                loadProgress = 0f;
                myThread = executor.submit(genPngTiles);
            } else if (saveFileFor.contentEquals("MAP")) {
                nifty.gotoScreen("loadingScreen");
                loadProgress = 0f;
                myThread = executor.submit(genMapTiles);
            }
        } else if (value.contentEquals("CANCEL")) {
                nifty.gotoScreen("image");
                ttState = TTStates.IMAGE;
        }
    }

    @NiftyEventSubscriber(id = "loadListBox")
    public void onLoadSelectionChanged(final String id, final ListBoxSelectionChangedEvent<String> event) {
        if (loadInit) {
            ListBox listbox = nifty.getScreen("loadfile").findNiftyControl("loadListBox", ListBox.class);
            String chosen = (String) listbox.getFocusItem();
            if (chosen != null && !chosen.toLowerCase().endsWith("map") && !chosen.toLowerCase().endsWith("png")) {
                loadAction("LOAD");
            }
        }
    }

    @NiftyEventSubscriber(id = "saveListBox")
    public void onSaveSelectionChanged(final String id, final ListBoxSelectionChangedEvent<String> event) {
        if (loadInit) {
            TextField name = nifty.getScreen("savefile").findNiftyControl("saveName", TextField.class);
            TextField label = nifty.getScreen("savefile").findNiftyControl("saveRoot", TextField.class);
            ListBox listbox = nifty.getScreen("savefile").findNiftyControl("saveListBox", ListBox.class);
            String chosen = (String) listbox.getFocusItem();
            if (chosen != null) {
                Logger.getLogger(Main.class.getName()).log(Level.INFO, "Chose: {0}{1}{2}",
                        new Object[]{label.getRealText(), fSep, chosen});
                String dir = label.getRealText().concat(fSep).concat(chosen);
                saveFile = name.getRealText();
                initSaveFile(dir);
            }
        }
    }

    @NiftyEventSubscriber(pattern = "slider.*")
    public void onSliderMove(String id, SliderChangedEvent event) {
        if (loadInit) {
            Slider slider1 = nifty.getScreen("image").findNiftyControl("slider1", Slider.class);
            Slider slider2 = nifty.getScreen("image").findNiftyControl("slider2", Slider.class);
            Slider slider3 = nifty.getScreen("image").findNiftyControl("slider3", Slider.class);
            int moved = event.getSlider().getId().charAt(6) - 48;
            switch (moved) {
                case 1:     // slider 1 has moved
                    if (slider2.getValue() < slider1.getValue()) {
                        slider2.setValue(slider1.getValue());
                    }
                    if (slider3.getValue() < slider2.getValue()) {
                        slider3.setValue(slider2.getValue());
                    }
                    break;
                case 2:     // slider 2 has moved
                    if (slider1.getValue() > slider2.getValue()) {
                        slider1.setValue(slider2.getValue());
                    }
                    if (slider3.getValue() < slider2.getValue()) {
                        slider3.setValue(slider2.getValue());
                    }
                    break;
                case 3:     // slider 3 has moved
                    if (slider2.getValue() > slider3.getValue()) {
                        slider2.setValue(slider3.getValue());
                    }
                    if (slider1.getValue() > slider2.getValue()) {
                        slider1.setValue(slider2.getValue());
                    }
                    break;
            }
            Element text = nifty.getScreen("image").findElementByName("s1value");
            text.getRenderer(TextRenderer.class).setText(" " + slider1.getValue() + "%");
            text = nifty.getScreen("image").findElementByName("s2value");
            text.getRenderer(TextRenderer.class).setText(" " + slider2.getValue() + "%");
            text = nifty.getScreen("image").findElementByName("s3value");
            text.getRenderer(TextRenderer.class).setText(" " + slider3.getValue() + "%");
        }
    }

    @NiftyEventSubscriber(id = "numTilesX")
    public void onNumTilesChangeX(String id, SliderChangedEvent event) {
        if (loadInit) {
            Element element = nifty.getCurrentScreen().findElementByName("tileSize");
            int tileSize = Integer.parseInt(element.getNiftyControl(DropDown.class).getSelection().toString());
            element = nifty.getCurrentScreen().findElementByName("numTilesX");
            int numTilesX = (int) element.getNiftyControl(Slider.class).getValue();
            int unitsX = numTilesX * tileSize;
            element = nifty.getCurrentScreen().findElementByName("numTilesZ");
            int numTilesZ = (int) element.getNiftyControl(Slider.class).getValue();
            int unitsZ = numTilesZ * tileSize;
            float scale = unitsX / (float) heightMapFileX;
            element = nifty.getCurrentScreen().findElementByName("xTiles");
            element.getRenderer(TextRenderer.class).setText("Tiles for X: " + numTilesX);
            element = nifty.getCurrentScreen().findElementByName("tileScaleX");
            element.getRenderer(TextRenderer.class).setText("1 to " + scale);
            element = nifty.getScreen("image").findElementByName("worldSize");
            String wSize = String.valueOf(unitsX).concat(" x ").concat(String.valueOf(unitsZ));
            element.getRenderer(TextRenderer.class).setText(wSize);
            mapTilesX = numTilesX;
            mapTilesZ = numTilesZ;
            mapTileSize = tileSize;
            mapScaleX = scale;
        }
    }

    @NiftyEventSubscriber(id = "numTilesZ")
    public void onNumTilesChangeZ(String id, SliderChangedEvent event) {
        if (loadInit) {
            Element element = nifty.getCurrentScreen().findElementByName("tileSize");
            int tileSize = Integer.parseInt(element.getNiftyControl(DropDown.class).getSelection().toString());
            element = nifty.getCurrentScreen().findElementByName("numTilesX");
            int numTilesX = (int) element.getNiftyControl(Slider.class).getValue();
            int unitsX = numTilesX * tileSize;
            element = nifty.getCurrentScreen().findElementByName("numTilesZ");
            int numTilesZ = (int) element.getNiftyControl(Slider.class).getValue();
            int unitsZ = numTilesZ * tileSize;
            float scale = unitsZ / (float) heightMapFileY;
            element = nifty.getCurrentScreen().findElementByName("zTiles");
            element.getRenderer(TextRenderer.class).setText("Tiles for Z: " + numTilesZ);
            element = nifty.getCurrentScreen().findElementByName("tileScaleZ");
            element.getRenderer(TextRenderer.class).setText("1 to " + scale);
            element = nifty.getScreen("image").findElementByName("worldSize");
            String wSize = String.valueOf(unitsX).concat(" x ").concat(String.valueOf(unitsZ));
            element.getRenderer(TextRenderer.class).setText(wSize);
            mapTilesX = numTilesX;
            mapTilesZ = numTilesZ;
            mapTileSize = tileSize;
            mapScaleZ = scale;
        }
    }

    @NiftyEventSubscriber(id = "tileSize")
    public void onTileSizeChange(String id, DropDownSelectionChangedEvent event) {
        if (loadInit) {
            Element element = nifty.getCurrentScreen().findElementByName("tileSize");
            int tileSize = Integer.parseInt(element.getNiftyControl(DropDown.class).getSelection().toString());
            element = nifty.getCurrentScreen().findElementByName("numTilesX");
            int numTilesX = (int) element.getNiftyControl(Slider.class).getValue();
            int unitsX = numTilesX * tileSize;
            element = nifty.getCurrentScreen().findElementByName("numTilesZ");
            int numTilesZ = (int) element.getNiftyControl(Slider.class).getValue();
            int unitsZ = numTilesZ * tileSize;
            float scale = unitsX / (float) heightMapFileX;
            element = nifty.getCurrentScreen().findElementByName("tileScaleX");
            element.getRenderer(TextRenderer.class).setText("1 to " + scale);
            mapScaleX = scale;
            scale = unitsZ / (float) heightMapFileY;
            element = nifty.getCurrentScreen().findElementByName("tileScaleZ");
            element.getRenderer(TextRenderer.class).setText("1 to " + scale);
            mapScaleZ = scale;
            element = nifty.getScreen("image").findElementByName("worldSize");
            String wSize = String.valueOf(unitsX).concat(" x ").concat(String.valueOf(unitsZ));
            element.getRenderer(TextRenderer.class).setText(wSize);
            mapTilesX = numTilesX;
            mapTilesZ = numTilesZ;
            mapTileSize = tileSize;
        }
    }
    
      @NiftyEventSubscriber(id = "maxHeight")
    public void onMaxHeightChange(String id, DropDownSelectionChangedEvent event) {
        if (loadInit) {
            Element element = nifty.getCurrentScreen().findElementByName("maxHeight");
            maxHeight = Integer.parseInt(element.getNiftyControl(DropDown.class).getSelection().toString());
        }
    }

    private void initLoadFile(String dir) {
        Element text = nifty.getScreen("loadfile").findElementByName("loadText");
        TextField label = nifty.getScreen("loadfile").findNiftyControl("loadRoot", TextField.class);
        ListBox listbox = nifty.getScreen("loadfile").findNiftyControl("loadListBox", ListBox.class);
        File directory;
        File[] list;
        loadDirRoot = dir;

        text.getRenderer(TextRenderer.class).setText("Select a " + loadType + " File to Load:");
        listbox.clear();
        listbox.addItem(".");
        listbox.addItem("..");
        listbox.setFocusItemByIndex(0);

        try {
            directory = new File(dir);
            dir = directory.getCanonicalPath();
            directory = new File(dir);
            list = directory.listFiles();
            for (File item : list) {
                if (item.isDirectory()) {
                    listbox.addItem(item.getName());
                }
            }
            for (File item : list) {
                if (item.isFile() && item.getName().toLowerCase().endsWith(loadType)) {
                    listbox.addItem(item.getName());
                }
            }
        } catch (Exception e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Failed to init loadfile! {0}", e);
        }
        label.setText(dir);
    }

    private void initSaveFile(String dir) {
        TextField name = nifty.getScreen("savefile").findNiftyControl("saveName", TextField.class);
        TextField label = nifty.getScreen("savefile").findNiftyControl("saveRoot", TextField.class);
        ListBox listbox = nifty.getScreen("savefile").findNiftyControl("saveListBox", ListBox.class);
        File directory;
        File[] list;
        saveDirRoot = dir;
        name.setText(saveFile);
        listbox.clear();
        listbox.addItem(".");
        listbox.addItem("..");
        listbox.setFocusItemByIndex(0);

        try {
            directory = new File(dir);
            dir = directory.getCanonicalPath();
            directory = new File(dir);
            list = directory.listFiles();
            for (File item : list) {
                if (item.isDirectory()) {
                    listbox.addItem(item.getName());
                }
            }
        } catch (Exception e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Failed to init savefile! {0}", e);
        }
        label.setText(dir);
    }

    private boolean loadMap() {
        flyCam.setMoveSpeed(100f);
        haveTerrain = false;
        // Init the TiledTerrain with them.
        tiledTerrain = new TerrainTiler(cam, 30, false, 2, terrainDirRoot.concat(fSep).concat("TiledTerrain.map"), this);
        //tiledTerrain = new TerrainTiler(cam, terrainDirRoot.concat(fSep).concat("TiledTerrain.map"), this);
        if (tiledTerrain != null) {
        //if (tiledTerrain.valid) {
            //tiledTerrain.setGridSize(5);
            tiledTerrain.setEnabled(true);
            haveTerrain = true;
            rootNode.attachChild(tiledTerrain);
        } else {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Failed to initiate tiledTerrain!");
            nifty.gotoScreen("start");
            ttState = TTStates.START;
        }
        return haveTerrain;
    }
    
    private void createTerrainGrid(String terrainDirRoot) {
        assetManager.registerLocator(terrainDirRoot, FileLocator.class);
        flyCam.setMoveSpeed(100f);
        File readFile = new File(terrainDirRoot.concat(fSep).concat("TerrainGrid.map"));
        if (readFile.isFile()) {
            FileReader reader = null;
            try {
                reader = new FileReader(readFile);
                BufferedReader breader = new BufferedReader(reader);
                String line;
                line = breader.readLine();
                if (line.startsWith("TerrainGrid")) {
                    line = breader.readLine();
                    mapTilesX = Integer.valueOf(line.substring(12));
                    line = breader.readLine();
                    mapTilesZ = Integer.valueOf(line.substring(12));
                    line = breader.readLine();
                    mapTileSize = Integer.valueOf(line.substring(12));
                    line = breader.readLine();
                    maxHeight = Integer.valueOf(line.substring(12));
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        Material tMat = new Material(assetManager, "Common/MatDefs/Terrain/HeightBasedTerrain.j3md");
        // SAND texture
        Texture sand = assetManager.loadTexture("radans/tteditor/assets/Textures/diffuse_0.png");
        sand.setWrap(Texture.WrapMode.Repeat);
        tMat.setTexture("region1ColorMap", sand);
        tMat.setVector3("region1", new Vector3f(-16f,(maxHeight*0.25f), 32f));

        // GRASS texture
        Texture grass = assetManager.loadTexture("radans/tteditor/assets/Textures/diffuse_1.png");
        grass.setWrap(Texture.WrapMode.Repeat);
        tMat.setTexture("region2ColorMap", grass);
        tMat.setVector3("region2", new Vector3f((maxHeight*0.25f),(maxHeight*0.5f), 32f));

        // BARK texture
        Texture bark = this.assetManager.loadTexture("radans/tteditor/assets/Textures/diffuse_3.png");
        bark.setWrap(Texture.WrapMode.Repeat);
        tMat.setTexture("region3ColorMap", bark);
        tMat.setVector3("region3", new Vector3f((maxHeight*0.5f),(maxHeight*0.75f), 32f));

        // ROCK texture
        Texture rock = this.assetManager.loadTexture("radans/tteditor/assets/Textures/diffuse_2.png");
        rock.setWrap(Texture.WrapMode.Repeat);
        tMat.setTexture("region4ColorMap", rock);
        tMat.setVector3("region4", new Vector3f((maxHeight*0.75f), maxHeight+16f, 32f));

        tMat.setTexture("slopeColorMap", rock);
        tMat.setFloat("slopeTileFactor", 32f);

        tMat.setFloat("terrainSize", mapTileSize+1);

        terrainGrid = new TerrainGrid("terrain", mapTileSize/4, (mapTileSize*2)+1, new ImageTileLoader(assetManager, new Namer() {

            @Override
            public String getName(int x, int y) {
                String fileName = String.format("tile%04d%04d.png", x+1, y+1);
                return fileName;
            }
        }));
        
        terrainGrid.setMaterial(tMat);
        terrainGrid.setLocalTranslation(0, 0, 0);
        terrainGrid.setLocalScale(1f, (maxHeight/256f), 1f);
        rootNode.attachChild(terrainGrid);

        TerrainLodControl control = new TerrainGridLodControl(terrainGrid, getCamera());
        control.setLodCalculator( new DistanceLodCalculator(mapTileSize/4, 2.7f) ); // patch size, and a multiplier
        terrainGrid.addControl(control);
        haveTerrain = true;
    }

    public void gridAction(String value) {
        if (value.contentEquals("EXIT")) {
            rootNode.detachChild(terrainGrid);
            terrainGrid.detachAllChildren();
            terrainGrid = null;
            haveTerrain = false;
            System.runFinalization();
            System.gc();
            nifty.gotoScreen("start");
            ttState = TTStates.START;
            assetManager.unregisterLocator(terrainDirRoot, FileLocator.class);
        }
    }
    
    public void editorAction(String value) {
        if (value.contentEquals("EXIT")) {
            rootNode.detachChild(tiledTerrain);
            tiledTerrain.setEnabled(false);
            // wait for terraintiler to stop and cleanup with 10sec timeout
            long nTimeout = System.currentTimeMillis()+10000;
//            while (tiledTerrain.getEnabled()) {
//                if (System.currentTimeMillis()>nTimeout) {
//                    break;
//                }
//            }
            tiledTerrain = null;
            haveTerrain = false;
            System.runFinalization();
            System.gc();
            nifty.gotoScreen("start");
            ttState = TTStates.START;
            assetManager.unregisterLocator(terrainDirRoot, FileLocator.class);
        }
    }

    private Callable<Boolean> loadImage = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {

            Logger.getLogger(Main.class.getName()).log(Level.INFO,
                    "Loading Image File....{0}", imageFile);
            imageBuffer = null;
            BufferedImage imgRead = ImageIO.read(new File(loadDirRoot.concat(fSep).concat(imageFile)));
            Raster imgRaster = imgRead.getRaster();
            int imgX = imgRead.getWidth();
            int imgY = imgRead.getHeight();
            imageFileX = imgX;
            imageFileY = imgY;
            loadProgress = 0f;
            if (loadFileFor.contentEquals("heightmap")) {
                // If loading heightmap load in a scaled version only
                int imgXscale = imgX / 512;
                int imgYscale = imgY / 512;
                if (imgXscale > imgYscale) {
                    imgYscale = imgXscale;
                } else {
                    imgXscale = imgYscale;
                }
                int tImgX = imgX / imgXscale;
                int tImgY = imgY / imgYscale;
                int imgType = imgRead.getType();
                System.out.println("Number Bands in Raster = "+imgRaster.getNumBands());
                if (imgType == BufferedImage.TYPE_USHORT_GRAY) {
                    // type=Gray16
                    // create a storage array and zero it all
                    long[][] newImg = new long[tImgX][tImgY];
                    for (int y = 0; y < tImgY; y++) {
                        for (int x = 0; x < tImgX; x++) {
                            newImg[x][y] = 0;
                        }
                    }
                    // read the image and add the values to the array
                    for (int lines = 0; lines < imgY; lines++) {
                        for (int pixels = 0; pixels < imgX; pixels++) {
                            int dot = imgRaster.getSample(pixels, lines, 0);
                            newImg[pixels / imgXscale][lines / imgYscale] += dot;
                        }
                        loadProgress = (float) lines / (float) imgY;
                    }
                    // average out the array and write it to a new image buffer
                    ByteBuffer image = ByteBuffer.allocateDirect(tImgX * tImgY * 2);
                    image.rewind();
                    for (int y = tImgY; y > 0; y--) {
                        for (int x = 0; x < tImgX; x++) {
                            int avg = (int) (newImg[x][y - 1] / (imgXscale * imgYscale));
                            image.put((byte) (avg % 256));
                            image.put((byte) (avg / 256));
                        }
                    }
                    // create the new image itself using the buffer
                    imageFileType = Format.Luminance16;
                    imageBuffer = new Texture2D(tImgX, tImgY, Format.Luminance16);
                    imageBuffer.getImage().setData(image);
                } else if (imgType == BufferedImage.TYPE_INT_RGB) {
                    // type=RGB
                    Logger.getLogger(Main.class.getName()).log(Level.WARNING,
                            "TODO: Not yet implemented load and scale RGB");
                    // TODO: load and scale RGB8
                    //imageBuffer = new Texture2D(512, 512, Image.Format.RGB8);
                    return false;
                } else if (imgType == BufferedImage.TYPE_INT_ARGB) {
                    // type=ARGB
                    Logger.getLogger(Main.class.getName()).log(Level.WARNING,
                            "TODO: Not yet implemented load and scale ARGB");
                    // TODO: load and scale RGBA8
                    //imageBuffer = new Texture2D(512, 512, Image.Format.RGB16);
                    return false;
                } else {
                    Logger.getLogger(Main.class.getName()).log(Level.WARNING,
                            "Unsupported Image Type in {0}", imageFile);
                    return false;
                }

            } else {
                // Loading a Texture so load the image directly into the buffer.
                loadProgress = 0.2f;
                assetManager.registerLocator(loadDirRoot, FileLocator.class);
                loadProgress = 0.4f;
                imageBuffer = (Texture) assetManager.loadTexture(imageFile);
                loadProgress = 0.8f;
                assetManager.unregisterLocator(loadDirRoot, FileLocator.class);
            }
            loadProgress = 1f;
            if (imageBuffer != null) {
                return true;
            }
            return false;
        }
    };
    
    private Callable<Boolean> genPngTiles = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Generating PNG Tiles...");
            // interpolate the mapfile into image tiles
            System.out.println("Map Tiles : " + mapTilesX + " x " + mapTilesZ);
            System.out.println("Tile Size : " + mapTileSize);
            if (saveFile.isEmpty()) {
                terrainDirRoot = saveDirRoot;
            } else {
                terrainDirRoot = saveDirRoot.concat(fSep).concat(saveFile);
            }
            System.out.println("Terrain Root Directory: "+terrainDirRoot);
            loadProgress = 0f;
            int iTileX = heightMapFileX / mapTilesX;    // x size of each image tile
            int iTileZ = heightMapFileY / mapTilesZ;    // y(z) size of each image tile
            System.out.println("Map Tile Size : "+iTileX+" x "+iTileZ);
            System.out.println("Map Scale : "+mapScaleX+" x "+mapScaleZ);
            BufferedImage imgRead = ImageIO.read(new File(heightMapFile));
            Raster imgRaster = imgRead.getRaster();
            for (int tileZ = 0; tileZ < mapTilesZ; tileZ++) {
                int qPz = 1;
                if (tileZ == (mapTilesZ - 1)) {
                    qPz = 0;
                }
                for (int tileX = 0; tileX < mapTilesX; tileX++) {
                    // convert array into a tile via interpolation
                    int qPx = 1;
                    if (tileX == (mapTilesX - 1)) {
                        qPx = 0;
                    }
                    System.out.println("Creating Tile: "+tileX+","+tileZ);
                    BufferedImage tbImage = new BufferedImage(mapTileSize+1, mapTileSize+1, BufferedImage.TYPE_USHORT_GRAY);
                    WritableRaster tbRaster = tbImage.getRaster();
                    int maxV = 0;
                    for (int z = 0; z < mapTileSize + qPz; z++) {
                        for (int x = 0; x < mapTileSize + qPx; x++) {
                            int ix = (int)((x + (tileX * mapTileSize)) / mapScaleX); // image x
                            int iy = (int)((z + (tileZ * mapTileSize)) / mapScaleZ); // image y
                            float imgV, imgVx, imgVy, imgVz;
                            imgV = imgRaster.getSampleFloat(ix, iy, 0);
                            imgVx = imgVy = imgVz = imgV;
                            if (ix < heightMapFileX-1) {
                                imgVx = imgRaster.getSampleFloat(ix+1, iy, 0);
                            }
                            if (iy < heightMapFileY-1) {
                                imgVy = imgRaster.getSampleFloat(ix, iy+1, 0);
                            }
                            if ((ix < heightMapFileX-1) && (iy < heightMapFileY-1)) {
                                imgVz = imgRaster.getSampleFloat(ix+1, iy+1, 0);
                            }
                            float sx = (x/(float)mapScaleX) % 1f;
                            float sy = (z/(float)mapScaleZ) % 1f;
                            imgV = (imgV * (1 - sx) + imgVx * sx) * (1 - sy) + (imgVy * (1 - sx) + imgVz * sx) * sy;
                            tbRaster.setSample(x, z, 0, imgV);
                            if (imgV>maxV) {
                                maxV = (int) imgV;
                            }
                        }
                    }
                    System.out.println("Max V = "+maxV);
                    // save tile as PNG file
                    String fileName = String.format("tile%04d%04d.png", tileX, tileZ);
                    System.out.println("Saving Tile: "+terrainDirRoot.concat(fSep).concat(fileName));
                    File fileOut = new File(terrainDirRoot);
                    fileOut.mkdirs();
                    fileOut = new File(terrainDirRoot.concat(fSep).concat(fileName));
                    ImageIO.write(tbImage, "PNG" , fileOut);
                    loadProgress = (float) (tileX + (tileZ * mapTilesZ)) / (float) (mapTilesX * mapTilesZ);
                    if (Thread.interrupted()) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Save Tile Thread Interrupted!");
                        return false;
                    }
                }
            }
            // if success write map info file and return true;
            File file = new File(terrainDirRoot.concat(fSep).concat("TerrainGrid.map"));
            FileWriter fileOut = new FileWriter(file);
            String text;
            try {
                text = "TerrainGrid Tile Set\r\n";
                fileOut.write(text);
                text = "# X Tiles = "+mapTilesX+"\r\n";
                fileOut.write(text);
                text = "# Z Tiles = "+mapTilesZ+"\r\n";
                fileOut.write(text);
                text = "Tile Size = "+mapTileSize+"\r\n";
                fileOut.write(text);
                text = "MaxHeight = "+maxHeight+"\r\n";
                fileOut.write(text);
                fileOut.close();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Error writing mapfile: {0}", ex);
            } finally {
                try {
                    fileOut.close();
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Error writing mapfile: {0}", ex);
                }
            }
            return true;
        }
    };

    private Callable<Boolean> genMapTiles = new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
            loadProgress = 0f;
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Generating Map Tiles...");
            // Get Textures from image gui and save them to the terrainDirRoot into TileTextures dir.
            int numTex = 0;
            float[] tp = new float[3];
            tp[0] = nifty.getScreen("image").findNiftyControl("slider1", Slider.class).getValue();
            tp[1] = nifty.getScreen("image").findNiftyControl("slider2", Slider.class).getValue();
            tp[2] = nifty.getScreen("image").findNiftyControl("slider3", Slider.class).getValue();
            // Compress the Textures array so there is no Tex,null,Tex,Tex etc so we only have actual textures with any nulls at the end.
            if (texColor[0] != null) {
                numTex++;
            }
            if (texColor[1] != null) {
                texColor[numTex] = texColor[1];
                texNormal[numTex] = texNormal[1];
                tp[numTex] = tp[1];
                numTex++;
            }
            if (texColor[2] != null) {
                texColor[numTex] = texColor[2];
                texNormal[numTex] = texNormal[2];
                tp[numTex] = tp[2];
                numTex++;
            }
            if (texColor[3] != null) {
                texColor[numTex] = texColor[3];
                texNormal[numTex] = texNormal[3];
                numTex++;
            }
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Number of Textures: {0}", numTex);
            // setup the terrain directory location.
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Map Tiles : {0} x {1}", new Object[]{mapTilesX, mapTilesZ});
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "MaxHeight : {0}", maxHeight);
            if (saveFile.isEmpty()) {
                terrainDirRoot = saveDirRoot;
            } else {
                terrainDirRoot = saveDirRoot.concat(fSep).concat(saveFile);
            }
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Terrain Root Directory: {0}", terrainDirRoot);
            // save the textures from the generator gui to files for later.
            File texDir = new File(terrainDirRoot.concat(fSep).concat("TileTextures"));
            texDir.mkdirs();
            for (int i = 0; i<numTex; i++) {
                loadProgress += 0.5f / numTex;
                String texFName = terrainDirRoot.concat(fSep).concat("TileTextures").concat(fSep).concat(String.format("TexColor%02d.png", i));
                File texFile = new File(texFName);
                BufferedImage img = ImageToAwt.convert(texColor[i].getImage(), true, true, 0);
                Logger.getLogger(Main.class.getName()).log(Level.INFO, "Saving Texture {0}", texFName);
                ImageIO.write(img, "PNG", texFile);
                if (texNormal[i] != null) {
                    texFName = terrainDirRoot.concat(fSep).concat("TileTextures").concat(fSep).concat(String.format("TexNormal%02d.png", i));
                    texFile = new File(texFName);
                    img = ImageToAwt.convert(texNormal[i].getImage(), true, true, 0);
                    Logger.getLogger(Main.class.getName()).log(Level.INFO, "Saving Normal {0}", texFName);
                    ImageIO.write(img, "PNG", texFile);
                }
            }
            // now re-load the textures from those files so the terrain tile's assets use them.
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Re-Loading textures from new location...");
            assetManager.registerLocator(terrainDirRoot, FileLocator.class);
            for (int i=0; i<4; i++) {
                loadProgress += 0.1f;
                if (i<numTex) {
                    String texFName = "TileTextures".concat("/").concat(String.format("TexColor%02d.png", i));
                    texColor[i] = assetManager.loadTexture(texFName);
                    texColor[i].setWrap(Texture.WrapMode.Repeat);
                    texFName = "TileTextures".concat("/").concat(String.format("TexNormal%02d.png", i));
                    texNormal[i] = assetManager.loadTexture(texFName);
                    texNormal[i].setWrap(Texture.WrapMode.Repeat);
                } else {
                    texColor[i] = null;
                    texNormal[i] = null;
                }
            }
            loadProgress = 0f;
            int iTileX = heightMapFileX / mapTilesX;    // x size of each map image tile
            int iTileZ = heightMapFileY / mapTilesZ;    // y(z) size of each map image tile
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Image Map Tile Size : {0} x {1}", new Object[]{iTileX, iTileZ});
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Image Map Scale     : {0} x {1}", new Object[]{mapScaleX, mapScaleZ});
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Terrain Tile Size   : {0} x {1}", new Object[]{mapTileSize, mapTileSize});
            // Create and save terrainQuads into .j3o files
            BufferedImage imgRead = ImageIO.read(new File(heightMapFile));
            Raster imgRaster = imgRead.getRaster();
            for (int tileZ = 0; tileZ < mapTilesZ; tileZ++) {
                int qPz = 1;
                if (tileZ == (mapTilesZ - 1)) {
                    qPz = 0;
                }
                for (int tileX = 0; tileX < mapTilesX; tileX++) {
                    // convert array into a tile via interpolation
                    int qPx = 1;
                    if (tileX == (mapTilesX - 1)) {
                        qPx = 0;
                    }
                    Logger.getLogger(Main.class.getName()).log(Level.INFO, "Creating Tile: {0},{1}", new Object[]{tileX, tileZ});
                    float hMap[] = new float[(mapTileSize+1)*(mapTileSize+1)];
                    Arrays.fill(hMap, 0);
                    int maxV = 0;
                    for (int z = 0; z < mapTileSize + qPz; z++) {
                        //System.out.print("\n"+z+",");
                        for (int x = 0; x < mapTileSize + qPx; x++) {
                            int ix = (int)((x + (tileX * mapTileSize)) / mapScaleX); // image x
                            int iy = (int)((z + (tileZ * mapTileSize)) / mapScaleZ); // image y
                            float imgV, imgVx, imgVy, imgVz;
                            imgV = imgRaster.getSampleFloat(ix, iy, 0);
                            imgVx = imgVy = imgVz = imgV;
                            if (ix < heightMapFileX-1) {
                                imgVx = imgRaster.getSampleFloat(ix+1, iy, 0);
                            }
                            if (iy < heightMapFileY-1) {
                                imgVy = imgRaster.getSampleFloat(ix, iy+1, 0);
                            }
                            if ((ix < heightMapFileX-1) && (iy < heightMapFileY-1)) {
                                imgVz = imgRaster.getSampleFloat(ix+1, iy+1, 0);
                            }
                            float sx = (x/(float)mapScaleX) % 1f;
                            float sy = (z/(float)mapScaleZ) % 1f;
                            imgV = (imgV * (1 - sx) + imgVx * sx) * (1 - sy) + (imgVy * (1 - sx) + imgVz * sx) * sy;
                            imgV = (imgV/65535) * maxHeight;
                            hMap[(z*(mapTileSize+1))+x] = imgV;
                            if (imgV>maxV) {
                                maxV = (int) imgV;
                            }
                        }
                    }
                    Logger.getLogger(Main.class.getName()).log(Level.INFO, "Max V = {0}", maxV);
                    // Generate TerrainQuad from hMap and Textures from gui using terrainLighting material
                    String tileName = String.format("TILE-%02d%02d.j3o", (tileX % 64), (tileZ % 64));
                    String dirName = String.format("DIR-%02d%02d", (tileX / 64), (tileZ / 64));
                    // TerrainQuad mesh
                    TerrainQuad tQuad = new TerrainQuad(dirName.concat(fSep).concat(tileName), mapTileSize/4+1, mapTileSize+1, hMap);
                    // Terrain Alphamap and material generation
                    // get height of grid as a factor of maxHeight and apply the one to four 
                    // textures from the GUI into the height ranges also from the gui
                    Logger.getLogger(Main.class.getName()).log(Level.INFO, "Creating Material: {0}", dirName.concat(fSep).concat(tileName));
                    Material tMat = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
                    tMat.setTexture("DiffuseMap", texColor[0]);
                    tMat.setFloat("DiffuseMap_0_scale", texColor[0].getImage().getWidth()/512f);
                    if (texNormal[0] != null) {
                        tMat.setTexture("NormalMap", texNormal[0]);
                    }
                    if (numTex > 1) {
                        tMat.setTexture("DiffuseMap_1", texColor[1]);
                        tMat.setFloat("DiffuseMap_1_scale", texColor[1].getImage().getWidth()/512f);
                        if (texNormal[1] != null) {
                            tMat.setTexture("NormalMap_1", texNormal[1]);
                        }
                    }
                    if (numTex > 2) {
                        tMat.setTexture("DiffuseMap_2", texColor[2]);
                        tMat.setFloat("DiffuseMap_2_scale", texColor[2].getImage().getWidth()/512f);
                        if (texNormal[2] != null) {
                            tMat.setTexture("NormalMap_2", texNormal[2]);
                        }
                    }
                    if (numTex > 3) {
                        tMat.setTexture("DiffuseMap_3", texColor[3]);
                        tMat.setFloat("DiffuseMap_3_scale", texColor[3].getImage().getWidth()/512f);
                        if (texNormal[3] != null) {
                            tMat.setTexture("NormalMap_3", texNormal[3]);
                        }
                    }
                    tMat.setColor("Diffuse", ColorRGBA.White);
                    tMat.setColor("Ambient", ColorRGBA.White);
                    tMat.setFloat("Shininess",0.5f);
                    tMat.setColor("Specular", ColorRGBA.White);
                    tMat.setBoolean("WardIso",true); 
                    tMat.setBoolean("useTriPlanarMapping",true);
                    // Now generate the Alphamap (RGBA Image where R=texture[0], G=[1], B=[2] & A=[3]
                    Logger.getLogger(Main.class.getName()).log(Level.INFO, "Creating Alphamap: {0}", dirName.concat(fSep).concat(tileName));
                    byte[] data = new byte[mapTileSize*mapTileSize*4];
                    Arrays.fill(data, (byte) 0);
                    ByteBuffer buffer = BufferUtils.createByteBuffer(data);
                    Image image = new Image(Format.RGBA8, mapTileSize, mapTileSize, buffer);
                    Texture2D aMap = new Texture2D(image);
                    ImageRaster aMapRaster = ImageRaster.create(aMap.getImage());
                    for (int y=0; y<mapTileSize; y++) {
                        for (int x=0; x<mapTileSize; x++) {
                            float h = hMap[(y*(mapTileSize+1))+x];
                            float r=0, g=0, b=0, a=0;
                            if (numTex==1 | h<(tp[0]*maxHeight/100)) {
                                r=1f;  // R
                            } else if (numTex==2 | h<tp[1]*maxHeight/100) {
                                g=1f;  // G
                            } else if (numTex==3 | h<tp[2]*maxHeight/100) {
                                b=1f;  // B
                            } else {
                                a=1f;  // A
                            }
                            aMapRaster.setPixel(x, mapTileSize-y-1, new ColorRGBA(r, g, b, a));
                        }
                    }
                    tMat.setTexture("AlphaMap", aMap);
                    // Material tMat = new Material( assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
                    // Apply Material to terrainQuad
                    tQuad.setMaterial(tMat);
                    Logger.getLogger(Main.class.getName()).log(Level.INFO, "Saving Tile: {0}", terrainDirRoot.concat(fSep).concat(dirName).concat(fSep).concat(tileName));
                    // Write tile to file
                    File fileOut = new File(terrainDirRoot.concat(fSep).concat(dirName));
                    fileOut.mkdirs();
                    fileOut = new File(terrainDirRoot.concat(fSep).concat(dirName).concat(fSep).concat(tileName));
                    BinaryExporter bExp = BinaryExporter.getInstance();
                    try {
                        bExp.save(tQuad, fileOut);
                    } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Failed to save tile. {0}", ex);
                    }
                    loadProgress = (float) (tileX + (tileZ * mapTilesZ)) / (float) (mapTilesX * mapTilesZ);
                    if (Thread.interrupted()) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Save Tile Thread Interrupted!");
                        return false;
                    }
                }
            }
            // Save map reference file
            File file = new File(terrainDirRoot.concat(fSep).concat("TiledTerrain.map"));
            FileWriter fileOut = new FileWriter(file);
            String text;
            try {
                text = "tiledTerrain : 1\r\n";
                fileOut.write(text);
                text = "useJars   : false\r\n";
                fileOut.write(text);
                text = "numTilesX : "+mapTilesX+"\r\n";
                fileOut.write(text);
                text = "numTilesZ : "+mapTilesZ+"\r\n";
                fileOut.write(text);
                text = "tileSize  : "+mapTileSize+"\r\n";
                fileOut.write(text);
                text = "tileScale : 1\r\n"; // TODO: Implement slider in UI to allow post-gen terrain scales
                fileOut.write(text);
                text = "tileType  : terrain\r\n";
                fileOut.write(text);
                fileOut.close();
                //TODO: Save a texturemap (DB?) file to detail textures used in each tile.
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Failed to write map info file. {0}", ex);
            } finally {
                try {
                    fileOut.close();
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Failed to close map info file. {0}", ex);
                }
            }
            return true;
        }
    };

}
