package mythruna.script;

import com.jme3.network.serializing.Serializable;
import mythruna.es.EntityComponent;
import mythruna.es.EntityId;

import java.util.Arrays;

@Serializable
public class DialogPrompt implements EntityComponent {

    private EntityId player;
    private String prompt;
    private DialogOption[] options;

    public DialogPrompt() {
    }

    public DialogPrompt(EntityId player, String prompt, DialogOption[] options) {
        this.player = player;
        this.prompt = prompt;
        this.options = options;
    }

    public Class<DialogPrompt> getType() {
        return DialogPrompt.class;
    }

    public EntityId getPlayer() {
        return this.player;
    }

    public String getPrompt() {
        return this.prompt;
    }

    public DialogOption[] getOptions() {
        return this.options;
    }

    public String toString() {
        return "DialogPrompt[" + this.prompt + " -> " + Arrays.asList(this.options) + "]";
    }
}