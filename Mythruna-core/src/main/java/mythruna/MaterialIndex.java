package mythruna;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector4f;
import com.jme3.texture.Texture;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class MaterialIndex {

    public static final int MAT_DIRT = 0;
    public static final int MAT_GRASS_SIDE = 1;
    public static final int MAT_GRASS = 2;
    public static final int MAT_SAND = 3;
    public static final int MAT_STONE = 4;
    public static final int MAT_COBBLE = 5;
    public static final int MAT_ROCK = 6;
    public static final int MAT_WATER = 7;
    public static final int MAT_BARK = 8;
    public static final int MAT_LEAVES = 9;
    public static final int MAT_WADDLE = 10;
    public static final int MAT_PLANKS_LR = 11;
    public static final int MAT_FIRE = 12;
    public static final int MAT_RAW_WOOD = 13;
    public static final int MAT_ROCK_WALL_SIDE = 14;
    public static final int MAT_STONE_TILE = 15;
    public static final int MAT_ROCK_TOP = 16;
    public static final int MAT_DIRT_TOP = 17;
    public static final int MAT_GLASS = 18;
    public static final int MAT_MINERALS = 19;
    public static final int MAT_BLACK_MARBLE = 20;
    public static final int MAT_PLANKS_UD = 21;
    public static final int MAT_GRASS_BLADES = 22;
    public static final int MAT_WATER2 = 24;
    public static final int MAT_LEAVES2 = 25;
    public static final int MAT_LEAVES3 = 26;
    public static final int MAT_PINE = 32;
    public static final int MAT_FLORA = 33;
    public static final int MAT_GRASS_BLADES_TEST = 500;
    public static Material DEBUG_MATERIAL;
    public static Material TRANSPARENT_MATERIAL;
    public static Material FIELD_MATERIAL;
    public static Material ROPE_MATERIAL;
    private static final String baseLightingDef = "MatDefs/LightingWithFog.j3md";
    private static final String baseWaterDef = "MatDefs/WaterLightingWithFog.j3md";
    private static final String baseGrassDef = "MatDefs/Grass.j3md";
    private static final String baseFlatDef = "Common/MatDefs/Misc/Unshaded.j3md";
    private static final String noiseTex = "Textures/noise.png";
    private static final String softNoiseTex = "Textures/soft-noise-256.jpg";
    private static final String baseLeafDef = "MatDefs/Leaf.j3md";
    protected static ColorRGBA ambient = ColorRGBA.DarkGray.clone();
    protected static ColorRGBA diffuse = ColorRGBA.White.clone();
    protected static ColorRGBA specular = new ColorRGBA(0.0F, 0.0F, 0.0F, 1.0F);
    protected static ColorRGBA fogColor = new ColorRGBA(0.9F, 0.9F, 0.9F, 1.0F);

    protected static Vector4f timeParms = new Vector4f(1.0F, 0.0F, 0.0F, 0.0F);

    private static MaterialIndex instance = new MaterialIndex();
    private AssetManager assets;
    private List<Material> materials = new ArrayList();
    private static Material waterMaterial1;
    private static Material waterMaterial2;
    private static boolean flat = false;
    private static String size = "";

    private static boolean vertexLighting = false;
    private static boolean lowQuality = false;
    private static boolean gritty = false;

    public static void initialize(AssetManager assets) {
        if (instance.assets != null)
            throw new RuntimeException("Material index already initialized.");
        instance.createBaseMaterials(assets);

        DEBUG_MATERIAL = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        DEBUG_MATERIAL.setColor("Color", ColorRGBA.Red);

        TRANSPARENT_MATERIAL = new Material(assets, "Common/MatDefs/Misc/Unshaded.j3md");
        TRANSPARENT_MATERIAL.setColor("Color", new ColorRGBA(1.0F, 0.0F, 0.0F, 0.0F));
        TRANSPARENT_MATERIAL.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        TextureKey key = new TextureKey("Textures/soft-noise-256.jpg");
        key.setGenerateMips(false);
        Texture t = assets.loadTexture(key);
        t.setWrap(Texture.WrapMode.Repeat);

        FIELD_MATERIAL = new Material(assets, "MatDefs/ForceField.j3md");

        FIELD_MATERIAL.setColor("Color", ColorRGBA.Red);
        FIELD_MATERIAL.setTexture("ColorMap", t);

        FIELD_MATERIAL.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        ROPE_MATERIAL = instance.createRopeMaterial(assets);
    }

    public static void setResolution(int width, int height) {
        float quadratic = height / 720.0F * 6.0F;

        for (Material m : getInstance().materials) {
            if (m != null) {
                if (m.getParam("Quadratic") != null) {
                    m.setFloat("Quadratic", quadratic);
                }
            }
        }
    }

    public static void setVertexLighting(boolean flag) {
        if (vertexLighting == flag)
            return;
        vertexLighting = flag;
        for (Material m : getInstance().materials) {
            if (m != null) {
                if (m.getParam("VertexLighting") != null) {
                    m.setBoolean("VertexLighting", vertexLighting);
                }
            }
        }
    }

    public static boolean getVertexLighting() {
        return vertexLighting;
    }

    public static void setLowQuality(boolean flag) {
        if (lowQuality == flag)
            return;
        lowQuality = flag;
        for (Material m : getInstance().materials) {
            if (m != null) {
                if (m.getParam("LowQuality") != null) {
                    m.setBoolean("LowQuality", lowQuality);
                }
            }
        }
    }

    public static boolean getLowQuality() {
        return lowQuality;
    }

    public static void setGritty(boolean flag) {
        if (gritty == flag)
            return;
        gritty = flag;

        if (instance == null) {
            return;
        }
        Texture gritTexture = getInstance().loadTexture("Textures/soft-noise-256.jpg", TextureRepeat.XY, true);

        dump();
        for (Material m : getInstance().materials) {
            if (m != null) {
                if (m.getParam("DiffuseMap") != null) {
                    MaterialDef def = m.getMaterialDef();
                    String name = def.getAssetName();
                    if ("MatDefs/LightingWithFog.j3md".equals(name)) {
                        if (gritty)
                            m.setTexture("NoiseMap", gritTexture);
                        else
                            m.clearParam("NoiseMap");
                    }
                }
            }
        }
    }

    public static boolean getGritty() {
        return gritty;
    }

    public static void dump() {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out));
        dump(out);
        out.flush();
    }

    public static void dump(PrintWriter out) {
        for (int i = 0; i < getInstance().materials.size(); i++) {
            Material m = (Material) getInstance().materials.get(i);
            if (m != null) {
                dump(m, i, out);
            }
        }
    }

    public static void dump(Material m, int index, PrintWriter out) {
        MaterialDef def = m.getMaterialDef();
        String name = m.getAssetName();
        if (name == null) {
            name = String.valueOf(index);
        }
        out.println("Material " + name + " : " + def.getAssetName() + " {");

        for (MatParam p : m.getParams()) {
            out.println("    " + p.getName() + " : " + p.getValueAsString());
        }

        out.println("}");
    }

    public static MaterialIndex getInstance() {
        return instance;
    }

    public static Material getMaterial(int index) {
        return (Material) getInstance().materials.get(index);
    }

    public static void setAmbient(ColorRGBA c) {
        ambient.set(c);
    }

    public static ColorRGBA getAmbient() {
        return ambient;
    }

    public static void setDiffuse(ColorRGBA c) {
        diffuse.set(c);
    }

    public static ColorRGBA getDiffuse() {
        return diffuse;
    }

    public static void setSpecular(ColorRGBA c) {
        specular.set(c);
    }

    public static ColorRGBA getSpecular() {
        return specular;
    }

    public static void setFogColor(ColorRGBA c) {
        fogColor.set(c);
    }

    public static ColorRGBA getFogColor() {
        return fogColor;
    }

    public static void setTimeParms(Vector4f v) {
        timeParms.set(v);
    }

    public static void setWaterWaves(float cos, float sin) {
        if (waterMaterial1 != null) {
            waterMaterial1.setFloat("WaveSin", sin);
            waterMaterial1.setFloat("WaveCos", cos);
        }
        if (waterMaterial2 != null) {
            waterMaterial2.setFloat("WaveSin", sin);
            waterMaterial2.setFloat("WaveCos", cos);
        }
    }

    protected MaterialIndex() {
    }

    protected void setLookupNormals(int index, boolean f) {
        ((Material) this.materials.get(index)).setBoolean("NormalLookup", f);
    }

    protected void createBaseMaterials(AssetManager assets) {
        this.assets = assets;

        addMaterial("Textures/" + size + "brown-dirt2.jpg", "Textures/brown-dirt-norm.jpg", 128.0F);
        setLookupNormals(0, true);
        addMaterial("Textures/" + size + "brown-dirt-side2.jpg", "Textures/brown-dirt-side-norms.jpg", "Textures/" + size + "brown-dirt-side-bump.jpg", 128.0F, false, TextureRepeat.X);
        setLookupNormals(1, true);
        addMaterial("Textures/" + size + "grass.jpg", "Textures/grass-norm.jpg", 16.0F);

        setLookupNormals(2, true);
        addMaterial("Textures/" + size + "sand.jpg", "Textures/grass-norm.jpg", 2.0F);

        setLookupNormals(3, true);
        addMaterial("Textures/" + size + "ROCK6-lighter.jpg", "Textures/ROCK6-norm.jpg", 10.0F);

        setLookupNormals(4, true);
        addMaterial("Textures/" + size + "pebbles.jpg", "Textures/pebbles-norm.jpg", "Textures/pebbles-bumps.jpg", 50.0F);

        setLookupNormals(5, true);
        addMaterial("Textures/" + size + "rock-wall.png", "Textures/rock-wall-normal.jpg", "Textures/rock-wall-bumps.jpg", 50.0F);

        setLookupNormals(6, true);
        addWaterMaterial("Textures/" + size + "water.png", null, 1.0F, true, true, false);

        setLookupNormals(7, true);
        addMaterial("Textures/" + size + "bark128.jpg", "Textures/bark128-normal.png", "Textures/bark128-bump.png", 64.0F);

        setLookupNormals(8, true);
        addMaterial("Textures/" + size + "leaves3.png", null, 16.0F, true);

        setLookupNormals(9, true);
        addMaterial("Textures/" + size + "waddle-daub-plain.png", "Textures/waddle-daub-plain-normal.png", "Textures/waddle-daub-plain-bumps.png", 55.0F, false, TextureRepeat.XY);

        setLookupNormals(10, true);
        addMaterial("Textures/" + size + "wood-planks-lr.png", "Textures/wood-planks-lr-normal.png", "Textures/wood-planks-lr-bump.png", 15.0F, false, TextureRepeat.XY);

        setLookupNormals(11, true);
        addLightMaterial("Textures/" + size + "fire7.jpg");
        addMaterial("Textures/" + size + "bark128-top.png", "Textures/bark128-top-normal.png", "Textures/bark128-top-bump.png", 60.0F);

        setLookupNormals(13, true);
        addMaterial("Textures/" + size + "ROCK-wall-top-side.png", "Textures/ROCK-wall-top-side-normal.png", "Textures/rock-wall-side-bumps.jpg", 50.0F, false, TextureRepeat.X);

        setLookupNormals(14, true);
        addMaterial("Textures/" + size + "rock-wall-top.png", "Textures/rock-wall-top-normal.png", 50.0F);

        setLookupNormals(15, true);
        addMaterial("Textures/" + size + "rock-wall-rock-top.png", "Textures/rock-wall-rock-top-normal.png", 50.0F);

        setLookupNormals(16, true);
        addMaterial("Textures/" + size + "brown-dirt-top.png", "Textures/brown-dirt-norm.jpg", 100.0F);

        setLookupNormals(17, true);
        addMaterial("Textures/" + size + "glass.png", null, 1.0F, true);

        setLookupNormals(18, true);
        addMaterial("Textures/" + size + "minerals.png", "Textures/minerals-normal.png", 10.0F);

        setLookupNormals(19, true);
        addMaterial("Textures/" + size + "black-marble.jpg", null, 1.0F);

        setLookupNormals(20, true);
        addMaterial("Textures/" + size + "wood-planks.png", "Textures/wood-planks-normal.png", "Textures/wood-planks-bump.png", 15.0F, false, TextureRepeat.XY);

        setLookupNormals(21, true);
        addGrassMaterial("Textures/" + size + "blades-atlas.png", "Textures/grass.jpg", "Textures/blades-bases-atlas.png", 100.0F);
        addMaterial("Textures/" + size + "bark90-128.jpg", "Textures/bark90-128-normal.png", "Textures/bark90-128-bump.png", 64.0F);

        setLookupNormals(23, true);
        addWaterMaterial("Textures/" + size + "water.png", null, 1.0F, true, true, true);

        setLookupNormals(24, true);
        addLeafMaterial("Textures/" + size + "leaf-atlas.png", null, null, 16.0F, true, null, true);
        addLeafMaterial("Textures/" + size + "leaf-atlas2.png", null, null, 16.0F, true, null, true);
        addMaterial("Textures/" + size + "white-marble.jpg", null, 1.0F);

        setLookupNormals(27, true);
        addMaterial("Textures/" + size + "wood-planks.png", null, null, 15.0F, false, TextureRepeat.XY);
        addLeafMaterial("Textures/" + size + "hay.png", null, null, 15.0F, true, null, true);
        addLeafMaterial("Textures/" + size + "hay-framed.png", null, null, 15.0F, true, null, true);
        addFlameMaterial("Textures/flame.png");
        addLeafMaterial("Textures/pine-branch.png", null, null, 16.0F, true, null, true);
        addFloraMaterial("Textures/" + size + "wildflowers.png");
        addMaterial("Textures/" + size + "bark-chopped-128.png", "Textures/bark-chopped-128-normal.png", "Textures/bark-chopped-128-bump.png", 60.0F, false, TextureRepeat.X);

        setLookupNormals(34, true);
        addMaterial("Textures/shingles.png", "Textures/shingles-normal.png", "Textures/shingles-bump.png", 15.0F, false, TextureRepeat.XY);

        setLookupNormals(35, true);
        addMaterial("Textures/shingles-lr.png", "Textures/shingles-normal-lr.png", "Textures/shingles-bump-lr.png", 15.0F, false, TextureRepeat.XY);

        setLookupNormals(36, true);
        addMaterial("Textures/mortared-rock.jpg", "Textures/mortared-rock-norm.jpg", "Textures/mortared-rock-bumps.jpg", 15.0F, false, TextureRepeat.XY);

        setLookupNormals(37, true);
        setNextIndex(100);
        addMaterial("Textures/" + size + "brown-dirt2.jpg", "Textures/brown-dirt-norm.jpg", 128.0F);
        addMaterial("Textures/" + size + "brown-dirt-side2.jpg", "Textures/brown-dirt-side-norms.jpg", "Textures/" + size + "brown-dirt-side-bump.jpg", 128.0F, false, TextureRepeat.X);
        addMaterial("Textures/" + size + "grass.jpg", "Textures/grass-norm.jpg", 16.0F);
        addMaterial("Textures/" + size + "sand.jpg", "Textures/grass-norm.jpg", 2.0F);
        addMaterial("Textures/" + size + "ROCK6-lighter.jpg", "Textures/ROCK6-norm.jpg", 10.0F);
        addMaterial("Textures/" + size + "pebbles.jpg", "Textures/pebbles-norm.jpg", "Textures/pebbles-bumps.jpg", 50.0F);
        addMaterial("Textures/" + size + "rock-wall.png", "Textures/ROCK8-norm.jpg", 50.0F);
        addWaterMaterial("Textures/" + size + "water.png", null, 1.0F, true, true, false);
        addMaterial("Textures/" + size + "bark128.jpg", "Textures/bark128-normal.png", "Textures/bark128-bump.png", 64.0F);
        addMaterial("Textures/" + size + "leaves3.png", null, 16.0F, true);
        addMaterial("Textures/" + size + "waddle-daub-plain.png", "Textures/waddle-daub-plain-normal.png", "Textures/waddle-daub-plain-bumps.png", 55.0F, false, TextureRepeat.XY);
        addMaterial("Textures/" + size + "wood-planks-lr.png", "Textures/wood-planks-lr-normal.png", "Textures/wood-planks-lr-bump.png", 15.0F, false, TextureRepeat.XY);
        addLightMaterial("Textures/" + size + "fire7.jpg");
        addMaterial("Textures/" + size + "bark128-top.png", "Textures/bark128-top-normal.png", "Textures/bark128-top-bump.png", 60.0F);
        addMaterial("Textures/" + size + "ROCK-wall-top-side.png", "Textures/ROCK-wall-top-side-normal.png", null, 50.0F, false, TextureRepeat.X);
        addMaterial("Textures/" + size + "rock-wall-top.png", "Textures/rock-wall-top-normal.png", 50.0F);
        addMaterial("Textures/" + size + "rock-wall-rock-top.png", "Textures/rock-wall-rock-top-normal.png", 50.0F);
        addMaterial("Textures/" + size + "brown-dirt-top.png", "Textures/brown-dirt-norm.jpg", 100.0F);
        addMaterial("Textures/" + size + "glass.png", null, 1.0F, true);
        addMaterial("Textures/" + size + "minerals.png", "Textures/minerals-normal.png", 10.0F);
        addMaterial("Textures/" + size + "black-marble.jpg", null, 1.0F);
        addMaterial("Textures/" + size + "wood-planks.png", "Textures/wood-planks-normal.png", "Textures/wood-planks-bump.png", 15.0F, false, TextureRepeat.XY);
        addGrassMaterial("Textures/" + size + "blades-atlas.png", "Textures/grass.jpg", "Textures/blades-bases-atlas.png", 100.0F);
        addMaterial("Textures/" + size + "bark90-128.jpg", "Textures/bark90-128-normal.png", "Textures/bark90-128-bump.png", 64.0F);
        addWaterMaterial("Textures/" + size + "water.png", null, 1.0F, true, true, true);
        addLeafMaterial("Textures/" + size + "leaf-atlas.png", null, null, 16.0F, true, null, true);
        addLeafMaterial("Textures/" + size + "leaf-atlas2.png", null, null, 16.0F, true, null, true);
        addMaterial("Textures/" + size + "white-marble.jpg", null, 1.0F);
        addMaterial("Textures/" + size + "wood-planks.png", null, null, 15.0F, false, TextureRepeat.XY);
        setNextIndex(134);
        addMaterial("Textures/" + size + "bark-chopped-128.png", "Textures/bark-chopped-128-normal.png", "Textures/bark-chopped-128-bump.png", 60.0F, false, TextureRepeat.X);
        addMaterial("Textures/shingles.png", "Textures/shingles-normal.png", "Textures/shingles-bump.png", 15.0F, false, TextureRepeat.XY);
        addMaterial("Textures/shingles-lr.png", "Textures/shingles-normal-lr.png", "Textures/shingles-bump-lr.png", 15.0F, false, TextureRepeat.XY);
        addMaterial("Textures/mortared-rock.jpg", "Textures/mortared-rock-norm.jpg", "Textures/mortared-rock-bumps.jpg", 15.0F, false, TextureRepeat.XY);
        setNextIndex(500);
        addMaterial("Textures/" + size + "grass.jpg", "Textures/grass-norm.jpg", 16.0F);
        ((Material) this.materials.get(500)).getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
    }

    protected Material createRopeMaterial(AssetManager assets) {
        Material mat = new Material(assets, "MatDefs/Rope.j3md");
        Texture t = loadTexture("Textures/rope.png", TextureRepeat.XY, true);
        if (t == null) {
            throw new RuntimeException("No texture found for: Textures/waddle-daub-plain.png");
        }
        mat.setTexture("DiffuseMap", t);
        t = loadTexture("Textures/rope-normal.jpg", TextureRepeat.XY, true);
        mat.setTexture("NormalMap", t);
        t = loadTexture("Textures/rope-bumps.jpg", TextureRepeat.XY, true);
        mat.setTexture("ParallaxMap", t);
        mat.setColor("Diffuse", diffuse);
        mat.setColor("Ambient", ambient);
        mat.setColor("Specular", specular);
        mat.setColor("FogColor", fogColor);
        mat.setVector4("TimeParms", timeParms);
        mat.setFloat("Shininess", 1.0F);
        mat.setBoolean("UseMaterialColors", true);

        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        float thickness = 0.1F;
        mat.setFloat("Thickness", thickness);

        return mat;
    }

    protected Texture loadTexture(String path, TextureRepeat texRepeat, boolean generateMips) {
        TextureKey key = new TextureKey(path);
        key.setGenerateMips(generateMips);

        Texture t = this.assets.loadTexture(key);
        if (t == null) {
            throw new RuntimeException("Error loading texture:" + path);
        }

        if (texRepeat != null) {
            switch (texRepeat.ordinal()) {
                case 1:
                    t.setWrap(Texture.WrapMode.Clamp);
                    break;
                case 2:
                    t.setWrap(Texture.WrapAxis.S, Texture.WrapMode.Repeat);
                    break;
                case 3:
                    t.setWrap(Texture.WrapAxis.T, Texture.WrapMode.Repeat);
                    break;
                case 4:
                    t.setWrap(Texture.WrapMode.Repeat);
            }

        }

        return t;
    }

    public void setNextIndex(int index) {
        while (this.materials.size() < index)
            this.materials.add(null);
    }

    public int addMaterial(String diffuseTex, String normalsTex, String bumpsTex, float shininess, boolean alpha, TextureRepeat texRepeat) {
        return addMaterial(diffuseTex, normalsTex, bumpsTex, shininess, alpha, texRepeat, true);
    }

    public int addMaterial(String diffuseTex, String normalsTex, String bumpsTex, float shininess, boolean alpha, TextureRepeat texRepeat, boolean generateMips) {
        Material mat = new Material(this.assets, "MatDefs/LightingWithFog.j3md");
        mat.setTexture("DiffuseMap", loadTexture(diffuseTex, texRepeat, generateMips));

        if ((normalsTex != null) && (!flat)) {
            mat.setTexture("NormalMap", loadTexture(normalsTex, texRepeat, generateMips));
        }
        if ((bumpsTex != null) && (!flat)) {
            mat.setTexture("ParallaxMap", loadTexture(bumpsTex, texRepeat, generateMips));
        }
        if (gritty) {
            mat.setTexture("NoiseMap", loadTexture("Textures/soft-noise-256.jpg", TextureRepeat.XY, true));
        }
        mat.setColor("Diffuse", diffuse);
        mat.setColor("Ambient", ambient);
        mat.setColor("Specular", specular);
        mat.setColor("FogColor", fogColor);
        mat.setVector4("TimeParms", timeParms);
        mat.setFloat("Shininess", shininess);
        mat.setBoolean("UseMaterialColors", true);

        mat.setBoolean("VertexLighting", vertexLighting);
        mat.setBoolean("LowQuality", lowQuality);

        if (alpha) {
            mat.setBoolean("UseAlpha", true);
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        }

        int result = this.materials.size();
        this.materials.add(mat);
        return result;
    }

    public int addLeafMaterial(String diffuseTex, String normalsTex, String bumpsTex, float shininess, boolean alpha, TextureRepeat texRepeat, boolean generateMips) {
        Material mat = new Material(this.assets, "MatDefs/Leaf.j3md");
        mat.setTexture("DiffuseMap", loadTexture(diffuseTex, texRepeat, generateMips));

        if ((normalsTex != null) && (!flat)) {
            mat.setTexture("NormalMap", loadTexture(normalsTex, texRepeat, generateMips));
        }
        if ((bumpsTex != null) && (!flat)) {
            mat.setTexture("ParallaxMap", loadTexture(bumpsTex, texRepeat, generateMips));
        }

        mat.setColor("Diffuse", diffuse);
        mat.setColor("Ambient", ambient);
        mat.setColor("Specular", specular);
        mat.setColor("FogColor", fogColor);
        mat.setFloat("Shininess", shininess);
        mat.setBoolean("UseMaterialColors", true);

        mat.setBoolean("VertexLighting", vertexLighting);
        mat.setBoolean("LowQuality", lowQuality);

        if (alpha) {
            mat.setBoolean("UseAlpha", true);
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        }

        int result = this.materials.size();
        this.materials.add(mat);
        return result;
    }

    protected int addMaterial(String diffuseTex, String normalsTex, float shininess) {
        return addMaterial(diffuseTex, normalsTex, null, shininess, false, TextureRepeat.XY);
    }

    protected int addMaterial(String diffuseTex, String normalsTex, String bumpsTex, float shininess) {
        return addMaterial(diffuseTex, normalsTex, bumpsTex, shininess, false, TextureRepeat.XY);
    }

    protected int addMaterial(String diffuseTex, String normalsTex, float shininess, boolean alpha) {
        return addMaterial(diffuseTex, normalsTex, null, shininess, alpha, TextureRepeat.XY);
    }

    protected int addFlameMaterial(String diffuseTex) {
        Material mat = new Material(this.assets, "MatDefs/Flame.j3md");
        mat.setTexture("Texture", loadTexture(diffuseTex, TextureRepeat.NONE, true));
        mat.setBoolean("PointSprite", true);
        mat.setFloat("Quadratic", 6.0F);

        mat.setFloat("Alpha", 1.0F);
        mat.setTexture("NoiseMap", loadTexture("Textures/noise.png", TextureRepeat.XY, true));

        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.AlphaAdditive);

        int result = this.materials.size();
        this.materials.add(mat);
        return result;
    }

    protected int addFloraMaterial(String diffuseTex) {
        Material mat = new Material(this.assets, "MatDefs/Flora.j3md");
        mat.setTexture("Texture", loadTexture(diffuseTex, TextureRepeat.NONE, true));
        mat.setBoolean("PointSprite", true);
        mat.setFloat("Quadratic", 6.0F);

        mat.setFloat("Alpha", 1.0F);
        mat.setTexture("NoiseMap", loadTexture("Textures/noise.png", TextureRepeat.XY, true));
        mat.setColor("Ambient", ambient);
        mat.setColor("Diffuse", diffuse);

        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        int result = this.materials.size();
        this.materials.add(mat);
        return result;
    }

    protected int addGrassMaterial(String diffuseTex, String groundTex, String groundMask, float shininess) {
        Material mat = new Material(this.assets, "MatDefs/Grass.j3md");
        Texture texture = loadTexture(diffuseTex, TextureRepeat.NONE, true);

        mat.setTexture("DiffuseMap", texture);
        mat.setTexture("GroundMap", loadTexture(groundTex, TextureRepeat.XY, true));
        mat.setTexture("GroundMask", loadTexture(groundMask, TextureRepeat.XY, true));
        mat.setTexture("NoiseMap", loadTexture("Textures/noise.png", TextureRepeat.XY, true));

        mat.setColor("Diffuse", diffuse);
        mat.setColor("Ambient", ambient);
        mat.setColor("FogColor", fogColor);
        mat.setColor("Specular", specular);
        mat.setFloat("Shininess", shininess);
        mat.setBoolean("UseMaterialColors", true);
        mat.setFloat("AlphaDiscardThreshold", 0.3F);

        mat.setBoolean("UseAlpha", true);

        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        mat.getAdditionalRenderState().setAlphaTest(true);
        mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        mat.setBoolean("VertexLighting", vertexLighting);
        mat.setBoolean("LowQuality", lowQuality);

        int result = this.materials.size();
        this.materials.add(mat);
        return result;
    }

    protected int addWaterMaterial(String diffuseTex, String normalsTex, float shininess, boolean alpha, boolean twoSided, boolean offset) {
        Material mat = new Material(this.assets, "MatDefs/WaterLightingWithFog.j3md");
        Texture texture = loadTexture(diffuseTex, TextureRepeat.XY, true);

        mat.setTexture("DiffuseMap", texture);

        if ((normalsTex != null) && (!flat)) {
            mat.setTexture("NormalMap", loadTexture(normalsTex, TextureRepeat.XY, true));
        }
        mat.setColor("Diffuse", diffuse);
        mat.setColor("Ambient", ambient);
        mat.setColor("FogColor", fogColor);
        mat.setColor("Specular", specular);
        mat.setFloat("Shininess", shininess);
        mat.setBoolean("UseMaterialColors", true);

        mat.setBoolean("VertexLighting", vertexLighting);
        mat.setBoolean("LowQuality", lowQuality);

        if (alpha) {
            mat.setBoolean("UseAlpha", true);
            mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        }

        if (twoSided) {
            mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        }
        if (offset) {
            mat.getAdditionalRenderState().setPolyOffset(0.1F, 0.1F);
        }

        if (waterMaterial1 == null) {
            waterMaterial1 = mat;
        } else if (waterMaterial2 == null) {
            waterMaterial2 = mat;
        }

        int result = this.materials.size();
        this.materials.add(mat);
        return result;
    }

    protected int addLightMaterial(String diffuseTex) {
        Material mat = new Material(this.assets, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", loadTexture(diffuseTex, TextureRepeat.XY, true));

        int result = this.materials.size();
        this.materials.add(mat);
        return result;
    }

    private static enum TextureRepeat {
        NONE, X, Y, XY
    }
}