package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.post.FilterPostProcessor;

/**
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    public Main() {
        super(new ConfigAppState());
    }

    @Override
    public void simpleInitApp() {
        FilterPostProcessor processor = assetManager.loadFilter("assets/Filters/SceneFilter.j3f");
        getViewPort().addProcessor(processor);
        rootNode.attachChild(assetManager.loadModel("assets/Scenes/MainScene.j3o"));
    }

}
