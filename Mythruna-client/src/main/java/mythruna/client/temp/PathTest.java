package mythruna.client.temp;

import mythruna.BlockTypeIndex;
import mythruna.DefaultWorld;
import mythruna.World;
import mythruna.db.*;
import org.progeeks.util.swing.ActionList;
import org.progeeks.util.swing.ActionUtils;
import org.progeeks.util.swing.CheckBoxAction;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathTest extends JFrame {
    private World world;
    private WorldDatabase worldDb;
    private BufferedImage image = new BufferedImage(1024, 1024, 2);
    private int[] raw;
    private int[][] heights = new int[1024][1024];
    private int[][] ridges = new int[1024][1024];
    private int[][] groups = new int[1024][1024];

    private int numOverlays = 2;
    private BufferedImage[] overlays = new BufferedImage[this.numOverlays];
    private CheckBoxAction[] checks = new CheckBoxAction[this.numOverlays];

    public PathTest() throws Exception {
        super("Path Test");

        setDefaultCloseOperation(2);

        this.world = createWorld(0);
        this.worldDb = this.world.getWorldDatabase();

        this.raw = ((DataBufferInt) this.image.getRaster().getDataBuffer()).getData();

        ActionList toolActions = new ActionList();

        for (int i = 0; i < this.overlays.length; i++) {
            this.overlays[i] = new BufferedImage(1024, 1024, 2);
            this.checks[i] = new CheckBoxAction("Layer " + (i + 1), true);

            this.checks[i].addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
                    PathTest.this.repaint();
                }
            });
            toolActions.add(this.checks[i]);
        }

        JToolBar tools = ActionUtils.createToolBar(toolActions);

        getContentPane().add(tools, "North");

        getContentPane().add(new MapPanel(), "Center");

        addMouseListener(new MouseClicker());

        pack();

        PathThread t = new PathThread();
        t.start();
    }

    private World createWorld(int seed)
            throws Exception {
        BlockTypeIndex.initialize();

        DefaultLeafDatabase leafDb = new DefaultLeafDatabase(new File("mythruna.db"), seed);
        DefaultBlueprintDatabase bpDb = new DefaultBlueprintDatabase(new File("mythruna.db/blueprints"));

        LeafFileLocator locator = new DefaultLeafFileLocator(new File("mythruna.db"));
        ColumnFactory colFactory = WorldUtils.createDefaultColumnFactory(locator, seed);
        WorldDatabase worldDb = new ColumnWorldDatabase(leafDb, colFactory);

        return new DefaultWorld(worldDb, bpDb, null);
    }

    protected int flatness1(int x, int y) {
        int h = this.heights[x][y];
        int total = 0;

        for (int i = x - 1; i <= x + 1; i++) {
            if ((i >= 0) && (i < 1024)) {
                for (int j = y - 1; j <= y + 1; j++) {
                    if ((j >= 0) && (j < 1024)) {
                        total += Math.abs(this.heights[i][j] - h);
                    }
                }
            }
        }
        return total;
    }

    protected int safeHeight(int x, int y, int h) {
        if ((x < 0) || (x >= 1024))
            return h;
        if ((y < 0) || (y >= 1024))
            return h;
        int e = this.heights[x][y];
        return Math.max(56, e);
    }

    protected int flatness2(int x, int y) {
        int h = this.heights[x][y];
        int total = 0;

        total += Math.abs(safeHeight(x - 1, y, h) - h);
        total += Math.abs(safeHeight(x + 1, y, h) - h);
        total += Math.abs(safeHeight(x, y - 1, h) - h);
        total += Math.abs(safeHeight(x, y + 1, h) - h);

        return total;
    }

    protected int flatness3(int x, int y) {
        int h = this.heights[x][y];
        int total = 0;

        int delta1 = safeHeight(x - 1, y, h) - h;
        int delta2 = safeHeight(x + 1, y, h) - h;
        int delta = Math.abs(delta2 - delta1);

        total += delta * delta;

        delta1 = safeHeight(x, y - 1, h) - h;
        delta2 = safeHeight(x, y + 1, h) - h;
        delta = Math.abs(delta2 - delta1);

        total += delta * delta;

        return total;
    }

    protected void buildFlatOverlay() {
        int[] data = ((DataBufferInt) this.overlays[0].getRaster().getDataBuffer()).getData();

        int max = 32;

        int pos = 0;
        for (int y = 0; y < 1024; y++) {
            for (int x = 0; x < 1024; x++) {
                int h = this.heights[x][y];
                if (h >= 56) {
                    int f2 = flatness3(x, y);

                    if (f2 == 1) {
                        f2 = 0;
                    }
                    this.ridges[x][y] = f2;

                    f2 = Math.min(max, f2 * f2 * f2);

                    if (f2 > 0) {
                        f2 = max;
                    }

                    int r = (max - f2) * 255 / max;

                    int clr = 0x80000000 | r << 16;

                    data[pos] = clr;
                } else {
                    this.ridges[x][y] = 'ÿ';
                }

                pos++;
            }
            repaint();
        }
    }

    protected int merge(int group, int merge, Map<Integer, List<Integer>> groupMap, int[][] groups) {
        List<Integer> current = (List<Integer>) groupMap.get(group);
        List<Integer> toMerge = (List<Integer>) groupMap.remove(merge);
        current.addAll(toMerge);

        for (Integer i : toMerge) {
            int x = i.intValue() % 1024;
            int y = i.intValue() / 1024;

            groups[x][y] = group;
        }

        return group;
    }

    protected void buildAreaOverlay() {
        int group = 0;
        int nextGroup = 1;
        int pos = -1;

        Map<Integer, List<Integer>> groupMap = new HashMap<Integer, List<Integer>>();

        for (int y = 0; y < 1024; y++) {
            int last = -1;
            for (int x = 0; x < 1024; x++) {
                int r = this.ridges[x][y];
                pos++;

                if (r != 0) {
                    last = r;
                } else {
                    if (last != 0) {
                        int above = y == 0 ? 0 : this.groups[x][(y - 1)];
                        if (above > 0)
                            group = above;
                        else
                            group = nextGroup++;
                    } else {
                        int above = y == 0 ? 0 : this.groups[x][(y - 1)];
                        if ((above > 0) && (above != group)) {
                            group = merge(group, above, groupMap, this.groups);
                        }
                    }
                    last = r;

                    List<Integer> list = groupMap.get(Integer.valueOf(group));
                    if (list == null) {
                        list = new ArrayList<Integer>();
                        groupMap.put(Integer.valueOf(group), list);
                    }
                    list.add(Integer.valueOf(pos));
                    this.groups[x][y] = group;
                }
            }
        }
        System.out.println("Last group:" + group);
        System.out.println("Group map size::" + groupMap.size());
        Map<Integer, Integer> colors = new HashMap<Integer, Integer>();
        int[] data = ((DataBufferInt) this.overlays[1].getRaster().getDataBuffer()).getData();
        pos = 0;
        for (int y = 0; y < 1024; y++) {
            for (int x = 0; x < 1024; x++) {
                int g = this.groups[x][y];
                if (g == 0) {
                    pos++;
                } else {
                    Integer color = colors.get(Integer.valueOf(g));
                    if (color == null) {
                        color = Integer.valueOf((int) (Math.random() * 16777215.0D));
                        colors.put(Integer.valueOf(g), color);
                    }
                    int clr = 0xAA000000 | color.intValue() & 0xFFFFFF;
                    data[pos] = clr;
                    pos++;
                }
            }
        }
        repaint();
    }

    protected void buildBaseImage() {
        int baseX = 0;
        int baseY = 0;

        for (int y = 0; y < 1024; y += 32) {
            int pos = y * 1024;
            for (int x = 0; x < 1024; x += 32) {
                ColumnInfo info = this.worldDb.getColumnInfo(x + baseX, y + baseY, true);

                byte[][] types = info.getTypes();
                byte[][] elevations = info.getElevations();

                for (int j = 0; j < 32; j++) {
                    int sub = pos + j * 1024;
                    for (int i = 0; i < 32; i++) {
                        int clr = -16777216;

                        this.heights[(x + i)][(y + j)] = elevations[i][j];

                        int t = types[i][j];
                        boolean underwater = (t & 0x80) != 0;
                        t &= 127;

                        switch (t) {
                            case 1:
                                clr = -6266880;
                                break;
                            case 2:
                                clr = -65536;
                                break;
                            case 3:
                                clr = -2237014;
                                break;
                            case 4:
                                clr = -5592406;
                                break;
                            case 7:
                                clr = -16776961;
                                break;
                            case 12:
                                clr = -13587968;
                                break;
                            case 13:
                                clr = -8388480;
                            case 5:
                            case 6:
                            case 8:
                            case 9:
                            case 10:
                            case 11:
                        }
                        if ((t == 2) || (t == 12)) {
                            int h = elevations[i][j];

                            h = Math.max(0, h - 56);
                            int g = h * 200 / 94 + 55 & 0xFF;
                            int r = 0;
                            int b = 0;
                            clr = 0xFF000000 | r << 16 | g << 8 | b;
                        }

                        if (underwater) {
                            int h = elevations[i][j];
                            if ((h < 0) || (h > 57))
                                h = 0;
                            int r = 0;
                            int g = (h < 41 ? 0 : (h - 41) * 16) & 0xFF;
                            int b = h * 255 / 57 & 0xFF;

                            clr = 0xFF000000 | r << 16 | g << 8 | b;
                        }

                        this.raw[sub] = clr;

                        sub++;
                    }
                }

                pos += 32;
                repaint();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        PathTest test = new PathTest();
        test.setVisible(true);
    }

    public class PathThread extends Thread {
        public PathThread() {
            setDaemon(true);
        }

        public void run() {
            PathTest.this.buildBaseImage();
            PathTest.this.buildFlatOverlay();
            System.out.println("Building area overlay...");
            PathTest.this.buildAreaOverlay();
            System.out.println("Done building area overlay.");
        }
    }

    private class MouseClicker extends MouseInputAdapter {
        private MouseClicker() {
        }

        public void mouseClicked(MouseEvent e) {
            System.out.println("Click:" + e);

            int x = e.getX();
            int y = 1024 - (PathTest.this.getHeight() - e.getY());

            System.out.println("Getting elevation info for:" + x + ", " + y);

            ColumnInfo info = PathTest.this.worldDb.getColumnInfo(x, y, true);

            System.out.println("Info:" + info);
            System.out.println("Type:" + info.getType(x, y) + "  height:" + info.getElevation(x, y));
        }
    }

    private class MapPanel extends JPanel {
        private Dimension pref = new Dimension(1024, 1024);

        private MapPanel() {
        }

        public Dimension getPreferredSize() {
            return (Dimension) this.pref.clone();
        }

        public Dimension getMinimumSize() {
            return (Dimension) this.pref.clone();
        }

        public void paint(Graphics g) {
            int y = getHeight() - 1024;
            g.drawImage(PathTest.this.image, 0, y, null);

            for (int i = 0; i < PathTest.this.overlays.length; i++) {
                if (PathTest.this.checks[i].isChecked())
                    g.drawImage(PathTest.this.overlays[i], 0, y, null);
            }
        }
    }
}