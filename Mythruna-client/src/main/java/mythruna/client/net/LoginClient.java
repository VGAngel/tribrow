package mythruna.client.net;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.*;
import mythruna.client.CommandStatusListener;
import mythruna.client.ErrorHandler;
import mythruna.es.EntityId;
import mythruna.msg.*;
import mythruna.server.AbstractMessageDelegator;
import org.progeeks.util.log.Log;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class LoginClient {
    static Log log = Log.getLog();
    private Client client;
    private String host;
    private int port;
    private volatile boolean loggedIn = false;
    private CommandStatusListener commandStatusListener;
    private EntityId playerEntity = new EntityId(-2147483648L);
    private AtomicInteger timeCount = new AtomicInteger(0);
    private long connected;
    private Vector3f location;
    private Quaternion facing;
    private ConcurrentLinkedQueue<Message> messages = new ConcurrentLinkedQueue();

    private ClientStateObserver clientObserver = new ClientStateObserver();
    private LoginClientHandler messageHandler = new LoginClientHandler();

    public LoginClient(String host, int port) throws IOException {
        this(Network.connectToServer("Mythruna", 20120627, host, port), host, port);
    }

    public LoginClient(Client client, String host, int port) {
        System.out.println("______LoginClient(" + client + ")");
        this.client = client;
        this.host = host;
        this.port = port;

        Messages.initialize();

        client.addMessageListener(this.messageHandler, new Class[]{LoginStatusMessage.class, AccountStatusMessage.class, TimeMessage.class, EntityStateMessage.class, WarpPlayerMessage.class, EntityListUpdateMessage.class});

        client.addClientStateListener(this.clientObserver);
        client.addErrorListener(this.clientObserver);
    }

    public void release() {
        this.client.removeMessageListener(this.messageHandler, new Class[]{LoginStatusMessage.class, AccountStatusMessage.class, TimeMessage.class, EntityStateMessage.class, WarpPlayerMessage.class, EntityListUpdateMessage.class});

        this.client.removeClientStateListener(this.clientObserver);
        this.client.removeErrorListener(this.clientObserver);
    }

    public Client getClient() {
        return this.client;
    }

    public int getId() {
        return this.client.getId();
    }

    public EntityId getPlayer() {
        if (!isLoggedIn()) {
            throw new RuntimeException("Player is not logged in.");
        }
        return this.playerEntity;
    }

    public void start() {
        this.client.start();
    }

    protected void connected() {
        System.out.println("***** Connected.");
        this.connected = System.currentTimeMillis();
    }

    public boolean checkUdp(long timeThreshold) {
        long time = System.currentTimeMillis();
        if (time - this.connected < timeThreshold) {
            return true;
        }

        if (this.timeCount.get() == 0) {
            String msg = "Client is not receiving UDP messages from the server.\nPlease make sure that UDP port " + this.port + " is not blocked.";

            if (this.commandStatusListener != null) {
                this.commandStatusListener.failed(msg);
                return false;
            }

            throw new RuntimeException(msg);
        }

        return true;
    }

    public void createAccount(String userId, String password, String email, String name, CommandStatusListener listener) {
        this.commandStatusListener = listener;
        if (!checkUdp(0L))
            return;
        this.client.send(new CreateAccountMessage(userId, password, email, name));
    }

    public void login(String userId, String password, CommandStatusListener listener) {
        System.out.println("Sending login to:" + this.client);
        this.commandStatusListener = listener;
        if (!checkUdp(0L))
            return;
        this.client.send(new LoginMessage(userId, password));
    }

    public void close() {
        System.out.println("****** LoginClient.close()");
        this.client.close();
    }

    public boolean isConnected() {
        return this.client.isConnected();
    }

    public boolean isLoggedIn() {
        return this.loggedIn;
    }

    protected void setLocation(Vector3f loc) {
        this.location = loc.clone();
    }

    protected void setFacing(Quaternion q) {
        this.facing = q.clone();
    }

    public Vector3f getLocation() {
        return this.location;
    }

    public Quaternion getFacing() {
        return this.facing;
    }

    public void transferState(MessageListener<Client> l) {
        Message m = null;
        while ((m = (Message) this.messages.poll()) != null) {
            l.messageReceived(this.client, m);
        }
    }

    protected class LoginClientHandler extends AbstractMessageDelegator<Client> {
        public LoginClientHandler() {
            super(LoginClientHandler.class, true);
        }

        protected Object getSourceDelegate(Client source) {
            return this;
        }

        public void updateTime(Client client, TimeMessage msg) {
            LoginClient.this.timeCount.incrementAndGet();
        }

        public void accountStatus(Client client, AccountStatusMessage msg) {
            if (!msg.getSuccess()) {
                LoginClient.this.commandStatusListener.failed(msg.getMessage());
            } else {
                LoginClient.this.commandStatusListener.successful(msg.getMessage());
            }
        }

        public void loginStatus(Client client, LoginStatusMessage msg) {
            if (!msg.isConnected()) {
                LoginClient.this.commandStatusListener.failed(msg.getMessage());
            } else {
                Vector3f pos = msg.getPosition();
                System.out.println("Location:" + pos);
                LoginClient.this.setLocation(pos);

                if (!LoginClient.this.loggedIn) {
                    LoginClient.this.loggedIn = true;
                    LoginClient.this.playerEntity = msg.getPlayer();
                    if (LoginClient.this.commandStatusListener != null)
                        LoginClient.this.commandStatusListener.successful(msg.getMessage());
                }
            }
        }

        public void updateEntityList(Client client, EntityListUpdateMessage msg) {
            if (LoginClient.log.isTraceEnabled())
                LoginClient.log.trace("Received:" + msg);
            LoginClient.this.messages.add(msg);
        }

        public void updateEntity(Client client, EntityStateMessage msg) {
            LoginClient.this.messages.add(msg);
        }

        public void warpPlayer(Client client, WarpPlayerMessage msg) {
            System.out.println("WarpPlayer to:" + msg.getLocation());
            Vector3f loc = msg.getLocation();
            LoginClient.this.setLocation(loc);
            LoginClient.this.setFacing(msg.getFacing());
        }
    }

    protected class ClientStateObserver
            implements ClientStateListener, ErrorListener<Client> {
        private boolean erroredOut = false;

        protected ClientStateObserver() {
        }

        public void clientConnected(Client c) {
            LoginClient.this.connected();
        }

        public void handleError(Client source, Throwable t) {
            System.out.println("****************************************");
            System.out.println("**** handle network Error(" + t + ")");
            t.printStackTrace();
            System.out.println("****************************************");

            if (this.erroredOut) {
                System.exit(-1);
                return;
            }
            this.erroredOut = true;

            ErrorHandler.handle(t, false);

            String message = "Unknown network error:" + t.getMessage();
            JOptionPane.showMessageDialog(null, message, "Disconnected", 0);
        }

        public void clientDisconnected(Client c, ClientStateListener.DisconnectInfo info) {
            System.out.println("****************************************");
            System.out.println("**** disconnected:" + (info == null ? "null" : info.reason));
            System.out.println("****************************************");

            if (info != null) {
                JOptionPane.showMessageDialog(null, info.reason, "Disconnected", 0);
            }
        }
    }
}