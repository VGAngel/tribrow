package de.PARlib;

import com.jme3.material.Material;
import com.jme3.asset.AssetManager;

public class MaterialHelper {

    public static Material get(AssetManager AM, String matName) {


        if (matName.equals("stone")) {
            Material M = new Material(AM, "Common/MatDefs/Misc/ShowNormals.j3md");
            //M.setTexture("ColorMap", AM.loadTexture("Textures/faalhaas.png"));
            return M;
        }

        return null;
    }

    public static void setSpatial() {

    }

}
