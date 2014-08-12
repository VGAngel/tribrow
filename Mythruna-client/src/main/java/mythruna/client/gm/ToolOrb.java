package mythruna.client.gm;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.ui.Picture;
import mythruna.client.ui.GuiAppState;
import mythruna.client.ui.HAlignment;
import mythruna.client.ui.Label;
import mythruna.client.ui.VAlignment;
import mythruna.client.view.BlockObject;

public class ToolOrb extends Node {
    private Picture orb;
    private float orbSize;
    private Label label;
    private GuiAppState gui;
    private Node perspectiveRoot;
    private BlockObject icon = null;

    public ToolOrb(AssetManager assets, float size, GuiAppState gui, Node perspectiveRoot) {
        this.orbSize = size;
        this.gui = gui;
        this.perspectiveRoot = perspectiveRoot;

        this.orb = new Picture("Orb");
        this.orb.setWidth(this.orbSize);
        this.orb.setHeight(this.orbSize);
        this.orb.setImage(assets, "Interface/gilded-orb-128.png", true);
        attachChild(this.orb);

        BitmapFont font = assets.loadFont("Interface/templar32.fnt");
        this.label = new Label(font);
        this.label.setHAlignment(HAlignment.CENTER);
        this.label.setVAlignment(VAlignment.CENTER);
        this.label.setText("Hand");
        this.label.setColor(new ColorRGBA(0.9F, 1.0F, 1.0F, 0.5F));

        System.out.println("************ Label width:" + this.label.getWidth() + " orb Size:" + this.orbSize);
        float maxLabelWidth = this.orbSize * 0.8F;
        if (this.label.getWidth() > maxLabelWidth) {
            float labelScale = maxLabelWidth / this.label.getWidth();
            System.out.println("label scale:" + labelScale);
            this.label.setLocalScale(labelScale);
        }

        this.label.setLocalTranslation(this.orbSize * 0.5F, this.orbSize * 0.5F, -1.0F);
        attachChild(this.label);
    }

    protected void setParent(Node parent) {
        super.setParent(parent);
        if ((parent != null) && (this.icon != null)) {
            this.perspectiveRoot.attachChild(this.icon.getNode());
        } else if (this.icon != null) {
            this.perspectiveRoot.detachChild(this.icon.getNode());
        }
    }

    public void setName(String name) {
        if (name == null) {
            name = "";
        }
        this.label.setText(name);
        this.label.setLocalScale(1.0F);

        float maxLabelWidth = this.orbSize * 0.8F;
        if (this.label.getWidth() > maxLabelWidth) {
            float labelScale = maxLabelWidth / this.label.getWidth();
            System.out.println("label scale:" + labelScale);
            this.label.setLocalScale(labelScale);
        }
    }

    public String getName() {
        return this.label.getText();
    }

    public void setIcon(BlockObject icon) {
        if (this.icon == icon) {
            return;
        }
        if (this.icon != null) {
            this.perspectiveRoot.detachChild(this.icon.getNode());
        }

        this.icon = icon;

        if (this.icon == null) {
            return;
        }
        this.perspectiveRoot.attachChild(icon.getNode());

        Vector3f loc = new Vector3f(this.orbSize * 0.5F, this.orbSize * 0.33F, 0.0F);
        Vector3f local = localToWorld(loc, null);
        Vector3f v = this.gui.screenToWorld(local.x, local.y, 20.0F);
        icon.getNode().setLocalTranslation(v);

        Quaternion quat0 = new Quaternion().fromAngles(0.0F, 0.7853982F, 0.0F);
        Quaternion quat1 = new Quaternion().fromAngles(0.7853982F, 0.0F, 0.0F);
        Vector3f back = icon.getNode().getWorldTranslation().mult(-1.0F).normalizeLocal();
        Quaternion quat2 = new Quaternion();
        quat2.lookAt(back, new Vector3f(0.0F, 1.0F, 0.0F));

        icon.getNode().setLocalRotation(quat2.mult(quat1).mult(quat0));
    }
}