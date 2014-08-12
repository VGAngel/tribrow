package mythruna.client;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import mythruna.Coordinates;
import mythruna.Vector3i;
import mythruna.db.ColumnInfo;
import mythruna.db.WorldDatabase;
import org.progeeks.util.ObservableMap;
import org.progeeks.util.beans.StandardBean;

public class PlayerState extends AbstractAppState {
    public static final String TYPE_AT_HEAD = "typeAtHead";
    public static final String TYPE_AT_FEET = "typeAtFeet";
    public static final String TYPE_UNDER_FEET = "typeUnderFeet";
    public static final String HEAD_IN_WATER = "headInWater";
    public static final String MOVING = "moving";
    public static final String RUNNING = "running";
    public static final String WALK_TYPE = "walkType";
    public static final String SUNLIGHT = "sunlight";
    private static float MOVE_EPSILON = 0.01F;

    private static final Vector3i defaultCoords = new Vector3i(0, 0, 0);
    private GameClient gameClient;
    private WorldDatabase worldDb;
    private ObservableMap<String, Object> values = new ObservableMap<>();

    private float headHeight = 1.7F;

    public PlayerState(GameClient gameClient) {
        this.gameClient = gameClient;
    }

    protected <T> T get(String key, T defaultValue) {
        Object o = this.values.get(key);
        return o == null ? defaultValue : (T) o;
    }

    public void setSunlight(int val) {
        this.values.put("sunlight", Integer.valueOf(val));
    }

    public int getSunlight() {
        return ((Integer) get("sunlight", Integer.valueOf(15))).intValue();
    }

    public void setTypeAtHead(int type) {
        this.values.put("typeAtHead", Integer.valueOf(type));
    }

    public int getTypeAtHead() {
        return ((Integer) get("typeAtHead", Integer.valueOf(0))).intValue();
    }

    public void setTypeAtFeet(int type) {
        this.values.put("typeAtFeet", Integer.valueOf(type));
    }

    public int getTypeAtFeet() {
        return ((Integer) get("typeAtFeet", Integer.valueOf(0))).intValue();
    }

    public void setTypeUnderFeet(int type) {
        this.values.put("typeUnderFeet", Integer.valueOf(type));
    }

    public int getTypeUnderFeet() {
        return ((Integer) get("typeUnderFeet", Integer.valueOf(0))).intValue();
    }

    public void setHeadInWater(boolean f) {
        this.values.put("headInWater", Boolean.valueOf(f));
    }

    public boolean isHeadInWater() {
        return ((Boolean) get("headInWater", Boolean.valueOf(false))).booleanValue();
    }

    public void setMoving(boolean f) {
        this.values.put("moving", Boolean.valueOf(f));
    }

    public boolean isMoving() {
        return ((Boolean) get("moving", Boolean.valueOf(false))).booleanValue();
    }

    public void setRunning(boolean f) {
        this.values.put("running", Boolean.valueOf(f));
    }

    public boolean isRunning() {
        return ((Boolean) get("running", Boolean.valueOf(false))).booleanValue();
    }

    public void setWalkType(int type) {
        this.values.put("walkType", Integer.valueOf(type));
    }

    public int getWalkType() {
        return ((Integer) get("walkType", Integer.valueOf(0))).intValue();
    }

    public StandardBean getBean() {
        return this.values;
    }

    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.worldDb = this.gameClient.getWorld().getWorldDatabase();
        setHeadInWater(false);
        setTypeAtFeet(0);
        setTypeAtHead(0);
    }

    protected int getBlockType(float x, float y, float z) {
        return this.worldDb.getCellType(Coordinates.worldToCell(x), Coordinates.worldToCell(y), Coordinates.worldToCell(z));
    }

    protected int getSunlight(float x, float y, float z) {
        return this.worldDb.getLight(0, Coordinates.worldToCell(x), Coordinates.worldToCell(y), Coordinates.worldToCell(z));
    }

    public void update(float tpf) {
        Vector3f loc = this.gameClient.getLocation();

        setSunlight(getSunlight(loc.x, loc.y, loc.z));

        Vector3f vel = this.gameClient.getVelocity();
        boolean move = (Math.abs(vel.x) >= MOVE_EPSILON) || (Math.abs(vel.y) >= MOVE_EPSILON);
        if ((!move) && (Math.abs(vel.z) < MOVE_EPSILON)) {
            setMoving(false);
            setWalkType(0);
            return;
        }

        ColumnInfo colInfo = this.worldDb.getColumnInfo(Coordinates.worldToCell(loc.x), Coordinates.worldToCell(loc.y), true);

        if (colInfo != null) {
            int water = colInfo.getCount((byte) 1);
            int ocean = colInfo.getCount((byte) 2);
            int ground = colInfo.getCount((byte) 3);
            int trees = colInfo.getCount((byte) 4);
            int hills = colInfo.getCount((byte) 5);
            int mountains = colInfo.getCount((byte) 6);

            String type = "Grasslands";
            if ((mountains > 200) || (loc.z > 100.0F)) {
                type = "mountains";
            } else if ((trees > 50) && (ocean < 100)) {
                type = "forest";
            } else if ((water > 50) && (ocean < 100) && (ground > 100)) {
                type = "lakeside";
            } else if ((ocean > 100) && (ocean < 800) && (ground > 100)) {
                type = "seaside";
            } else if (ocean >= 800) {
                type = "open water";
            }

        }

        int typeAtFeet = getBlockType(loc.x, loc.y, loc.z - this.headHeight + 0.3F);
        int typeUnderFeet = getBlockType(loc.x, loc.y, loc.z - this.headHeight - 0.3F);

        int typeAtHead = getBlockType(loc.x, loc.y, loc.z);
        boolean inWater = false;
        boolean headInWater = false;
        if ((typeAtFeet == 7) || (typeAtFeet == 8)) {
            if (typeAtHead == 7) {
                headInWater = true;
                inWater = true;
            } else if (typeAtHead == 8) {
                inWater = true;

                float zHead = loc.z;
                float zHeadDelta = zHead - (float) Math.floor(zHead);

                headInWater = zHeadDelta < 0.89F;
            } else {
                headInWater = false;
            }

        } else {
            headInWater = (typeAtHead == 7) || (typeAtHead == 8);
        }

        setMoving(true);
        setHeadInWater(headInWater);
        setTypeAtFeet(typeAtFeet);
        setTypeUnderFeet(typeUnderFeet);
        setTypeAtHead(typeAtHead);

        if (!move) {
            setWalkType(0);
        } else if ((typeAtFeet == 8) && (typeUnderFeet != 8) && (typeUnderFeet != 0))
            setWalkType(8);
        else if (typeUnderFeet == 7)
            setWalkType(0);
        else
            setWalkType(typeUnderFeet);
    }
}