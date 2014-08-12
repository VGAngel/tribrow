package mythruna.client.ui;

import com.jme3.app.Application;
import com.jme3.asset.TextureKey;
import com.jme3.font.BitmapFont;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import mythruna.client.GameClient;
import mythruna.es.Entity;
import mythruna.es.EntityData;
import mythruna.es.EntitySet;
import mythruna.es.FieldFilter;
import mythruna.script.DialogOption;
import mythruna.script.DialogPrompt;
import mythruna.script.NumberParameter;
import org.progeeks.util.log.Log;

import java.util.StringTokenizer;

public class DialogState extends ObservableState {
    static Log log = Log.getLog();
    private GameClient gameClient;
    private EntityData ed;
    private float scale = 1.0F;
    private Node promptPanel;
    private Border border;
    private Geometry background;
    private Node optionPanel;
    private Geometry divider;
    private BitmapFont font;
    private Label promptText;
    private EntitySet entities;
    private DialogPrompt prompt;
    private Vector3f position = new Vector3f(38.0F, 30.0F, 0.0F);

    private ColorRGBA promptColor = new ColorRGBA(0.3568628F, 0.07450981F, 0.07450981F, 1.0F);
    private ColorRGBA optionColor = new ColorRGBA(0.08235294F, 0.12549F, 0.2862745F, 1.0F);
    private ColorRGBA selectColor = new ColorRGBA(0.1215686F, 0.3921569F, 0.2078431F, 1.0F);
    private ColorRGBA shadowColor = new ColorRGBA(0.0F, 0.0F, 0.0F, 0.0F);

    private ColorRGBA dividerColor = new ColorRGBA(0.5019608F, 0.3411765F, 0.2352941F, 0.35F);

    public DialogState(GameClient gameClient) {
        super("Dialog State", false);
        this.gameClient = gameClient;
        this.ed = gameClient.getEntityData();

        this.entities = this.ed.getEntities(new FieldFilter(DialogPrompt.class, "player", gameClient.getPlayer()), new Class[]{DialogPrompt.class});
    }

    public void checkActive() {
        if (this.entities.applyChanges()) {
            if (this.promptPanel == null) {
                return;
            }
            if ((!this.entities.getAddedEntities().isEmpty()) || (!this.entities.getChangedEntities().isEmpty())) {
                Entity e = (Entity) this.entities.iterator().next();
                setPrompt((DialogPrompt) e.get(DialogPrompt.class));
            } else if (!this.entities.getRemovedEntities().isEmpty()) {
                setPrompt(null);
            } else {
                log.warn("Unknown state in dialog entity set.  It says there are changes but there aren't:" + this.entities);
            }
        }
    }

    protected void setPrompt(DialogPrompt prompt) {
        System.out.println("setPrompt(" + prompt + ")");

        if (this.prompt == prompt)
            return;
        this.prompt = prompt;

        if (this.prompt == null) {
            setEnabled(false);
        } else if ((this.prompt.getOptions() == null) || (this.prompt.getOptions().length == 0)) {
            this.gameClient.getConsole().echo(prompt.getPrompt());
            setEnabled(false);
        } else {
            updatePrompt();
            setEnabled(true);
        }
    }

    protected String makePromptFit(String s) {
        float max = 450.0F;

        if (this.font.getLineWidth(s) < max) {
            return s;
        }

        s = s.replaceAll("(\\S)<br>", "$1 <br>");

        StringBuilder sb = new StringBuilder();
        StringBuilder line = new StringBuilder();
        StringTokenizer st = new StringTokenizer(s, " \t\r\n");
        while (st.hasMoreTokens()) {
            String next = st.nextToken();
            if ("<br>".equals(next)) {
                sb.append("\n");
                line = new StringBuilder();
            } else {
                line.append(next);
                if (this.font.getLineWidth(line) > max) {
                    sb.append("\n");
                    line = new StringBuilder();
                    line.append(next);
                }
                if (line.length() > 0) {
                    sb.append(" ");
                    line.append(" ");
                }
                sb.append(next);
            }
        }
        return sb.toString();
    }

    protected void updatePrompt() {
        System.out.println("updatePrompt()");

        Application app = getApplication();

        String text = this.prompt.getPrompt();
        text = makePromptFit(text);

        this.promptText.setText(text);

        this.optionPanel.detachAllChildren();

        float y = -24.0F;

        int index = 1;
        for (DialogOption opt : this.prompt.getOptions()) {
            Label l = new Label(this.font);
            l.setText(index + ". " + opt.getText());
            l.setColor(this.optionColor);
            l.setShadowColor(this.shadowColor);
            l.setLocalTranslation(0.0F, y, 1.0F);

            OptionCommand cmd = new OptionCommand(index);
            l.addControl(new MouseEventControl(new MouseListener[]{new LabelCommandAdapter(l, cmd)}));

            this.optionPanel.attachChild(l);
            y -= 35.0F;

            index++;
        }

        float height = this.promptText.getHeight() + 30.0F;
        y = Math.max(height, 170.0F);

        this.promptText.setLocalTranslation(39.0F, 564.0F - y * 0.5F, 0.1F);

        this.divider.setLocalTranslation(14.0F, 564.0F - y, 0.1F);

        y += 14.0F;

        this.optionPanel.setLocalTranslation(39.0F, 564.0F - y, 0.0F);
    }

    protected void initialize(Application app) {
        super.initialize(app);

        this.font = app.getAssetManager().loadFont("Interface/templar32.fnt");

        GuiAppState guiState = (GuiAppState) getState(GuiAppState.class);
        this.scale = (guiState.getCamera().getHeight() / 720.0F);
        if (this.scale > 1.0F) {
            this.scale = 1.0F;
        }
        Camera cam = app.getCamera();

        this.promptPanel = new Node("Prompt");
        this.promptPanel.setLocalScale(this.scale);

        this.border = new Border(528.0F, 564.0F, app.getAssetManager());
        this.promptPanel.attachChild(this.border);

        Quad quad = new Quad(528.0F, 564.0F);
        float x = 2.0625F;
        float y = 2.203125F;
        quad.setBuffer(VertexBuffer.Type.TexCoord, 2, new float[]{0.0F, y, x, y, x, 0.0F, 0.0F, 0.0F});

        this.background = new Geometry("bg", quad);
        Material m = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        Texture t = app.getAssetManager().loadTexture("Textures/sand.jpg");
        t.setWrap(Texture.WrapMode.Repeat);
        m.setTexture("ColorMap", t);
        m.setColor("Color", new ColorRGBA(1.0F, 1.0F, 1.0F, 0.85F));
        m.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        this.background.setMaterial(m);
        this.background.setLocalTranslation(0.0F, 0.0F, -0.1F);
        this.promptPanel.attachChild(this.background);

        this.promptText = new Label(this.font);
        this.promptText.setText("Testing");
        this.promptText.setVAlignment(VAlignment.CENTER);

        this.promptText.setLocalTranslation(39.0F, 479.0F, 0.1F);
        this.promptText.setColor(this.promptColor);
        this.promptText.setShadowColor(this.shadowColor);
        this.promptPanel.attachChild(this.promptText);

        this.optionPanel = new Node("Options");
        this.optionPanel.setLocalTranslation(39.0F, 370.0F, 0.0F);
        this.promptPanel.attachChild(this.optionPanel);

        Material horzMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key = new TextureKey("Textures/sand.jpg");
        key.setGenerateMips(false);
        t = app.getAssetManager().loadTexture(key);
        t.setWrap(Texture.WrapMode.Repeat);
        horzMat.setTexture("ColorMap", t);
        horzMat.setColor("Color", this.dividerColor);
        horzMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        Quad horizontal = new Quad(500.0F, 6.0F);
        x /= 256.0F;
        y = 0.023438F;
        horizontal.setBuffer(VertexBuffer.Type.TexCoord, 2, new float[]{0.0F, y, x, y, x, 0.0F, 0.0F, 0.0F});

        this.divider = new Geometry("horz divider", horizontal);
        this.divider.setMaterial(horzMat);
        this.divider.setLocalTranslation(14.0F, 394.0F, 0.0F);
        this.promptPanel.attachChild(this.divider);
    }

    protected void enable() {
        super.enable();

        GuiAppState guiState = (GuiAppState) getState(GuiAppState.class);
        guiState.addDependent(this);
        Camera cam = guiState.getCamera();

        this.promptPanel.setLocalTranslation(this.position.x, cam.getHeight() - this.border.getHeight() - this.position.y, this.position.z);

        guiState.getOrthoRoot().attachChild(this.promptPanel);
    }

    protected void disable() {
        super.disable();

        GuiAppState guiState = (GuiAppState) getState(GuiAppState.class);
        guiState.removeDependent(this);
        guiState.getOrthoRoot().detachChild(this.promptPanel);
    }

    public void render(RenderManager rm) {
        super.render(rm);
    }

    public void update(float tpf) {
    }

    protected class OptionCommand extends ActionCommand {
        private int number;

        public OptionCommand(int number) {
            super("Option");
            this.number = number;
        }

        public void execute(Object source, Object action) {
            DialogState.this.gameClient.execute("DialogOption", DialogState.this.gameClient.getPlayer(), new NumberParameter(this.number));
        }
    }

    protected class LabelCommandAdapter extends LabelSelector {
        private ActionCommand cmd;

        public LabelCommandAdapter(Label label, ActionCommand cmd) {
            super(label, DialogState.this.optionColor, DialogState.this.shadowColor, DialogState.this.selectColor);
            this.cmd = cmd;
        }

        public void click(MouseButtonEvent event) {
            event.setConsumed();

            System.out.println("Click.");

            if (this.cmd != null)
                this.cmd.execute(this, null);
        }
    }
}
