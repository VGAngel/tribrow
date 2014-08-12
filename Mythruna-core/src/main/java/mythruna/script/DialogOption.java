package mythruna.script;

import com.jme3.network.serializing.Serializable;
import groovy.lang.Closure;

@Serializable
public final class DialogOption {

    private String text;
    private transient Closure action;

    public DialogOption() {
    }

    public DialogOption(String text, Closure action) {
        this.text = text;
        this.action = action;
    }

    public String getText() {
        return this.text;
    }

    public Closure getAction() {
        return this.action;
    }

    public String toString() {
        return "DialogOption[" + this.text + ", " + this.action + "]";
    }
}