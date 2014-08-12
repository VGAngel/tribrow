package mythruna.client.net;

import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import mythruna.db.BlueprintData;
import mythruna.db.BlueprintDatabase;
import mythruna.msg.BlueprintDataMessage;
import mythruna.msg.GetBlueprintMessage;
import org.progeeks.util.log.Log;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class RemoteBlueprintDatabase implements BlueprintDatabase {

    static Log log = Log.getLog();
    private Client client;
    private Map<Long, PendingRequest> pendingRequests = new ConcurrentHashMap();

    public RemoteBlueprintDatabase(Client client) {
        this.client = client;

        client.addMessageListener(new MessageObserver(), new Class[]{BlueprintDataMessage.class});
    }

    public void close() {
        for (PendingRequest req : this.pendingRequests.values())
            req.close();
    }

    public List<Long> getIds() throws IOException {
        throw new UnsupportedOperationException("Remotely iterating over all blueprints is not support.");
    }

    public BlueprintData getBlueprint(long id, boolean load) throws IOException {
        return getBlueprint(id);
    }

    public BlueprintData getBlueprint(long id) throws IOException {
        if (log.isTraceEnabled()) {
            log.trace("RemoteBlueprintDatabase.getBlueprint(" + id + ")");
        }

        PendingRequest request = (PendingRequest) this.pendingRequests.get(Long.valueOf(id));
        if (request == null) {
            synchronized (this.pendingRequests) {
                request = (PendingRequest) this.pendingRequests.get(Long.valueOf(id));
                if (request == null) {
                    GetBlueprintMessage msg = new GetBlueprintMessage(id);
                    msg.setReliable(true);

                    request = new PendingRequest(msg);
                    this.pendingRequests.put(Long.valueOf(id), request);
                    this.client.send(msg);
                }
            }

        }

        try {
            return request.getResult();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted waiting for blueprint data.", e);
        }
    }

    public BlueprintData createBlueprint(String name, int xSize, int ySize, int zSize, float scale, int[][][] cells)
            throws IOException {
        throw new UnsupportedOperationException("Remotely creating blueprints through this interface is not supported.");
    }

    protected class PendingRequest {
        private GetBlueprintMessage request;
        private AtomicReference<BlueprintData> bpData = new AtomicReference();
        private CountDownLatch received = new CountDownLatch(1);

        public PendingRequest(GetBlueprintMessage request) {
            this.request = request;
        }

        public void close() {
            this.received.countDown();
        }

        public void dataReceived(BlueprintData bpData) {
            this.bpData.set(bpData);
            this.received.countDown();
        }

        public BlueprintData getResult() throws InterruptedException {
            this.received.await();
            return (BlueprintData) this.bpData.get();
        }

        public String toString() {
            return "PendingRequest[" + this.request + "]";
        }
    }

    protected class MessageObserver
            implements MessageListener<Client> {
        protected MessageObserver() {
        }

        public void messageReceived(Client client, Message m) {
            BlueprintDataMessage msg = (BlueprintDataMessage) m;

            RemoteBlueprintDatabase.PendingRequest request = (RemoteBlueprintDatabase.PendingRequest) RemoteBlueprintDatabase.this.pendingRequests.remove(Long.valueOf(msg.getId()));
            if (request == null) {
                RemoteBlueprintDatabase.log.error("Received blueprint data but no request is pending, id:" + msg.getId());
                return;
            }

            request.dataReceived(msg.getData());
        }
    }
}