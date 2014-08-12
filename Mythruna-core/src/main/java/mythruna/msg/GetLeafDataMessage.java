package mythruna.msg;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import mythruna.db.LeafData;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Serializable
public class GetLeafDataMessage extends AbstractMessage {

    private transient AtomicReference<LeafData> result = new AtomicReference();
    private transient CountDownLatch received = new CountDownLatch(1);
    private int x;
    private int y;
    private int z;
    private long version;

    public GetLeafDataMessage() {
    }

    public GetLeafDataMessage(int x, int y, int z, long version) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.version = version;
    }

    public boolean matches(ReturnLeafDataMessage msg) {
        if ((msg.getX() != this.x) || (msg.getY() != this.y) || (msg.getZ() != this.z))
            return false;
        return true;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public long getVersion() {
        return this.version;
    }

    public void setResult(LeafData result) {
        this.result.set(result);
        this.received.countDown();
    }

    public LeafData waitForResult() throws InterruptedException {
        this.received.await();
        return (LeafData) this.result.get();
    }

    public String toString() {
        return "GetLeafDataMessage[ " + this.x + ", " + this.y + ", " + this.z + "]";
    }
}