package mythruna.client.net;

import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import mythruna.Coordinates;
import mythruna.Vector3i;
import mythruna.client.GameClient;
import mythruna.db.AbstractColumnWorldDatabase;
import mythruna.db.ColumnFactory;
import mythruna.db.LeafData;
import mythruna.msg.GetLeafDataMessage;
import mythruna.msg.ResetLeafMessage;
import mythruna.msg.ReturnLeafDataMessage;
import mythruna.msg.SetBlockMessage;
import org.progeeks.util.log.Log;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RemoteWorldDatabase extends AbstractColumnWorldDatabase {
    static Log log = Log.getLog();
    private GameClient gameClient;
    private Client client;
    private Map<Object, PendingRequest> pendingRequests = new ConcurrentHashMap();
    private ExecutorService blockSetter = Executors.newSingleThreadExecutor();

    public RemoteWorldDatabase(GameClient gameClient, Client client) {
        this.gameClient = gameClient;
        this.client = client;

        client.addMessageListener(new MessageObserver(), new Class[]{SetBlockMessage.class, ReturnLeafDataMessage.class, ResetLeafMessage.class});
    }

    public void setSeed(int seed) {
        throw new UnsupportedOperationException("Cannot set the seed on a remote database.");
    }

    public int getSeed() {
        return -1;
    }

    public ColumnFactory getColumnFactory() {
        return null;
    }

    public void close() {
        this.blockSetter.shutdown();
    }

    public boolean leafExists(int x, int y, int z) {
        return false;
    }

    protected Object toLeafKey(int x, int y, int z) {
        int i = Coordinates.worldToLeaf(x);
        int j = Coordinates.worldToLeaf(y);
        int k = Coordinates.worldToLeaf(z);
        return i + "x" + j + "x" + k;
    }

    protected LeafData loadLeaf(int x, int y, int z)
            throws IOException {
        LeafData result = null;

        x = Coordinates.leafToWorld(Coordinates.worldToLeaf(x));
        y = Coordinates.leafToWorld(Coordinates.worldToLeaf(y));
        z = Coordinates.leafToWorld(Coordinates.worldToLeaf(z));

        Object key = toLeafKey(x, y, z);

        PendingRequest req = (PendingRequest) this.pendingRequests.get(key);
        if (req == null) {
            synchronized (this.pendingRequests) {
                req = (PendingRequest) this.pendingRequests.get(key);
                if (req == null) {
                    GetLeafDataMessage msg = new GetLeafDataMessage(x, y, z, -1L);
                    req = new PendingRequest(msg);
                    this.pendingRequests.put(key, req);
                    this.client.send(0, msg);
                }
            }

        }

        try {
            result = req.waitForResult();

            result.clearChanged();

            return result;
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for leaf:" + req, e);
        }
    }

    protected PendingRequest requestLeaf(int x, int y, int z)
            throws IOException {
        x = Coordinates.leafToWorld(Coordinates.worldToLeaf(x));
        y = Coordinates.leafToWorld(Coordinates.worldToLeaf(y));
        z = Coordinates.leafToWorld(Coordinates.worldToLeaf(z));

        Object key = toLeafKey(x, y, z);

        PendingRequest req = (PendingRequest) this.pendingRequests.get(key);
        if (req != null) {
            return req;
        }

        synchronized (this.pendingRequests) {
            req = (PendingRequest) this.pendingRequests.get(key);
            if (req != null) {
                return req;
            }
            GetLeafDataMessage msg = new GetLeafDataMessage(x, y, z, -1L);
            req = new PendingRequest(msg);
            this.pendingRequests.put(key, req);
            this.client.send(0, msg);

            return req;
        }
    }

    protected void writeLeaf(LeafData leaf)
            throws IOException {
    }

    public int setCellType(int x, int y, int z, int type, LeafData leaf) {
        Message m = new SetBlockMessage(this.gameClient.getRawTime(), x, y, z, type);
        this.client.send(0, m);

        SetBlock cmd = new SetBlock(new Vector3i(x, y, z), type);
        this.blockSetter.execute(cmd);

        return -1;
    }

    protected LeafData[] createLeafs(int x, int y) {
        return null;
    }

    protected void setBlock(Vector3i loc, int type) {
        LeafData leaf = getLeaf(loc.x, loc.y, loc.z, false);
        if (leaf == null) {
            return;
        }

        int current = leaf.getWorld(loc.x, loc.y, loc.z);

        if (current == type) {
            return;
        }
        super.setCellType(loc.x, loc.y, loc.z, type, null);
    }

    protected void addLeafData(ReturnLeafDataMessage m) {
        Object key = toLeafKey(m.getX(), m.getY(), m.getZ());

        PendingRequest req = (PendingRequest) this.pendingRequests.get(key);
        if (req == null) {
            log.error("No pending request found for response:" + m);
            return;
        }

        if (req.apply(m))
            this.pendingRequests.remove(key);
    }

    protected boolean loadLeafs(AbstractColumnWorldDatabase.Column col) throws IOException {
        LeafData[] leafs = col.getLeafs();
        PendingRequest[] reqs = new PendingRequest[leafs.length];

        for (int i = 0; i < leafs.length; i++) {
            reqs[i] = requestLeaf(col.getX(), col.getY(), Coordinates.leafToWorld(i));
        }

        int loadedCount = 0;
        for (int i = 0; i < leafs.length; i++) {
            try {
                leafs[i] = reqs[i].waitForResult();
                if (leafs[i] == null) {
                    log.error("Null leaf for:" + col.getX() + ", " + col.getY() + ", " + Coordinates.leafToWorld(i));
                } else {
                    loadedCount++;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted waiting for leaf:" + reqs[i], e);
            }
        }

        return false;
    }

    protected void resetLeafData(Vector3i loc) {
        try {
            System.out.println("resetLeafData(" + loc + ")");
            try {
                AbstractColumnWorldDatabase.Column col = getColumn(loc.x, loc.y, false);
                System.out.println("col:" + col);
                if (col == null) {
                    System.out.println("We don't have this column yet.");
                    return;
                }

                LeafData leaf = loadLeaf(loc.x, loc.y, loc.z);
                System.out.println("leaf:" + leaf);

                int k = Coordinates.worldToLeaf(loc.z);

                System.out.println("k:" + k);

                col.getLeafs()[k] = leaf;

                resetLeaf(leaf);
            } catch (IOException e) {
                log.error("Error retrieving reset leaf data for:" + loc);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    protected class PendingRequest {
        private GetLeafDataMessage request;
        private byte[][] buffers;
        private int count = 0;

        public PendingRequest(GetLeafDataMessage msg) {
            this.request = msg;
        }

        public LeafData waitForResult() throws InterruptedException {
            return this.request.waitForResult();
        }

        public boolean apply(ReturnLeafDataMessage msg) {
            if (!this.request.matches(msg)) {
                return false;
            }
            if (msg.getTotal() == 1) {
                LeafData leaf = msg.getLeafData();
                this.request.setResult(leaf);
                return true;
            }

            if (this.buffers == null) {
                this.buffers = new byte[msg.getTotal()][];
            }
            this.buffers[msg.getPart()] = msg.getData();
            this.count += 1;

            if (this.count < msg.getTotal()) {
                return false;
            }
            LeafData leaf = msg.getLeafData(this.buffers);
            this.request.setResult(leaf);
            return true;
        }

        public String toString() {
            return "PendingRequest[" + this.request + "]";
        }
    }

    protected class ResetLeaf
            implements Runnable {
        Vector3i loc;

        public ResetLeaf(Vector3i loc) {
            this.loc = loc;
        }

        public void run() {
            RemoteWorldDatabase.this.resetLeafData(this.loc);
        }
    }

    protected class SetBlock
            implements Runnable {
        Vector3i loc;
        int type;

        public SetBlock(Vector3i loc, int type) {
            this.loc = loc;
            this.type = type;
        }

        public void run() {
            RemoteWorldDatabase.this.setBlock(this.loc, this.type);
        }
    }

    protected class MessageObserver
            implements MessageListener<Client> {
        protected MessageObserver() {
        }

        public void messageReceived(Client us, Message m) {
            if ((m instanceof SetBlockMessage)) {
                RemoteWorldDatabase.SetBlock cmd = new RemoteWorldDatabase.SetBlock(((SetBlockMessage) m).getLocation(), ((SetBlockMessage) m).getType());

                RemoteWorldDatabase.this.blockSetter.execute(cmd);
            } else if ((m instanceof ReturnLeafDataMessage)) {
                RemoteWorldDatabase.this.addLeafData((ReturnLeafDataMessage) m);
            } else if ((m instanceof ResetLeafMessage)) {
                ResetLeafMessage msg = (ResetLeafMessage) m;
                RemoteWorldDatabase.ResetLeaf cmd = new RemoteWorldDatabase.ResetLeaf(new Vector3i(msg.getX(), msg.getY(), msg.getZ()));
                RemoteWorldDatabase.this.blockSetter.execute(cmd);
            }
        }
    }
}
