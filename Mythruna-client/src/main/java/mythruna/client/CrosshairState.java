package mythruna.client;

import com.jme3.app.Application;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.ui.Picture;
import mythruna.client.ui.HAlignment;
import mythruna.client.ui.Label;
import mythruna.client.ui.ObservableState;
import mythruna.client.ui.VAlignment;
import mythruna.es.Entity;
import mythruna.es.EntitySet;
import mythruna.es.FieldFilter;
import mythruna.item.ReticleStyle;
import org.progeeks.util.ObjectUtils;

public class CrosshairState extends ObservableState {
    private GameClient client;
    private Node guiRoot;
    private Node crosshair;
    private Picture cursor;
    private Label text;
    private String imageName;
    private float width;
    private float height;
    private float scale = 1.0F;
    private float maxScale = 1.0F;
    private EntitySet us;
    private ReticleStyle style;

    public CrosshairState(GameClient client, Node guiRoot) {
        super("Crosshair", true);
        this.guiRoot = guiRoot;
        this.client = client;
    }

    public CrosshairState(GameClient client, Node guiRoot, String imageName, int width, int height) {
        this(client, guiRoot);
        this.client = client;
        setImage(imageName, width, height);
    }

    public void setImage(String imageName, float width, float height) {
        if ((ObjectUtils.areEqual(this.imageName, imageName)) && (this.width == width) && (this.height == height)) {
            return;
        }

        setStyle(null);

        this.imageName = imageName;
        this.width = width;
        this.height = height;

        if (imageName == null) {
            if (this.cursor != null)
                this.cursor.removeFromParent();
            this.cursor = null;
            return;
        }

        if (this.crosshair == null) {
            return;
        }
        updateCursor();
    }

    protected void updateCursor() {
        if (this.cursor == null) {
            this.cursor = new Picture("Cursor");
            this.crosshair.attachChild(this.cursor);
        }

        this.cursor.setLocalTranslation(-this.width * 0.5F * this.scale, -this.height * 0.5F * this.scale, 0.0F);
        this.cursor.setWidth(this.width * this.scale);
        this.cursor.setHeight(this.height * this.scale);
        this.cursor.setImage(getApplication().getAssetManager(), this.imageName, true);

        this.text.setLocalTranslation(0.0F, -this.height * 0.4F * this.scale, 0.0F);
    }

    protected void updateStyle() {
        Entity e = this.us.getEntity(this.client.getPlayer());
        System.out.println("-------------------updateslots() e:" + e);
        if (e == null) {
            setStyle(null);
            return;
        }

        ReticleStyle s = (ReticleStyle) e.get(ReticleStyle.class);
        setStyle(s);
    }

    public void setStyle(ReticleStyle style) {
        if (this.style == style)
            return;
        this.style = style;

        System.out.println("setStyle(" + style + ")");
        if (style == null)
            this.text.setText(null);
        else
            this.text.setText(style.getText());
    }

    protected void initialize(Application app) {
        super.initialize(app);

        this.crosshair = new Node("CrosshairReticle");

        Camera cam = app.getCamera();
        this.crosshair.setLocalTranslation(cam.getWidth() * 0.5F, cam.getHeight() * 0.5F, -1.0F);

        this.scale = (cam.getHeight() / 720.0F);
        if (this.scale > this.maxScale) {
            this.scale = this.maxScale;
        }
        this.text = new Label(app);
        this.text.setHAlignment(HAlignment.CENTER);
        this.text.setVAlignment(VAlignment.TOP);
        this.text.setLocalTranslation(0.0F, 0.0F, 0.0F);
        this.text.setLocalScale(this.scale * 0.75F);
        this.crosshair.attachChild(this.text);

        if (this.imageName != null)
            updateCursor();
    }

    protected void enable() {
        if (this.crosshair.getParent() != null) {
            return;
        }
        this.guiRoot.attachChild(this.crosshair);

        FieldFilter filter = new FieldFilter(ReticleStyle.class, "id", this.client.getPlayer());
        this.us = this.client.getEntityData().getEntities(filter, new Class[]{ReticleStyle.class});
        updateStyle();
    }

    protected void disable() {
        this.us.release();
        this.guiRoot.detachChild(this.crosshair);
    }

    public void update(float tpf) {
        if (this.us.applyChanges()) {
            updateStyle();
        }
    }
}