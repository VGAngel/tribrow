package mythruna.client;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import com.jme3.util.BufferUtils;
import com.jme3.util.Screenshots;
import mythruna.client.ui.MessageLog;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScreenshotAppState extends AbstractAppState implements ActionListener, SceneProcessor {

    private static final Logger logger = Logger.getLogger(ScreenshotAppState.class.getName());
    private boolean capture = false;
    private Renderer renderer;
    private ByteBuffer outBuf;
    private String appName;
    private long shotIndex = System.currentTimeMillis();
    private BufferedImage awtImage;
    private ViewPort active;
    private Application app;

    public ScreenshotAppState() {
    }

    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        this.app = app;

        InputManager inputManager = app.getInputManager();
        inputManager.addMapping("ScreenShot", new Trigger[]{new KeyTrigger(60)});
        inputManager.addListener(this, new String[]{"ScreenShot"});

        this.appName = "Mythruna";
    }

    public void update(float tpf) {
        if ((this.active != null) && (!this.capture)) {
            this.active.removeProcessor(this);
            this.active = null;
        }
    }

    public void onAction(String name, boolean value, float tpf) {
        if (value) {
            if (this.active == null) {
                List vps = this.app.getRenderManager().getPostViews();

                for (int i = vps.size() - 1; i >= 0; i--) {
                    ViewPort last = (ViewPort) vps.get(i);
                    if (last.isEnabled()) {
                        this.active = last;
                        break;
                    }
                }

                if (this.active != null) {
                    this.active.addProcessor(this);
                    this.capture = true;
                } else {
                    logger.log(Level.SEVERE, "No active post viewports found.");
                }
            }
        }
    }

    public void initialize(RenderManager rm, ViewPort vp) {
        this.renderer = rm.getRenderer();
        reshape(vp, vp.getCamera().getWidth(), vp.getCamera().getHeight());
    }

    public boolean isInitialized() {
        return (super.isInitialized()) && (this.renderer != null);
    }

    public void reshape(ViewPort vp, int w, int h) {
        this.outBuf = BufferUtils.createByteBuffer(w * h * 4);
        this.awtImage = new BufferedImage(w, h, 6);
    }

    public void preFrame(float tpf) {
    }

    public void postQueue(RenderQueue rq) {
    }

    public void postFrame(FrameBuffer out) {
        if (this.capture) {
            this.capture = false;
            this.shotIndex += 1L;

            this.renderer.readFrameBuffer(out, this.outBuf);
            Screenshots.convertScreenShot(this.outBuf, this.awtImage);

            byte[] pixels = ((DataBufferByte) this.awtImage.getRaster().getDataBuffer()).getData();
            for (int pos = 0; pos < pixels.length; pos += 4) {
                pixels[pos] = -1;
            }

            BufferedImage outImage = this.awtImage;
            try {
                String name = this.appName + this.shotIndex + ".png";
                System.out.println("Writing file:" + name);
                ImageIO.write(outImage, "png", new File(name));
                MessageLog.addMessage("Saved screenshot to:" + name);
                System.out.println("Done.");
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Error while saving screenshot", ex);
            }
        }
    }
}