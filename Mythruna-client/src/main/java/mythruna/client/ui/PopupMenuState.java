package mythruna.client.ui;

import com.jme3.app.Application;
import com.jme3.audio.AudioNode;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;
import mythruna.MaterialIndex;
import mythruna.client.ClientOptions;
import mythruna.client.ConveyerCamera;
import mythruna.client.GameClient;
import mythruna.client.anim.Animation;
import mythruna.client.anim.AnimationState;
import mythruna.client.anim.AnimationTask;
import mythruna.es.*;
import mythruna.geom.Trifold;
import mythruna.script.*;
import org.progeeks.util.log.Log;

public class PopupMenuState extends ObservableState {
    static Log log = Log.getLog();

    private static final ColorRGBA titleColor = new ColorRGBA(0.3568628F, 0.07450981F, 0.07450981F, 1.0F);
    private static final ColorRGBA selectColor = new ColorRGBA(0.1215686F, 0.3921569F, 0.2078431F, 1.0F);
    private static final ColorRGBA itemColor = new ColorRGBA(0.2431373F, 0.2431373F, 0.4156863F, 1.0F);
    private static final ColorRGBA shadowColor = new ColorRGBA(0.0F, 0.0F, 0.0F, 0.0F);
    private GameClient gameClient;
    private EntityData ed;
    private AudioNode switchOn;
    private AudioNode switchOff;
    private AudioNode click;
    private Node buttons;
    private float buttonScale = 1.0F;

    private boolean mouseWasEnabled = false;

    private ColorRGBA enabledColor = new ColorRGBA(0.2039216F, 0.4431373F, 0.839216F, 1.0F);
    private EntitySet entities;
    private ContextActions actions;

    public PopupMenuState(GameClient gameClient) {
        super("Context Popup Menu", false);
        this.gameClient = gameClient;
        this.ed = gameClient.getEntityData();

        this.entities = this.ed.getEntities(new FieldFilter(ContextActions.class, "player", gameClient.getPlayer()), new Class[]{ContextActions.class});
    }

    public void checkActive() {
        if (this.entities.applyChanges()) {
            if (this.buttons == null) {
                return;
            }
            if ((!this.entities.getAddedEntities().isEmpty()) || (!this.entities.getChangedEntities().isEmpty())) {
                Entity e = (Entity) this.entities.iterator().next();
                setActions((ContextActions) e.get(ContextActions.class));
            } else if (!this.entities.getRemovedEntities().isEmpty()) {
                setActions(null);
            } else {
                log.warn("Unknown state in actions entity set.  It says there are changes but there aren't:" + this.entities);
            }
        }
    }

    protected void setActions(ContextActions actions) {
        System.out.println("setActions(" + actions + ")");
        if (this.actions == actions)
            return;
        this.actions = actions;

        if (this.actions == null) {
            setEnabled(false);
        } else {
            updateActions();
            setEnabled(true);
        }
    }

    protected void updateActions() {
        Application app = getApplication();
        Camera cam = getApplication().getCamera();

        Vector3f pos = this.actions.getPosition();
        if (pos == null) {
            pos = new Vector3f(cam.getWidth() * 0.5F, cam.getHeight() * 0.5F, 0.0F);
        }
        this.buttons.setLocalTranslation(pos.x, pos.y, 10.0F);

        this.buttons.detachAllChildren();

        String name = this.actions.getName();
        String title = this.actions.getTitle();
        EntityId target = this.actions.getTarget();
        ActionReference[] refs = this.actions.getActions();

        float y = 0.0F;

        ColorRGBA titleColor = new ColorRGBA(0.0F, 0.0F, 0.2F, 1.0F);
        ColorRGBA bgColor = new ColorRGBA(0.5F, 0.5F, 0.4F, 1.0F);

        Label l = new Label(app);
        l.setText(name);
        l.setColor(titleColor);
        l.setShadowColor(new ColorRGBA(0.9F, 0.9F, 1.0F, 1.0F));
        l.setVAlignment(VAlignment.TOP);
        l.setLocalTranslation(0.0F, y, 2.0F);

        this.buttons.attachChild(l);
        y -= 28.0F;
        float yDiv = y - 4.0F;

        float width = l.getWidth();
        float xOffset = 5.0F;

        for (ActionReference ar : refs) {
            l = new Label(app);
            l.setText(ar.getName());
            l.setColor(itemColor);
            l.setShadowColor(shadowColor);
            l.setVAlignment(VAlignment.TOP);
            l.setLocalTranslation(0.0F, y, 2.0F);

            ActionCommand cmd = new NamedActionCommand(ar, target, this.actions.getParameter());
            l.addControl(new MouseEventControl(new MouseListener[]{new LabelCommandAdapter(l, cmd)}));

            this.buttons.attachChild(l);
            y -= 24.0F;

            width = Math.max(width, l.getWidth());
        }
        y -= 10.0F;

        float xMarginLeft = 7.0F;
        float xMarginRight = 17.0F;
        float yMarginTop = 7.0F;
        float yMarginBottom = 18.0F;

        Quad div = new Quad(width, 4.0F);
        Geometry divGeom = new Geometry("div", div);
        Material mDiv = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mDiv.setColor("Color", new ColorRGBA(0.4352941F, 0.1960784F, 0.1960784F, 1.0F));
        divGeom.setMaterial(mDiv);
        divGeom.setLocalTranslation(0.0F, yDiv, 0.1F);
        this.buttons.attachChild(divGeom);

        float quadWidth = width + 2.0F * xOffset + xMarginLeft + xMarginRight;
        float quadHeight = Math.abs(y) + yMarginTop + yMarginBottom;
        Trifold quad = new Trifold(quadWidth, quadHeight);
        quad.setFoldCoordinates(new Vector2f(21.0F, 18.0F), new Vector2f(quadWidth - 17.0F, quadHeight - 21.0F));
        quad.setFoldTextureCoordinates(new Vector2f(0.328125F, 0.28125F), new Vector2f(0.734375F, 0.671875F));

        quad.updateGeometry();

        Geometry bg = new Geometry("menu bg", quad);
        Material m = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        m.setTexture("ColorMap", app.getAssetManager().loadTexture("Interface/popup-border.png"));
        m.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        bg.setMaterial(m);
        bg.setLocalTranslation(-xOffset - xMarginLeft, y - yMarginBottom, -0.1F);

        this.buttons.attachChild(bg);

        Quad cancel = new Quad(cam.getWidth(), cam.getHeight());
        Geometry cancelGeom = new Geometry("Cancel BG", cancel);
        m = MaterialIndex.TRANSPARENT_MATERIAL.clone();

        cancelGeom.setMaterial(m);
        cancelGeom.addControl(new MouseEventControl(new MouseListener[]{new CommandAdapter(new CancelCommand())}));
        cancelGeom.setLocalTranslation(-pos.x, -pos.y, -0.2F);
        this.buttons.attachChild(cancelGeom);
    }

    protected void initialize(Application app) {
        super.initialize(app);

        Camera cam = app.getCamera();
        float radialScale = 1.0F;

        this.buttonScale = Math.min(1.0F, cam.getWidth() / 1000.0F);
        this.buttons = new Node("PopupMenu");
        this.buttons.setLocalTranslation(cam.getWidth() * 0.5F, cam.getHeight() * 0.5F, 0.0F);

        this.buttons.setLocalScale(this.buttonScale);

        this.switchOn = new AudioNode(app.getAssetManager(), "Sounds/switch.ogg", false);
        this.switchOn.setPositional(false);

        this.switchOff = new AudioNode(app.getAssetManager(), "Sounds/switch-off-short.ogg", false);
        this.switchOff.setPositional(false);

        this.click = new AudioNode(app.getAssetManager(), "Sounds/button-click.ogg", false);
        this.click.setPositional(false);
    }

    protected void enable() {
        super.enable();

        GuiAppState guiState = (GuiAppState) getState(GuiAppState.class);
        Camera cam = guiState.getCamera();
        float radialScale = cam.getHeight() / 720.0F;

        this.switchOn.setVolume(ClientOptions.getInstance().getSoundEffectsVolume() * 0.8F);
        this.switchOn.play();

        Node gui = guiState.getOrthoRoot();

        guiState.addDependent(this);
        gui.attachChild(this.buttons);

        this.buttons.setLocalScale(0.01F);

        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.scale(this.buttons, 0.01F, this.buttonScale, 0.25F)});

        this.mouseWasEnabled = ((ConveyerCamera) getState(ConveyerCamera.class)).isEnabled();
        ((ConveyerCamera) getState(ConveyerCamera.class)).setEnabled(false);
    }

    protected void disable() {
        super.disable();

        ((ConveyerCamera) getState(ConveyerCamera.class)).setEnabled(this.mouseWasEnabled);

        this.gameClient.execute("Close Context Menu", null, null);

        GuiAppState guiState = (GuiAppState) getState(GuiAppState.class);
        Node gui = ((GuiAppState) getState(GuiAppState.class)).getOrthoRoot();

        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.detach(this.buttons, 0.25F)});
        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.scale(this.buttons, this.buttonScale, 0.01F, 0.25F)});

        guiState.removeDependent(this, 0.25F);
    }

    public void render(RenderManager rm) {
        super.render(rm);
    }

    public void update(float tpf) {
    }

    protected class CancelCommand extends ActionCommand {
        public CancelCommand() {
            super("Cancel");
        }

        public void execute(Object source, Object a) {
            PopupMenuState.this.switchOff.setVolume(ClientOptions.getInstance().getSoundEffectsVolume() * 0.8F);
            PopupMenuState.this.switchOff.play();
            PopupMenuState.this.setEnabled(false);
        }
    }

    protected class SetNameCommand extends ActionCommand {
        private ActionReference ref;
        private EntityId target;

        public SetNameCommand(ActionReference ref, EntityId target) {
            super(ref.getName());
            this.target = target;
            this.ref = ref;
        }

        public void execute(Object source, Object a) {
            PopupMenuState.this.setEnabled(false);

            System.out.println("Source:" + source);
            StringEntryState state = (StringEntryState) source;
            System.out.println("Entered text:" + state.getText());

            String s = state.getText().trim();
            if (s.length() > 25) {
                s = s.substring(0, 25);
            }
            Name name = new Name(s);
            PopupMenuState.this.gameClient.executeRef(this.ref, this.target, new ComponentParameter(name));
        }
    }

    protected class NamedActionCommand extends ActionCommand {
        private ActionReference ref;
        private EntityId target;
        private ActionParameter parm;

        public NamedActionCommand(ActionReference ref, EntityId target, ActionParameter parm) {
            super(ref.getName());
            this.target = target;
            this.ref = ref;
            this.parm = parm;
        }

        public void execute(Object source, Object a) {
            if (this.ref.getType() == ActionType.NameComponent) {
                StringEntryState state = new StringEntryState("Enter a New Name", new ActionCommand[]{new PopupMenuState.SetNameCommand(this.ref, this.target), new PopupMenuState.CancelCommand()});

                PopupMenuState.this.getApplication().getStateManager().attach(state);
                return;
            }

            PopupMenuState.this.click.setVolume(ClientOptions.getInstance().getSoundEffectsVolume() * 0.8F);
            PopupMenuState.this.click.play();

            PopupMenuState.this.setEnabled(false);
            PopupMenuState.this.gameClient.executeRef(this.ref, this.target, this.parm);
        }
    }

    protected class CommandAdapter extends MouseAdapter {
        private ActionCommand cmd;

        public CommandAdapter(ActionCommand cmd) {
            this.cmd = cmd;
        }

        public void click(MouseButtonEvent event) {
            event.setConsumed();
            this.cmd.execute(this, null);
        }
    }

    protected class LabelCommandAdapter extends LabelSelector {
        private ActionCommand cmd;

        public LabelCommandAdapter(Label label, ActionCommand cmd) {
            super(label, PopupMenuState.itemColor, PopupMenuState.shadowColor, PopupMenuState.selectColor);
            this.cmd = cmd;
        }

        public void click(MouseButtonEvent event) {
            event.setConsumed();
            this.cmd.execute(this, null);
        }
    }
}