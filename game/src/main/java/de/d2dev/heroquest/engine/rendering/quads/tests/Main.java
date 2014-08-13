package de.d2dev.heroquest.engine.rendering.quads.tests;

import com.jme3.asset.DesktopAssetManager;
import de.d2dev.fourseasons.resource.DummyResourceLocator;
import de.d2dev.heroquest.engine.rendering.quads.JmeRenderer;
import de.d2dev.heroquest.engine.rendering.quads.QuadRenderModel;

/**
 * TestMain
 * @author Justus
 */
public class Main {
	
    public static void main(String[] args) {
        QuadRenderModel map = new TestMap();
        DesktopAssetManager desktopAssetManager = new DesktopAssetManager();
        JmeRenderer stupidRenderer = new JmeRenderer(map, desktopAssetManager);
        stupidRenderer.start();
    }
}
