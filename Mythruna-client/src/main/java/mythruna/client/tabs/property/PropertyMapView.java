package mythruna.client.tabs.property;

import com.jme3.app.Application;
import com.jme3.font.BitmapFont;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import mythruna.client.GameClient;
import mythruna.client.ui.HAlignment;
import mythruna.client.ui.Label;
import mythruna.es.Entity;
import org.progeeks.util.log.Log;

public class PropertyMapView extends Node {
    static Log log = Log.getLog();

    private static final ColorRGBA titleColor = new ColorRGBA(0.3568628F, 0.07450981F, 0.07450981F, 1.0F);
    private static final ColorRGBA shadowColor = new ColorRGBA(0.0F, 0.0F, 0.0F, 0.0F);
    private GameClient gameClient;
    private Application app;
    private BitmapFont writingFont;
    private Entity property;
    private float width = 200.0F;
    private float height = 200.0F;

    public PropertyMapView(GameClient gameClient, Application app) {
        this.gameClient = gameClient;
        this.app = app;

        this.writingFont = app.getAssetManager().loadFont("Interface/templar32.fnt");

        createMapView();
    }

    public void setProperty(Entity property) {
        if (this.property == property)
            return;
        this.property = property;
    }

    protected void createMapView() {
        Quad quad = new Quad(this.width, this.height);

        Geometry geom = new Geometry("MapView", quad);
        Material m = new Material(this.app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", ColorRGBA.Green);
        geom.setMaterial(m);
        geom.setLocalTranslation(0.0F, 70.0F, -1.0F);
        attachChild(geom);

        Label l = new Label(this.writingFont);
        l.setText("Coming Soon");
        l.setColor(titleColor);
        l.setShadowColor(shadowColor);
        l.setHAlignment(HAlignment.CENTER);
        l.setLocalTranslation(this.width * 0.5F, this.height * 0.5F + 24.0F, 0.1F);
        attachChild(l);
    }
}