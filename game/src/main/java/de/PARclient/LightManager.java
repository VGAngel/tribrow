package de.PARclient;

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.terrain.geomipmap.TerrainQuad;

public class LightManager {
    private GameClient app = null;
    private Node mainNode = null;
    
    public LightManager(GameClient client, Node root) {
        app = client;
        mainNode = root;
    }

    public void init(MapManager mapManager) {
        initGridShadow(mapManager);
        initLighting();
        initBloom(2f);
    }

     /**
     * initiates shadows for single quad-based maps
     */
    public void initShadow(MapManager mapMan) {
        mainNode.setShadowMode(RenderQueue.ShadowMode.Off);
        TerrainQuad terrain = mapMan.getTerrainQuad();
        terrain.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        initBaseShadow();
    }

    /**
     * Initializes shadows for grid-based maps
     */
    public void initGridShadow(MapManager mapMan) {
        mainNode.setShadowMode(RenderQueue.ShadowMode.Off);
        TerrainQuad terrain = mapMan.getTerrainGrid();
        terrain.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        initBaseShadow();
    }

    /**
     * Define and initiate the lighting in the world
     */
    public void initLighting() {
        AmbientLight amb = new AmbientLight();
        amb.setColor(ColorRGBA.White.mult(0.75f));
        mainNode.addLight(amb);
        
        /*
         DirectionalLight light1 = new DirectionalLight();
         light1.setDirection((new Vector3f(-1, -1, -1)).normalize());
         light1.setColor(ColorRGBA.Yellow.mult(0.2f));
         rootNode.addLight(light1);
         */

        DirectionalLight light2 = new DirectionalLight();
        light2.setDirection((new Vector3f(-1, -0.6f, -1)));
        light2.setColor(ColorRGBA.White.mult(1.25f));
        mainNode.addLight(light2);

    }   

    /**
     * Initiate the Bloom-lighting render, giving lighted things a 'glow' 
     */ 
    public void initBloom(float scale) {
        FilterPostProcessor fpp = new FilterPostProcessor(app.getAssetManager());
        BloomFilter bloom = new BloomFilter();
        bloom.setBlurScale(scale); // intensity of the bloom
        fpp.addFilter(bloom);
        app.getViewPort().addProcessor(fpp);
    }
    
    /**
     * Initiates shadow processing. Still uses the outdated PSSM render
     *
     * @todo replace with OccularOcclusion
     */
    public void initBaseShadow() {
        PssmShadowRenderer pssmRenderer = new PssmShadowRenderer(app.getAssetManager(), 1024, 16);
        pssmRenderer.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
        pssmRenderer.setLambda(0.2f);
        pssmRenderer.setShadowIntensity(0.5f);
        pssmRenderer.setCompareMode(PssmShadowRenderer.CompareMode.Hardware);
        pssmRenderer.setEdgesThickness(5);
        pssmRenderer.setFilterMode(PssmShadowRenderer.FilterMode.PCFPOISSON);
        //pssmRenderer.displayDebug();
        app.getViewPort().addProcessor(pssmRenderer);
    }
}
