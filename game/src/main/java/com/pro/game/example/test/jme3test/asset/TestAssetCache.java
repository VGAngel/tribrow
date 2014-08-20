package com.pro.game.example.test.jme3test.asset;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetProcessor;
import com.jme3.asset.CloneableAssetProcessor;
import com.jme3.asset.CloneableSmartAsset;
import com.jme3.asset.cache.AssetCache;
import com.jme3.asset.cache.SimpleAssetCache;
import com.jme3.asset.cache.WeakRefAssetCache;
import com.jme3.asset.cache.WeakRefCloneAssetCache;

import java.util.ArrayList;
import java.util.List;

public class TestAssetCache {

    /**
     * Counter for asset keys
     */
    private static int counter = 0;

    /**
     * Dummy data is an asset having 10 KB to put a dent in the garbage collector
     */
    private static class DummyData implements CloneableSmartAsset {

        private AssetKey key;
        private byte[] data = new byte[10 * 1024];

        @Override
        public Object clone() {
            try {
                DummyData clone = (DummyData) super.clone();
                clone.data = data.clone();
                return clone;
            } catch (CloneNotSupportedException ex) {
                throw new AssertionError();
            }
        }

        public byte[] getData() {
            return data;
        }

        public AssetKey getKey() {
            return key;
        }

        public void setKey(AssetKey key) {
            this.key = key;
        }
    }

    /**
     * Dummy key is indexed by a generated ID
     */
    private static class DummyKey extends AssetKey<DummyData> implements Cloneable {

        private int id = 0;

        public DummyKey() {
            super(".");
            id = counter++;
        }

        public DummyKey(int id) {
            super(".");
            this.id = id;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public boolean equals(Object other) {
            return ((DummyKey) other).id == id;
        }

        @Override
        public DummyKey clone() {
            return new DummyKey(id);
        }

        @Override
        public String toString() {
            return "ID=" + id;
        }
    }

    private static void runTest(boolean cloneAssets, boolean smartCache, boolean keepRefs, int limit) {
        counter = 0;
        List<Object> refs = new ArrayList<Object>(limit);

        AssetCache cache;
        AssetProcessor proc = null;

        if (cloneAssets) {
            proc = new CloneableAssetProcessor();
        }

        if (smartCache) {
            if (cloneAssets) {
                cache = new WeakRefCloneAssetCache();
            } else {
                cache = new WeakRefAssetCache();
            }
        } else {
            cache = new SimpleAssetCache();
        }

        System.gc();
        System.gc();
        System.gc();
        System.gc();

        long memory = Runtime.getRuntime().freeMemory();

        while (counter < limit) {
            // Create a key
            DummyKey key = new DummyKey();

            // Create some data
            DummyData data = new DummyData();

            // Post process the data before placing it in the cache
            if (proc != null) {
                data = (DummyData) proc.postProcess(key, data);
            }

            if (data.key != null) {
                // Keeping a hard reference to the key in the cache
                // means the asset will never be collected => bug
                throw new AssertionError();
            }

            cache.addToCache(key, data);

            // Get the asset from the cache
            AssetKey<DummyData> keyToGet = key.clone();

            // NOTE: Commented out because getFromCache leaks the original key
//            DummyData someLoaded = (DummyData) cache.getFromCache(keyToGet);
//            if (someLoaded != data){
//                // Failed to get the same asset from the cache => bug
//                // Since a hard reference to the key is kept, 
//                // it cannot be collected at this point.
//                throw new AssertionError();
//            }

            // Clone the asset
            if (proc != null) {
                // Data is now the clone!
                data = (DummyData) proc.createClone(data);
                if (smartCache) {
                    // Registering a clone is only needed
                    // if smart cache is used.
                    cache.registerAssetClone(keyToGet, data);
                    // The clone of the asset must have the same key as the original
                    // otherwise => bug
                    if (data.key != key) {
                        throw new AssertionError();
                    }
                }
            }

            // Keep references to the asset => *should* prevent
            // collections of the asset in the cache thus causing
            // an out of memory error.
            if (keepRefs) {
                // Prevent the saved references from taking too much memory ..
                if (cloneAssets) {
                    data.data = null;
                }
                refs.add(data);
            }

            if ((counter % 1000) == 0) {
                long newMem = Runtime.getRuntime().freeMemory();
                System.out.println("Allocated objects: " + counter);
                System.out.println("Allocated memory: " + ((memory - newMem) / (1024 * 1024)) + " MB");
                memory = newMem;
            }
        }
    }

    public static void main(String[] args) {
        // Test cloneable smart asset
        System.out.println("====== Running Cloneable Smart Asset Test ======");
        runTest(true, true, false, 100000);

        // Test non-cloneable smart asset
        System.out.println("====== Running Non-cloneable Smart Asset Test ======");
        runTest(false, true, false, 100000);
    }
}
