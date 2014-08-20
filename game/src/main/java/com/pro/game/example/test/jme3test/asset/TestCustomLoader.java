package com.pro.game.example.test.jme3test.asset;

import com.jme3.asset.AssetLoader;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.system.JmeSystem;

/**
 * Demonstrates loading a file from a custom {@link AssetLoader}
 */
public class TestCustomLoader {
    public static void main(String[] args) {
        AssetManager assetManager = JmeSystem.newAssetManager();
        assetManager.registerLocator("/", ClasspathLocator.class);
        assetManager.registerLoader(TextLoader.class, "fnt");
        System.out.println(assetManager.loadAsset("Interface/Fonts/Console.fnt"));
    }
}
