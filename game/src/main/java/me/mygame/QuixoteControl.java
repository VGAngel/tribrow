/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * @author normenhansen
 */
public class QuixoteControl extends AbstractControl {
    //Any local variables should be encapsulated by getters/setters so they
    //appear in the SDK properties window and can be edited.
    //Right-click a local variable to encapsulate it with getters and setters.

    private Spatial myMill;
    private float speed = 1.0f;
    private static Random rnd = new Random(System.currentTimeMillis());
    private final Quaternion lookRotation = new Quaternion();

    @Override
    protected void controlUpdate(float tpf) {
        if (myMill == null) {
            millers();
        }
        runToTheMills(tpf);
    }

    public void millers() {
        Node node = spatial.getParent();
        if (node != null) {
            final List<Spatial> mills = new LinkedList<Spatial>();
            node.depthFirstTraversal(new SceneGraphVisitor() {
                public void visit(Spatial spatial) {
                    if (spatial.getName().equals("assets/Models/RotatingMill.j3o")) {
                        mills.add(spatial);
                    }
                }
            });
            if (mills.isEmpty()) {
                return;
            }
            int no = (int) ((mills.size() - 1) * rnd.nextFloat());
            myMill = mills.get(no);
        }
    }

    public void runToTheMills(float tpf) {
        if (myMill != null) {
            Vector3f aim = myMill.getWorldTranslation();
            Vector3f dist = aim.subtract(spatial.getWorldTranslation());
            if (dist.length() < 1) {
                myMill = null;
            } else {
                dist.normalizeLocal();
                lookRotation.lookAt(dist, Vector3f.UNIT_Y);
                spatial.setLocalRotation(lookRotation);
                spatial.move(dist.multLocal(speed * tpf));
            }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //Only needed for rendering-related operations,
        //not called when spatial is culled.
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        QuixoteControl control = new QuixoteControl();
        control.setSpeed(speed);
        control.setSpatial(spatial);
        return control;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled();
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule in = im.getCapsule(this);
        this.speed = in.readFloat("speed", 1.0f);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule out = ex.getCapsule(this);
        out.write(speed, "speed", 1.0f);
    }
}
