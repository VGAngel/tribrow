package mythruna.client.view;

import com.jme3.app.Application;
import com.jme3.audio.AudioNode;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import mythruna.client.ClientOptions;
import mythruna.client.GameClient;
import mythruna.client.PostProcessingState;
import mythruna.client.anim.Animation;
import mythruna.client.anim.AnimationState;
import mythruna.client.anim.AnimationTask;
import mythruna.client.ui.*;
import mythruna.es.*;
import mythruna.geom.Trifold;
import mythruna.script.ActionReference;
import mythruna.script.ActionType;
import mythruna.script.ComponentParameter;
import mythruna.script.RadialActions;
import org.progeeks.util.log.Log;

import java.util.ArrayList;
import java.util.List;

public class RadialMenuState extends ObservableState {
    static Log log = Log.getLog();
    private GameClient gameClient;
    private EntityData ed;
    private AudioNode switchTabs;
    private AudioNode switchOff;
    private Node buttons;
    private float buttonScale = 1.0F;

    private ColorRGBA enabledColor = new ColorRGBA(0.2039216F, 0.4431373F, 0.839216F, 1.0F);
    private EntitySet entities;
    private RadialActions actions;
    private List<AnimationTask> disableAnimations = new ArrayList();

    public RadialMenuState(GameClient gameClient) {
        super("Object Radial Menu", false);
        this.gameClient = gameClient;
        this.ed = gameClient.getEntityData();

        this.entities = this.ed.getEntities(new FieldFilter(RadialActions.class, "player", gameClient.getPlayer()), new Class[]{RadialActions.class});
    }

    public void checkActive() {
        if (this.entities.applyChanges()) {
            if (this.buttons == null) {
                return;
            }
            if ((!this.entities.getAddedEntities().isEmpty()) || (!this.entities.getChangedEntities().isEmpty())) {
                Entity e = (Entity) this.entities.iterator().next();
                setRadialActions((RadialActions) e.get(RadialActions.class));
            } else if (!this.entities.getRemovedEntities().isEmpty()) {
                setRadialActions(null);
            } else {
                log.warn("Unknown state in radial actions entity set.  It says there are changes but there aren't:" + this.entities);
            }
        }
    }

    protected void setRadialActions(RadialActions actions) {
        System.out.println("setRadialActions(" + actions + ")");
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

        float radialScale = 1.0F;
        this.buttons.detachAllChildren();

        Vector3f buttonCenter = this.buttons.getWorldTranslation();

        String name = this.actions.getName();
        String title = this.actions.getTitle();
        EntityId target = this.actions.getTarget();
        ActionReference[] refs = this.actions.getActions();

        int slots = refs.length;
        if (slots % 2 == 0)
            slots++;
        float delta = 6.283186F / slots;
        float angle = 4.712389F;

        if (slots == refs.length) {
            angle += delta * 0.5F;
        }

        Texture2D radialLine = (Texture2D) app.getAssetManager().loadTexture("Interface/radial-line.png");

        int i = 0;
        for (ActionReference ar : refs) {
            angle -= delta;

            float x = FastMath.cos(angle) * 200.0F * radialScale;
            float y = FastMath.sin(angle) * 200.0F * radialScale;
            Button b = createButton(ar.getName(), x, y, 0.0F);
            b.addCommand(new NamedActionCommand(ar, target));

            this.buttons.attachChild(b);

            Picture p = new Picture(ar.getName() + " radial");
            p.setWidth(150.0F);
            p.setHeight(150.0F);
            p.setTexture(app.getAssetManager(), radialLine, true);
            b.attachChild(p);

            Quaternion rot = new Quaternion().fromAngles(0.0F, 0.0F, angle - 0.7853982F);
            p.setLocalRotation(rot);

            p.setLocalTranslation(b.worldToLocal(buttonCenter, null));
        }

        Button center = createCenterButton(name, 0.0F, 0.0F, 1.0F);
        center.setTextColor(new ColorRGBA(0.9568628F, 1.0F, 0.6078432F, 1.0F));
        center.addCommand(new CancelCommand());
        this.buttons.attachChild(center);

        Camera cam = getApplication().getCamera();

        if (title != null) {
            Button banner = createTitle(title, 0.0F, -(200.0F + radialScale + 32.0F), 1.0F);
            banner.setTextOffset(new Vector3f(0.0F, 6.0F, 0.0F));
            banner.setFont("Interface/templar32.fnt");
            banner.setTextColor(new ColorRGBA(0.05098039F, 0.12549F, 0.4352941F, 1.0F));
            banner.addCommand(new CancelCommand());
            this.buttons.attachChild(banner);
        }

        this.switchOff = new AudioNode(app.getAudioRenderer(), app.getAssetManager(), "Sounds/switch-off-short.ogg", false);
        this.switchOff.setPositional(false);
    }

    protected void initialize(Application app) {
        super.initialize(app);

        Camera cam = app.getCamera();
        float radialScale = 1.0F;

        this.buttonScale = Math.min(1.0F, cam.getWidth() / 1000.0F);
        this.buttons = new Node("RadialButtons");
        this.buttons.setLocalTranslation(cam.getWidth() * 0.5F, cam.getHeight() * 0.5F, 0.0F);

        this.buttons.setLocalScale(this.buttonScale);
    }

    protected void enable() {
        super.enable();

        AnimationState anim = (AnimationState) getState(AnimationState.class);
        anim.remove(this.disableAnimations);
        this.disableAnimations.clear();

        GuiAppState guiState = (GuiAppState) getState(GuiAppState.class);
        Camera cam = guiState.getCamera();
        float radialScale = cam.getHeight() / 720.0F;

        ((PostProcessingState) getStateManager().getState(PostProcessingState.class)).setRadialFadeOn(3.0F * radialScale, 3.0F * radialScale);

        Node gui = guiState.getOrthoRoot();

        guiState.addDependent(this);
        gui.attachChild(this.buttons);

        this.buttons.setLocalScale(0.01F);

        ((AnimationState) getState(AnimationState.class)).add(new AnimationTask[]{Animation.scale(this.buttons, 0.01F, this.buttonScale, 0.25F)});
    }

    protected void disable() {
        super.disable();

        if (this.actions != null) {
            this.gameClient.execute("Close Radial", null, null);
        }

        ((PostProcessingState) getStateManager().getState(PostProcessingState.class)).setRadialFade(false);

        GuiAppState guiState = (GuiAppState) getState(GuiAppState.class);
        Node gui = ((GuiAppState) getState(GuiAppState.class)).getOrthoRoot();
        this.switchOff.setVolume(ClientOptions.getInstance().getSoundEffectsVolume() * 0.8F);
        this.switchOff.play();

        AnimationState anim = (AnimationState) getState(AnimationState.class);
        this.disableAnimations.addAll(anim.add(new AnimationTask[]{Animation.detach(this.buttons, 0.25F)}));
        this.disableAnimations.addAll(anim.add(new AnimationTask[]{Animation.scale(this.buttons, this.buttonScale, 0.01F, 0.25F)}));

        this.disableAnimations.addAll(guiState.removeDependent(this, 0.25F));
    }

    public void render(RenderManager rm) {
        super.render(rm);
    }

    public void update(float tpf) {
    }

    protected Button createButton(String text, float x, float y, float z) {
        float triRadius = 75.0F;
        float triWidth = 150.0F;
        Trifold trifold = new Trifold(triWidth, triRadius * 2.0F);
        trifold.setFoldTextureCoordinates(new Vector2f(0.5F, 0.5F), new Vector2f(0.5F, 0.5F));
        trifold.setFoldCoordinates(new Vector2f(triRadius, triRadius), new Vector2f(triWidth - triRadius, triRadius));
        trifold.updateGeometry();

        Texture texture = getApplication().getAssetManager().loadTexture("Interface/bubble.png");
        Button button = new Button(getApplication(), trifold, texture);
        button.setText(text);
        button.setLocalTranslation(x - triWidth * 0.5F, y - triWidth * 0.5F, z);

        button.setTextColor(new ColorRGBA(0.4F, 0.6F, 0.6F, 1.0F));
        button.setTextShadowColor(new ColorRGBA(0.1F, 0.2F, 0.2F, 1.0F));
        button.setTextSelectedColor(ColorRGBA.Yellow);
        return button;
    }

    protected Button createCenterButton(String text, float x, float y, float z) {
        float triRadius = 17.0F;
        float triWidth = 150.0F;
        Trifold trifold = new Trifold(triWidth, triRadius * 2.0F);
        trifold.setFoldTextureCoordinates(new Vector2f(0.5F, 0.5F), new Vector2f(0.5F, 0.5F));
        trifold.setFoldCoordinates(new Vector2f(triRadius, triRadius), new Vector2f(triWidth - triRadius, triRadius));
        trifold.updateGeometry();

        Texture texture = getApplication().getAssetManager().loadTexture("Interface/bubble.png");
        Button button = new Button(getApplication(), trifold, texture);
        button.setText(text);
        button.setLocalTranslation(x - triWidth * 0.5F, y - triRadius, z);

        button.setTextColor(new ColorRGBA(0.4F, 0.6F, 0.6F, 1.0F));
        button.setTextShadowColor(new ColorRGBA(0.1F, 0.2F, 0.2F, 1.0F));
        button.setTextSelectedColor(ColorRGBA.Yellow);
        return button;
    }

    protected Button createTitle(String text, float x, float y, float z) {
        float triHeight = 64.0F;
        float triWidth = 512.0F;
        Trifold trifold = new Trifold(triWidth, triHeight);
        trifold.setFoldTextureCoordinates(new Vector2f(0.2714844F, 0.0F), new Vector2f(0.726563F, 1.0F));
        trifold.setFoldCoordinates(new Vector2f(139.0F, 0.0F), new Vector2f(372.0F, 64.0F));
        trifold.updateGeometry();

        Texture texture = getApplication().getAssetManager().loadTexture("Interface/banner.png");

        Button button = new Button(getApplication(), trifold, texture);
        button.setText(text);
        button.setLocalTranslation(x - triWidth * 0.5F, y - triHeight, z);

        button.setTextColor(new ColorRGBA(0.4F, 0.6F, 0.6F, 1.0F));
        button.setTextShadowColor(new ColorRGBA(0.1F, 0.2F, 0.2F, 1.0F));
        button.setTextSelectedColor(ColorRGBA.Yellow);
        return button;
    }

    protected class CancelCommand extends ActionCommand {
        public CancelCommand() {
            super("Cancel");
        }

        public void execute(Object source, Object a) {
            RadialMenuState.this.setEnabled(false);
        }
    }

    protected class SetNameCommand extends ActionCommand {
        private ActionReference ref;
        private EntityId target;

        public SetNameCommand(ActionReference ref, EntityId target) {
            super("SetName");
            this.target = target;
            this.ref = ref;
        }

        public void execute(Object source, Object a) {
            RadialMenuState.this.setEnabled(false);

            System.out.println("Source:" + source);
            StringEntryState state = (StringEntryState) source;
            System.out.println("Entered text:" + state.getText());

            String s = state.getText().trim();
            if (s.length() > 25) {
                s = s.substring(0, 25);
            }
            Name name = new Name(s);
            RadialMenuState.this.gameClient.executeRef(this.ref, this.target, new ComponentParameter(name));
        }
    }

    protected class NamedActionCommand extends ActionCommand {
        private ActionReference ref;
        private EntityId target;

        public NamedActionCommand(ActionReference ref, EntityId target) {
            super("NamedAction");
            this.target = target;
            this.ref = ref;
        }

        public void execute(Object source, Object a) {
            if (this.ref.getType() == ActionType.NameComponent) {
                String title = this.ref.getName() + " " + RadialMenuState.this.actions.getName();
                StringEntryState state = new StringEntryState(title, new ActionCommand[]{new RadialMenuState.SetNameCommand(this.ref, this.target), new RadialMenuState.CancelCommand()});

                RadialMenuState.this.getApplication().getStateManager().attach(state);
                return;
            }

            RadialMenuState.this.setRadialActions(null);

            RadialMenuState.this.gameClient.executeRef(this.ref, this.target, null);
        }
    }
}
