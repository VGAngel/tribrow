package mythruna.client.tabs.map;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

public class MapUtils {
    private static float fallOff = 0.01F;

    public MapUtils() {
    }

    public static BufferedImage createImage(WorldMap worldMap, int xLeaf, int yLeaf, int size) {
        return createImage(worldMap.getTypes(xLeaf, yLeaf, size), xLeaf, yLeaf, size);
    }

    public static BufferedImage createImage(byte[][] types, int xLeaf, int yLeaf, int size) {
        int imageSize = size * 32;

        System.out.println("Creating map images...");
        BufferedImage map = new BufferedImage(imageSize, imageSize, 2);
        BufferedImage background = new BufferedImage(imageSize, imageSize, 2);

        return drawImage(map, background, types, xLeaf, yLeaf, size);
    }

    public static BufferedImage drawImage(BufferedImage map, BufferedImage background, byte types[][], int xLeaf, int yLeaf, int size) {
        System.out.println("Drawing map...");
        int imageSize = size * 32;
        int pixels[] = ((DataBufferInt) map.getRaster().getDataBuffer()).getData();
        int bgPixels[] = ((DataBufferInt) background.getRaster().getDataBuffer()).getData();
        Arrays.fill(pixels, 0);
        Arrays.fill(bgPixels, 0);
        Graphics g = background.getGraphics();
        System.out.println("Building image from types...");
        long start = System.nanoTime();
        int border = 2;
        int index = border + border * imageSize;
        int skip = border * 2;
        float alpha[][] = new float[imageSize][imageSize];
        int waterColor = 0x2a749f;
        int groundColor = 0x779f48;
        for (int y = 2; y < imageSize - 2; y++) {
            int lastEdge = -1;
            for (int x = 2; x < imageSize - 2; x++) {
                int c = 0;
                byte t = types[x][y];
                if (t == 0) {
                    if (lastEdge >= 0) {
                        xAlphaSpan(alpha, lastEdge, -1, y, imageSize);
                        lastEdge = -1;
                    }
                } else if (t == 1) {
                    int count = 0;
                    if (types[x - 1][y - 1] > 1)
                        count++;
                    if (types[x][y - 1] > 1)
                        count++;
                    if (types[x + 1][y - 1] > 1)
                        count++;
                    if (types[x - 1][y] > 1)
                        count++;
                    if (types[x + 1][y] > 1)
                        count++;
                    if (types[x - 1][y + 1] > 1)
                        count++;
                    if (types[x][y + 1] > 1)
                        count++;
                    if (types[x + 1][y + 1] > 1)
                        count++;
                    if (types[x - 2][y - 2] > 1)
                        count++;
                    if (types[x][y - 2] > 1)
                        count++;
                    if (types[x + 2][y - 2] > 1)
                        count++;
                    if (types[x - 2][y] > 1)
                        count++;
                    if (types[x + 2][y] > 1)
                        count++;
                    if (types[x - 2][y + 2] > 1)
                        count++;
                    if (types[x][y + 2] > 1)
                        count++;
                    if (types[x + 2][y + 2] > 1)
                        count++;
                    if (count > 0) {
                        xAlphaSpan(alpha, lastEdge, x, y, imageSize);
                        lastEdge = x;
                        float darken = Math.max(0.25F, 1.0F - (float) count / 6F);
                        if (darken < 0.0F)
                            darken = 0.0F;
                        alpha[x][y] = 1.0F;
                        c = (int) (255F * (1.0F - darken)) << 24;
                    } else {
                        c = waterColor;
                    }
                } else if (t != 0)
                    c = groundColor;
                if (c != 0)
                    pixels[index] = c;
                index++;
            }

            if (lastEdge >= 0)
                xAlphaSpan(alpha, lastEdge, -1, y, imageSize);
            index += skip;
        }

        for (int x = 0; x < imageSize; x++) {
            float a = 0.0F;
            for (int y = 0; y < imageSize; y++) {
                float t = alpha[x][y];
                float n = a - fallOff;
                if (t < a)
                    alpha[x][y] = a;
                else if (n < t)
                    n = t;
                a = n;
            }

            a = 0.0F;
            for (int y = imageSize - 1; y >= 0; y--) {
                float t = alpha[x][y];
                float n = a - fallOff;
                if (t < a)
                    alpha[x][y] = a;
                else if (n < t)
                    n = t;
                a = n;
            }

        }

        g.drawImage(map, 1, 0, null);
        g.drawImage(map, 1, 1, null);
        g.drawImage(map, 0, 1, null);
        index = 0;
        for (int y = 0; y < imageSize; y++) {
            for (int x = 0; x < imageSize; x++) {
                if (types[x][y] != 0) {
                    float f = 0.0F;
                    int v = pixels[index];
                    if (types[x][y] == 1) {
                        f = alpha[x][y];
                        if ((v & 0xff) > 0) {
                            f *= 0.75F;
                            f = f * f * f;
                            if (f > 0.25F)
                                f = 0.25F;
                        } else {
                            f = (float) (v >> 24 & 0xff) / 255F;
                            if (f < 0.3F)
                                f = 0.3F;
                            v |= 0x20;
                        }
                    } else {
                        f = alpha[x][y];
                        f = f * f * f * f * f;
                        if (f > 0.5F)
                            f = 0.5F;
                    }
                    int a = (int) (255F * f);
                    v = v & 0xffffff | a << 24;
                    pixels[index] = v;
                }
                index++;
            }

        }

        long end = System.nanoTime();
        System.out.println((new StringBuilder()).append("Image produced in ").append((double) (end - start) / 1000000D).append(" ms").toString());
        g.drawImage(map, 0, 0, null);
        g.dispose();
        return background;
    }

    private static void xAlphaSpan(float[][] alpha, int last, int current, int y, int imageSize) {
        if (last < 0) {
            for (float a = 1.0F; a > 0.0F; a -= fallOff) {
                if (current <= 0)
                    break;
                alpha[(--current)][y] = a;
            }
            return;
        }
        if (current < 0) {
            for (float a = 1.0F; a > 0.0F; a -= fallOff) {
                if (last >= imageSize - 1)
                    break;
                alpha[(++last)][y] = a;
            }
            return;
        }
        if (last - 1 == current) {
            return;
        }
        for (float a = 1.0F; a > 0.0F; a -= fallOff) {
            if (current > 0)
                alpha[(--current)][y] = a;
            if (last < imageSize - 1) {
                alpha[(++last)][y] = a;
            }
            if (current <= last)
                return;
        }
    }

    private static int mix(int a1, int a2, float mix) {
        float f = a1 * (1.0F - mix) + a2 * mix;
        return (int) f;
    }
}
