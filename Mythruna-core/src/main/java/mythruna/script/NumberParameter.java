package mythruna.script;

import com.jme3.network.serializing.Serializable;

@Serializable
public class NumberParameter implements ActionParameter {

    private Number number;

    public NumberParameter() {
    }

    public NumberParameter(Number n) {
        this.number = n;
    }

    public Number getNumber() {
        return this.number;
    }

    public String toString() {
        return "NumberParameter[" + this.number + "]";
    }
}