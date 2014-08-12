package mythruna.client.tabs.map;

import com.jme3.texture.Image;
import com.jme3.util.BufferUtils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;

public class ImageConverter {
    private BufferedImage original;
    private ByteBuffer data;

    public ImageConverter(BufferedImage original) {
        this.original = original;
        this.data = BufferUtils.createByteBuffer(original.getWidth() * original.getHeight() * 4);
    }

    public Image toImage() {
        int width = this.original.getWidth();
        int height = this.original.getHeight();
        int[] pixels = ((DataBufferInt) this.original.getRaster().getDataBuffer()).getData();

        this.data.clear();

        boolean flip = true;
        int index;
        int x;
        for (int y = 0; y < height; y++) {
            if (flip)
                index = (height - y - 1) * width;
            else
                index = y * width;
            for (x = 0; x < width; ) {
                int argb = pixels[index];
                byte a = (byte) (argb >> 24 & 0xFF);
                byte r = (byte) (argb >> 16 & 0xFF);
                byte g = (byte) (argb >> 8 & 0xFF);
                byte b = (byte) (argb & 0xFF);

                this.data.put(r).put(g).put(b).put(a);

                x++;
                index++;
            }

        }

        this.data.flip();
        return new Image(Image.Format.RGBA8, width, height, this.data);
    }
}