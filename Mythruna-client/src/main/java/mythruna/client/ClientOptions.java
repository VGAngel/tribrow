package mythruna.client;

import mythruna.Coordinates;
import org.progeeks.util.Inspector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class ClientOptions {
    private static ClientOptions instance = new ClientOptions();
    private static final String PREFS_ROOT = "mythruna";
    private static String[] availableOptions = {"ambientSounds", "ambientSoundsVolume", "ambientEffects", "ambientEffectsVolume", "soundEffects", "soundEffectsVolume", "geometryThreadCount", "maxUpdatesPerFrame", "geometryBuildDelay", "mouseSensitivity", "invertMouse", "modalMap"};

    private static Set<String> optionNames = new HashSet<>(Arrays.asList(availableOptions));
    public static final float ROTATION_SPEED_MAX = 6.0F;
    private Inspector inspector = new Inspector(this);
    private Preferences prefs;
    private int geometryThreads;
    private int maxUpdatesPerFrame;
    private long geometryBuildDelay;
    private float rotationSpeed;
    private boolean invertMouse;
    private boolean ambientSounds = true;
    private float ambientSoundsVolume = 1.0F;
    private boolean ambientEffects = true;
    private float ambientEffectsVolume = 1.0F;
    private boolean soundEffects = true;
    private float soundEffectsVolume = 1.0F;
    private boolean modalMap = true;
    private boolean firstTime = true;

    private Map<String, PrefLink> prefLinks = new ConcurrentHashMap<>();

    protected ClientOptions() {
        this.geometryThreads = Math.min(2, Runtime.getRuntime().availableProcessors());
        this.geometryBuildDelay = (this.geometryThreads > 1 ? 20L : 0L);
        this.maxUpdatesPerFrame = 2;

        this.rotationSpeed = 1.2F;

        setupPrefs();
        loadPrefs();
    }

    public static ClientOptions getInstance() {
        return instance;
    }

    public void reset() {
        try {
            getPrefs().removeNode();
            this.prefs = null;
            loadPrefs();
            save();
        } catch (BackingStoreException e) {
            throw new RuntimeException("Error storing preferences", e);
        }
    }

    protected void setupPrefs() {
        addPref(new FloatPref("rotationSpeed", 1.2F));
        addPref(new BooleanPref("invertMouse", false));
        addPref(new BooleanPref("ambientSounds", true));
        addPref(new FloatPref("ambientSoundsVolume", 1.0F));
        addPref(new BooleanPref("ambientEffects", true));
        addPref(new FloatPref("ambientEffectsVolume", 1.0F));
        addPref(new BooleanPref("soundEffects", true));
        addPref(new FloatPref("soundEffectsVolume", 1.0F));
        addPref(new BooleanPref("firstTime", true));
    }

    protected void addPref(PrefLink p) {
        this.prefLinks.put(p.getName(), p);
    }

    protected void loadPrefs() {
        long start = System.nanoTime();
        for (PrefLink p : this.prefLinks.values())
            p.loadValue();
        long end = System.nanoTime();
        System.out.println("Loaded preferences in " + (end - start / 1000000.0D) + " ms");
    }

    public void save() {
        long start = System.nanoTime();
        System.out.println("--Saving client options.");
        for (PrefLink p : this.prefLinks.values()) {
            p.storeValue();
        }
        try {
            getPrefs().flush();
            long end = System.nanoTime();
            System.out.println("Saved preferences in " + (end - start / 1000000.0D) + " ms");
        } catch (BackingStoreException e) {
            throw new RuntimeException("Error storing preferences", e);
        }
    }

    public Preferences getPrefs() {
        if (this.prefs == null) {
            Preferences userRoot = Preferences.userRoot();
            this.prefs = userRoot.node("mythruna");

            System.out.println("Prefs:" + this.prefs);
            Preferences test = Preferences.userNodeForPackage(Coordinates.class);
            System.out.println("Test:" + test);
        }
        return this.prefs;
    }

    public Set<String> options() {
        return optionNames;
    }

    public <T> T getOption(String option) {
        return (T) this.inspector.get(option);
    }

    public void setOption(String option, Object value) {
        this.inspector.set(option, value);
    }

    public void setFirstTime(boolean f) {
        this.firstTime = f;
    }

    public boolean isFirstTime() {
        return this.firstTime;
    }

    public void setInvertMouse(boolean f) {
        this.invertMouse = f;
    }

    public boolean getInvertMouse() {
        return this.invertMouse;
    }

    public void setAmbientSounds(boolean f) {
        this.ambientSounds = f;
    }

    public boolean getAmbientSounds() {
        return this.ambientSounds;
    }

    public void setAmbientEffects(boolean f) {
        this.ambientEffects = f;
    }

    public boolean getAmbientEffects() {
        return this.ambientEffects;
    }

    public void setSoundEffects(boolean f) {
        this.soundEffects = f;
    }

    public boolean getSoundEffects() {
        return this.soundEffects;
    }

    public void setAmbientSoundsVolume(float f) {
        this.ambientSoundsVolume = f;
    }

    public float getAmbientSoundsVolume() {
        return this.ambientSoundsVolume;
    }

    public void setAmbientEffectsVolume(float f) {
        this.ambientEffectsVolume = f;
    }

    public float getAmbientEffectsVolume() {
        return this.ambientEffectsVolume;
    }

    public void setSoundEffectsVolume(float f) {
        this.soundEffectsVolume = f;
    }

    public float getSoundEffectsVolume() {
        return this.soundEffectsVolume;
    }

    public void setGeometryThreadCount(int val) {
        this.geometryThreads = val;
    }

    public int getGeometryThreadCount() {
        return this.geometryThreads;
    }

    public void setMaxUpdatesPerFrame(int val) {
        this.maxUpdatesPerFrame = val;
    }

    public int getMaxUpdatesPerFrame() {
        return this.maxUpdatesPerFrame;
    }

    public void setGeometryBuildDelay(long val) {
        this.geometryBuildDelay = val;
    }

    public long getGeometryBuildDelay() {
        return 10L;
    }

    public void setRotationSpeed(float f) {
        this.rotationSpeed = f;
    }

    public float getRotationSpeed() {
        return this.rotationSpeed;
    }

    public void setMouseSensitivity(float f) {
        this.rotationSpeed = (f * 6.0F);
    }

    public float getMouseSensitivity() {
        return this.rotationSpeed / 6.0F;
    }

    public void setModalMap(boolean b) {
        this.modalMap = b;
    }

    public boolean getModalMap() {
        return this.modalMap;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Options:\n");
        sb.append("  ambientSounds=" + getAmbientSounds() + "\n");
        sb.append("  ambientEffects=" + getAmbientEffects() + "\n");
        sb.append("  soundEffects=" + getSoundEffects() + "\n");
        sb.append("  geometryThreadCount=" + getGeometryThreadCount() + "\n");
        sb.append("  maxUpdatesPerFrame=" + getMaxUpdatesPerFrame() + "\n");
        sb.append("  geometryBuildDelay=" + getGeometryBuildDelay() + "\n");
        sb.append("  rotationSpeed=" + getRotationSpeed());

        return sb.toString();
    }

    private class BooleanPref extends ClientOptions.PrefLink {
        private boolean defValue;

        public BooleanPref(String name, boolean defValue) {
            super(name);
            this.defValue = defValue;
        }

        public void loadValue() {
            boolean value = ClientOptions.this.getPrefs().getBoolean(this.name, this.defValue);
            ClientOptions.this.setOption(this.name, value);
        }

        public void storeValue() {
            boolean value = ((Boolean) ClientOptions.this.getOption(this.name)).booleanValue();
            if (value != this.defValue)
                ClientOptions.this.getPrefs().putBoolean(this.name, value);
            else
                ClientOptions.this.getPrefs().remove(this.name);
        }
    }

    private class FloatPref extends ClientOptions.PrefLink {
        private float defValue;

        public FloatPref(String name, float defValue) {
            super(name);
            this.defValue = defValue;
        }

        public void loadValue() {
            float value = ClientOptions.this.getPrefs().getFloat(this.name, this.defValue);
            ClientOptions.this.setOption(this.name, value);
        }

        public void storeValue() {
            float value = ((Float) ClientOptions.this.getOption(this.name)).floatValue();
            if (value != this.defValue)
                ClientOptions.this.getPrefs().putFloat(this.name, value);
            else
                ClientOptions.this.getPrefs().remove(this.name);
        }
    }

    private abstract class PrefLink {
        protected String name;

        public PrefLink(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public abstract void loadValue();

        public abstract void storeValue();
    }
}