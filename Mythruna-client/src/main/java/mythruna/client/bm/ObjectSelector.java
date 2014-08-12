package mythruna.client.bm;

import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import mythruna.BlockType;
import mythruna.BlockTypeIndex;
import mythruna.BlockTypeIndex.BlockGroup;
import mythruna.World;
import mythruna.client.GameClient;
import mythruna.client.ModeManager;
import mythruna.client.WorldIntersector;
import mythruna.client.WorldIntersector.Intersection;
import mythruna.client.view.DragControl;
import mythruna.client.view.LocalArea;
import mythruna.es.EntityData;
import mythruna.es.EntityId;
import mythruna.geom.GeomFactory;
import mythruna.geom.GeomPartBuffer;
import mythruna.script.ActionReference;
import mythruna.script.ToolActions;
import org.progeeks.util.log.Log;

public class ObjectSelector implements AnalogListener, ActionListener {
    static Log log = Log.getLog();
    private static final String ADJUST_ITEM = "Adjust Sub-type";
    private static final String ITEM_UP = "Item Up";
    private static final String ITEM_DOWN = "Item Down";
    private static final String TYPE_UP = "Type Up";
    private static final String TYPE_DOWN = "Type Down";
    private GameClient gameClient;
    private AppStateManager stateManager;
    private InputManager inputManager;
    private World world;
    private List<ToolGroup> toolGroups = new ArrayList();
    private ObjectToolGroup objectTools;
    private ClaimsGroup claimTools;
    private List<ToolGroup> dynamicGroups = new ArrayList();
    private ToolActions lastActions;
    private int group = 0;
    private int type = 0;

    private boolean typeDebounce = false;

    private Tool lastTool = null;
    private LocalArea localArea;
    private EntityId heldEntity = null;
    private float heldDistance = 0.0F;
    private Vector3f heldOffset = null;
    private float angleOffset = 0.0F;
    private Quaternion rotDelta;
    private boolean oldWay = true;

    private boolean enabled = true;

    public ObjectSelector(GameClient gameClient, InputManager inputManager, AppStateManager stateManager)
    {
        if (gameClient == null)
            throw new IllegalArgumentException("GameClient cannot be null.");
        this.gameClient = gameClient;
        this.world = gameClient.getWorld();
        this.stateManager = stateManager;
        this.inputManager = inputManager;
        registerWithInput();

        for (BlockTypeIndex.BlockGroup bg : BlockTypeIndex.groups())
        {
            this.toolGroups.add(createBlockToolGroup(bg));
        }

        this.objectTools = new ObjectToolGroup("Object Mode");
        this.toolGroups.add(this.objectTools);

        this.claimTools = new ClaimsGroup();
        this.toolGroups.add(this.claimTools);

        for (ToolGroup tg : this.toolGroups) {
            tg.initialize(this);
        }
        if (!this.toolGroups.isEmpty())
            ((ToolGroup)this.toolGroups.get(0)).setVisible(true);
    }

    public void setEnabled(boolean f)
    {
        if (this.enabled == f)
            return;
        this.enabled = f;
        if (this.enabled)
        {
            registerWithInput();
        }
        else
        {
            unregisterInput();
        }
    }

    protected ToolGroup createBlockToolGroup(BlockTypeIndex.BlockGroup bg)
    {
        ToolGroup g = new ToolGroup(bg.getName());
        for (BlockType type : bg.getTypes())
            g.getTools().add(new BlockTool(type));
        return g;
    }

    protected void loadDynamicTools()
    {
        EntityData ed = this.world.getEntityData();
        ToolActions ta = (ToolActions)ed.getComponent(this.gameClient.getPlayer(), ToolActions.class);
        if (ta == null)
        {
            System.out.println("No tool actions found yet.  Will try again next frame.");
            return;
        }

        System.out.println("Tool actions:" + ta);
        this.lastActions = ta;

        for (ActionReference ar : ta.getActions())
        {
            System.out.println("Adding tool for:" + ar);

            ToolGroup g = getGroup(ar.getGroup());
            if (g == null)
            {
                g = new ToolGroup(ar.getGroup());
                this.dynamicGroups.add(g);
                this.toolGroups.add(g);
            }

            ActionTool t = new ActionTool(ar);
            g.getTools().add(t);
        }

        for (ToolGroup tg : this.dynamicGroups)
        {
            System.out.println("Initializing:" + tg + "  containing tools:" + tg.getTools());
            tg.initialize(this);
        }
    }

    public void update()
    {
        if ((this.lastActions == null) && (this.dynamicGroups.isEmpty()))
        {
            loadDynamicTools();
        }

        if (this.objectTools.update())
        {
            this.type = Math.min(this.type, getGroupSize(this.group) - 1);

            this.lastTool = null;
        }

        if ((this.claimTools != null) && (this.claimTools.update()))
        {
            this.type = Math.min(this.type, getGroupSize(this.group) - 1);

            this.lastTool = null;
        }
    }

    public void setLocalArea(LocalArea localArea)
    {
        this.localArea = localArea;
    }

    public World getWorld()
    {
        return this.world;
    }

    public LocalArea getLocalArea()
    {
        return this.localArea;
    }

    public GameClient getGameClient()
    {
        return this.gameClient;
    }

    public Vector3f getLocation()
    {
        return this.localArea.getLocation();
    }

    public AppStateManager getStateManager()
    {
        return this.stateManager;
    }

    public String getLabel()
    {
        return getSelectedTool().getName();
    }

    public Spatial getIcon()
    {
        Spatial icon = getSelectedTool().getIcon();
        if (icon == null)
            icon = getToolGroup(this.group).getIcon();
        return icon;
    }

    public Tool getSelectedTool()
    {
        if (this.lastTool != null) {
            return this.lastTool;
        }
        ToolGroup tg = getToolGroup(this.group);
        if (tg.getTools().size() == 0)
            return null;
        this.lastTool = ((Tool)tg.getTools().get(this.type));
        return this.lastTool;
    }

    public ToolGroup getToolGroup(int index)
    {
        return (ToolGroup)this.toolGroups.get(index);
    }

    protected ToolGroup getGroup(String name)
    {
        for (ToolGroup tg : this.toolGroups)
        {
            if (tg.getName().equals(name))
                return tg;
        }
        return null;
    }

    protected int getGroupSize(int index)
    {
        return getToolGroup(index).getTools().size();
    }

    protected void registerWithInput()
    {
        this.inputManager.addMapping("Adjust Sub-type", new Trigger[] { new KeyTrigger(29) });
        this.inputManager.addMapping("Type Up", new Trigger[] { new KeyTrigger(52) });
        this.inputManager.addMapping("Type Down", new Trigger[] { new KeyTrigger(51) });

        this.inputManager.addListener(this, new String[] { "Adjust Sub-type", "Type Up", "Type Down" });

        ModeManager.instance.addMode("Adjust Sub-type", "Item Up", "Item Down", this);

        ModeManager.instance.setDefaultMode("Type Up", "Type Down", this);
    }

    protected void unregisterInput()
    {
        if (this.inputManager.hasMapping("Adjust Sub-type"))
            this.inputManager.deleteMapping("Adjust Sub-type");
        if (this.inputManager.hasMapping("Type Up"))
            this.inputManager.deleteMapping("Type Up");
        if (this.inputManager.hasMapping("Type Down")) {
            this.inputManager.deleteMapping("Type Down");
        }
        this.inputManager.removeListener(this);
        ModeManager.instance.removeMode("Adjust Sub-type");
        ModeManager.instance.clearDefaultMode("Type Up", "Type Down");
    }

    public void onAnalog(String name, float value, float tpf)
    {
        if (isCapturingView()) {
            return;
        }
        try
        {
            if (ModeManager.instance.isActive("Adjust Sub-type"))
            {
                if (name.equals("Type Up"))
                    name = "Item Up";
                else if (name.equals("Type Down")) {
                    name = "Item Down";
                }
            }

            if (("Item Up".equals(name)) && (!this.typeDebounce))
            {
                this.type += 1;
                if (this.type >= getGroupSize(this.group))
                    this.type = 0;
            }
            else if (("Item Down".equals(name)) && (!this.typeDebounce))
            {
                this.type -= 1;
                if (this.type < 0)
                    this.type = Math.max(0, getGroupSize(this.group) - 1);
            }
            else if (("Type Up".equals(name)) && (!this.typeDebounce))
            {
                setGroup(this.group + 1);
            }
            else if (("Type Down".equals(name)) && (!this.typeDebounce))
            {
                setGroup(this.group - 1);
            }
            else if ("Adjust Sub-type".equals(name))
            {
                return;
            }
            this.lastTool = null;
        }
        catch (RuntimeException e)
        {
            log.error("Error processing input:" + name, e);
        }
    }

    protected void setGroup(int newGroup)
    {
        if (this.group == newGroup) {
            return;
        }
        if (this.group < this.toolGroups.size()) {
            ((ToolGroup)this.toolGroups.get(this.group)).setVisible(false);
        }
        this.group = newGroup;
        if (this.group >= this.toolGroups.size())
            this.group = 0;
        if (this.group < 0) {
            this.group = (this.toolGroups.size() - 1);
        }
        ((ToolGroup)this.toolGroups.get(this.group)).setVisible(true);

        this.type = Math.max(0, Math.min(this.type, getGroupSize(this.group) - 1));
        this.lastTool = null;
    }

    public void onAction(String name, boolean value, float tpf)
    {
        try
        {
            if ("Adjust Sub-type".equals(name))
            {
                if ((value) && (ModeManager.instance.getMode() == null))
                    ModeManager.instance.setMode("Adjust Sub-type", value);
                else if (!value)
                    ModeManager.instance.setMode("Adjust Sub-type", value);
            }
            else if ("Type Up".equals(name))
            {
                this.typeDebounce = value;
            }
            else if ("Type Down".equals(name))
            {
                this.typeDebounce = value;
            }

        }
        catch (RuntimeException e)
        {
            log.error("Error processing input:" + name, e);
        }
    }

    protected Spatial createBlockIcon(BlockType type)
    {
        GeomFactory gf = type.getGeomFactory();

        GeomPartBuffer buffer = new GeomPartBuffer();

        for (int i = 0; i < 6; i++)
        {
            gf.createGeometry(buffer, 0, 0, 0, 0, 0, 0, 1.0F, 0.5F, type, i);
        }
        gf.createInternalGeometry(buffer, 0, 0, 0, 0, 0, 0, 1.0F, 0.5F, type);

        Node node = buffer.createNode("Icon:" + type.getName());
        node.setLocalTranslation(-0.5F, -0.5F, -0.5F);

        return node;
    }

    public boolean isCapturingView()
    {
        return getSelectedTool().isCapturingView();
    }

    public boolean showBlockSelection()
    {
        return getSelectedTool().showBlockSelection();
    }

    protected WorldIntersector.Intersection intersectWorld(Vector3f pos, Vector3f dir, Integer[] skipTypes)
    {
        Ray ray = new Ray(pos, dir);
        ray.setLimit(10.0F);

        WorldIntersector wi = new WorldIntersector(this.localArea, ray, skipTypes);
        WorldIntersector.Intersection hit = null;
        if (wi.hasNext())
        {
            WorldIntersector.Intersection isect = wi.next();

            hit = isect;
        }

        return hit;
    }

    public void viewMoved(Vector3f pos, Vector3f dir, Quaternion rotation)
    {
        getSelectedTool().viewMoved(pos, dir, rotation);
    }

    protected CollisionResult intersectObjects(Vector3f pos, Vector3f dir)
    {
        WorldIntersector.Intersection hit = intersectWorld(pos, dir, new Integer[0]);

        return intersectObjects(pos, dir, hit);
    }

    public Node findEntityParent(Geometry g)
    {
        for (Node n = g.getParent(); n != null; n = n.getParent())
        {
            if (n.getUserData("id") != null)
                return n;
        }
        return null;
    }

    protected CollisionResult intersectObjects(Vector3f pos, Vector3f dir, WorldIntersector.Intersection limit)
    {
        Ray ray = new Ray(pos.clone(), dir.clone());
        if (limit != null)
        {
            Vector3f delta = limit.getPoint().subtract(pos);
            ray.setLimit(delta.length());
        }

        long start = System.nanoTime();

        CollisionResults results = new CollisionResults();

        ray.origin = new Vector3f(0.0F, 0.0F, 0.0F);

        float y = ray.direction.z;
        ray.direction.z = ray.direction.y;
        ray.direction.y = y;

        Node target = this.localArea;
        target.updateGeometricState();

        target.collideWith(ray, results);
        long end = System.nanoTime();
        log.debug("Collided in:" + (end - start / 1000000.0D) + " ms");

        log.debug("Collision count:" + results.size());

        for (int i = 0; i < results.size(); i++)
        {
            float d = results.getCollision(i).getDistance();

            Node parent = findEntityParent(results.getCollision(i).getGeometry());
            if (parent == null)
            {
                System.out.println("No ID'ed parent found for:" + results.getCollision(i).getGeometry());
            }
            else
            {
                Long id = (Long)parent.getUserData("id");
                if ((id != null) || (parent.getControl(DragControl.class) != null))
                {
                    log.info("Selected id:" + id);
                    return results.getCollision(i);
                }
            }
        }
        return null;
    }

    public void selectBlockType(Vector3f pos, Vector3f dir, Quaternion rotation)
    {
        WorldIntersector.Intersection hit = intersectWorld(pos, dir, new Integer[0]);
        if (hit == null) {
            return;
        }
        log.debug("hit type:" + hit.getType());
        if (hit.getType() <= 0) {
            return;
        }
        BlockType blockType = BlockTypeIndex.types[hit.getType()];

        int groupIndex = 0;
        for (BlockTypeIndex.BlockGroup g : BlockTypeIndex.groups())
        {
            int typeIndex = 0;
            for (BlockType t : g.getTypes())
            {
                if (t == blockType)
                {
                    this.group = groupIndex;
                    this.type = typeIndex;

                    this.lastTool = null;
                    break;
                }
                typeIndex++;
            }
            groupIndex++;
        }
    }

    public void select(Vector3f pos, Vector3f dir, Quaternion rotation, boolean value)
    {
        getSelectedTool().select(pos, dir, rotation, value);
    }

    public void place(Vector3f pos, Vector3f dir, Quaternion rotation, boolean value)
    {
        getSelectedTool().place(pos, dir, rotation, value);
    }
}