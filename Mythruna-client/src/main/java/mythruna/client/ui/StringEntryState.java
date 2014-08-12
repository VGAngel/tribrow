package mythruna.client.ui;

import com.jme3.app.Application;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.*;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import mythruna.client.GameAppState;
import mythruna.client.anim.Animation;
import mythruna.client.anim.AnimationState;
import mythruna.client.anim.AnimationTask;
import mythruna.geom.Trifold;

public class StringEntryState extends ObservableState {
    public static final ActionCommand OK_COMMAND = new ActionCommand("Ok");
    private String message;
    private ActionCommand[] actions;
    private InputObserver inputObserver = new InputObserver();
    private Node dialog;
    private Geometry background;
    private TextField textField;
    private float width;
    private float height;

    public StringEntryState(String message, ActionCommand[] actions) {
        super(message, true);
        this.message = message;
        this.actions = actions;

        if ((actions == null) || (actions.length == 0))
            this.actions = new ActionCommand[]{OK_COMMAND};
    }

    public String getText() {
        return this.textField.getText();
    }

    protected void initialize(Application app) {
        super.initialize(app);

        float z = 10.0F;

        this.dialog = new Node("Text Entry Dialog");

        Camera cam = app.getCamera();

        Quad fadeMesh = new Quad(cam.getWidth(), cam.getHeight());
        this.background = new Geometry("Fade", fadeMesh);
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        mat.setColor("Color", new ColorRGBA(0.0F, 0.0F, 0.0F, 0.5F));
        this.background.setMaterial(mat);
        this.background.setLocalTranslation(0.0F, 0.0F, z);

        this.background.addControl(new MouseEventControl());
        ((MouseEventControl) this.background.getControl(MouseEventControl.class)).addMouseListener(new MouseObserver());

        z += 2.0F;

        float padding = 50.0F;

        Vector3f base = new Vector3f();
        float w = 0.0F;
        float h = 0.0F;

        Label messageLabel = new Label(app);
        messageLabel.setText(this.message);
        messageLabel.setColor(new ColorRGBA(0.2431373F, 0.3607843F, 0.5568628F, 1.0F));
        messageLabel.setHAlignment(HAlignment.CENTER);
        messageLabel.setVAlignment(VAlignment.TOP);
        this.dialog.attachChild(messageLabel);

        Vector3f[] bounds = messageLabel.getLabelBounds();

        base.set(bounds[0].x, bounds[1].y, z);
        w = Math.abs(bounds[1].x - bounds[0].x);
        h = Math.abs(bounds[1].y - bounds[0].y);
        messageLabel.setLocalTranslation(0.0F, h * 0.5F, z);

        float textWidth = Math.max(w + padding * 1.5F, 150.0F * this.actions.length);
        this.textField = new TextField(app.getAssetManager().loadFont("Interface/templar32.fnt"), app.getAssetManager(), textWidth);

        this.textField.setActive(true);
        this.textField.setLocalTranslation(-textWidth * 0.5F, -h * 1.6F, z);
        this.dialog.attachChild(this.textField);

        w += padding * 2.0F;
        h += padding * 2.0F;

        float labelHeight = h;

        base.x = (-w * 0.5F);
        base.y = (-(h - padding));

        Node buttons = new Node("Buttons");

        float x = 0.0F;
        for (ActionCommand cmd : this.actions) {
            if (x != 0.0F) {
                x += 5.0F;
            }
            Button b = new Button(app, 150.0F, 34.0F);
            b.setText(cmd.getName());
            b.setLocalTranslation(x, 0.0F, 0.0F);
            b.addCommand(new ButtonCommand(cmd));
            x += 150.0F;

            buttons.attachChild(b);
        }

        h += 39.0F;
        base.y -= 39.0F;

        buttons.setLocalTranslation(-x * 0.5F, base.y, z + 2.0F);
        this.dialog.attachChild(buttons);

        base.y -= 15.0F;
        h += 15.0F;

        x += 30.0F;

        System.out.println("w:" + w + "   x:" + x);

        if (w < x) {
            float delta = x - w;
            w = x;
            base.x -= delta * 0.5F;
        }

        Trifold bgMesh = new Trifold(w, h);
        Geometry geom = new Geometry("Background", bgMesh);
        Material bgMaterial = new Material(app.getAssetManager(), "MatDefs/Composed.j3md");
        bgMaterial.setTexture("Map", app.getAssetManager().loadTexture("Interface/gradient.jpg"));
        Texture sand = app.getAssetManager().loadTexture("Textures/sand.jpg");
        sand.setWrap(Texture.WrapMode.Repeat);
        bgMaterial.setTexture("Mix", sand);
        bgMaterial.setVector3("MixParms", new Vector3f(0.01F, 0.01F, 1.0F));

        geom.setMaterial(bgMaterial);
        geom.setLocalTranslation(base.x, base.y, z - 1.0F);

        this.dialog.attachChild(geom);

        this.height = h;
        this.width = w;

        Trifold borderMesh = new Trifold(128.0F, 128.0F);
        borderMesh.setFoldTextureCoordinates(new Vector2f(0.09375F, 0.09375F), new Vector2f(0.90625F, 0.90625F));

        borderMesh.setFoldCoordinates(new Vector2f(12.0F, 12.0F), new Vector2f(116.0F, 116.0F));
        borderMesh.setSize(this.width, this.height);
        borderMesh.updateGeometry();

        Material borderMaterial = new Material(app.getAssetManager(), "MatDefs/Composed.j3md");
        borderMaterial.setTexture("Map", app.getAssetManager().loadTexture("Interface/border.png"));
        Texture leather = app.getAssetManager().loadTexture("Interface/leather.jpg");
        leather.setWrap(Texture.WrapMode.Repeat);
        borderMaterial.setTexture("Mix", leather);
        borderMaterial.setVector3("MixParms", new Vector3f(0.0039063F, 0.0039063F, 1.0F));
        borderMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        Geometry border = new Geometry("Palette Border", borderMesh);
        border.setMaterial(borderMaterial);
        border.setLocalTranslation(base.x, base.y, z);

        this.dialog.attachChild(border);
    }

    protected void enable() {
        super.enable();

        ((GameAppState) getState(GameAppState.class)).setEnableTextEntry(false);

        Node gui = ((GameAppState) getState(GameAppState.class)).getGuiNode();

        InputRedirector.getInstance().addFirstRawInputListener(this.inputObserver);

        InputRedirector.getInstance().addRawKeyListener(this.textField);

        Camera cam = getApplication().getCamera();

        float scale = cam.getWidth() / 1280.0F;
        if (scale > 1.0F) {
            scale = 1.0F;
        }
        this.dialog.setLocalScale(0.01F);
        this.dialog.setLocalTranslation(cam.getWidth() * 0.5F, cam.getHeight() * 0.5F + this.height * 0.5F, 10.0F);

        gui.attachChild(this.background);
        gui.attachChild(this.dialog);

        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.scale(this.dialog, 0.01F, scale, 0.25F)});
    }

    protected void disable() {
        super.disable();

        InputRedirector.getInstance().removeRawKeyListener(this.textField);
        ((GameAppState) getState(GameAppState.class)).setEnableTextEntry(true);

        Node gui = ((GameAppState) getState(GameAppState.class)).getGuiNode();

        gui.detachChild(this.dialog);
        gui.detachChild(this.background);

        InputRedirector.getInstance().removeRawInputListener(this.inputObserver);
    }

    private class InputObserver
            implements RawInputListener {
        private InputObserver() {
        }

        public void beginInput() {
        }

        public void endInput() {
        }

        public void onJoyAxisEvent(JoyAxisEvent evt) {
        }

        public void onJoyButtonEvent(JoyButtonEvent evt) {
        }

        public void onKeyEvent(KeyInputEvent evt) {
            if (evt.getKeyCode() == 1) {
                if (!evt.isPressed()) {
                    StringEntryState.this.getStateManager().detach(StringEntryState.this);

                    if (StringEntryState.this.actions.length > 1) {
                        StringEntryState.this.actions[(StringEntryState.this.actions.length - 1)].execute(StringEntryState.this, "Escape");
                    }

                }

                evt.setConsumed();
            } else if ((evt.getKeyCode() == 28) || (evt.getKeyCode() == 156)) {
                if (!evt.isPressed()) {
                    StringEntryState.this.getStateManager().detach(StringEntryState.this);

                    StringEntryState.this.actions[0].execute(StringEntryState.this, "Enter");
                }

                evt.setConsumed();
            } else if (evt.getKeyCode() == 15) {
                evt.setConsumed();
            }
        }

        public void onMouseButtonEvent(MouseButtonEvent evt) {
        }

        public void onMouseMotionEvent(MouseMotionEvent evt) {
        }

        public void onTouchEvent(TouchEvent evt) {
        }
    }

    private class MouseObserver
            implements MouseListener {
        private MouseObserver() {
        }

        public void mouseButtonEvent(MouseButtonEvent event, Spatial capture) {
            event.setConsumed();
        }

        public void mouseEntered(MouseMotionEvent event, Spatial capture) {
            event.setConsumed();
        }

        public void mouseExited(MouseMotionEvent event, Spatial capture) {
            event.setConsumed();
        }

        public void mouseMoved(MouseMotionEvent event, Spatial capture) {
            event.setConsumed();
        }
    }

    private class ButtonCommand
            implements Command {
        private ActionCommand cmd;

        public ButtonCommand(ActionCommand cmd) {
            this.cmd = cmd;
        }

        public void execute(Object source, Object action) {
            StringEntryState.this.getStateManager().detach(StringEntryState.this);
            if (this.cmd != null)
                this.cmd.execute(StringEntryState.this, action);
        }
    }
}