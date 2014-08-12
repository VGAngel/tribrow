package mythruna.client.ui;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;

import java.util.HashSet;
import java.util.Set;

public class TextField extends Node implements KeyListener {

    private static String SPECIAL = "`~!@#$%^&*()_+-=[]\\{}|;':\",./<>?";
    private BitmapText text;
    private BitmapFont font;
    private int cursor = 0;
    private boolean active;
    private StringBuilder edit = new StringBuilder("|");

    private Set<Integer> toConsume = new HashSet();

    public TextField(BitmapFont font, AssetManager assets, float width) {
        super("TextField");

        this.font = font;
        setCullHint(Spatial.CullHint.Always);

        Quad quad = new Quad(width, font.getPreferredSize());
        Geometry g = new Geometry("TextField.back", quad);
        Material m = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        m.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        m.setColor("Color", new ColorRGBA(0.0F, 0.0F, 0.0F, 0.5F));
        g.setMaterial(m);
        attachChild(g);

        this.text = new BitmapText(font, false);
        this.text.setLocalTranslation(0.0F, font.getPreferredSize(), 0.0F);

        attachChild(this.text);
    }

    public void setText(String s) {
        this.edit = new StringBuilder(s + "|");
        this.cursor = (this.edit.length() - 1);
        this.text.setText(this.edit);
    }

    public String getText() {
        this.edit.deleteCharAt(this.cursor);
        String s = this.edit.toString();

        this.edit.insert(this.cursor, (char) '|');

        return s;
    }

    public void setActive(boolean a) {
        if (this.active == a)
            return;
        this.active = a;

        if (this.active) {
            setCullHint(Spatial.CullHint.Never);
            this.toConsume.clear();
        } else {
            setCullHint(Spatial.CullHint.Always);
        }
    }

    public boolean isActive() {
        return this.active;
    }

    protected boolean isAllowed(char c) {
        return SPECIAL.indexOf(c) >= 0;
    }

    public void onKeyEvent(KeyInputEvent evt) {
        char c = evt.getKeyChar();
        if (!evt.isReleased()) {
            boolean changed = false;

            if ((c == '\r') || (c == '\n')) {
                return;
            }
            if ((Character.isLetterOrDigit(c)) || (Character.isSpace(c)) || (isAllowed(c))) {
                this.edit.insert(this.cursor++, c);
                changed = true;
            } else if ((evt.getKeyCode() == 14) && (this.cursor > 0)) {
                this.cursor -= 1;
                this.edit.deleteCharAt(this.cursor);
                changed = true;
            }

            if (changed) {
                this.text.setText(this.edit);
                evt.setConsumed();
                this.toConsume.add(Integer.valueOf(evt.getKeyCode()));
            }

        } else if (this.toConsume.remove(Integer.valueOf(evt.getKeyCode()))) {
            evt.setConsumed();
        }
    }
}