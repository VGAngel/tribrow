package mythruna.client.ui;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.font.BitmapFont;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import mythruna.client.ClientOptions;
import mythruna.geom.Trifold;
import org.progeeks.util.ObjectUtils;

public class Button extends Node {
    public static final ColorRGBA DEFAULT_COLOR = ColorRGBA.Black;
    public static final ColorRGBA DEFAULT_SHADOW = new ColorRGBA(0.972549F, 0.9411765F, 0.7529412F, 1.0F);
    public static final ColorRGBA DEFAULT_SELECTED = ColorRGBA.Green;
    private AssetManager assets;
    private Label label;
    private Geometry background;
    private Trifold backgroundMesh;
    private Texture backgroundTexture;
    private Material bgMaterial;
    private float width;
    private float height;
    private Vector3f textOffset = new Vector3f();
    private float textScale = 1.0F;
    private AudioNode click;
    private ColorRGBA textColor = DEFAULT_COLOR;
    private ColorRGBA shadowColor = DEFAULT_SHADOW;
    private ColorRGBA selectedColor = DEFAULT_SELECTED;
    private boolean highlight = false;

    private CommandList commands = new CommandList();

    public Button(Application app, float width, float height) {
        this(app, new Trifold(width, height), app.getAssetManager().loadTexture("Interface/BrassButton.png"));
    }

    public Button(Application app, Trifold backgroundMesh, Texture backgroundTexture) {
        this(app.getAssetManager(), backgroundMesh, backgroundTexture);
    }

    public Button(AssetManager assets, Trifold backgroundMesh, Texture backgroundTexture) {
        this.assets = assets;
        this.width = backgroundMesh.getSize().x;
        this.height = backgroundMesh.getSize().y;
        this.backgroundMesh = backgroundMesh;
        this.backgroundTexture = backgroundTexture;

        setupChildren();
        addControl(new MouseEventControl());
        ((MouseEventControl) getControl(MouseEventControl.class)).addMouseListener(new MouseObserver());

        this.click = new AudioNode(assets, "Sounds/button-click.ogg", false);
        this.click.setPositional(false);
    }

    public float getHeight() {
        return this.height;
    }

    public float getWidth() {
        return this.width;
    }

    public void addCommand(Command cmd) {
        this.commands.addCommand(cmd);
    }

    public void removeCommand(Command cmd) {
        this.commands.removeCommand(cmd);
    }

    public void setTextOffset(Vector3f offset) {
        this.textOffset.set(offset);
        setChildBase(0.0F, 0.0F, 0.0F);
    }

    public void setTextScale(float scale) {
        if (this.textScale == scale)
            return;
        this.textScale = scale;
        setChildBase(0.0F, 0.0F, 0.0F);
    }

    public void setTextColor(ColorRGBA color) {
        if (ObjectUtils.areEqual(color, this.textColor))
            return;
        this.textColor = (color == null ? DEFAULT_COLOR : color);
        if (!this.highlight) {
            this.label.setColor(this.textColor);
            this.label.setShadowColor(this.shadowColor);
        }
    }

    public void setTextShadowColor(ColorRGBA color) {
        if (ObjectUtils.areEqual(color, this.textColor))
            return;
        this.shadowColor = (color == null ? DEFAULT_SHADOW : color);
        if (!this.highlight) {
            this.label.setColor(this.textColor);
            this.label.setShadowColor(this.shadowColor);
        }
    }

    public void setTextSelectedColor(ColorRGBA color) {
        if (ObjectUtils.areEqual(color, this.selectedColor))
            return;
        this.selectedColor = (color == null ? DEFAULT_SELECTED : color);
        if (this.highlight)
            this.label.setColor(this.selectedColor);
    }

    public void setBackgroundColor(ColorRGBA color) {
        this.bgMaterial.setColor("Color", color);
    }

    public void setFont(String fontName) {
        BitmapFont font = this.assets.loadFont(fontName);
        this.label.setFont(font);
    }

    private void setupChildren() {
        BitmapFont font = this.assets.loadFont("Interface/knights32.fnt");
        this.label = new Label(font);

        this.label.setColor(this.textColor);
        this.label.setShadowColor(this.shadowColor);
        this.label.setHAlignment(HAlignment.CENTER);
        this.label.setVAlignment(VAlignment.CENTER);
        this.label.setLocalScale(this.textScale);
        attachChild(this.label);

        this.background = new Geometry("Button Bg", this.backgroundMesh);
        this.bgMaterial = new Material(this.assets, "Common/MatDefs/Misc/Unshaded.j3md");
        this.bgMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        this.bgMaterial.setTexture("ColorMap", this.backgroundTexture);
        this.background.setMaterial(this.bgMaterial);
        attachChild(this.background);

        setChildBase(0.0F, 0.0F, 0.0F);
    }

    protected void setChildBase(float x, float y, float z) {
        this.label.setLocalTranslation(this.textOffset.x + x + this.width * 0.5F, this.textOffset.y + y + this.height * 0.5F + 2.0F, this.textOffset.z + z + 0.2F);

        this.label.setLocalScale(this.textScale);
        this.background.setLocalTranslation(x, y, z);
    }

    public void setText(String text) {
        this.label.setText(text);
    }

    protected void runCommands() {
        System.out.println("runCommands():" + this);

        this.commands.execute(this, "Click");
    }

    public String toString() {
        return "Button[" + this.label + "]";
    }

    private class MouseObserver implements MouseListener {
        private int xDown;
        private int yDown;

        private MouseObserver() {
        }

        public void mouseButtonEvent(MouseButtonEvent event, Spatial capture) {
            event.setConsumed();

            if (event.isPressed()) {
                this.xDown = event.getX();
                this.yDown = event.getY();
                Button.this.click.setVolume(ClientOptions.getInstance().getSoundEffectsVolume() * 0.6F);
                Button.this.click.play();
                Button.this.setChildBase(1.0F, -2.0F, -0.1F);
            } else {
                Button.this.setChildBase(0.0F, 0.0F, 0.0F);
                int x = event.getX();
                int y = event.getY();
                if ((Math.abs(x - this.xDown) < 3) && (Math.abs(y - this.yDown) < 3))
                    Button.this.runCommands();
            }
        }

        public void mouseEntered(MouseMotionEvent event, Spatial capture) {
            Button.this.label.setColor(Button.this.selectedColor);
            Button.this.highlight = true;
        }

        public void mouseExited(MouseMotionEvent event, Spatial capture) {
            Button.this.label.setColor(Button.this.textColor);
            Button.this.label.setShadowColor(Button.this.shadowColor);
            Button.this.highlight = false;
        }

        public void mouseMoved(MouseMotionEvent event, Spatial capture) {
        }
    }
}