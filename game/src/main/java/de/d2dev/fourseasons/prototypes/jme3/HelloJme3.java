package de.d2dev.fourseasons.prototypes.jme3;

import com.jme3.app.SimpleApplication;
import com.jme3.scene.Spatial;

public class HelloJme3 extends SimpleApplication {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HelloJme3 app = new HelloJme3();
		app.start();
	}

	@Override
	public void simpleInitApp() {
		assetManager.registerLocator("D:/codingextra/d2dev/4seasons/assets", com.jme3.asset.plugins.FileLocator.class.getName());
		Spatial torus = assetManager.loadModel("models/Torus.mesh.xml");
		torus.setLocalTranslation( torus.getLocalTranslation().setY(-2) );
		rootNode.attachChild(torus);
	}

}
