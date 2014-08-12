package mythruna.client.ui;

import com.jme3.app.Application;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import mythruna.MaterialIndex;
import org.progeeks.util.ObjectUtils;

public class Label extends Node {
    private String text;
    private BitmapFont font;
    private BitmapText textBitmap;
    private BitmapText shadowBitmap;
    private Quad bgBox;
    private Geometry bgGeom;
    private ColorRGBA color;
    private ColorRGBA shadowColor;
    private ColorRGBA bgColor;
    private HAlignment alignment = HAlignment.LEFT;
    private VAlignment vAlignment = VAlignment.BOTTOM;
    private Vector3f shadowOffset = new Vector3f(1.0F, -1.0F, -0.1F);

    public Label() {
    }

    public Label(Application app) {
        this(app.getAssetManager().loadFont("Interface/knights32.fnt"));
    }

    public Label(BitmapFont font) {
        setFont(font);
    }

    public void setHAlignment(HAlignment alignment) {
        if (this.alignment == alignment)
            return;
        this.alignment = alignment;
        resetAlignment();
    }

    public HAlignment getHAlignment() {
        return this.alignment;
    }

    public float getHeight() {
        return this.textBitmap.getHeight() * getLocalScale().y;
    }

    public float getWidth() {
        return this.textBitmap.getLineWidth() * getLocalScale().x;
    }

    public Vector3f[] getLabelBounds() {
        Vector3f min = this.textBitmap.getWorldTranslation().clone();
        Vector3f max = min.clone();
        max.addLocal(getWidth(), -getHeight(), this.shadowOffset.z);
        return new Vector3f[]{min, max};
    }

    public void setVAlignment(VAlignment alignment) {
        if (this.vAlignment == alignment)
            return;
        this.vAlignment = alignment;
        resetAlignment();
    }

    public VAlignment getVAlignment() {
        return this.vAlignment;
    }

    protected void resetAlignment() {
        if (this.textBitmap == null) {
            return;
        }
        this.textBitmap.setBox(null);
        float xOffset = this.textBitmap.getLineWidth();
        float yOffset = this.textBitmap.getHeight();
        float quadOffset = this.textBitmap.getHeight();

        Rectangle rect = new Rectangle(0.0F, 0.0F, xOffset, yOffset);
        this.textBitmap.setBox(rect);
        this.shadowBitmap.setBox(rect);

        switch (this.alignment.ordinal()) {
            case 1:
                xOffset = 0.0F;
                this.textBitmap.setAlignment(BitmapFont.Align.Left);
                this.shadowBitmap.setAlignment(BitmapFont.Align.Left);
                break;
            case 2:
                this.textBitmap.setAlignment(BitmapFont.Align.Right);
                this.shadowBitmap.setAlignment(BitmapFont.Align.Right);
                break;
            case 3:
                xOffset *= 0.5F;
                this.textBitmap.setAlignment(BitmapFont.Align.Center);
                this.shadowBitmap.setAlignment(BitmapFont.Align.Center);
        }

        switch (this.vAlignment.ordinal()) {
            case 1:
                yOffset = 0.0F;
                break;
            case 2:
                quadOffset = 0.0F;
                break;
            case 3:
                yOffset *= 0.5F;
                quadOffset *= 0.5F;
        }

        this.textBitmap.setLocalTranslation(-xOffset, yOffset, 0.0F);
        this.shadowBitmap.setLocalTranslation(this.shadowOffset.x - xOffset, this.shadowOffset.y + yOffset, this.shadowOffset.z);

        if ((rect.width != this.bgBox.getWidth()) || (rect.height != this.bgBox.getHeight())) {
            this.bgBox = new Quad(rect.width, rect.height);
            this.bgGeom.setMesh(this.bgBox);
        }

        this.bgGeom.setLocalTranslation(-xOffset, -quadOffset, -0.6F);
    }

    protected void resetText() {
        if (this.textBitmap == null) {
            return;
        }
        this.textBitmap.setText(this.text);
        this.shadowBitmap.setText(this.text);
        resetAlignment();

        if (this.text == null) {
            setCullHint(Spatial.CullHint.Always);
        } else {
            setCullHint(Spatial.CullHint.Inherit);
        }
    }

    protected void resetColor() {
        if (this.textBitmap == null) {
            return;
        }
        this.textBitmap.setColor(getColor());
        this.shadowBitmap.setColor(getShadowColor());
    }

    public void setFont(BitmapFont font) {
        if (ObjectUtils.areEqual(this.font, font))
            return;
        this.font = font;
        if (this.textBitmap != null)
            detachChild(this.textBitmap);
        if (this.shadowBitmap != null) {
            detachChild(this.shadowBitmap);
        }
        this.textBitmap = new BitmapText(font);
        this.shadowBitmap = new BitmapText(font);
        this.shadowBitmap.setLocalTranslation(this.shadowOffset);

        this.bgBox = new Quad(120.0F, 120.0F);
        this.bgGeom = new Geometry("Label BG", this.bgBox);
        this.bgGeom.setMaterial(MaterialIndex.TRANSPARENT_MATERIAL.clone());

        if (this.bgColor != null) {
            this.bgGeom.getMaterial().setColor("Color", this.bgColor);
        }
        resetText();
        resetColor();
        attachChild(this.shadowBitmap);
        attachChild(this.textBitmap);
        attachChild(this.bgGeom);
    }

    public BitmapFont getFont() {
        return this.font;
    }

    public void setText(String text) {
        if (ObjectUtils.areEqual(this.text, text))
            return;
        this.text = text;
        resetText();
    }

    public String getText() {
        return this.text;
    }

    public void setBackground(ColorRGBA c) {
        if (c == null) {
            c = new ColorRGBA(1.0F, 0.0F, 0.0F, 0.0F);
        }
        this.bgColor = c;
        if (this.bgGeom != null) {
            this.bgGeom.getMaterial().setColor("Color", this.bgColor);
        }
    }

    public ColorRGBA getBackground() {
        return this.bgColor;
    }

    public void setColor(ColorRGBA color) {
        setColor(color, false);
    }

    public void setColor(ColorRGBA color, boolean resetShadow) {
        if (ObjectUtils.areEqual(this.color, color))
            return;
        this.color = color;
        this.shadowColor = null;
        resetColor();
    }

    public ColorRGBA getColor() {
        return this.color == null ? ColorRGBA.White : this.color;
    }

    public void setShadowColor(ColorRGBA color) {
        if (ObjectUtils.areEqual(this.shadowColor, color))
            return;
        this.shadowColor = color;
        resetColor();
    }

    public ColorRGBA getShadowColor() {
        if (this.shadowColor == null) {
            this.shadowColor = new ColorRGBA();
            this.shadowColor.interpolate(getColor(), ColorRGBA.Black, 0.8F);
        }
        return this.shadowColor;
    }

    public String toString() {
        return "Label[" + this.text + "]";
    }
}