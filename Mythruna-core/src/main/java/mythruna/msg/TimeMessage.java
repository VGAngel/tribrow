package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class TimeMessage extends AbstractMessage {

    private long time;
    private double gameTime;

    public TimeMessage() {
    }

    public TimeMessage(long time, double gameTime) {
        this.time = time;
        this.gameTime = gameTime;
    }

    public double getGameTime() {
        return this.gameTime;
    }

    public long getTime() {
        return this.time;
    }

    public String toString() {
        return "TimeMessage[ time:" + this.time + ", gameTime:" + this.gameTime + "]";
    }
}