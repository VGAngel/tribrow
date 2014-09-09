package com.pro.game.example.run;

import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.debug.Grid;
import com.pro.game.example.RtsCamNew;

/**
 * Created by Valentyn.Polishchuk on 12/30/13
 */
public class GameRun extends SimpleApplication {

    public static void main(String[] args) {
        GameRun app = new GameRun();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        Vector3f centerPos = new Vector3f(0f, 0f, 0f);

        attachCoordinateAxes(centerPos);
        attachGrid(centerPos, 1, ColorRGBA.Gray);

//        RtsCameraControl rtsCam = new RtsCameraControl(cam, rootNode);
//        rtsCam.registerWithInput(inputManager);
//        rtsCam.setCenter(centerPos);

         getStateManager().detach(getStateManager().getState(FlyCamAppState.class));

         RtsCamNew rtsCam = new RtsCamNew(RtsCamNew.UpVector.Y_UP);
         rtsCam.setCenter(new Vector3f(0, 0, 0));
         rtsCam.setDistance(50);
         rtsCam.setMaxSpeed(RtsCamNew.DoF.FWD, 100, 0.5f);
         rtsCam.setMaxSpeed(RtsCamNew.DoF.SIDE, 100, 0.5f);
         rtsCam.setMaxSpeed(RtsCamNew.DoF.DISTANCE, 100, 0.5f);
//         rtsCam.setHeightProvider(new HeightProvider() {
//                 public float getHeight(Vector2f coord) {
//                         return terrain.getHeight(coord)+10;
//                     }
//             });
         getStateManager().attach(rtsCam);

        viewPort.setBackgroundColor(ColorRGBA.Gray);

        initCrossHairs();
    }

    private void attachGrid(Vector3f pos, int size, ColorRGBA color){
        Geometry geo = new Geometry("wireframe grid", new Grid(size, size, 0.2f) );
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", color);
        geo.setMaterial(mat);
        geo.center().move(pos);
        rootNode.attachChild(geo);
    }

    private void attachCoordinateAxes(Vector3f pos){
        Arrow arrow = new Arrow(Vector3f.UNIT_X);
        arrow.setLineWidth(4); // make arrow thicker
        putShape(arrow, ColorRGBA.Red).setLocalTranslation(pos);

        arrow = new Arrow(Vector3f.UNIT_Y);
        arrow.setLineWidth(4); // make arrow thicker
        putShape(arrow, ColorRGBA.Green).setLocalTranslation(pos);

        arrow = new Arrow(Vector3f.UNIT_Z);
        arrow.setLineWidth(4); // make arrow thicker
        putShape(arrow, ColorRGBA.Blue).setLocalTranslation(pos);
    }

    private Geometry putShape(Mesh shape, ColorRGBA color){
        Geometry geo = new Geometry("coordinate axis", shape);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", color);
        geo.setMaterial(mat);
        rootNode.attachChild(geo);
        return geo;
    }

    /**
     * A centred plus sign to help the player aim.
     */
    protected void initCrossHairs() {
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 1.5f);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation( // center
                settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
                settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);

        BitmapText ch2 = new BitmapText(guiFont, false);
        ch2.setSize(guiFont.getCharSet().getRenderedSize());
        ch2.setText("WASD, QE, ZX, RF - Camera Controls");
        ch2.setColor(new ColorRGBA(1f, 0.8f, 0.1f, 1f));
        ch2.setLocalTranslation(settings.getWidth() * 0.3f, settings.getHeight() * 0.1f, 0);
        guiNode.attachChild(ch2);
    }

}
