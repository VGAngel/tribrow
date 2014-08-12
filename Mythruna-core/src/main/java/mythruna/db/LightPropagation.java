package mythruna.db;

import mythruna.BlockType;
import mythruna.Coordinates;

import java.util.*;

public class LightPropagation {

    private WorldDatabase world;
    private Map<String, LeafData> cache = new HashMap();

    private Map<LeafData, LeafLight> leafLights = new HashMap();
    private LinkedHashSet<LeafLight> pendingLeafs = new LinkedHashSet();
    private Map<LeafData, LeafShadow> leafShadows = new HashMap();
    private LinkedHashSet<LeafShadow> pendingShadows = new LinkedHashSet();
    private List<DarkCell> dark;
    private Set<LeafData> changed = new HashSet();

    public LightPropagation(WorldDatabase world) {
        this.world = world;
    }

    protected void propagateLights() {
        while (this.pendingLeafs.size() > 0) {
            Iterator it = this.pendingLeafs.iterator();
            LeafLight ll = (LeafLight) it.next();
            it.remove();

            ll.doPropagation();
            ll.commit();
        }
    }

    protected List<RelightCell> propagateShadows() {
        LinkedList relight = new LinkedList();

        while (this.pendingShadows.size() > 0) {
            Iterator it = this.pendingShadows.iterator();
            LeafShadow ll = (LeafShadow) it.next();
            it.remove();

            ll.doPropagation();
            ll.commit();

            relight.addAll(ll.getRelight());
        }
        return relight;
    }

    public void addRelights(List<RelightCell> relight, LightType light) {
        for (RelightCell c : relight) {
            LeafLight ll = getLeafLight(c.x, c.y, c.z, light);
            ll.externalEnterLight(c.x, c.y, c.z, -1, c.value);
        }
    }

    public Set<LeafData> changeLighting(LeafData leaf, int x, int y, int z, int old, int type) {
        if (old == type) {
            return Collections.emptySet();
        }
        long start = System.nanoTime();

        int sunlight = leaf.getWorldSunlight(x, y, z);
        int localLight = leaf.getWorldLocalLight(x, y, z);

        int i = x - leaf.getX();
        int j = y - leaf.getY();
        int k = z - leaf.getZ();

        BlockType oldType = mythruna.BlockTypeIndex.types[old];

        if (LightType.TORCHLIGHT.isBlockType(old)) {
            this.dark = new ArrayList();

            LeafShadow shade = getLeafShadow(leaf, LightType.TORCHLIGHT);

            leaf.setWorldType(x, y, z, 0);
            shade.enterLightRaw(i, j, k, -1, LightType.TORCHLIGHT.getMaximumValue(old));
            List relight = propagateShadows();

            LeafLight leafLight = getLeafLight(leaf, LightType.TORCHLIGHT, false);
            for (DarkCell cell : this.dark) {
                leafLight.reflood(cell.x, cell.y, cell.z);
            }
            addRelights(relight, LightType.TORCHLIGHT);
            this.dark = null;

            propagateLights();

            this.leafLights.clear();
            this.leafShadows.clear();
        } else if (old != 0) {
            leaf.setWorldType(x, y, z, 0);

            LeafLight leafLight = getLeafLight(leaf, LightType.SUNLIGHT, false);
            leafLight.reflood(x, y, z);
            propagateLights();

            this.leafLights.clear();

            leafLight = getLeafLight(leaf, LightType.TORCHLIGHT, false);
            leafLight.reflood(x, y, z);

            propagateLights();

            this.leafLights.clear();
            this.leafShadows.clear();
        }

        BlockType newType = mythruna.BlockTypeIndex.types[type];

        if (LightType.TORCHLIGHT.isBlockType(type)) {
            LeafLight leafLight = getLeafLight(leaf, LightType.TORCHLIGHT, false);
            leafLight.enterLightRaw(i, j, k, -1, LightType.TORCHLIGHT.getMaximumValue(type));

            propagateLights();
        } else if (type != 0) {
            this.dark = new ArrayList();

            LeafShadow shadeSun = getLeafShadow(leaf, LightType.SUNLIGHT);
            shadeSun.shade(x, y, z);
            List relight = propagateShadows();

            LeafLight leafLight = getLeafLight(leaf, LightType.SUNLIGHT, false);
            for (DarkCell cell : this.dark) {
                leafLight.reflood(cell.x, cell.y, cell.z);
            }
            addRelights(relight, LightType.TORCHLIGHT);

            leaf.setWorldType(x, y, z, type);

            propagateLights();

            this.leafLights.clear();
            this.leafShadows.clear();

            leaf.setWorldType(x, y, z, old);

            this.dark = new ArrayList();

            LeafShadow shadeTorches = getLeafShadow(leaf, LightType.TORCHLIGHT);
            shadeTorches.shade(x, y, z);
            relight = propagateShadows();

            shadeTorches.setDark(x, y, z);

            leafLight = getLeafLight(leaf, LightType.TORCHLIGHT, false);
            for (DarkCell cell : this.dark) {
                leafLight.reflood(cell.x, cell.y, cell.z);
            }
            addRelights(relight, LightType.TORCHLIGHT);

            leaf.setWorldType(x, y, z, type);

            propagateLights();

            this.dark = null;
        }

        long end = System.nanoTime();

        this.leafLights.clear();
        this.leafShadows.clear();

        return this.changed;
    }

    public Set<LeafData> refreshLights(LeafData leaf) {
        long start = System.nanoTime();

        if ((leaf == null) || (!leaf.hasCells())) {
            return null;
        }
        getLeafLight(leaf, LightType.SUNLIGHT, true);
        propagateLights();

        long end = System.nanoTime();

        this.leafLights.clear();
        LeafLight leafLight = getLeafLight(leaf, LightType.TORCHLIGHT, true);
        initializeLocalLights(leafLight);

        start = System.nanoTime();

        propagateLights();

        end = System.nanoTime();

        leaf.getInfo().lit = true;

        return this.changed;
    }

    public Set<LeafData> refreshLights(LeafData[] leafs) {
        long start = System.nanoTime();

        for (int i = leafs.length - 1; i >= 0; i--) {
            LeafLight ll = getLeafLight(leafs[i], LightType.SUNLIGHT, true);

            propagateLights();

            LeafInfo info = leafs[i].getInfo();
            this.cache.put(toKey(info.x, info.y, info.z), leafs[i]);

            leafs[i].getInfo().lit = true;
        }

        long end = System.nanoTime();

        start = System.nanoTime();

        this.leafLights.clear();

        for (int i = leafs.length - 1; i >= 0; i--) {
            LeafLight ll = getLeafLight(leafs[i], LightType.TORCHLIGHT, true);
            initializeLocalLights(ll);

            propagateLights();
        }

        end = System.nanoTime();

        return this.changed;
    }

    protected String toKey(int x, int y, int z) {
        int i = Coordinates.worldToLeaf(x);
        int j = Coordinates.worldToLeaf(y);
        int k = Coordinates.worldToLeaf(z);
        return i + "x" + j + "x" + k;
    }

    protected LeafData getCached(int x, int y, int z) {
        String key = toKey(x, y, z);
        return (LeafData) this.cache.get(key);
    }

    protected LeafLight getLeafLight(int x, int y, int z, LightType light) {
        LeafData leaf = getCached(x, y, z);
        if (leaf == null) {
            leaf = this.world.getLeaf(x, y, z, false);
        }

        if ((leaf == null) || (!leaf.hasCells())) {
            return null;
        }
        if (!leaf.contains(x, y, z)) {
            System.out.println("!!!!!! What's going on here:" + leaf + "  but we asked for one that could hold:" + x + ", " + y + ", " + z);
        }
        return getLeafLight(leaf, light, false);
    }

    protected void initializeColumnLights(LeafData[] leafs, LightType light) {
    }

    protected LeafLight getLeafLight(LeafData leaf, LightType light, boolean clearLights) {
        if (!leaf.hasCells()) {
            return null;
        }

        LeafLight result = (LeafLight) this.leafLights.get(leaf);
        if (result == null) {
            result = new LeafLight(leaf);
            result.setLightType(light);
            this.leafLights.put(leaf, result);
            if (clearLights) {
                result.clearLights();

                initializeTop(result, this.world, clearLights);
                initializeSide(result, this.world, 0, clearLights);
                initializeSide(result, this.world, 1, clearLights);
                initializeSide(result, this.world, 2, clearLights);
                initializeSide(result, this.world, 3, clearLights);
                initializeBottom(result, this.world, clearLights);
            }

        }

        this.pendingLeafs.add(result);

        return result;
    }

    protected LeafShadow getLeafShadow(int x, int y, int z, LightType light) {
        LeafData leaf = this.world.getLeaf(x, y, z, false);
        if ((leaf == null) || (!leaf.hasCells())) {
            return null;
        }
        if (!leaf.contains(x, y, z)) {
            System.out.println("!!!!!! What's going on here:" + leaf + "  but we asked for one that could hold:" + x + ", " + y + ", " + z);
        }
        return getLeafShadow(leaf, light);
    }

    protected LeafShadow getLeafShadow(LeafData leaf, LightType light) {
        LeafShadow result = (LeafShadow) this.leafShadows.get(leaf);
        if (result == null) {
            result = new LeafShadow(leaf);
            result.setLightType(light);
            this.leafShadows.put(leaf, result);
        }

        this.pendingShadows.add(result);

        return result;
    }

    protected void initializeLocalLights(LeafLight ll) {
        LightType light = ll.light;

        if (light == LightType.SUNLIGHT) {
            return;
        }

        LeafData leaf = ll.getLeaf();
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                for (int k = 0; k < 32; k++) {
                    int v = leaf.getTypeUnchecked(i, j, k);
                    if (light.isBlockType(v)) {
                        ll.enterLightRaw(i, j, k, -1, light.getMaximumValue(v));
                    }
                }
            }
        }
    }

    protected void initializeTop(LeafLight ll, WorldDatabase world, boolean clearLights) {
        LeafData above = findNeighbor(ll.getLeaf().getInfo(), 4, true);

        initializeTop(ll, above, clearLights);
    }

    protected void initializeTop(LeafLight ll, LeafData above, boolean clearLights) {
        if ((above == null) || (above.isEmpty())) {
            if ((ll.light == LightType.SUNLIGHT) && (clearLights)) {
                int k = 31;
                for (int i = 0; i < 32; i++) {
                    for (int j = 0; j < 32; j++) {
                        ll.enterLightRaw(i, j, k, 5, ll.light.getMaximumValue(0));
                    }
                }
            }
        } else if (above.isLit()) {
            int k = 31;
            for (int i = 0; i < 32; i++) {
                for (int j = 0; j < 32; j++) {
                    int value = above.getLightUnchecked(ll.lightType, i, j, 0);

                    if (value > 0) {
                        int aboveType = above.getTypeUnchecked(i, j, 0);
                        if (ll.canExit(5, aboveType)) {
                            value = ll.light.getNextLightValue(value, 5, aboveType);

                            ll.enterLightRaw(i, j, k, 5, value);
                        }
                    }
                }
            }
        } else {
            throw new RuntimeException("Not sure how this can happen.  LeafData:" + above);
        }
    }

    protected void initializeSide(LeafLight ll, WorldDatabase world, int dir, boolean clearLights) {
        LeafData neighbor = findNeighbor(ll.getLeaf().getInfo(), dir, false);

        if ((neighbor == null) || (!neighbor.hasCells()) || (!neighbor.isLit())) {
            return;
        }

        if ((dir == 0) || (dir == 1)) {
            int j = 31;
            int jNeighbor = 0;
            if (dir == 0) {
                j = 0;
                jNeighbor = 31;
            }

            for (int i = 0; i < 32; i++) {
                for (int k = 0; k < 32; k++) {
                    int value = neighbor.getLightUnchecked(ll.lightType, i, jNeighbor, k);
                    if (value > 0) {
                        int type = neighbor.getTypeUnchecked(i, jNeighbor, k);
                        if (ll.canExit(mythruna.Direction.INVERSE[dir], type)) {
                            value = ll.light.getNextLightValue(value, dir, type);

                            ll.enterLightRaw(i, j, k, mythruna.Direction.INVERSE[dir], value);
                        }
                    }
                }
            }
        } else if ((dir == 2) || (dir == 3)) {
            int i = 31;
            int iNeighbor = 0;
            if (dir == 3) {
                i = 0;
                iNeighbor = 31;
            }

            for (int j = 0; j < 32; j++) {
                for (int k = 0; k < 32; k++) {
                    int value = neighbor.getLightUnchecked(ll.lightType, iNeighbor, j, k);
                    if (value > 0) {
                        int type = neighbor.getTypeUnchecked(iNeighbor, j, k);
                        if (ll.canExit(mythruna.Direction.INVERSE[dir], type)) {
                            value = ll.light.getNextLightValue(value, dir, type);

                            ll.enterLightRaw(i, j, k, mythruna.Direction.INVERSE[dir], value);
                        }
                    }
                }
            }
        }
    }

    protected void initializeBottom(LeafLight ll, WorldDatabase world, boolean clearLights) {
        LeafData neighbor = findNeighbor(ll.getLeaf().getInfo(), 5, false);

        if ((neighbor == null) || (!neighbor.isLit())) {
            return;
        }

        int k = 31;
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                int value = neighbor.getLightUnchecked(ll.lightType, i, j, k);

                if ((ll.light == LightType.SUNLIGHT) && (value == ll.light.getMaximumValue(0))) {
                    value--;
                }

                if (value > 0) {
                    int type = neighbor.getTypeUnchecked(i, j, k);
                    if (ll.canExit(4, type)) {
                        value = ll.light.getNextLightValue(value, 4, type);

                        ll.enterLightRaw(i, j, 0, 4, value);
                    }
                }
            }
        }
    }

    private LeafData findNeighbor(LeafInfo info, int direction, boolean load) {
        int x = info.x;
        int y = info.y;
        int z = info.z;

        x += mythruna.Direction.DIRS[direction][0] * 32;
        y += mythruna.Direction.DIRS[direction][1] * 32;
        z += mythruna.Direction.DIRS[direction][2] * 32;

        LeafData result = getCached(x, y, z);
        if (result != null) {
            return result;
        }

        return this.world.getLeaf(x, y, z, load);
    }

    protected static class DarkCell {
        int x;
        int y;
        int z;

        public DarkCell(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    protected static class RelightCell {
        int x;
        int y;
        int z;
        int value;

        public RelightCell(int x, int y, int z, int value) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.value = value;
        }
    }

    protected static class ChangedCell {
        int i;
        int j;
        int k;
        int dir;
        int value;

        public ChangedCell(int i, int j, int k, int dir, int value) {
            this.i = i;
            this.j = j;
            this.k = k;
            this.dir = dir;
            this.value = value;
        }
    }

    protected class LeafShadow {
        private LeafData leaf;
        private LinkedList<LightPropagation.ChangedCell> pending = new LinkedList();
        private LinkedList<LightPropagation.RelightCell> relight = new LinkedList();
        private LightType light = LightType.SUNLIGHT;
        private int lightType = this.light.getLightIndex();
        private int changeCount = 0;

        public LeafShadow(LeafData leaf) {
            this.leaf = leaf;
        }

        public LinkedList<LightPropagation.RelightCell> getRelight() {
            return this.relight;
        }

        public LeafData getLeaf() {
            return this.leaf;
        }

        public void setLightType(LightType type) {
            this.light = type;
            this.lightType = this.light.getLightIndex();
        }

        public final boolean canEnter(int direction, int type) {
            if ((type == 0) || (direction == -1)) {
                return true;
            }
            BlockType blockType = mythruna.BlockTypeIndex.types[type];
            if (blockType.isTransparent()) {
                return true;
            }
            return !blockType.isSolid(mythruna.Direction.INVERSE[direction]);
        }

        public final boolean canExit(int direction, int type) {
            if ((type == 0) || (direction == -1)) {
                return true;
            }
            BlockType blockType = mythruna.BlockTypeIndex.types[type];
            if (blockType.isTransparent()) {
                return true;
            }
            return !blockType.isSolid(direction);
        }

        public void setDark(int x, int y, int z) {
            int i = x - this.leaf.getX();
            int j = y - this.leaf.getY();
            int k = z - this.leaf.getZ();

            this.leaf.setLightUnchecked(this.lightType, i, j, k, 0);
            this.changeCount += 1;
            LightPropagation.this.dark.add(new LightPropagation.DarkCell(this.leaf.getX() + i, this.leaf.getY() + j, this.leaf.getZ() + k));
        }

        public void shade(int x, int y, int z) {
            int i = x - this.leaf.getX();
            int j = y - this.leaf.getY();
            int k = z - this.leaf.getZ();

            for (int d = 0; d < 6; d++) {
                int a = mythruna.Direction.DIRS[d][0];
                int b = mythruna.Direction.DIRS[d][1];
                int c = mythruna.Direction.DIRS[d][2];
                int l = LightPropagation.this.world.getLight(this.lightType, x + a, y + b, z + c);

                int v = LightPropagation.this.world.getCellType(x + a, y + b, z + c);

                int nextVal = this.light.getNextLightValue(l, mythruna.Direction.INVERSE[d], v);

                enterLight(i + a, j + b, k + c, mythruna.Direction.INVERSE[d], nextVal);
            }
        }

        public boolean enterLightRaw(int i, int j, int k, int direction, int value) {
            if (value != this.leaf.getLightUnchecked(this.lightType, i, j, k)) {
                return false;
            }
            int v = this.leaf.getTypeUnchecked(i, j, k);

            if (canEnter(direction, v)) {
                if (this.light.isBlockType(v)) {
                    this.relight.add(new LightPropagation.RelightCell(this.leaf.getX() + i, this.leaf.getY() + j, this.leaf.getZ() + k, this.light.getMaximumValue(v)));
                }

                this.leaf.setLightUnchecked(this.lightType, i, j, k, 0);
                this.changeCount += 1;

                LightPropagation.this.dark.add(new LightPropagation.DarkCell(this.leaf.getX() + i, this.leaf.getY() + j, this.leaf.getZ() + k));

                this.pending.add(new LightPropagation.ChangedCell(i, j, k, direction, value));
                return true;
            }

            return false;
        }

        public void enterLight(int i, int j, int k, int direction, int value) {
            i += mythruna.Direction.DIRS[direction][0];
            j += mythruna.Direction.DIRS[direction][1];
            k += mythruna.Direction.DIRS[direction][2];

            if ((i < 0) || (j < 0) || (k < 0) || (i >= 32) || (j >= 32) || (k >= 32)) {
                int x = i + this.leaf.getX();
                int y = j + this.leaf.getY();
                int z = k + this.leaf.getZ();

                LeafShadow neighbor = LightPropagation.this.getLeafShadow(x, y, z, this.light);
                if (neighbor == null) {
                    return;
                }
                neighbor.externalEnterLight(x, y, z, direction, value);
                return;
            }

            enterLightRaw(i, j, k, direction, value);
        }

        public void externalEnterLight(int x, int y, int z, int direction, int value) {
            int i = x - this.leaf.getX();
            int j = y - this.leaf.getY();
            int k = z - this.leaf.getZ();

            if ((i < 0) || (j < 0) || (k < 0) || (i >= 32) || (j >= 32) || (k >= 32)) {
                throw new RuntimeException("External light entered at bad location:" + i + ", " + j + ", " + k + "  in leaf:" + this.leaf);
            }
            enterLightRaw(i, j, k, direction, value);
        }

        public void doPropagation() {
            while (this.pending.size() > 0) {
                LightPropagation.ChangedCell cc = (LightPropagation.ChangedCell) this.pending.removeFirst();

                int existing = this.leaf.getLightUnchecked(this.lightType, cc.i, cc.j, cc.k);
                if (existing <= cc.value) {
                    int v = this.leaf.getTypeUnchecked(cc.i, cc.j, cc.k);

                    for (int d = 0; d < 6; d++) {
                        if (mythruna.Direction.INVERSE[d] != cc.dir) {
                            int nextVal = this.light.getNextLightValue(cc.value, d, v);
                            if (nextVal > 0) {
                                if (canExit(d, v)) {
                                    enterLight(cc.i, cc.j, cc.k, d, nextVal);
                                }
                            }
                        }
                    }
                }
            }
        }

        public void commit() {
            if (this.changeCount > 0) {
                this.leaf.markChanged();

                LightPropagation.this.changed.add(this.leaf);
            }
        }

        public String toString() {
            return "LeafLight[" + this.leaf + "]";
        }
    }

    protected class LeafLight {
        private LeafData leaf;
        private LinkedList<LightPropagation.ChangedCell> pending = new LinkedList();
        private LightType light = LightType.SUNLIGHT;
        private int lightType = this.light.getLightIndex();
        private int changeCount = 0;

        public LeafLight(LeafData leaf) {
            this.leaf = leaf;
        }

        public LeafData getLeaf() {
            return this.leaf;
        }

        public void setLightType(LightType type) {
            this.light = type;
            this.lightType = this.light.getLightIndex();
        }

        public void clearLights() {
            for (int i = 0; i < 32; i++) {
                for (int j = 0; j < 32; j++) {
                    for (int k = 0; k < 32; k++) {
                        this.leaf.setLightUnchecked(this.lightType, i, j, k, 0);
                    }
                }
            }
        }

        public final boolean canEnter(int direction, int type) {
            if ((type == 0) || (direction == -1)) {
                return true;
            }
            BlockType blockType = mythruna.BlockTypeIndex.types[type];
            if (blockType.isTransparent()) {
                return true;
            }
            return !blockType.isSolid(mythruna.Direction.INVERSE[direction]);
        }

        public final boolean canExit(int direction, int type) {
            if ((type == 0) || (direction == -1)) {
                return true;
            }
            BlockType blockType = mythruna.BlockTypeIndex.types[type];
            if (blockType.isTransparent()) {
                return true;
            }
            return !blockType.isSolid(direction);
        }

        public void reflood(int x, int y, int z) {
            for (int d = 0; d < 6; d++) {
                reinitialize(x + mythruna.Direction.DIRS[d][0], y + mythruna.Direction.DIRS[d][1], z + mythruna.Direction.DIRS[d][2]);
            }
        }

        public void reinitialize(int x, int y, int z) {
            int i = x - this.leaf.getX();
            int j = y - this.leaf.getY();
            int k = z - this.leaf.getZ();

            if ((i < 0) || (j < 0) || (k < 0) || (i >= 32) || (j >= 32) || (k >= 32)) {
                LeafLight neighbor = LightPropagation.this.getLeafLight(x, y, z, this.light);
                if (neighbor == null) {
                    return;
                }
                neighbor.reinitialize(x, y, z);
                return;
            }

            int existing = this.leaf.getLightUnchecked(this.lightType, i, j, k);

            if ((existing == 15) && (this.light == LightType.SUNLIGHT)) {
                this.pending.add(new LightPropagation.ChangedCell(i, j, k, 5, existing));
            } else {
                this.pending.add(new LightPropagation.ChangedCell(i, j, k, -1, existing));
            }
        }

        public boolean enterLightRaw(int i, int j, int k, int direction, int value) {
            if (value <= this.leaf.getLightUnchecked(this.lightType, i, j, k)) {
                return false;
            }
            int v = this.leaf.getTypeUnchecked(i, j, k);

            if (canEnter(direction, v)) {
                this.leaf.setLightUnchecked(this.lightType, i, j, k, value);
                this.changeCount += 1;

                this.pending.add(new LightPropagation.ChangedCell(i, j, k, direction, value));
                return true;
            }

            return false;
        }

        public void enterLight(int i, int j, int k, int direction, int value) {
            i += mythruna.Direction.DIRS[direction][0];
            j += mythruna.Direction.DIRS[direction][1];
            k += mythruna.Direction.DIRS[direction][2];

            if ((i < 0) || (j < 0) || (k < 0) || (i >= 32) || (j >= 32) || (k >= 32)) {
                int x = i + this.leaf.getX();
                int y = j + this.leaf.getY();
                int z = k + this.leaf.getZ();

                LeafLight neighbor = LightPropagation.this.getLeafLight(x, y, z, this.light);
                if (neighbor == null) {
                    return;
                }
                neighbor.externalEnterLight(x, y, z, direction, value);
                return;
            }

            enterLightRaw(i, j, k, direction, value);
        }

        public void externalEnterLight(int x, int y, int z, int direction, int value) {
            int i = x - this.leaf.getX();
            int j = y - this.leaf.getY();
            int k = z - this.leaf.getZ();

            if ((i < 0) || (j < 0) || (k < 0) || (i >= 32) || (j >= 32) || (k >= 32)) {
                throw new RuntimeException("External light entered at bad location:" + i + ", " + j + ", " + k + "  in leaf:" + this.leaf);
            }
            enterLightRaw(i, j, k, direction, value);
        }

        public void doPropagation() {
            while (this.pending.size() > 0) {
                LightPropagation.ChangedCell cc = (LightPropagation.ChangedCell) this.pending.removeFirst();

                int existing = this.leaf.getLightUnchecked(this.lightType, cc.i, cc.j, cc.k);
                if (existing <= cc.value) {
                    int v = this.leaf.getTypeUnchecked(cc.i, cc.j, cc.k);

                    for (int d = 0; d < 6; d++) {
                        if (mythruna.Direction.INVERSE[d] != cc.dir) {
                            int nextVal = this.light.getNextLightValue(cc.value, d, v);
                            if (nextVal > 0) {
                                if (canExit(d, v)) {
                                    enterLight(cc.i, cc.j, cc.k, d, nextVal);
                                }
                            }
                        }
                    }
                }
            }
        }

        public void commit() {
            if (this.changeCount > 0) {
                this.leaf.markChanged();

                LightPropagation.this.changed.add(this.leaf);
            }
        }

        public String toString() {
            return "LeafLight[" + this.leaf + "]";
        }
    }
}