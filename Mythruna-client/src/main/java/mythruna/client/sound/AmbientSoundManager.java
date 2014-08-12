package mythruna.client.sound;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.Listener;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import mythruna.BlockType;
import mythruna.MaterialType;
import mythruna.client.ClientOptions;
import mythruna.client.GameClient;
import mythruna.client.PlayerState;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

public class AmbientSoundManager extends AbstractAppState {
    private GameClient gameClient;
    private PlayerState playerState;
    private AmbientSound underwater;
    private AmbientSound forest;
    private AmbientSound forestEffects;
    private AmbientSound swimming;
    private SoundNode splash;
    private ModalSound walking = new ModalSound();
    private SoundNode ground;
    private SoundNode sand;
    private SoundNode water;
    private SoundNode hard;
    private SoundNode wood;
    private SoundNode leaves;
    private Listener audioListener = new Listener();
    private AmbientSound currentAmbient;
    private AmbientSound currentAmbientEffects;
    private AmbientSound currentOverlay;
    private Map<MaterialType, SoundNode> walkingSounds = new HashMap();

    private int lastType = 0;

    private boolean soundEffectsOn = false;

    public AmbientSoundManager(GameClient gameClient) {
        this.gameClient = gameClient;
    }

    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        this.playerState = ((PlayerState) stateManager.getState(PlayerState.class));

        AudioRenderer audio = app.getAudioRenderer();
        audio.setListener(this.audioListener);

        boolean positional = true;
        this.splash = new SoundNode(audio, app.getAssetManager(), "Sounds/Water,_Splash,_Lunge_03-low.ogg", false);
        this.splash.setPositional(positional);
        this.splash.setReverbEnabled(false);

        boolean stream = false;
        this.ground = new SoundNode(audio, app.getAssetManager(), "Sounds/footsteps/ground.ogg", stream);

        this.ground.setPositional(positional);

        this.ground.setLooping(true);
        this.ground.setReverbEnabled(false);

        this.sand = new SoundNode(audio, app.getAssetManager(), "Sounds/footsteps/sand.ogg", stream);
        this.sand.setPositional(positional);
        this.sand.setLooping(true);
        this.sand.setReverbEnabled(false);

        this.water = new SoundNode(audio, app.getAssetManager(), "Sounds/footsteps/water.ogg", stream);
        this.water.setPositional(positional);
        this.water.setLooping(true);
        this.water.setReverbEnabled(false);

        this.hard = new SoundNode(audio, app.getAssetManager(), "Sounds/footsteps/hard.ogg", stream);
        this.hard.setPositional(positional);

        this.hard.setLooping(true);
        this.hard.setReverbEnabled(false);

        this.wood = new SoundNode(audio, app.getAssetManager(), "Sounds/footsteps/wood.ogg", stream);
        this.wood.setPositional(positional);

        this.wood.setLooping(true);
        this.wood.setReverbEnabled(false);

        this.leaves = new SoundNode(audio, app.getAssetManager(), "Sounds/footsteps/leaves.ogg", stream);
        this.leaves.setPositional(false);

        this.leaves.setLooping(true);
        this.leaves.setReverbEnabled(false);

        this.forest = new AmbientEnvironment(app, this.audioListener);
        setAmbient(this.forest);
        this.forestEffects = new AmbientEffects(app, this.audioListener);
        setAmbientEffects(this.forestEffects);

        this.underwater = new AmbientLoop(app, "Sounds/ambient/Underwater_Ambience_1-low.ogg");
        this.swimming = new AmbientLoop(app, "Sounds/ambient/Ocean_Lapping_on_Wooden_Hull_2-low.ogg");
        this.swimming.setVolume(0.35F);

        this.playerState.getBean().addPropertyChangeListener(new PlayerObserver());

        this.walkingSounds.put(MaterialType.DIRT, this.ground);
        this.walkingSounds.put(MaterialType.GRASS, this.ground);
        this.walkingSounds.put(MaterialType.SAND, this.sand);
        this.walkingSounds.put(MaterialType.STONE, this.hard);
        this.walkingSounds.put(MaterialType.COBBLE, this.hard);
        this.walkingSounds.put(MaterialType.ROCK, this.hard);
        this.walkingSounds.put(MaterialType.WATER, this.water);
        this.walkingSounds.put(MaterialType.WOOD, this.wood);
        this.walkingSounds.put(MaterialType.WADDLE, this.wood);
        this.walkingSounds.put(MaterialType.SHINGLES, this.wood);
        this.walkingSounds.put(MaterialType.GLASS, this.hard);
        this.walkingSounds.put(MaterialType.MARBLE, this.hard);
        this.walkingSounds.put(MaterialType.LEAVES, this.leaves);
        this.walkingSounds.put(MaterialType.EMPTY, null);

        this.audioListener.setLocation(new Vector3f(512.5F, 0.0F, 512.5F));
    }

    public void update(float tpf) {
        Vector3f loc = this.gameClient.getLocation();
        Quaternion dir = this.gameClient.getFacing();

        if (loc != null) {
            if (dir != null) {
                this.audioListener.setRotation(dir);
            }

            this.audioListener.setLocation(new Vector3f(loc.x, loc.z, loc.y));
        }

        ClientOptions options = ClientOptions.getInstance();
        this.soundEffectsOn = options.getSoundEffects();
        float effectsVolume = options.getSoundEffectsVolume();
        this.walking.setEnabled(this.soundEffectsOn);
        this.walking.setMasterVolume(effectsVolume);
        this.underwater.setEnabled(this.soundEffectsOn);
        this.underwater.setMasterVolume(effectsVolume);
        this.swimming.setEnabled(this.soundEffectsOn);
        this.swimming.setMasterVolume(effectsVolume);
        this.splash.setVolume(effectsVolume);

        this.forest.setEnabled(options.getAmbientSounds());
        this.forestEffects.setEnabled(options.getAmbientEffects());
        this.forest.setMasterVolume(options.getAmbientSoundsVolume());
        this.forestEffects.setMasterVolume(options.getAmbientEffectsVolume());

        if (this.playerState.isRunning())
            this.walking.setPitch(1.25F);
        else {
            this.walking.setPitch(1.0F);
        }
        long time = this.gameClient.getTime(GameClient.TimeType.RENDER);
        this.walking.update(time);

        if (this.currentAmbient != null)
            this.currentAmbient.update(tpf);
        if (this.currentAmbientEffects != null)
            this.currentAmbientEffects.update(tpf);
        if (this.currentOverlay != null)
            this.currentOverlay.update(tpf);
    }

    protected void setAmbient(AmbientSound ambient) {
        if (this.currentAmbient == ambient)
            return;
        if (this.currentAmbient != null)
            this.currentAmbient.stop();
        this.currentAmbient = ambient;
    }

    protected void unsetAmbient(AmbientSound ambient) {
        if (this.currentAmbient != ambient)
            return;
        this.currentAmbient.stop();
        this.currentAmbient = null;
    }

    protected void setAmbientEffects(AmbientSound ambient) {
        if (this.currentAmbientEffects == ambient)
            return;
        if (this.currentAmbientEffects != null)
            this.currentAmbientEffects.stop();
        this.currentAmbientEffects = ambient;
    }

    protected void unsetAmbientEffects(AmbientSound ambient) {
        if (this.currentAmbientEffects != ambient)
            return;
        this.currentAmbientEffects.stop();
        this.currentAmbientEffects = null;
    }

    protected void setOverlay(AmbientSound ambient) {
        if (this.currentOverlay == ambient)
            return;
        if (this.currentOverlay != null)
            this.currentOverlay.stop();
        this.currentOverlay = ambient;
    }

    protected void unsetOverlay(AmbientSound ambient) {
        if (this.currentOverlay != ambient)
            return;
        this.currentOverlay.stop();
        this.currentOverlay = null;
    }

    protected void resetWaterState() {
        if (this.playerState.isHeadInWater()) {
            unsetOverlay(this.swimming);
            setAmbient(this.underwater);
            setAmbientEffects(null);
            this.walking.stop();
        } else {
            setAmbient(this.forest);
            setAmbientEffects(this.forestEffects);

            if (this.playerState.getTypeAtFeet() == 7) {
                setOverlay(this.swimming);
            } else {
                unsetOverlay(this.swimming);
            }
        }
    }

    public void setUnderwater(boolean f) {
        if (!isInitialized()) ;
    }

    protected void splash() {
        System.out.println("*********** SPLASH **************");
        if (this.splash.isPlaying())
            this.splash.stop();
        if (!this.soundEffectsOn)
            return;
        this.splash.play();
    }

    protected class PlayerObserver implements PropertyChangeListener {
        protected PlayerObserver() {
        }

        public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();

            if ("headInWater".equals(name)) {
                AmbientSoundManager.this.resetWaterState();
            } else if ("typeAtFeet".equals(name)) {
                int type = ((Integer) e.getNewValue()).intValue();
                int old = ((Integer) e.getOldValue()).intValue();

                if ((old == 0) && ((type == 7) || (type == 8))) {
                    AmbientSoundManager.this.splash();
                }

                AmbientSoundManager.this.resetWaterState();
            } else if ("sunlight".equals(name)) {
                float sun = AmbientSoundManager.this.playerState.getSunlight() - 4.0F;

                if (sun > 0.0F) {
                    sun /= 11.0F;
                } else sun = 0.0F;

                if (AmbientSoundManager.this.forest != null) {
                    AmbientSoundManager.this.forest.setVolume(sun * 1.5F);
                    AmbientSoundManager.this.forestEffects.setVolume(sun * 1.5F);
                }

                float local = 1.0F - sun * 0.5F;

                AmbientSoundManager.this.walking.setVolume(local);
            } else if ("walkType".equals(name)) {
                int type = AmbientSoundManager.this.playerState.getWalkType();

                if ((type == 0) || (AmbientSoundManager.this.playerState.isHeadInWater())) {
                    AmbientSoundManager.this.walking.stop();
                    return;
                }

                BlockType bt = mythruna.BlockTypeIndex.types[type];
                if (bt == null) {
                    AmbientSoundManager.this.walking.stop();
                    return;
                }

                MaterialType material = bt.getMaterial();
                SoundNode sound = (SoundNode) AmbientSoundManager.this.walkingSounds.get(material);
                AmbientSoundManager.this.walking.nextSound(sound, 500L, 500L);
            }
        }
    }
}