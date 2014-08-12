package mythruna.client.ui;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;

public class MessageLog extends ObservableState {
    private static MessageLog instance = new MessageLog();

    private float visibleTime = 15.0F;
    private Node node;
    private BitmapFont font;
    private float fontSize;
    private int backlog = 20;
    private List<MessageEntry> log = new CopyOnWriteArrayList();
    private float screenHeight;
    private float screenWidth;
    private float y;
    private float yTarget = 0.0F;
    private float rate = 0.0F;
    private int lastSize = 0;
    private AudioNode ping;
    private float alphaOverride = 0.0F;

    private boolean playSound = false;

    protected MessageLog() {
        super("MessageLog", true);
    }

    public void setAllVisible(boolean f) {
        if (f)
            this.alphaOverride = 1.0F;
        else
            this.alphaOverride = 0.0F;
    }

    public void move(float yNew, float duration) {
        this.yTarget = yNew;
        this.rate = ((yNew - this.y) / duration);
    }

    protected void initialize(Application app) {
        super.initialize(app);

        Camera cam = app.getCamera();

        this.screenHeight = cam.getHeight();
        this.screenWidth = cam.getWidth();

        this.font = app.getAssetManager().loadFont("Interface/knights24.fnt");
        this.ping = new AudioNode(app.getAssetManager(), "Sounds/paper-small.wav", false);
        this.ping.setReverbEnabled(false);

        this.node = new Node("MessageLog");
        this.node.setLocalTranslation(3.0F, 0.0F, -10.0F);

        this.fontSize = this.font.getPreferredSize();

        int count = (int) (this.screenHeight / this.fontSize);
        if (count > this.backlog) {
            this.backlog = count;
        } else {
            this.fontSize = (this.screenHeight / this.backlog);
        }
    }

    protected void enable() {
        if (this.node.getParent() != null) {
            return;
        }
        Node guiNode = ((SimpleApplication) getApplication()).getGuiNode();
        guiNode.attachChild(this.node);
    }

    protected void disable() {
        Node guiNode = ((SimpleApplication) getApplication()).getGuiNode();
        guiNode.detachChild(this.node);
    }

    public void update(float tpf) {
        if (this.playSound) {
            this.ping.playInstance();
            this.playSound = false;
        }

        int oldSize = this.lastSize;
        this.lastSize = this.log.size();
        int delta = this.lastSize - oldSize;
        float time;
        float yStart;
        int line;
        if (delta > 0) {
            time = Math.min(1.0F, delta * 0.2F);
            yStart = -delta * this.fontSize;
            line = 1;
            for (MessageEntry e : this.log) {
                if (line <= delta) {
                    e.setLocalTranslation(0.0F, yStart + line * this.fontSize, 0.0F);
                    this.node.attachChild(e.getText());
                }

                e.move(line * this.fontSize, time);

                line++;
            }
        } else if (delta < 0) {
            throw new RuntimeException("how did the message log shrink without us knowing?");
        }

        for (MessageEntry e : this.log) {
            e.update(tpf);
            if (e.isDone()) {
                System.out.println("Dropping message:" + e.message);
                this.node.detachChild(e.text);
                this.log.remove(e);
                this.lastSize -= 1;
            }
        }

        if (this.rate != 0.0F) {
            float temp = this.y + this.rate * tpf;
            if (((this.rate > 0.0F) && (temp > this.yTarget)) || ((this.rate < 0.0F) && (temp < this.yTarget))) {
                this.rate = 0.0F;
                this.y = this.yTarget;
                Vector3f loc = this.node.getLocalTranslation();
                this.node.setLocalTranslation(loc.x, this.y, loc.z);
            } else if (Math.abs(temp - this.y) > 0.1D) {
                Vector3f loc = this.node.getLocalTranslation();
                this.y = temp;
                this.node.setLocalTranslation(loc.x, this.y, loc.z);
            }
        }
    }

    public void add(String msg) {
        if ((msg.indexOf('\n') >= 0) || (msg.indexOf('\r') >= 0)) {
            StringTokenizer st = new StringTokenizer(msg, "\r\n");
            while (st.hasMoreTokens()) {
                add(st.nextToken());
            }
            return;
        }

        if (this.font != null) {
            float lineWidth = this.font.getLineWidth(msg);

            while (lineWidth > this.screenWidth - 20.0F) {
                int index = (int) ((this.screenWidth - 20.0F) / lineWidth * msg.length());

                index = Math.min(msg.length() - 1, index);

                String sub = msg.substring(0, index);
                int split = sub.lastIndexOf(' ');
                if (split < 0) {
                    add(sub);
                    msg = msg.substring(index);
                } else {
                    sub = sub.substring(0, split);
                    add(sub);
                    msg = msg.substring(split);
                }

                lineWidth = this.font.getLineWidth(msg);
            }

        }

        this.log.add(0, new MessageEntry(msg));
    }

    public static void addMessage(String msg) {
        addMessage(msg, false);
    }

    public static void addMessage(String msg, boolean makeSound) {
        getInstance().add(msg);
        if ((makeSound) && (getInstance().ping != null)) {
            getInstance().playSound = true;
        }
    }

    public static MessageLog getInstance() {
        return instance;
    }

    private class MessageEntry {
        private float timeVisible;
        private String message;
        private volatile BitmapText text;
        private BitmapText textShadow;
        private float y = 0.0F;
        private float yTarget = 0.0F;
        private float rate = 0.0F;
        private float alpha = 1.0F;

        public MessageEntry(String message) {
            this.timeVisible = 0.0F;
            this.message = message;
        }

        public void move(float yNew, float duration) {
            this.y = getText().getLocalTranslation().y;
            this.yTarget = yNew;
            this.rate = ((yNew - this.y) / duration);
        }

        public boolean isDone() {
            if (this.text == null)
                return false;
            return this.y > MessageLog.this.screenHeight - this.text.getLineHeight();
        }

        protected float getTargetAlpha() {
            if (MessageLog.this.alphaOverride != 0.0F) {
                return MessageLog.this.alphaOverride;
            }
            return Math.max(0.0F, (MessageLog.this.visibleTime - this.timeVisible) / MessageLog.this.visibleTime);
        }

        public void setLocalTranslation(float x, float y, float z) {
            getText().setLocalTranslation(x, y, z);
            float lineHeight = getText().getHeight();
            if (y < lineHeight)
                getText().setCullHint(Spatial.CullHint.Always);
            else
                getText().setCullHint(Spatial.CullHint.Inherit);
        }

        public void update(float tpf) {
            if (this.y > 0.0F) {
                if (MessageLog.this.alphaOverride == 0.0F) {
                    this.timeVisible += tpf;
                }
                float temp = getTargetAlpha();
                if ((Math.abs(temp - this.alpha) > 0.01D) || ((temp == 0.0F) && (this.alpha != 0.0F))) {
                    this.alpha = temp;
                    this.text.setColor(new ColorRGBA(1.0F, 1.0F, 1.0F, this.alpha));
                    this.textShadow.setColor(new ColorRGBA(0.0F, 0.0F, 0.0F, this.alpha));
                }

            }

            if (this.rate != 0.0F) {
                float temp = this.y + this.rate * tpf;
                if (((this.rate > 0.0F) && (temp > this.yTarget)) || ((this.rate < 0.0F) && (temp < this.yTarget))) {
                    this.rate = 0.0F;
                    this.y = this.yTarget;
                    Vector3f loc = getText().getLocalTranslation();
                    setLocalTranslation(loc.x, this.y, loc.z);
                } else if (Math.abs(temp - this.y) > 0.1D) {
                    Vector3f loc = getText().getLocalTranslation();
                    this.y = temp;
                    setLocalTranslation(loc.x, this.y, loc.z);
                }
            }
        }

        public BitmapText getText() {
            if (this.text != null)
                return this.text;
            this.text = new BitmapText(MessageLog.this.font, false);
            this.text.setSize(MessageLog.this.fontSize);
            this.text.setText(this.message);
            this.text.setColor(ColorRGBA.White);

            this.textShadow = new BitmapText(MessageLog.this.font, false);
            this.textShadow.setSize(MessageLog.this.fontSize);
            this.textShadow.setText(this.message);
            this.textShadow.setColor(ColorRGBA.Black);
            this.textShadow.setLocalTranslation(1.0F, -1.0F, -2.0F);
            this.text.attachChild(this.textShadow);

            return this.text;
        }
    }
}