package mythruna.client.sound;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioRenderer;

public class SoundNode extends AudioNode {
    public SoundNode(AudioRenderer audioRenderer, AssetManager assetManager, String name, boolean stream) {
        super(assetManager, name, stream);
    }

    public boolean isPlaying() {
        return getStatus() == AudioNode.Status.Playing;
    }

    public String toString() {
        return super.toString() + "(" + this.key + ")";
    }
}