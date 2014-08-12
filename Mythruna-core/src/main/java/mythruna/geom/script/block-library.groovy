package mythruna.geom.script

import com.jme3.math.FastMath
import com.jme3.math.Quaternion
import com.jme3.math.Vector2f
import com.jme3.math.Vector3f

/*
 * $Id: block-library.groovy 2172 2012-04-15 10:24:07Z pspeed $
 *
 *  Defines the basic API for block type libraries.
 *
 * Copyright (c) 2012, Paul Speed
 * All rights reserved.
 */
import mythruna.*;
import mythruna.geom.*;
import mythruna.phys.*;


interface BlockTypeBuilder {

    String getName();

    String getDescription();

    BlockType build( int id );
}

class SimpleBlockTypeBuilder implements BlockTypeBuilder {
    
    private String name;
    private String description;
    private MaterialType materialType;
    private int group;
    private GeomFactory factory; 
    
    public SimpleBlockTypeBuilder( String name, String desc, MaterialType type, int group, GeomFactory factory ) {
        this.name = name;
        this.description = desc;
        this.materialType = type;
        this.group = group;
        this.factory = factory;
    }

    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public BlockType build( int id ) {
    
        boolean opaque = true;
        if( group > 0 )
            opaque = false;                                           
            
        BlockType bt = new BlockType( id, description, materialType, opaque, group, factory );
    
        return bt;               
    }
}

class VertexBuilder implements Cloneable {

    static Quaternion[] ROT = new Quaternion[4];
    
    static {
        ROT[0] = new Quaternion().fromAngles( 0, 0, 0 )
        ROT[1] = new Quaternion().fromAngles( 0, 0, (float)FastMath.HALF_PI )
        ROT[2] = new Quaternion().fromAngles( 0, 0, (float)FastMath.PI )
        ROT[3] = new Quaternion().fromAngles( 0, 0, (float)(FastMath.HALF_PI + FastMath.PI) )
    };

    public int index = -1;
    private Vector3f pos;
    private Vector2f texture; 
    private Vector3f normal;
    private Vector3f tangent;
    
    public VertexBuilder(  double x, double y, double z ) {
        pos = new Vector3f( (float)x, (float)y, (float)z );
    }

    public VertexBuilder clone() {
//System.out.println( "Cloning VertexBuilder:" + this );    
        VertexBuilder result = super.clone();
        if( pos != null )
            result.pos = pos.clone();
        if( texture != null )
            result.texture = texture.clone();
        if( normal != null )
            result.normal = normal.clone();
        if( tangent != null )
            result.tangent = tangent.clone();
        return result;
    }
 
    public void rotate(int dirDelta) {
        if( dirDelta == 0 ) {
            return;
        }
        if( pos != null )
            pos = ROT[dirDelta].mult(pos);
        if( normal != null )
            normal = ROT[dirDelta].mult(normal);
        if( tangent != null )
            tangent = ROT[dirDelta].mult(tangent);
    }
    
    public Vector2f texture( double x, double y ) {
        texture = new Vector2f( (float)x, (float)y );
    }
     
    public Vector3f normal( double x, double y, double z ) {
        normal = new Vector3f( (float)x, (float)y, (float)z );       
    }
     
    public Vector3f tangent( double x, double y, double z ) {
        tangent = new Vector3f( (float)x, (float)y, (float)z );       
    }
    
    public boolean equals( Object o ) {
        if( o == null )
            return false;
        if( o.getClass() != getClass() )
            return false;
        
        if( o.pos != pos )
            return false;
        if( o.texture != texture )
            return false;
        if( o.normal != normal )
            return false;
        if( o.tangent != tangent )
            return false;
        return true;
    }
    
    public int hashCode() {
        return pos.hashCode();
    }
    
    public String toString() {
        return getClass().getName() + System.identityHashCode(this) + "[" + index + ", " + pos + ", " + texture + "]";
    } 
}

class PolyBuilder implements Cloneable {
    private int material;
    List verts = new ArrayList();
    private int maxVerts = 3;

    public PolyBuilder( int maxVerts ) {
        this.maxVerts = maxVerts;
    }
    
    public PolyBuilder( int maxVerts, int material ) {
        this.maxVerts = maxVerts;
        this.material = material;
    }

    public PolyBuilder clone() {
//System.out.println( "Cloning PolyBuilder:" + this );    
        PolyBuilder result = super.clone();
        result.verts = result.verts.collect{ it.clone() }
        return result;   
    }

    public void material( int material ) {
        this.material = material;
    }
 
    public int getMaterial() {
        return material;
    }
 
    public void replaceMaterial( int repl, int with ) {
        if( repl == material )
            material = with;
    }
 
    public Vector3f allNormals( double x, double y, double z ) {
        Vector3f normal = new Vector3f( (float)x, (float)y, (float)z );
        verts.each{ it.normal = normal };
        return normal;
    }
    
    public Vector3f allTangents( double x, double y, double z ) {
        Vector3f tangent = new Vector3f( (float)x, (float)y, (float)z );
        verts.each{ it.tangent = tangent };
        return tangent;
    }               
       
    public VertexBuilder vertex( double x, double y, double z, Closure config ) {
        if( verts.size() == maxVerts ) {
            throw new RuntimeException( "Shape already has " + maxVerts + " vertexes." );
        }
        VertexBuilder builder = new VertexBuilder(x,y,z);
        verts.add(builder);
        config.resolveStrategy = Closure.DELEGATE_ONLY;
        config.setDelegate(builder);    
        config();
        return builder;
    }
 
    public VertexBuilder vertex( int index, Closure config ) {
        VertexBuilder builder = verts.get(index);
        config.resolveStrategy = Closure.DELEGATE_ONLY;
        config.setDelegate(builder);    
        config();
        return builder;
    }
    
    public void rotate( int dirDelta ) {
        verts.each { it.rotate(dirDelta) }
    }

    public void retexture( int dir ) {    
//println( "retexture(" + dir + ")---------" );
        if( dir < 0 )
            throw new IllegalArgumentException( "Invalid direction:" + dir );
                
        Vector3f min = new Vector3f(100,100,100);
        Vector3f max = new Vector3f(-100,-100,-100);
        verts.each {
            min.minLocal( it.pos );
            max.maxLocal( it.pos );
        }
        
//println( "retexture min:" + min + "  max:" + max );
        
        Vector3f delta = max.subtract(min);
        
        // How we change texture coordinates is 
        // direction specific.
        Vector3f sVec = new Vector3f();
        Vector3f tVec = new Vector3f();
        Vector3f corner = new Vector3f();
        float sScale = 1;
        float tScale = 1;
        switch( dir ) {
            case Direction.NORTH:
                sVec.x = -1;
                tVec.z = 1;
                sScale = 1 / delta.x;
                tScale = 1 / delta.z;
                corner.x = max.x;
                corner.y = min.y;
                corner.z = min.z;
                break;
            case Direction.SOUTH:
                sVec.x = 1;
                tVec.z = 1;
                sScale = 1 / delta.x;
                tScale = 1 / delta.z;
                corner.x = min.x;
                corner.y = max.y;
                corner.z = min.z;
                break;
            case Direction.EAST:
                sVec.y = -1;
                tVec.z = 1;
                sScale = 1 / delta.y;
                tScale = 1 / delta.z;
                corner.x = max.x;
                corner.y = max.y;
                corner.z = min.z;
                break;
            case Direction.WEST:
                sVec.y = 1;
                tVec.z = 1;
                sScale = 1 / delta.y;
                tScale = 1 / delta.z;
                corner.x = min.x;
                corner.y = min.y;
                corner.z = min.z;
                break;
            case Direction.UP:
                sVec.x = 1;
                tVec.y = -1;
                sScale = 1 / delta.x;
                tScale = 1 / delta.y;
                corner.x = min.x;
                corner.y = max.y;
                corner.z = max.z;
                break;
            case Direction.DOWN:
                sVec.x = 1;
                tVec.y = 1;
                sScale = 1 / delta.x;
                tScale = 1 / delta.y;
                corner.x = min.x;
                corner.y = min.y;
                corner.z = min.z;
                break;
        } 
        
        verts.each {
            
            Vector3f v = it.pos.subtract(corner);
//System.out.println( "  rel v:" + v );            
            float s = sVec.dot(v) * sScale;
            float t = tVec.dot(v) * tScale;
//println( "  s:" + s + "  t:" + t );            
            it.texture = new Vector2f(s,t);
        }
    }
 
    protected PolyBuilder[] makeTriangles() {
        if( verts.size() == 3 ) {
            PolyBuilder[] result = new PolyBuilder[1];
            result[0] = this;
            return result;
        }
        
        if( verts.size() != 4 ) {
            throw new RuntimeException( "Cannot make triangle from shape with only " + verts.size() + " vertexes." );         
        }
        
        PolyBuilder[] result = new PolyBuilder[2];
        result[0] = new PolyBuilder(3, material);
        result[1] = new PolyBuilder(3, material);
        
        result[0].verts.add( verts.get(0) );
        result[0].verts.add( verts.get(1) );
        result[0].verts.add( verts.get(2) );
        
        result[1].verts.add( verts.get(0) );
        result[1].verts.add( verts.get(2) );
        result[1].verts.add( verts.get(3) );
        
        return result;
    } 
    
    public String toString() {
        return getClass().getName() + System.identityHashCode(this) + "[" + material + ", " + verts + "]";
    } 
}

class SideBuilder implements Cloneable {
    private int dir;
    private BoundaryShape shape;
    private List list = new ArrayList();
    private Map pols = new HashMap();
    
    public SideBuilder( int dir, BoundaryShape shape ) {
        this.dir = dir;
        this.shape = shape;
    }

    public void clear() {
        pols.clear();
    }

    public SideBuilder clone() {
//System.out.println( "Clonding side builder:" + this );    
        SideBuilder result = super.clone();
        result.pols = new HashMap();
        Map clones = new HashMap();
        result.list = new ArrayList();        
        pols.entrySet().each {
            List newList = it.value.collect { tri ->
                Object clone = tri.clone();
                clones.put( tri, clone );
                return clone; 
            }
            result.pols.put( it.key, newList );        
        }
        
        // Need to do this to make sure the list is in the same
        // order.
        list.each {
            result.list.add( clones.get(it) );
        }        
        return result;
    }
 
    public void replaceMaterial( int replace, int with ) {
        List tris = pols.remove(replace);
        if( tris == null ) 
            return;
        
        tris.each { it.replaceMaterial( replace, with ) }
        pols.put( with, tris );
    }
 
    public void rotate( int dirDelta ) {
        int originalDir = dir;
        dir = Direction.rotate(dir, dirDelta); 
        if( shape != null ) {
            shape = shape.rotate(originalDir, dirDelta);
        }
        pols.entrySet().each {
            it.getValue().each{ tri -> tri.rotate(dirDelta) };
        } 
    }
    
    protected List getPols( int material ) {
        List result = pols.get(material);
        if( result == null ) {
            result = new ArrayList();
            pols.put(material, result);            
        }
        return result;
    }
    
    public PolyBuilder triangle( Closure config ) {
        PolyBuilder builder = new PolyBuilder(3);
        config.resolveStrategy = Closure.DELEGATE_ONLY;
        config.setDelegate(builder);    
        config();
 
        list.add(builder);       
        getPols(builder.getMaterial()).add(builder);
        
        return builder;
    }

    public PolyBuilder quad( Closure config ) {
        PolyBuilder builder = new PolyBuilder(4);
        config.resolveStrategy = Closure.DELEGATE_ONLY;
        config.setDelegate(builder);    
        config();

        list.add(builder);       
        getPols(builder.getMaterial()).add(builder);
 
        // Split the quad into two triangles
        /*PolyBuilder[] split = builder.makeTriangles();       
        
        getTriangles(builder.getMaterial()).add(split[0]);
        getTriangles(builder.getMaterial()).add(split[1]);*/
        
        return builder;
    }

    public PolyBuilder triangle( int index, Closure config ) {
        PolyBuilder result = list.get(index);
        if( result.maxVerts != 3 ) {
            throw new RuntimeException( "Shape at index:" + index + " is not a triangle." );
        }
        config.resolveStrategy = Closure.DELEGATE_ONLY;
        config.setDelegate(result);    
        config();
        return result; 
    }

    public PolyBuilder quad( int index, Closure config ) {
        PolyBuilder result = list.get(index);
        if( result.maxVerts != 4 ) {
            throw new RuntimeException( "Shape at index:" + index + " is not a quad." );
        }
        config.resolveStrategy = Closure.DELEGATE_ONLY;
        config.setDelegate(result);    
        config();
        return result; 
    }

    protected Vector3f flip( Vector3f v ) {
        if( v == null ) return null;
        return new Vector3f( v.x, v.z, v.y );
    }

    private GeomPart createPart( int material, List polygons ) {
    
        // Build the vertex index
        List verts = new ArrayList();
 
        List tris = polygons.collect { it.makeTriangles() }.flatten();
        for( PolyBuilder t : tris ) {                                
            for( VertexBuilder v : t.verts ) {
                int id = verts.indexOf(v);
                if( id < 0 ) {
                    id = verts.size();
                    verts.add(v);
                } 
                v.index = id;
            }
        }

        GeomPart result = new GeomPart( material, dir ); 
 
        List indexes = tris.collect{ it.verts.collect{ it.index } }.flatten();
        List pos = verts.collect{ flip(it.pos)?.toArray(null) }.findAll{it != null}
        List tex = verts.collect{ it.texture?.toArray(null) }.findAll{it != null}  
        List norm = verts.collect{ flip(it.normal)?.toArray(null) }.findAll{it != null}
        List tan = verts.collect{ flip(it.tangent)?.toArray(null) }.findAll{it != null}

//System.out.println( "vert count:" + verts.size() );
//        println "Dir:" + dir;
//        println "Verts:" + verts;
//        println "pos:" + pos.flatten();
//        println "indexes:" + indexes;
//        println "tex:" + tex.flatten();
//        println "norm:" + norm.flatten();
//        println "tangent:" + tan.flatten();
 
        result.setCoords( pos.flatten().toArray(new float[0]) );
                
        if( tex.size() > 0 ) {
            if( tex.size() != pos.size() ) {
                throw new RuntimeException( "Number of texture coordinates:" + tex.size() + " does not match vertexes:" + verts.size() );
            }       
            result.setTexCoords( tex.flatten().toArray(new float[0]) );
        }
               
        if( norm.size() > 0 ) {
            if( norm.size() != pos.size() ) {
                throw new RuntimeException( "Number of normals:" + norm.size() + " does not match vertexes:" + verts.size() );
            }       
            result.setNormals( norm.flatten().toArray(new float[0]) );
        }

        if( tan.size() > 0 ) {
            if( tan.size() != pos.size() ) {
                throw new RuntimeException( "Number of tangents:" + tan.size() + " does not match vertexes:" + verts.size() );
            }       
            result.setTangents( tan.flatten().toArray(new float[0]) );
        }
        
        result.setIndexes( indexes.toArray(new int[0]) );          
        
        return result;   
    }

    
    public List build() {
 
        // Create parts for each material
        List result = new ArrayList();
        for( Map.Entry e : pols.entrySet() ) {
//println( "Creating part for:" + e.getKey() + "  = " + e.getValue() );        
            result.add( createPart(e.getKey(), e.getValue()) );
        }
        
        return result;       
    }   
}


class FromTemplateWrapper {
    ConfigurableBlockTypeBuilder builder;
    
    public FromTemplateWrapper(ConfigurableBlockTypeBuilder builder) {
        this.builder = builder;
    }
 
    // The assumption is that builder is already a deep clone so it is 
    // safe to muck with its stuff directly.
    protected void rotate90( int dirDelta ) {
        if( builder.collider != null )
            builder.collider = builder.collider.rotate(dirDelta);
        if( builder.internal != null )
            builder.internal.values().each{ it.rotate(dirDelta) };
            
        if( dirDelta == 1 || dirDelta == 3 ) {
            if( builder.transparency != null ) {
                // Swap the x,y axes.
                float x = builder.transparency[0];
                builder.transparency[0] = builder.transparency[1];
                builder.transparency[1] = x;
            } 
        }
        
        // The compass dir sides need to be remapped
        SideBuilder[] newSides = new SideBuilder[Direction.DIR_COUNT];
        newSides[4] = builder.sides[4];
        newSides[5] = builder.sides[5];
        for( int d = 0; d < 4; d++ ) {
            if( builder.sides[d] == null )
                continue;
            int rot = Direction.rotate(d, dirDelta);
            newSides[rot] = builder.sides[d];
            //newSides[rot].rotate(dirDelta);           
        }
 
        // Every side needs to rotate
        newSides.each { if( it != null ) it.rotate(dirDelta) };
        
        builder.sides = newSides;             
    }
 
    public void replaceMaterial( int replace, int with ) {
        builder.sides.each {
            if( it != null ) {
                it.replaceMaterial( replace, with ); 
            }
        }
        if( builder.internal != null ) {
            builder.internal.values().each{ it.replaceMaterial( replace, with ) };
        }
    }
    
    public void remapMaterials( Map map ) {
        map.entrySet().each {
            replaceMaterial( it.key, it.value );
        }
    }
    
    public void rotate( double angle ) {
        // Need to rotate the directional parts
        if( angle == 0 )
            return;
        if( angle == 90 || angle == 180 || angle == 270 || angle == -90 || angle == -180 || angle == -270 ) {
            int dirDelta = (int)(angle/90);
            while( dirDelta < 0 )
                dirDelta += 4;
            rotate90(dirDelta);
            return;
        }            
 
        throw new UnsupportedOperationException( "Currently only support rotations in 90 degree increments." );   
    }    
}

class ConfigurableBlockTypeBuilder implements BlockTypeBuilder {
    
    private String name;
    private String description;
    private MaterialType materialType;
    private int group;
    private SideBuilder[] sides = new SideBuilder[Direction.DIR_COUNT];
    private Map internal = new HashMap();
    private Collider collider;
    private float[] transparency;
    private boolean clipped = true;
    private Double volume;
    
    public ConfigurableBlockTypeBuilder( String name, String desc, MaterialType type, int group ) {
        this.name = name;
        this.description = desc;
        this.materialType = type;
        this.group = group;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }    
    
    public BlockType build( int id ) {
 
//System.out.println( "Building:" + name + ", " + description ); 
        PartFactory[] sideParts = new PartFactory[Direction.DIR_COUNT];
        for( int i = 0; i < sides.length; i++ ) {
            SideBuilder s = sides[i]; 
            if( s == null )
                continue;
                
            sideParts[i] = new DefaultPartFactory( s.shape, s.build().toArray( new GeomPart[1] ) );                
        }
         
//System.out.println( "Block type parts:" + Arrays.asList(sideParts) ); 
//System.out.println( "Internal builder:" + internal );
        PartFactory internalParts = null;
        if( internal != null && !internal.isEmpty() ) {
            List parts = internal.values().collect{ it.build() }.flatten();
            
            internalParts = new DefaultPartFactory( parts.toArray( new GeomPart[parts.size()] ) );
            //internalParts = new DefaultPartFactory( internal.build().toArray( new GeomPart[1] ) );
        }                
 
        GeomFactory factory = new DefaultGeomFactory(collider, transparency, volume, clipped, sideParts, internalParts);
    
        boolean opaque = group <= 0;
        BlockType bt = new BlockType( id, description, materialType, opaque, group, factory );
            
        return bt;
    }
    
    public void group( int group ) {
        this.group = group;
    }
 
    public void volume( double d ) {
        this.volume = d;
    }
 
    public void transparency( double d ) {
        transparency = new float[3];
        transparency[0] = (float)d;
        transparency[1] = (float)d;
        transparency[2] = (float)d;
    }

    public void transparency( double[] array ) {
        transparency = new float[3];
        transparency[0] = (float)array[0];
        transparency[1] = (float)array[1];
        transparency[2] = (float)array[2];
    }
 
    public void collider( Collider collider ) {
        this.collider = collider;
    }
 
    public void noclip() {
        this.clipped = false;
    }

    public void fromTemplate( String name ) {
//        println "Looking up:" + name;
        ConfigurableBlockTypeBuilder template = LibraryBuilder.templates.get(name);
//        println "Found:" + template;
        
        if( template == null ) {
            throw new RuntimeException( "Template not found:" + name );
        }
        
        // Copy what was defined
        if( template.transparency != null )
            transparency( template.transparency );
        if( template.internal != null ) {
//System.out.println( "Cloning internal:" + template.internal );
            template.internal.entrySet().each {
                this.internal.put( it.key, it.value.clone() );
            }
//System.out.println( "Cloned internal:" + internal );            
        }
        for( int d = 0; d < Direction.DIR_COUNT; d++ ) {
            if( template.sides[d] != null ) {
                sides[d] = template.sides[d].clone();
            }                
        }

        // the group comes from the addType() call
        //this.group = template.group;
        
        this.collider = template.collider;
        this.clipped = template.clipped;
        this.volume = template.volume;
        
//System.out.println( "sides:" + sides );
//System.out.println( "transparency:" + transparency );                
    }
    
    public void fromTemplate( String name, Closure config ) {
        fromTemplate(name);
                
        config.resolveStrategy = Closure.DELEGATE_ONLY;
        config.setDelegate( new FromTemplateWrapper(this) );    
        config();                   
    }
 
    protected void side( int dir, BoundaryShape shape, Closure config ) {
        if( sides[dir] == null )
            sides[dir] = new SideBuilder(dir, shape);
        
        config.resolveStrategy = Closure.DELEGATE_ONLY;
        config.setDelegate(sides[dir]);    
        config();           
    } 
    
    public void internal( int dir, Closure config ) {
        SideBuilder b = internal.get(dir);
        if( b == null ) {
            b = new SideBuilder(dir, null);
            internal.put(dir, b);
        }         
        //internal = new SideBuilder(dir, null);
        
        config.resolveStrategy = Closure.DELEGATE_ONLY;
        config.setDelegate(b);    
        config();           
    } 

    public void internal( Closure config ) {
        internal( -1, config );
    } 
    
    public void north( BoundaryShape shape, Closure config ) {
        side( Direction.NORTH, shape, config );
    }
    public void south( BoundaryShape shape, Closure config ) {
        side( Direction.SOUTH, shape, config );
    }
    public void east( BoundaryShape shape, Closure config ) {
        side( Direction.EAST, shape, config );
    }
    public void west( BoundaryShape shape, Closure config ) {
        side( Direction.WEST, shape, config );
    }
    public void up( BoundaryShape shape, Closure config ) {
        side( Direction.UP, shape, config );
    }
    public void down( BoundaryShape shape, Closure config ) {
        side( Direction.DOWN, shape, config );
    }
    public void north( Closure config ) {
        side( Direction.NORTH, null, config );
    }
    public void south( Closure config ) {
        side( Direction.SOUTH, null, config );
    }
    public void east( Closure config ) {
        side( Direction.EAST, null, config );
    }
    public void west( Closure config ) {
        side( Direction.WEST, null, config );
    }
    public void up( Closure config ) {
        side( Direction.UP, null, config );
    }
    public void down( Closure config ) {
        side( Direction.DOWN, null, config );
    }
}

class LibraryBuilder {
 
    static Map templates = new HashMap();
       
    private Map fixedIds = new HashMap();
    private Map blockTypes = new LinkedHashMap();
    private List accumulator = new ArrayList();  // kind of a hack. accumulates the tools for a group

    private Map toolGroups = new LinkedHashMap();
 
    public void forceIndex( Map values ) {        
        fixedIds.putAll(values);       
    }
    
    public void addTemplate( String name, Closure config ) {
//println "addTemplate(" + name + ")";    
        BlockTypeBuilder b = new ConfigurableBlockTypeBuilder(name, name, null, 0);
        
        // I think ultimately we want to hold these and execute them
        // after all of the libraries are loaded but we'll see.
        
        config.resolveStrategy = Closure.DELEGATE_ONLY;
        config.setDelegate(b);
        config();
 
        templates.put( name, b );       
    }
 
    public void addToolGroup( String name, Closure config ) {
 
        accumulator.clear();   
        // A fake passthrough for now
        config.resolveStrategy = Closure.DELEGATE_ONLY;
        config.setDelegate(this);
        config();
        
        toolGroups.put( name, accumulator.clone() );        
    }
    
    public void addType( String name, String desc, MaterialType materialType, int group, GeomFactory factory ) {
 
        addType( 0, name, desc, materialType, group, factory );
    }

    public void addType( int forcedId, String name, String desc, MaterialType materialType, int group, GeomFactory factory ) {
    
        BlockTypeBuilder b = new SimpleBlockTypeBuilder(name, desc, materialType, group, factory);
        
        blockTypes.put( name, b );
        accumulator.add(b);
        if( forcedId > 0 )
            fixedIds.put(name, forcedId);    
    }
    
    public void addType( String name, String desc, MaterialType materialType, int group, Closure config ) {
        addType( 0, name, desc, materialType, group, config );    
    }
    
    public void addType( String name, String desc, MaterialType materialType, Closure config ) {
        addType( 0, name, desc, materialType, 0, config );
    }

    public void addType( int forcedId, String name, String desc, MaterialType materialType, int group, Closure config ) {
    
//println "addType(" + forcedId + ", " + name + ", " + desc + ", " + materialType + ", " + group + ")";    
        BlockTypeBuilder b = new ConfigurableBlockTypeBuilder(name, desc, materialType, group);
        
        // I think ultimately we want to hold these and execute them
        // after all of the libraries are loaded but we'll see.
        
        config.resolveStrategy = Closure.DELEGATE_ONLY;
        config.setDelegate(b);
        config();
        
        blockTypes.put(name, b);       
        accumulator.add(b);
        if( forcedId > 0 ) {
            fixedIds.put(name, forcedId);
        }
    }
    
    public void addType( int forcedId, String name, String desc, MaterialType materialType, Closure config ) {
        addType( forcedId, name, desc, materialType, 0, config );
    }
    
    public void build() {
        
        int nextId = 500; // for now
 
        Map refMap = new HashMap();       
        for( BlockTypeBuilder b : blockTypes.values() ) {        
            Integer id = fixedIds.get(b.getName());
//System.out.println( "name:" + b.getName() + "  id:" + id );            
            if( id == null ) {
                id = nextId++;
            } else {
                // Clear the existing one to be sure
                BlockTypeIndex.set( id, null );
            }
            
            BlockType type = b.build(id); 
            BlockTypeIndex.set( id, type );
            refMap.put( b, type );
        }
 
        for( Map.Entry e : toolGroups.entrySet() ) {
            println "Processing group:" + e;
 
            List types = e.value.collect{ refMap.get(it) };
            BlockTypeIndex.setGroup( e.key, types );
        }       
    }
}


Object blockLibrary( Map args, Closure config ) {
    String name = args.get("name");
    if( name == null ) {
        throw new IllegalArgumentException( "Block libraries must have a name." );
    }
 
    LibraryBuilder builder = new LibraryBuilder();
    config.resolveStrategy = Closure.DELEGATE_ONLY;
    config.setDelegate(builder);    
    config();

    // Register it or build it or something
       
    builder.build();       
       
    return null;
}


