/*
 * $Id: base-blocks.groovy 2271 2012-06-26 08:11:08Z pspeed $
 *
 * Copyright (c) 2012, Paul Speed
 * All rights reserved.
 */

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import mythruna.BlockTypeIndex;
import mythruna.Direction;
import mythruna.MaterialType;
import mythruna.ShapeIndex;
import mythruna.geom.*;
import mythruna.phys.collision.*;

blockLibrary( name:"mythruna.base" ) {

    println "Configuring base block library";
    
    // Make sure that no matter what order we create these types,
    // we are still backwards compatible with existing worlds that
    // don't have a stored index.
    /*forceIndex( 
        "dirt":1, 
        "grass":2,
        "sand":3,
        "stone":4, 
        "dirt-ramp-n":48, 
        "dirt-ramp-s":49, 
        "dirt-ramp-e":50, 
        "dirt-ramp-w":51
    )*/
    
    addToolGroup( "Dirt" ) {
        // Regular dirt
        addType( 1, "dirt", "Dirt", MaterialType.DIRT, 0, new CubeFactory(0) );
        
        addType( 264, "dirt-half-dn", "Half Dirt", MaterialType.DIRT ) {
            fromTemplate( "half-dn" )
        }
        
        // Dirt + grass
        addType( 2, "grass", "Grass", MaterialType.GRASS, 0, new CubeFactory(1,2,0) );
 
        /*
        Maybe I'll resurrect this one later, so I'm going to "burn" the ID
        for now.
        addType( 265, "grass-half-dn", "Half Grass", MaterialType.GRASS ) {
            fromTemplate( "half-dn" ) { remapMaterials( 0:1 ) }
            
            internal( Direction.UP ) {
                replaceMaterial(1, 2);
            }
            
            down {
                replaceMaterial(1, 0);
            }
        }
        */
        
        // Dirt slopes       
        addType( 48, "dirt-ramp-n", "Dirt Slope", MaterialType.DIRT ) {
            fromTemplate( "wedge-n" );            
            down() { quad(0) { retexture(Direction.DOWN) } }
        }

        addType( 49, "dirt-ramp-s", "Dirt Slope", MaterialType.DIRT ) {
            fromTemplate( "wedge-s" ); 
            down() { quad(0) { retexture(Direction.DOWN) } }
        }
      
        addType( 50, "dirt-ramp-e", "Dirt Slope", MaterialType.DIRT ) {
            fromTemplate( "wedge-e" ); 
            down() { quad(0) { retexture(Direction.DOWN) } }
        }
      
        addType( 51, "dirt-ramp-w", "Dirt Slope", MaterialType.DIRT ) {
            fromTemplate( "wedge-w" ) ;
            down() { quad(0) { retexture(Direction.DOWN) } }
        }
        
        // Dirt half slopes(bottom)       
        addType( 266, "dirt-half-ramp-n", "Dirt Half Slope", MaterialType.DIRT ) {
            fromTemplate( "half-wedge-n" );
        }

        addType( 267, "dirt-half-ramp-s", "Dirt Half Slope", MaterialType.DIRT ) {
            fromTemplate( "half-wedge-s" ); 
        }
      
        addType( 268, "dirt-half-ramp-e", "Dirt Half Slope", MaterialType.DIRT ) {
            fromTemplate( "half-wedge-e" ); 
        }
      
        addType( 269, "dirt-half-ramp-w", "Dirt Half Slope", MaterialType.DIRT ) {
            fromTemplate( "half-wedge-w" ) ;
        }
 
        // Half corner (bottom)       
        addType( 270, "dirt-half-corner-ne", "Dirt Half Crnr", MaterialType.DIRT ) {
            fromTemplate( "half-corner-ne" );
        }

        addType( 271, "dirt-half-corner-nw", "Dirt Half Crnr", MaterialType.DIRT ) {
            fromTemplate( "half-corner-nw" ); 
        }
      
        addType( 272, "dirt-half-corner-sw", "Dirt Half Crnr", MaterialType.DIRT ) {
            fromTemplate( "half-corner-sw" ); 
        }
      
        addType( 273, "dirt-half-corner-se", "Dirt Half Crnr", MaterialType.DIRT ) {
            fromTemplate( "half-corner-se" ) ;
        }
        
        // Dirt half slopes(up)       
        addType( 274, "dirt-half-ramp-up-n", "Dirt Half Slope", MaterialType.DIRT ) {
            fromTemplate( "half-wedge-up-n" );
        }

        addType( 275, "dirt-half-ramp-up-s", "Dirt Half Slope", MaterialType.DIRT ) {
            fromTemplate( "half-wedge-up-s" ); 
        }
      
        addType( 276, "dirt-half-ramp-up-e", "Dirt Half Slope", MaterialType.DIRT ) {
            fromTemplate( "half-wedge-up-e" ); 
        }
      
        addType( 277, "dirt-half-ramp-up-w", "Dirt Half Slope", MaterialType.DIRT ) {
            fromTemplate( "half-wedge-up-w" ) ;
        }

        // Half corner (top)       
        addType( 302, "dirt-shallow-corner-ne", "Dirt Half Crnr", MaterialType.DIRT ) {
            fromTemplate( "shallow-corner-ne" );
        }

        addType( 303, "dirt-shallow-corner-nw", "Dirt Half Crnr", MaterialType.DIRT ) {
            fromTemplate( "shallow-corner-nw" ); 
        }

        addType( 304, "dirt-shallow-corner-sw", "Dirt Half Crnr", MaterialType.DIRT ) {
            fromTemplate( "shallow-corner-sw" ); 
        }
      
        addType( 305, "dirt-shallow-corner-se", "Dirt Half Crnr", MaterialType.DIRT ) {
            fromTemplate( "shallow-corner-se" ) ;
        }
    }
    
    addToolGroup( "Sand" ) {
        addType( 3, "sand", "Sand", MaterialType.SAND, 0, new CubeFactory(3) );
        
        addType( 262, "sand-half-dn", "Half Sand", MaterialType.SAND ) {
            fromTemplate( "half-dn" ) { remapMaterials( 0:3 ) }
        }
        
        // Dirt half slopes(bottom)       
        addType( 312, "sand-half-ramp-n", "Sand Half Slope", MaterialType.SAND ) {
            fromTemplate( "half-wedge-n" ) { remapMaterials( 0:3, 100:103 ) }
        }

        addType( 313, "sand-half-ramp-s", "Sand Half Slope", MaterialType.SAND ) {
            fromTemplate( "half-wedge-s" ) { remapMaterials( 0:3, 100:103 ) }
        }
      
        addType( 314, "sand-half-ramp-e", "Sand Half Slope", MaterialType.SAND ) {
            fromTemplate( "half-wedge-e" ) { remapMaterials( 0:3, 100:103 ) }
        }
      
        addType( 315, "sand-half-ramp-w", "Sand Half Slope", MaterialType.SAND ) {
            fromTemplate( "half-wedge-w" )  { remapMaterials( 0:3, 100:103 ) }
        }
 
        // Half corner (bottom)       
        addType( 316, "sand-half-corner-ne", "Sand Half Crnr", MaterialType.SAND ) {
            fromTemplate( "half-corner-ne" ) { remapMaterials( 0:3, 100:103 ) }
        }

        addType( 317, "sand-half-corner-nw", "Sand Half Crnr", MaterialType.SAND ) {
            fromTemplate( "half-corner-nw" ) { remapMaterials( 0:3, 100:103 ) }
        }
      
        addType( 318, "sand-half-corner-sw", "Sand Half Crnr", MaterialType.SAND ) {
            fromTemplate( "half-corner-sw" ) { remapMaterials( 0:3, 100:103 ) }
        }
      
        addType( 319, "sand-half-corner-se", "Sand Half Crnr", MaterialType.SAND ) {
            fromTemplate( "half-corner-se" )  { remapMaterials( 0:3, 100:103 ) }
        }
        
        // Dirt half slopes(up)       
        addType( 320, "sand-half-ramp-up-n", "Sand Half Slope", MaterialType.SAND ) {
            fromTemplate( "half-wedge-up-n" ) { remapMaterials( 0:3, 100:103 ) }
        }

        addType( 321, "sand-half-ramp-up-s", "Sand Half Slope", MaterialType.SAND ) {
            fromTemplate( "half-wedge-up-s" ) { remapMaterials( 0:3, 100:103 ) }
        }
      
        addType( 322, "sand-half-ramp-up-e", "Sand Half Slope", MaterialType.SAND ) {
            fromTemplate( "half-wedge-up-e" ) { remapMaterials( 0:3, 100:103 ) }
        }
      
        addType( 323, "sand-half-ramp-up-w", "Sand Half Slope", MaterialType.SAND ) {
            fromTemplate( "half-wedge-up-w" )  { remapMaterials( 0:3, 100:103 ) }
        }

        // Half corner (top)       
        addType( 324, "sand-shallow-corner-ne", "Sand Half Crnr", MaterialType.SAND ) {
            fromTemplate( "shallow-corner-ne" ) { remapMaterials( 0:3, 100:103 ) }
        }

        addType( 325, "sand-shallow-corner-nw", "Sand Half Crnr", MaterialType.SAND ) {
            fromTemplate( "shallow-corner-nw" ) { remapMaterials( 0:3, 100:103 ) } 
        }

        addType( 326, "sand-shallow-corner-sw", "Sand Half Crnr", MaterialType.SAND ) {
            fromTemplate( "shallow-corner-sw" ) { remapMaterials( 0:3, 100:103 ) } 
        }
      
        addType( 327, "sand-shallow-corner-se", "Sand Half Crnr", MaterialType.SAND ) {
            fromTemplate( "shallow-corner-se" )  { remapMaterials( 0:3, 100:103 ) }
        }
        
    }
            
    addToolGroup( "Stone" ) {    
        addType( 4, "stone", "Stone", MaterialType.STONE, 0, new CubeFactory(4) );

        addType( 80, "stone-half-up", "Stone Slab Top", MaterialType.STONE ) {
            fromTemplate( "half-up" ) { remapMaterials( 0:4 ) }
        }
        addType( 81, "stone-half-dn", "Stone Slab Bottom", MaterialType.STONE ) {
            fromTemplate( "half-dn" ) { remapMaterials( 0:4 ) }
        }

        addType( 108, "stone-wall-n", "Stone Wall", MaterialType.STONE ) {
            fromTemplate( "wall-n" ) { remapMaterials( 0:4 ) }
        }
        addType( 109, "stone-wall-s", "Stone Wall", MaterialType.STONE ) {
            fromTemplate( "wall-s" ) { remapMaterials( 0:4 ) }
        }
        addType( 107, "stone-wall-e", "Stone Wall", MaterialType.STONE ) {
            fromTemplate( "wall-e" ) { remapMaterials( 0:4 ) }
        }
        addType( 106, "stone-wall-w", "Stone Wall", MaterialType.STONE ) {
            fromTemplate( "wall-w" ) { remapMaterials( 0:4 ) }
        }

        addType( 26, "stone-pillar-ne", "Stone Pillar", MaterialType.STONE ) {
            fromTemplate( "pillar-ne" ) { remapMaterials( 0:4 ) }
        }
        addType( 25, "stone-pillar-nw", "Stone Pillar", MaterialType.STONE ) {
            fromTemplate( "pillar-nw" ) { remapMaterials( 0:4 ) }
        }
        addType( 27, "stone-pillar-sw", "Stone Pillar", MaterialType.STONE ) {
            fromTemplate( "pillar-sw" ) { remapMaterials( 0:4 ) }
        }
        addType( 28, "stone-pillar-se", "Stone Pillar", MaterialType.STONE ) {
            fromTemplate( "pillar-se" ) { remapMaterials( 0:4 ) }
        }


        addType( 70, "stone-beam-dn-n", "Stone Beam Bottom", MaterialType.STONE ) {
            fromTemplate( "beam-dn-n" ) { remapMaterials( 0:4 ) }
        }
        addType( 71, "stone-beam-dn-s", "Stone Beam Bottom", MaterialType.STONE ) {
            fromTemplate( "beam-dn-s" ) { remapMaterials( 0:4 ) }
        }
        addType( 69, "stone-beam-dn-e", "Stone Beam Bottom", MaterialType.STONE ) {
            fromTemplate( "beam-dn-e" ) { remapMaterials( 0:4 ) }
        }
        addType( 68, "stone-beam-dn-w", "Stone Beam Bottom", MaterialType.STONE ) {
            fromTemplate( "beam-dn-w" ) { remapMaterials( 0:4 ) }
        }
        
        addType( 74, "stone-beam-up-n", "Stone Beam Bottom", MaterialType.STONE ) {
            fromTemplate( "beam-up-n" ) { remapMaterials( 0:4 ) }
        }
        addType( 75, "stone-beam-up-s", "Stone Beam Bottom", MaterialType.STONE ) {
            fromTemplate( "beam-up-s" ) { remapMaterials( 0:4 ) }
        }
        addType( 73, "stone-beam-up-e", "Stone Beam Bottom", MaterialType.STONE ) {
            fromTemplate( "beam-up-e" ) { remapMaterials( 0:4 ) }
        }
        addType( 72, "stone-beam-up-w", "Stone Beam Bottom", MaterialType.STONE ) {
            fromTemplate( "beam-up-w" ) { remapMaterials( 0:4 ) }
        }
         
        addType( 164, "stone-col", "Stone Column", MaterialType.STONE, 0, 
                 new CylinderFactory(104,104,0.5f) ); 
        addType( 165, "stone-col-half", "Stone Post", MaterialType.STONE, 0, 
                 new CylinderFactory(104,104,0.25f) );
                 
        addType( 366, "stone-spar-ns", "Spar", MaterialType.STONE, 0, 
                 new CylinderFactory2(104,104,0.5f,0.5f,new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0)) ); 
        addType( 367, "stone-spar-ew", "Spar", MaterialType.STONE, 0, 
                 new CylinderFactory2(104,104,0.5f,0.5f,new Quaternion().fromAngles(0, 0, FastMath.HALF_PI)) ); 
        addType( 364, "stone-spar-ns-half", "Spar", MaterialType.STONE, 0, 
                 new CylinderFactory2(104,104,0.25f,0.5f,new Quaternion().fromAngles(FastMath.HALF_PI, 0, 0)) ); 
        addType( 365, "stone-spar-ew-half", "Spar", MaterialType.STONE, 0, 
                 new CylinderFactory2(104,104,0.25f,0.5f,new Quaternion().fromAngles(0, 0, FastMath.HALF_PI)) ); 
                
        addType( 242, "stone-cone-up", "Stone Cone", MaterialType.STONE, 0, 
                 new ConeFactory(104,104,Direction.UP,1.2f,0.5f) ); 
        addType( 243, "stone-cone-dn", "Stone Cone", MaterialType.STONE, 0, 
                 new ConeFactory(104,104,Direction.DOWN,1.2f,0.5f) ); 
        addType( 244, "stone-spike-up", "Stone Spike", MaterialType.STONE, 0, 
                 new ConeFactory(104,104,Direction.UP,1.1f,0.25f) ); 
        addType( 245, "stone-spike-dn", "Stone Spike", MaterialType.STONE, 0, 
                 new ConeFactory(104,104,Direction.DOWN,1.1f,0.25f) );
                          
        addType( 139, "stone-angle-ne", "Stone Angle", MaterialType.STONE ) {
            fromTemplate( "angle-ne" ) { remapMaterials( 0:4, 100:104 ) }
        }
        addType( 136, "stone-angle-nw", "Stone Angle", MaterialType.STONE ) {
            fromTemplate( "angle-nw" ) { remapMaterials( 0:4, 100:104 ) }
        }
        addType( 137, "stone-angle-sw", "Stone Angle", MaterialType.STONE ) {
            fromTemplate( "angle-sw" ) { remapMaterials( 0:4, 100:104 ) }
        }
        addType( 138, "stone-angle-se", "Stone Angle", MaterialType.STONE ) {
            fromTemplate( "angle-se" ) { remapMaterials( 0:4, 100:104 ) }
        }
    }
       
    addToolGroup( "Stone Slopes" ) {    
        addType( 44, "stone-ramp-n", "Stone Slope", MaterialType.STONE ) {
            fromTemplate( "wedge-n" ) { remapMaterials( 0:4, 100:104 ) }
            down() { quad(0) { retexture(Direction.DOWN) } }
        }
        addType( 45, "stone-ramp-s", "Stone Slope", MaterialType.STONE ) {
            fromTemplate( "wedge-s" ) { remapMaterials( 0:4, 100:104 ) }   
            down() { quad(0) { retexture(Direction.DOWN) } }
        }      
        addType( 46, "stone-ramp-e", "Stone Slope", MaterialType.STONE ) {
            fromTemplate( "wedge-e" ) { remapMaterials( 0:4, 100:104 ) }   
            down() { quad(0) { retexture(Direction.DOWN) } }
        }      
        addType( 47, "stone-ramp-w", "Stone Slope", MaterialType.STONE ) {
            fromTemplate( "wedge-w" ) { remapMaterials( 0:4, 100:104 ) }   
            down() { quad(0) { retexture(Direction.DOWN) } }
        }

        addType( 76, "stone-ramp-up-n", "Stone Slope Bottom", MaterialType.STONE ) {
            fromTemplate( "wedge-up-n" ) { remapMaterials( 0:4, 100:104 ) }
        }
        addType( 77, "stone-ramp-up-s", "Stone Slope Bottom", MaterialType.STONE ) {
            fromTemplate( "wedge-up-s" ) { remapMaterials( 0:4, 100:104 ) }   
        }      
        addType( 78, "stone-ramp-up-e", "Stone Slope Bottom", MaterialType.STONE ) {
            fromTemplate( "wedge-up-e" ) { remapMaterials( 0:4, 100:104 ) }   
        }      
        addType( 79, "stone-ramp-up-w", "Stone Slope Bottom", MaterialType.STONE ) {
            fromTemplate( "wedge-up-w" ) { remapMaterials( 0:4, 100:104 ) }              
        }
        
        // half slopes(bottom)       
        addType( 328, "stone-half-ramp-n", "Sand Half Slope", MaterialType.STONE ) {
            fromTemplate( "half-wedge-n" ) { remapMaterials( 0:4, 100:104 ) }
        }

        addType( 329, "stone-half-ramp-s", "Sand Half Slope", MaterialType.STONE ) {
            fromTemplate( "half-wedge-s" ) { remapMaterials( 0:4, 100:104 ) }
        }
      
        addType( 330, "stone-half-ramp-e", "Sand Half Slope", MaterialType.STONE ) {
            fromTemplate( "half-wedge-e" ) { remapMaterials( 0:4, 100:104 ) }
        }
      
        addType( 331, "stone-half-ramp-w", "Sand Half Slope", MaterialType.STONE ) {
            fromTemplate( "half-wedge-w" )  { remapMaterials( 0:4, 100:104 ) }
        }
 
        // Half corner (bottom)       
        addType( 332, "stone-half-corner-ne", "Sand Half Crnr", MaterialType.STONE ) {
            fromTemplate( "half-corner-ne" ) { remapMaterials( 0:4, 100:104 ) }
        }

        addType( 333, "stone-half-corner-nw", "Sand Half Crnr", MaterialType.STONE ) {
            fromTemplate( "half-corner-nw" ) { remapMaterials( 0:4, 100:104 ) }
        }
      
        addType( 334, "stone-half-corner-sw", "Sand Half Crnr", MaterialType.STONE ) {
            fromTemplate( "half-corner-sw" ) { remapMaterials( 0:4, 100:104 ) }
        }
      
        addType( 335, "stone-half-corner-se", "Sand Half Crnr", MaterialType.STONE ) {
            fromTemplate( "half-corner-se" )  { remapMaterials( 0:4, 100:104 ) }
        }
        
        // half slopes(up)       
        addType( 336, "stone-half-ramp-up-n", "Sand Half Slope", MaterialType.STONE ) {
            fromTemplate( "half-wedge-up-n" ) { remapMaterials( 0:4, 100:104 ) }
        }

        addType( 337, "stone-half-ramp-up-s", "Sand Half Slope", MaterialType.STONE ) {
            fromTemplate( "half-wedge-up-s" ) { remapMaterials( 0:4, 100:104 ) }
        }
      
        addType( 338, "stone-half-ramp-up-e", "Sand Half Slope", MaterialType.STONE ) {
            fromTemplate( "half-wedge-up-e" ) { remapMaterials( 0:4, 100:104 ) }
        }
      
        addType( 339, "stone-half-ramp-up-w", "Sand Half Slope", MaterialType.STONE ) {
            fromTemplate( "half-wedge-up-w" )  { remapMaterials( 0:4, 100:104 ) }
        }

        // Half corner (top)       
        addType( 340, "stone-shallow-corner-ne", "Sand Half Crnr", MaterialType.STONE ) {
            fromTemplate( "shallow-corner-ne" ) { remapMaterials( 0:4, 100:104 ) }
        }

        addType( 341, "stone-shallow-corner-nw", "Sand Half Crnr", MaterialType.STONE ) {
            fromTemplate( "shallow-corner-nw" ) { remapMaterials( 0:4, 100:104 ) } 
        }

        addType( 342, "stone-shallow-corner-sw", "Sand Half Crnr", MaterialType.STONE ) {
            fromTemplate( "shallow-corner-sw" ) { remapMaterials( 0:4, 100:104 ) } 
        }
      
        addType( 343, "stone-shallow-corner-se", "Sand Half Crnr", MaterialType.STONE ) {
            fromTemplate( "shallow-corner-se" )  { remapMaterials( 0:4, 100:104 ) }
        }

        addType( 344, "stone-corner-ne", "Stone Outer Crnr", MaterialType.STONE ) {  
            fromTemplate( "slope-corner-ne" ) {  remapMaterials( 0:4, 100:104 ) }
        }                
        addType( 345, "stone-corner-nw", "Stone Outer Crnr", MaterialType.STONE ) {  
            fromTemplate( "slope-corner-nw" ) {  remapMaterials( 0:4, 100:104 ) }
        }                
        addType( 346, "stone-corner-sw", "Stone Outer Crnr", MaterialType.STONE ) { 
            fromTemplate( "slope-corner-sw" ) {  remapMaterials( 0:4, 100:104 ) }
        }                
        addType( 347, "stone-corner-se", "Stone Outer Crnr", MaterialType.STONE ) { 
            fromTemplate( "slope-corner-se" ) {  remapMaterials( 0:4, 100:104 ) }
        }

        addType( 348, "stone-hi-ramp-n", "Steep Stone Slope", MaterialType.STONE ) {
            fromTemplate( "hi-wedge-n" ) { remapMaterials( 0:4, 100:104 ) }
        }
        addType( 349, "stone-hi-ramp-s", "Steep Stone Slope", MaterialType.STONE ) {
            fromTemplate( "hi-wedge-s" ) { remapMaterials( 0:4, 100:104 ) }
        }      
        addType( 350, "stone-hi-ramp-e", "Steep Stone Slope", MaterialType.STONE ) {
            fromTemplate( "hi-wedge-e" ) { remapMaterials( 0:4, 100:104 ) }
        }      
        addType( 351, "stone-hi-ramp-w", "Steep Stone Slope", MaterialType.STONE ) {
            fromTemplate( "hi-wedge-w" ) { remapMaterials( 0:4, 100:104 ) }
        }
        
        addType( 352, "stone-hi-ramp-wide-n", "Steep Stone Slope", MaterialType.STONE ) {
            fromTemplate( "hi-wedge-wide-n" ) { remapMaterials( 0:4, 100:104 ) }
        }
        addType( 353, "stone-hi-ramp-wide-s", "Steep Stone Slope", MaterialType.STONE ) {
            fromTemplate( "hi-wedge-wide-s" ) { remapMaterials( 0:4, 100:104 ) }
        }      
        addType( 354, "stone-hi-ramp-wide-e", "Steep Stone Slope", MaterialType.STONE ) {
            fromTemplate( "hi-wedge-wide-e" ) { remapMaterials( 0:4, 100:104 ) }
        }      
        addType( 355, "stone-hi-ramp-wide-w", "Steep Stone Slope", MaterialType.STONE ) {
            fromTemplate( "hi-wedge-wide-w" ) { remapMaterials( 0:4, 100:104 ) }
        }
        
        addType( 356, "stone-hi-ramp-up-n", "Steep Slope Bottom", MaterialType.STONE ) {
            fromTemplate( "hi-wedge-up-n" ) { remapMaterials( 0:4, 100:104 ) }
        }
        addType( 357, "stone-hi-ramp-up-s", "Steep Slope Bottom", MaterialType.STONE ) {
            fromTemplate( "hi-wedge-up-s" ) { remapMaterials( 0:4, 100:104 ) }
        }      
        addType( 358, "stone-hi-ramp-up-e", "Steep Slope Bottom", MaterialType.STONE ) {
            fromTemplate( "hi-wedge-up-e" ) { remapMaterials( 0:4, 100:104 ) }
        }      
        addType( 359, "stone-hi-ramp-up-w", "Steep Slope Bottom", MaterialType.STONE ) {
            fromTemplate( "hi-wedge-up-w" ) { remapMaterials( 0:4, 100:104 ) }
        }
        
        addType( 360, "stone-hi-ramp-wide-up-n", "Steep Slope Bottom", MaterialType.STONE ) {
            fromTemplate( "hi-wedge-wide-up-n" ) { remapMaterials( 0:4, 100:104 ) }
        }
        addType( 361, "stone-hi-ramp-wide-up-s", "Steep Slope Bottom", MaterialType.STONE ) {
            fromTemplate( "hi-wedge-wide-up-s" ) { remapMaterials( 0:4, 100:104 ) }
        }      
        addType( 362, "stone-hi-ramp-wide-up-e", "Steep Slope Bottom", MaterialType.STONE ) {
            fromTemplate( "hi-wedge-wide-up-e" ) { remapMaterials( 0:4, 100:104 ) }
        }      
        addType( 363, "stone-hi-ramp-wide-up-w", "Steep Slope Bottom", MaterialType.STONE ) {
            fromTemplate( "hi-wedge-wide-up-w" ) { remapMaterials( 0:4, 100:104 ) }
        }
        
    }
 
    addToolGroup( "Cobble" ) {    
        addType( 43, "cobble", "Cobble", MaterialType.COBBLE, 0, new CubeFactory(5) );

        addType( 263, "cobble-half-dn", "Half Cobble", MaterialType.COBBLE ) {
            fromTemplate( "half-dn" ) { remapMaterials( 0:5 ) }
        }

        addType( 23, "cobble-wall-n", "Cobble Wall", MaterialType.COBBLE ) {
            fromTemplate( "wall-n" ) { remapMaterials( 0:5 ) }
        }
        addType( 24, "cobble-wall-s", "Cobble Wall", MaterialType.COBBLE ) {
            fromTemplate( "wall-s" ) { remapMaterials( 0:5 ) }
        }
        addType( 22, "cobble-wall-e", "Cobble Wall", MaterialType.COBBLE ) {
            fromTemplate( "wall-e" ) { remapMaterials( 0:5 ) }
        }
        addType( 21, "cobble-wall-w", "Cobble Wall", MaterialType.COBBLE ) {
            fromTemplate( "wall-w" ) { remapMaterials( 0:5 ) }
        }

        addType( 151, "cobble-angle-ne", "Cobble Angle", MaterialType.COBBLE ) {
            fromTemplate( "angle-ne" ) { remapMaterials( 0:5, 100:105 ) }
        }
        addType( 148, "cobble-angle-nw", "Cobble Angle", MaterialType.COBBLE ) {
            fromTemplate( "angle-nw" ) { remapMaterials( 0:5, 100:105 ) }
        }
        addType( 149, "cobble-angle-sw", "Cobble Angle", MaterialType.COBBLE ) {
            fromTemplate( "angle-sw" ) { remapMaterials( 0:5, 100:105 ) }
        }
        addType( 150, "cobble-angle-se", "Cobble Angle", MaterialType.COBBLE ) {
            fromTemplate( "angle-se" ) { remapMaterials( 0:5, 100:105 ) }
        }
    }

    addToolGroup( "Mortared Rock" ) {    
        addType( 368, "mortrock", "Mortared Rock", MaterialType.MORTARED_ROCK, 0, new CubeFactory(37) );

        addType( 369, "mortrock-half-dn", "Half Mrt Rock", MaterialType.MORTARED_ROCK ) {
            fromTemplate( "half-dn" ) { remapMaterials( 0:37 ) }
        }

        addType( 370, "mortrock-wall-n", "Mrt Rock Wall", MaterialType.MORTARED_ROCK ) {
            fromTemplate( "wall-n" ) { remapMaterials( 0:37 ) }
        }
        addType( 371, "mortrock-wall-s", "Mrt Rock Wall", MaterialType.MORTARED_ROCK ) {
            fromTemplate( "wall-s" ) { remapMaterials( 0:37 ) }
        }
        addType( 372, "mortrock-wall-e", "Mrt Rock Wall", MaterialType.MORTARED_ROCK ) {
            fromTemplate( "wall-e" ) { remapMaterials( 0:37 ) }
        }
        addType( 373, "mortrock-wall-w", "Mrt Rock Wall", MaterialType.MORTARED_ROCK ) {
            fromTemplate( "wall-w" ) { remapMaterials( 0:37 ) }
        }

        addType( 374, "mortrock-angle-ne", "Mrt Rock Angle", MaterialType.MORTARED_ROCK ) {
            fromTemplate( "angle-ne" ) { remapMaterials( 0:37, 100:137 ) }
        }
        addType( 375, "mortrock-angle-nw", "Mrt Rock Angle", MaterialType.MORTARED_ROCK ) {
            fromTemplate( "angle-nw" ) { remapMaterials( 0:37, 100:137 ) }
        }
        addType( 376, "mortrock-angle-sw", "Mrt Rock Angle", MaterialType.MORTARED_ROCK ) {
            fromTemplate( "angle-sw" ) { remapMaterials( 0:37, 100:137 ) }
        }
        addType( 377, "mortrock-angle-se", "Mrt Rock Angle", MaterialType.MORTARED_ROCK ) {
            fromTemplate( "angle-se" ) { remapMaterials( 0:37, 100:137 ) }
        }
        
        addType( 378, "mortrock-ramp-n", "Mrt Rock Slope", MaterialType.MORTARED_ROCK ) {
            fromTemplate( "wedge-n" ) { remapMaterials( 0:37, 100:137 ) }
            down() { quad(0) { retexture(Direction.DOWN) } }
            east() { triangle(0) { retexture(Direction.EAST) } }
            west() { triangle(0) { retexture(Direction.WEST) } }
        }
        addType( 379, "mortrock-ramp-s", "Mrt Rock Slope", MaterialType.MORTARED_ROCK ) {
            fromTemplate( "wedge-s" ) { remapMaterials( 0:37, 100:137 ) }   
            down() { quad(0) { retexture(Direction.DOWN) } }
            east() { triangle(0) { retexture(Direction.EAST) } }
            west() { triangle(0) { retexture(Direction.WEST) } }
        }      
        addType( 380, "mortrock-ramp-e", "Mrt Rock Slope", MaterialType.MORTARED_ROCK ) {
            fromTemplate( "wedge-e" ) { remapMaterials( 0:37, 100:137 ) }   
            down() { quad(0) { retexture(Direction.DOWN) } }
            north() { triangle(0) { retexture(Direction.NORTH) } }
            south() { triangle(0) { retexture(Direction.SOUTH) } }
        }      
        addType( 381, "mortrock-ramp-w", "Mrt Rock Slope", MaterialType.MORTARED_ROCK ) {
            fromTemplate( "wedge-w" ) { remapMaterials( 0:37, 100:137 ) }   
            down() { quad(0) { retexture(Direction.DOWN) } }
            north() { triangle(0) { retexture(Direction.NORTH) } }
            south() { triangle(0) { retexture(Direction.SOUTH) } }
        }
    }

    addToolGroup( "Wood Slopes" ) {
    
        addType( 33, "wood-ramp-n", "Wood Slope", MaterialType.WOOD ) {
            fromTemplate( "wedge-n" ) { remapMaterials( 0:11, 100:121 ) }
        }
        addType( 34, "wood-ramp-s", "Wood Slope", MaterialType.WOOD ) {
            fromTemplate( "wedge-s" ) { remapMaterials( 0:11, 100:121 ) }
        }      
        addType( 35, "wood-ramp-e", "Wood Slope", MaterialType.WOOD ) {
            fromTemplate( "wedge-e" ) { remapMaterials( 0:11, 100:121 ) }
        }      
        addType( 36, "wood-ramp-w", "Wood Slope", MaterialType.WOOD ) {
            fromTemplate( "wedge-w" ) { remapMaterials( 0:11, 100:121 ) }
        }

        addType( 219, "wood-corner-ne", "Wood Outer Crnr", MaterialType.WOOD ) {  
            fromTemplate( "slope-corner-ne" ) {  remapMaterials( 0:11, 100:121 ) }
        }                
        addType( 220, "wood-corner-nw", "Wood Outer Crnr", MaterialType.WOOD ) {  
            fromTemplate( "slope-corner-nw" ) {  remapMaterials( 0:11, 100:121 ) }
        }                
        addType( 221, "wood-corner-sw", "Wood Outer Crnr", MaterialType.WOOD ) { 
            fromTemplate( "slope-corner-sw" ) {  remapMaterials( 0:11, 100:121 ) }
        }                
        addType( 222, "wood-corner-se", "Wood Outer Crnr", MaterialType.WOOD ) { 
            fromTemplate( "slope-corner-se" ) {  remapMaterials( 0:11, 100:121 ) }
        }

        addType( 37, "wood-ramp-up-n", "Wood Slope Bottom", MaterialType.WOOD ) {
            fromTemplate( "wedge-up-n" ) { remapMaterials( 0:11, 100:121 ) }
        }
        addType( 38, "wood-ramp-up-s", "Wood Slope Bottom", MaterialType.WOOD ) {
            fromTemplate( "wedge-up-s" ) { remapMaterials( 0:11, 100:121 ) }   
        }      
        addType( 39, "wood-ramp-up-e", "Wood Slope Bottom", MaterialType.WOOD ) {
            fromTemplate( "wedge-up-e" ) { remapMaterials( 0:11, 100:121 ) }   
        }      
        addType( 40, "wood-ramp-up-w", "Wood Slope Bottom", MaterialType.WOOD ) {
            fromTemplate( "wedge-up-w" ) { remapMaterials( 0:11, 100:121 ) }   
        }

        addType( 286, "wood-hi-ramp-n", "Steep Wood Slope", MaterialType.WOOD ) {
            fromTemplate( "hi-wedge-n" ) { remapMaterials( 0:11, 100:121 ) }
        }
        addType( 287, "wood-hi-ramp-s", "Steep Wood Slope", MaterialType.WOOD ) {
            fromTemplate( "hi-wedge-s" ) { remapMaterials( 0:11, 100:121 ) }
        }      
        addType( 288, "wood-hi-ramp-e", "Steep Wood Slope", MaterialType.WOOD ) {
            fromTemplate( "hi-wedge-e" ) { remapMaterials( 0:11, 100:121 ) }
        }      
        addType( 289, "wood-hi-ramp-w", "Steep Wood Slope", MaterialType.WOOD ) {
            fromTemplate( "hi-wedge-w" ) { remapMaterials( 0:11, 100:121 ) }
        }
        
        addType( 290, "wood-hi-ramp-wide-n", "Steep Wood Slope", MaterialType.WOOD ) {
            fromTemplate( "hi-wedge-wide-n" ) { remapMaterials( 0:11, 100:121 ) }
        }
        addType( 291, "wood-hi-ramp-wide-s", "Steep Wood Slope", MaterialType.WOOD ) {
            fromTemplate( "hi-wedge-wide-s" ) { remapMaterials( 0:11, 100:121 ) }
        }      
        addType( 292, "wood-hi-ramp-wide-e", "Steep Wood Slope", MaterialType.WOOD ) {
            fromTemplate( "hi-wedge-wide-e" ) { remapMaterials( 0:11, 100:121 ) }
        }      
        addType( 293, "wood-hi-ramp-wide-w", "Steep Wood Slope", MaterialType.WOOD ) {
            fromTemplate( "hi-wedge-wide-w" ) { remapMaterials( 0:11, 100:121 ) }
        }
        
        addType( 294, "wood-hi-ramp-up-n", "Steep Slope Bottom", MaterialType.WOOD ) {
            fromTemplate( "hi-wedge-up-n" ) { remapMaterials( 0:11, 100:121 ) }
        }
        addType( 295, "wood-hi-ramp-up-s", "Steep Slope Bottom", MaterialType.WOOD ) {
            fromTemplate( "hi-wedge-up-s" ) { remapMaterials( 0:11, 100:121 ) }
        }      
        addType( 296, "wood-hi-ramp-up-e", "Steep Slope Bottom", MaterialType.WOOD ) {
            fromTemplate( "hi-wedge-up-e" ) { remapMaterials( 0:11, 100:121 ) }
        }      
        addType( 297, "wood-hi-ramp-up-w", "Steep Slope Bottom", MaterialType.WOOD ) {
            fromTemplate( "hi-wedge-up-w" ) { remapMaterials( 0:11, 100:121 ) }
        }
        
        addType( 298, "wood-hi-ramp-wide-up-n", "Steep Slope Bottom", MaterialType.WOOD ) {
            fromTemplate( "hi-wedge-wide-up-n" ) { remapMaterials( 0:11, 100:121 ) }
        }
        addType( 299, "wood-hi-ramp-wide-up-s", "Steep Slope Bottom", MaterialType.WOOD ) {
            fromTemplate( "hi-wedge-wide-up-s" ) { remapMaterials( 0:11, 100:121 ) }
        }      
        addType( 300, "wood-hi-ramp-wide-up-e", "Steep Slope Bottom", MaterialType.WOOD ) {
            fromTemplate( "hi-wedge-wide-up-e" ) { remapMaterials( 0:11, 100:121 ) }
        }      
        addType( 301, "wood-hi-ramp-wide-up-w", "Steep Slope Bottom", MaterialType.WOOD ) {
            fromTemplate( "hi-wedge-wide-up-w" ) { remapMaterials( 0:11, 100:121 ) }
        }
    } 
 

    addToolGroup( "Shingles" ) { 
        addType( 254, "shingles-ramp-n", "Shingles", MaterialType.SHINGLES ) {
            fromTemplate( "wedge-n" ) { remapMaterials( 0:11, 100:135 ) }
        }
        addType( 255, "shingles-ramp-s", "Shingles", MaterialType.SHINGLES ) {
            fromTemplate( "wedge-s" ) { remapMaterials( 0:11, 100:135 ) }
        }      
        addType( 256, "shingles-ramp-e", "Shingles", MaterialType.SHINGLES ) {
            fromTemplate( "wedge-e" ) { remapMaterials( 0:11, 100:135 ) }
        }      
        addType( 257, "shingles-ramp-w", "Shingles", MaterialType.SHINGLES ) {
            fromTemplate( "wedge-w" ) { remapMaterials( 0:11, 100:135 ) }
        }

        addType( 258, "shingles-corner-ne", "Shingles Crnr", MaterialType.SHINGLES ) {
            fromTemplate( "slope-corner-ne" ) {  remapMaterials( 0:11, 100:135 ) }
        }                
        addType( 259, "shingles-corner-nw", "Shingles Crnr", MaterialType.SHINGLES ) {
            fromTemplate( "slope-corner-nw" ) {  remapMaterials( 0:11, 100:135 ) }
        }                
        addType( 260, "shingles-corner-sw", "Shingles Crnr", MaterialType.SHINGLES ) {
            fromTemplate( "slope-corner-sw" ) {  remapMaterials( 0:11, 100:135 ) }
        }                
        addType( 261, "shingles-corner-se", "Shingles Crnr", MaterialType.SHINGLES ) {
            fromTemplate( "slope-corner-se" ) {  remapMaterials( 0:11, 100:135 ) }
        }
                        
        addType( 278, "shingles-hi-ramp-n", "Steep Shingles", MaterialType.SHINGLES ) {
            fromTemplate( "hi-wedge-n" ) { remapMaterials( 0:11, 100:135 ) }
        }
        addType( 279, "shingles-hi-ramp-s", "Steep Shingles", MaterialType.SHINGLES ) {
            fromTemplate( "hi-wedge-s" ) { remapMaterials( 0:11, 100:135 ) }
        }      
        addType( 280, "shingles-hi-ramp-e", "Steep Shingles", MaterialType.SHINGLES ) {
            fromTemplate( "hi-wedge-e" ) { remapMaterials( 0:11, 100:135 ) }
        }      
        addType( 281, "shingles-hi-ramp-w", "Steep Shingles", MaterialType.SHINGLES ) {
            fromTemplate( "hi-wedge-w" ) { remapMaterials( 0:11, 100:135 ) }
        }
        
        addType( 282, "shingles-hi-ramp-wide-n", "Steep Shingles", MaterialType.SHINGLES ) {
            fromTemplate( "hi-wedge-wide-n" ) { remapMaterials( 0:11, 100:135 ) }
        }
        addType( 283, "shingles-hi-ramp-wide-s", "Steep Shingles", MaterialType.SHINGLES ) {
            fromTemplate( "hi-wedge-wide-s" ) { remapMaterials( 0:11, 100:135 ) }
        }      
        addType( 284, "shingles-hi-ramp-wide-e", "Steep Shingles", MaterialType.SHINGLES ) {
            fromTemplate( "hi-wedge-wide-e" ) { remapMaterials( 0:11, 100:135 ) }
        }      
        addType( 285, "shingles-hi-ramp-wide-w", "Steep Shingles", MaterialType.SHINGLES ) {
            fromTemplate( "hi-wedge-wide-w" ) { remapMaterials( 0:11, 100:135 ) }
        }
   }                
      
}




 /*       
1, "Dirt", MaterialType.DIRT, 0, 0 ),
2, "Grass", MaterialType.GRASS, 0, 1, 2, 0 ),
3, "Sand", MaterialType.SAND, 0, 3 )
4, "Stone", MaterialType.STONE, 0, 4 ),
5, "Rock Capped", MaterialType.STONE, 0, 14, 15, 6 ),
6, "Rock", MaterialType.ROCK, 0, 6, 16, 6 ),
7, "Water", MaterialType.WATER, 1, 0.99f, 24, 24, 24, 24, 7, 24 ),
8, "Water Top", MaterialType.WATER, 1, 0.99f, 0.3f, 24, 24, 24, 24, 7, 24 )
9, "Tree Trunk", MaterialType.WOOD, 0, 8, 13, 13 ),
10, "Leaves", MaterialType.LEAVES, 2, 9 ),
11, "W&D Block", MaterialType.WADDLE, 0, 10 ),
12, "Wood Planks Bottom", MaterialType.WOOD, 0, 11, -0.3f ),
13, "W&D Wall", MaterialType.WADDLE, 0, 10, 0, 0, 0, 0.5f, 1.0f, 1.0f ),
14, "W&D Wall", MaterialType.WADDLE, 0, 10, 0.5f, 0, 0, 1f, 1.0f, 1.0f ),
15, "W&D Wall", MaterialType.WADDLE, 0, 10, 0, 0, 0, 1.0f, 0.5f, 1.0f ),
16, "W&D Wall", MaterialType.WADDLE, 0, 10, 0, 0.5f, 0, 1.0f, 1.0f, 1.0f )
17, "Wood Pillar", MaterialType.WOOD, 0, 21, 0, 0, 0, 0.5f, 0.5f, 1.0f ),
18, "Wood Pillar", MaterialType.WOOD, 0, 21, 0.5f, 0, 0, 1.0f, 0.5f, 1.0f ),
19, "Wood Pillar", MaterialType.WOOD, 0, 21, 0, 0.5f, 0, 0.5f, 1.0f, 1.0f ),
20, "Wood Pillar", MaterialType.WOOD, 0, 21, 0.5f, 0.5f, 0, 1.0f, 1.0f, 1.0f ),
21, "Cobble Wall", MaterialType.COBBLE, 0, 5, 0, 0, 0, 0.5f, 1.0f, 1.0f ),
22, "Cobble Wall", MaterialType.COBBLE, 0, 5, 0.5f, 0, 0, 1f, 1.0f, 1.0f ),
23, "Cobble Wall", MaterialType.COBBLE, 0, 5, 0, 0, 0, 1.0f, 0.5f, 1.0f ),
24, "Cobble Wall", MaterialType.COBBLE, 0, 5, 0, 0.5f, 0, 1.0f, 1.0f, 1.0f ),
25, "Stone Pillar", MaterialType.STONE, 0, 4, 0, 0, 0, 0.5f, 0.5f, 1.0f ),
26, "Stone Pillar", MaterialType.STONE, 0, 4, 0.5f, 0, 0, 1.0f, 0.5f, 1.0f ),
27, "Stone Pillar", MaterialType.STONE, 0, 4, 0, 0.5f, 0, 0.5f, 1.0f, 1.0f ),
28, "Stone Pillar", MaterialType.STONE, 0, 4, 0.5f, 0.5f, 0, 1.0f, 1.0f, 1.0f ),
29, "Magic Light", MaterialType.EMPTY, -1,  
30, "Glass", MaterialType.GLASS, 4, 0.99f, 18 ),
31, "Wood Planks Top", MaterialType.WOOD, 0, 11, 0.0f, 0.0f, 0.7f, 1.0f, 1.0f, 1.0f ),
32, "Wood Planks", MaterialType.WOOD, 0, 11, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f ),
33, "Wood Slope", MaterialType.WOOD, 0, Direction.NORTH, Direction.DOWN, 11, 121, 21 ),
34, "Wood Slope", MaterialType.WOOD, 0, Direction.SOUTH, Direction.DOWN, 11, 121, 21 ),
35, "Wood Slope", MaterialType.WOOD, 0, Direction.EAST, Direction.DOWN, 11, 111, 11 ),
36, "Wood Slope", MaterialType.WOOD, 0, Direction.WEST, Direction.DOWN, 11, 111, 11 ),
37, "Wood Slope Bottom", MaterialType.WOOD, 0, Direction.NORTH, Direction.UP, 11, 21, 121 ),
38, "Wood Slope Bottom", MaterialType.WOOD, 0, Direction.SOUTH, Direction.UP, 11, 21, 121 ),
39, "Wood Slope Bottom", MaterialType.WOOD, 0, Direction.EAST, Direction.UP, 11, 11, 111 ),
40, "Wood Slope Bottom", MaterialType.WOOD, 0, Direction.WEST, Direction.UP, 11, 11, 111 ),
41, "Mineral Vein", MaterialType.STONE, 0, 19 )
42, "Black Marble", MaterialType.MARBLE, 0, 20 ),
43, "Cobble", MaterialType.COBBLE, 0, 5 ),
44, "Stone Slope", MaterialType.STONE, 0, Direction.NORTH, Direction.DOWN, 4,104,4 ),
45, "Stone Slope", MaterialType.STONE, 0, Direction.SOUTH, Direction.DOWN, 4,104,4 ),
46, "Stone Slope", MaterialType.STONE, 0, Direction.EAST, Direction.DOWN, 4,104,4 ),
47, "Stone Slope", MaterialType.STONE, 0, Direction.WEST, Direction.DOWN, 4,104,4 ),
48, "Dirt Slope", MaterialType.DIRT, 0, Direction.NORTH, Direction.DOWN, 0, 100, 0 ),
49, "Dirt Slope", MaterialType.DIRT, 0, Direction.SOUTH, Direction.DOWN, 0, 100, 0 ),
50, "Dirt Slope", MaterialType.DIRT, 0, Direction.EAST, Direction.DOWN, 0, 100, 0 ),
51, "Dirt Slope", MaterialType.DIRT, 0, Direction.WEST, Direction.DOWN, 0, 100, 0 )
52, "Wood Wall", MaterialType.WOOD, 0, 21, 0, 0, 0, 0.3f, 1.0f, 1.0f ),
53, "Wood Wall", MaterialType.WOOD, 0, 21, 0.7f, 0, 0, 1f, 1.0f, 1.0f ),
54, "Wood Wall", MaterialType.WOOD, 0, 21, 0, 0, 0, 1.0f, 0.3f, 1.0f ),
55, "Wood Wall", MaterialType.WOOD, 0, 21, 0, 0.7f, 0, 1.0f, 1.0f, 1.0f ),
56, "Glass Panel", MaterialType.GLASS, 0, 18,   0.20f, 0, 0,     0.30f, 1.0f, 1.0f,  0.99f ),
57, "Glass Panel", MaterialType.GLASS, 0, 18,   0.70f, 0, 0,     0.80f, 1.0f, 1.0f,  0.99f ),
58, "Glass Panel", MaterialType.GLASS, 0, 18,   0, 0.20f, 0,     1.0f, 0.30f, 1.0f,  0.99f ),
59, "Glass Panel", MaterialType.GLASS, 0, 18,   0, 0.70f, 0,     1.0f, 0.80f, 1.0f,  0.99f )
60, "Wood Beam Bottom", MaterialType.WOOD, 0, 11, 21, 21, 0, 0, 0,      0.5f, 1.0f, 0.5f ),
61, "Wood Beam Bottom", MaterialType.WOOD, 0, 11, 21, 21, 0.5f, 0, 0,   1.0f, 1.0f, 0.5f ),
62, "Wood Beam Bottom", MaterialType.WOOD, 0, 11, 0, 0.0f, 0,   1.0f, 0.5f, 0.5f ),
63, "Wood Beam Bottom", MaterialType.WOOD, 0, 11, 0f, 0.5f, 0,  1.0f, 1.0f, 0.5f ),
64, "Wood Beam Top", MaterialType.WOOD, 0, 11, 21, 21, 0, 0, 0.5f,      0.5f, 1.0f, 1.0f ),
65, "Wood Beam Top", MaterialType.WOOD, 0, 11, 21, 21, 0.5f, 0, 0.5f,   1.0f, 1.0f, 1.0f ),
66, "Wood Beam Top", MaterialType.WOOD, 0, 11, 0, 0.0f, 0.5f,   1.0f, 0.5f, 1.0f ),
67, "Wood Beam Top", MaterialType.WOOD, 0, 11, 0f, 0.5f, 0.5f,  1.0f, 1.0f, 1.0f ),
68, "Stone Beam Bottom", MaterialType.STONE, 0, 4, 0, 0, 0,      0.5f, 1.0f, 0.5f ),
69, "Stone Beam Bottom", MaterialType.STONE, 0, 4, 0.5f, 0, 0,   1.0f, 1.0f, 0.5f ),
70, "Stone Beam Bottom", MaterialType.STONE, 0, 4, 0, 0.0f, 0,   1.0f, 0.5f, 0.5f ),
71, "Stone Beam Bottom", MaterialType.STONE, 0, 4, 0f, 0.5f, 0,  1.0f, 1.0f, 0.5f ),
72, "Stone Beam Top", MaterialType.STONE, 0, 4, 0, 0, 0.5f,      0.5f, 1.0f, 1.0f ),
73, "Stone Beam Top", MaterialType.STONE, 0, 4, 0.5f, 0, 0.5f,   1.0f, 1.0f, 1.0f ),
74, "Stone Beam Top", MaterialType.STONE, 0, 4, 0, 0.0f, 0.5f,   1.0f, 0.5f, 1.0f ),
75, "Stone Beam Top", MaterialType.STONE, 0, 4, 0f, 0.5f, 0.5f,  1.0f, 1.0f, 1.0f ),
76, "Stone Slope Bottom", MaterialType.STONE, 0, Direction.NORTH, Direction.UP, 4,4,104 ),
77, "Stone Slope Bottom", MaterialType.STONE, 0, Direction.SOUTH, Direction.UP, 4,4,104 ),
78, "Stone Slope Bottom", MaterialType.STONE, 0, Direction.EAST, Direction.UP, 4,4,104 ),
79, "Stone Slope Bottom", MaterialType.STONE, 0, Direction.WEST, Direction.UP, 4,4,104 ),
80, "Stone Slab Top", MaterialType.STONE, 0, 4, 0.0f, 0.0f, 0.5f, 1.0f, 1.0f, 1.0f ),
81, "Stone Slab Bottom", MaterialType.STONE, 0, 4, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.5f ),
82, "Tall Grass", MaterialType.FLORA, 0, 0.99f, 22 ),
83, "Medium Grass", MaterialType.FLORA, 0, 0.75f, 22 ),
84, "Short Grass", MaterialType.FLORA, 0, 0.5f, 22 ),
85, "Log", MaterialType.WOOD, 0, 8, 8,    13, 13, 23, 23 ),
86, "Log", MaterialType.WOOD, 0, 23, 23,  23, 23, 13, 13 ),
87, "Wood Planks-90", MaterialType.WOOD, 0, 21, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f ),
88, "Wood Planks Top-90", MaterialType.WOOD, 0, 21, 0.0f, 0.0f, 0.7f, 1.0f, 1.0f, 1.0f ),
89, "Wood Planks Bottom-90", MaterialType.WOOD, 0, 21, -0.3f ),
90, "Trunk Slope", MaterialType.WOOD, 0, Direction.NORTH, Direction.DOWN, 23, 108, 8 ),
91, "Trunk Slope", MaterialType.WOOD, 0, Direction.SOUTH, Direction.DOWN, 23, 108, 8 ),
92, "Trunk Slope", MaterialType.WOOD, 0, Direction.EAST, Direction.DOWN, 23, 123, 23 ),
93, "Trunk Slope", MaterialType.WOOD, 0, Direction.WEST, Direction.DOWN, 23, 123, 23 ),
94, "Trunk Slope Bottom", MaterialType.WOOD, 0, Direction.NORTH, Direction.UP, 23, 8, 108 ),
95, "Trunk Slope Bottom", MaterialType.WOOD, 0, Direction.SOUTH, Direction.UP, 23, 8, 108 ),
96, "Trunk Slope Bottom", MaterialType.WOOD, 0, Direction.EAST, Direction.UP, 23, 23, 123 ),
97, "Trunk Slope Bottom", MaterialType.WOOD, 0, Direction.WEST, Direction.UP, 23, 23, 123 ),
98, "Leaves Slope Bottom", MaterialType.LEAVES, 2, Direction.NORTH, Direction.UP, 9, 9, 109 ),
99, "Leaves Slope Bottom", MaterialType.LEAVES, 2, Direction.SOUTH, Direction.UP, 9, 9, 109 ),
100, "Leaves Slope Bottom", MaterialType.LEAVES, 2, Direction.EAST, Direction.UP, 9, 9, 109 ),
101, "Leaves Slope Bottom", MaterialType.LEAVES, 2, Direction.WEST, Direction.UP, 9, 9, 109 ),
102, "Leaves Slope", MaterialType.LEAVES, 2, Direction.NORTH, Direction.DOWN, 9, 109, 9 ),
103, "Leaves Slope", MaterialType.LEAVES, 2, Direction.SOUTH, Direction.DOWN, 9, 109, 9 ),
104, "Leaves Slope", MaterialType.LEAVES, 2, Direction.EAST, Direction.DOWN, 9, 109, 9 ),
105, "Leaves Slope", MaterialType.LEAVES, 2, Direction.WEST, Direction.DOWN, 9, 109, 9 ),
106, "Stone Wall", MaterialType.STONE, 0, 4, 0, 0, 0, 0.5f, 1.0f, 1.0f ),
107, "Stone Wall", MaterialType.STONE, 0, 4, 0.5f, 0, 0, 1f, 1.0f, 1.0f ),
108, "Stone Wall", MaterialType.STONE, 0, 4, 0, 0, 0, 1.0f, 0.5f, 1.0f ),
109, "Stone Wall", MaterialType.STONE, 0, 4, 0, 0.5f, 0, 1.0f, 1.0f, 1.0f ),
110, "Blk Mrbl Slab Top", MaterialType.MARBLE, 0, 20, 0.0f, 0.0f, 0.5f, 1.0f, 1.0f, 1.0f ),
111, "Blk Mrbl Slab Bottom", MaterialType.MARBLE, 0, 20, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.5f ),
112, "Blk Mrbl Wall", MaterialType.MARBLE, 0, 20, 0, 0, 0, 0.5f, 1.0f, 1.0f ),
113, "Blk Mrbl Wall", MaterialType.MARBLE, 0, 20, 0.5f, 0, 0, 1f, 1.0f, 1.0f ),
114, "Blk Mrbl Wall", MaterialType.MARBLE, 0, 20, 0, 0, 0, 1.0f, 0.5f, 1.0f ),
115, "Blk Mrbl Wall", MaterialType.MARBLE, 0, 20, 0, 0.5f, 0, 1.0f, 1.0f, 1.0f ),
116, "Blk Mrbl Pillar", MaterialType.MARBLE, 0, 20, 0, 0, 0, 0.5f, 0.5f, 1.0f ),
117, "Blk Mrbl Pillar", MaterialType.MARBLE, 0, 20, 0.5f, 0, 0, 1.0f, 0.5f, 1.0f ),
118, "Blk Mrbl Pillar", MaterialType.MARBLE, 0, 20, 0, 0.5f, 0, 0.5f, 1.0f, 1.0f ),
119, "Blk Mrbl Pillar", MaterialType.MARBLE, 0, 20, 0.5f, 0.5f, 0, 1.0f, 1.0f, 1.0f ),
120, "Blk Mrbl Beam Bottom", MaterialType.MARBLE, 0, 20, 0, 0, 0,      0.5f, 1.0f, 0.5f ),
121, "Blk Mrbl Beam Bottom", MaterialType.MARBLE, 0, 20, 0.5f, 0, 0,   1.0f, 1.0f, 0.5f ),
122, "Blk Mrbl Beam Bottom", MaterialType.MARBLE, 0, 20, 0, 0.0f, 0,   1.0f, 0.5f, 0.5f ),
123, "Blk Mrbl Beam Bottom", MaterialType.MARBLE, 0, 20, 0f, 0.5f, 0,  1.0f, 1.0f, 0.5f ),
124, "Blk Mrbl Beam Top", MaterialType.MARBLE, 0, 20, 0, 0, 0.5f,      0.5f, 1.0f, 1.0f ),
125, "Blk Mrbl Beam Top", MaterialType.MARBLE, 0, 20, 0.5f, 0, 0.5f,   1.0f, 1.0f, 1.0f ),
126, "Blk Mrbl Beam Top", MaterialType.MARBLE, 0, 20, 0, 0.0f, 0.5f,   1.0f, 0.5f, 1.0f ),
127, "Blk Mrbl Beam Top", MaterialType.MARBLE, 0, 20, 0f, 0.5f, 0.5f,  1.0f, 1.0f, 1.0f ),
128, "Blk Mrbl Slope", MaterialType.MARBLE, 0, Direction.NORTH, Direction.DOWN, 20, 120, 20 ),
129, "Blk Mrbl Slope", MaterialType.MARBLE, 0, Direction.SOUTH, Direction.DOWN, 20, 120, 20 ),
130, "Blk Mrbl Slope", MaterialType.MARBLE, 0, Direction.EAST, Direction.DOWN, 20, 120, 20 ),
131, "Blk Mrbl Slope", MaterialType.MARBLE, 0, Direction.WEST, Direction.DOWN, 20, 120, 20 ),
132, "Blk Mrbl Slope Bottom", MaterialType.MARBLE, 0, Direction.NORTH, Direction.UP, 20, 20, 120 ),
133, "Blk Mrbl Slope Bottom", MaterialType.MARBLE, 0, Direction.SOUTH, Direction.UP, 20, 20, 120 ),
134, "Blk Mrbl Slope Bottom", MaterialType.MARBLE, 0, Direction.EAST, Direction.UP, 20, 20, 120 ),
135, "Blk Mrbl Slope Bottom", MaterialType.MARBLE, 0, Direction.WEST, Direction.UP, 20, 20, 120 ),
136, "Stone Angle", MaterialType.STONE, 0, new AngleFactory( Direction.NORTH, Direction.WEST, 4, 104 ) ),                 
137, "Stone Angle", MaterialType.STONE, 0, new AngleFactory( Direction.WEST, Direction.SOUTH, 4, 104 ) ),                 
138, "Stone Angle", MaterialType.STONE, 0, new AngleFactory( Direction.SOUTH, Direction.EAST, 4, 104 ) ),                 
139, "Stone Angle", MaterialType.STONE, 0, new AngleFactory( Direction.EAST, Direction.NORTH, 4, 104 ) ),
140, "Rock Angle", MaterialType.ROCK, 0, new AngleFactory( Direction.NORTH, Direction.WEST, 6, 106 ) ),                 
141, "Rock Angle", MaterialType.ROCK, 0, new AngleFactory( Direction.WEST, Direction.SOUTH, 6, 106 ) ),                 
142, "Rock Angle", MaterialType.ROCK, 0, new AngleFactory( Direction.SOUTH, Direction.EAST, 6, 106 ) ),                 
143, "Rock Angle", MaterialType.ROCK, 0, new AngleFactory( Direction.EAST, Direction.NORTH, 6, 106 ) ),                 
144, "Rock Capped Angle", MaterialType.ROCK, 0, new AngleFactory( Direction.NORTH, Direction.WEST, 14, 15, 6, 106 ) ),                 
145, "Rock Capped Angle", MaterialType.ROCK, 0, new AngleFactory( Direction.WEST, Direction.SOUTH, 14, 15, 6, 106 ) ),                 
146, "Rock Capped Angle", MaterialType.ROCK, 0, new AngleFactory( Direction.SOUTH, Direction.EAST, 14, 15, 6, 106 ) ),                 
147, "Rock Capped Angle", MaterialType.ROCK, 0, new AngleFactory( Direction.EAST, Direction.NORTH, 14, 15, 6, 106 ) )                 
148, "Cobble Angle", MaterialType.COBBLE, 0, new AngleFactory( Direction.NORTH, Direction.WEST, 5, 105 ) ),                 
149, "Cobble Angle", MaterialType.COBBLE, 0, new AngleFactory( Direction.WEST, Direction.SOUTH, 5, 105 ) ),                 
150, "Cobble Angle", MaterialType.COBBLE, 0, new AngleFactory( Direction.SOUTH, Direction.EAST, 5, 105 ) ),                 
151, "Cobble Angle", MaterialType.COBBLE, 0, new AngleFactory( Direction.EAST, Direction.NORTH, 5, 105 ) )                 
152, "Leaves Angle", MaterialType.LEAVES, 2, new AngleFactory( Direction.NORTH, Direction.WEST, 9, 109 ) ),                 
153, "Leaves Angle", MaterialType.LEAVES, 2, new AngleFactory( Direction.WEST, Direction.SOUTH, 9, 109 ) ),                 
154, "Leaves Angle", MaterialType.LEAVES, 2, new AngleFactory( Direction.SOUTH, Direction.EAST, 9, 109 ) ),                 
155, "Leaves Angle", MaterialType.LEAVES, 2, new AngleFactory( Direction.EAST, Direction.NORTH, 9, 109 ) ),                 
156, "Blk Mrbl Angle", MaterialType.MARBLE, 0, new AngleFactory( Direction.NORTH, Direction.WEST, 20, 120 ) ),                 
157, "Blk Mrbl Angle", MaterialType.MARBLE, 0, new AngleFactory( Direction.WEST, Direction.SOUTH, 20, 120 ) ),                 
158, "Blk Mrbl Angle", MaterialType.MARBLE, 0, new AngleFactory( Direction.SOUTH, Direction.EAST, 20, 120 ) ),                 
159, "Blk Mrbl Angle", MaterialType.MARBLE, 0, new AngleFactory( Direction.EAST, Direction.NORTH, 20, 120 ) ),
160, "Leaves", MaterialType.LEAVES, 3, new TreeLeafFactory(25) ),
161, "Trunk", MaterialType.WOOD, 0, new CylinderFactory(108,113,0.5f) ), 
162, "Wood Column", MaterialType.WOOD, 0, new CylinderFactory(128,128,0.5f) ), 
163, "Blk Marble Column", MaterialType.MARBLE, 0, new CylinderFactory(120,120,0.5f) ), 
164, "Stone Column", MaterialType.STONE, 0, new CylinderFactory(104,104,0.5f) ), 
165, "Stone Post", MaterialType.STONE, 0, new CylinderFactory(104,104,0.25f) ),
166, "Wood Post", MaterialType.WOOD, 0, new CylinderFactory(128,128,0.25f) ) 
167, "Blk Marble Post", MaterialType.MARBLE, 0, new CylinderFactory(120,120,0.25f) ),
168, "Trunk", MaterialType.WOOD, 0, new CylinderFactory(108,113,0.25f) ), 
169, "Branch", MaterialType.WOOD, 0, new CylinderFactory2(108,113,0.25f,Direction.EAST) ), 
170, "Branch", MaterialType.WOOD, 0, new CylinderFactory2(108,113,0.25f,Direction.NORTH) ),
171, "Branch", MaterialType.WOOD, 0, new CylinderFactory2(108,113,0.25f,Direction.WEST) ), 
172, "Branch", MaterialType.WOOD, 0, new CylinderFactory2(108,113,0.25f,Direction.SOUTH) ),
173, "Leaves 2", MaterialType.LEAVES, 3, new TreeLeafFactory(26) ),
174, "White Marble", MaterialType.MARBLE, 0, 27 ),
175, "Wht Mrbl Slab Top", MaterialType.MARBLE, 0, 27, 0.0f, 0.0f, 0.5f, 1.0f, 1.0f, 1.0f ),
176, "Wht Mrbl Slab Bottom", MaterialType.MARBLE, 0, 27, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.5f ),
177, "Wht Mrbl Wall", MaterialType.MARBLE, 0, 27, 0, 0, 0, 0.5f, 1.0f, 1.0f ),
178, "Wht Mrbl Wall", MaterialType.MARBLE, 0, 27, 0.5f, 0, 0, 1f, 1.0f, 1.0f ),
179, "Wht Mrbl Wall", MaterialType.MARBLE, 0, 27, 0, 0, 0, 1.0f, 0.5f, 1.0f ),
180, "Wht Mrbl Wall", MaterialType.MARBLE, 0, 27, 0, 0.5f, 0, 1.0f, 1.0f, 1.0f ),
181, "Wht Mrbl Pillar", MaterialType.MARBLE, 0, 27, 0, 0, 0, 0.5f, 0.5f, 1.0f ),
182, "Wht Mrbl Pillar", MaterialType.MARBLE, 0, 27, 0.5f, 0, 0, 1.0f, 0.5f, 1.0f ),
183, "Wht Mrbl Pillar", MaterialType.MARBLE, 0, 27, 0, 0.5f, 0, 0.5f, 1.0f, 1.0f ),
184, "Wht Mrbl Pillar", MaterialType.MARBLE, 0, 27, 0.5f, 0.5f, 0, 1.0f, 1.0f, 1.0f ),
185, "Wht Mrbl Beam Bottom", MaterialType.MARBLE, 0, 27, 0, 0, 0,      0.5f, 1.0f, 0.5f ),
186, "Wht Mrbl Beam Bottom", MaterialType.MARBLE, 0, 27, 0.5f, 0, 0,   1.0f, 1.0f, 0.5f ),
187, "Wht Mrbl Beam Bottom", MaterialType.MARBLE, 0, 27, 0, 0.0f, 0,   1.0f, 0.5f, 0.5f ),
188, "Wht Mrbl Beam Bottom", MaterialType.MARBLE, 0, 27, 0f, 0.5f, 0,  1.0f, 1.0f, 0.5f ),
189, "Wht Mrbl Beam Top", MaterialType.MARBLE, 0, 27, 0, 0, 0.5f,      0.5f, 1.0f, 1.0f ),
190, "Wht Mrbl Beam Top", MaterialType.MARBLE, 0, 27, 0.5f, 0, 0.5f,   1.0f, 1.0f, 1.0f ),
191, "Wht Mrbl Beam Top", MaterialType.MARBLE, 0, 27, 0, 0.0f, 0.5f,   1.0f, 0.5f, 1.0f ),
192, "Wht Mrbl Beam Top", MaterialType.MARBLE, 0, 27, 0f, 0.5f, 0.5f,  1.0f, 1.0f, 1.0f ),
193, "Wht Mrbl Slope", MaterialType.MARBLE, 0, Direction.NORTH, Direction.DOWN, 27, 127, 27 ),
194, "Wht Mrbl Slope", MaterialType.MARBLE, 0, Direction.SOUTH, Direction.DOWN, 27, 127, 27 ),
195, "Wht Mrbl Slope", MaterialType.MARBLE, 0, Direction.EAST, Direction.DOWN, 27, 127, 27 ),
196, "Wht Mrbl Slope", MaterialType.MARBLE, 0, Direction.WEST, Direction.DOWN, 27, 127, 27 ),
197, "Wht Mrbl Slope Bottom", MaterialType.MARBLE, 0, Direction.NORTH, Direction.UP, 27, 27, 127 ),
198, "Wht Mrbl Slope Bottom", MaterialType.MARBLE, 0, Direction.SOUTH, Direction.UP, 27, 27, 127 ),
199, "Wht Mrbl Slope Bottom", MaterialType.MARBLE, 0, Direction.EAST, Direction.UP, 27, 27, 127 ),
200, "Wht Mrbl Slope Bottom", MaterialType.MARBLE, 0, Direction.WEST, Direction.UP, 27, 27, 127 ),
201, "Wht Mrbl Angle", MaterialType.MARBLE, 0, new AngleFactory( Direction.NORTH, Direction.WEST, 27, 127 ) ),                 
202, "Wht Mrbl Angle", MaterialType.MARBLE, 0, new AngleFactory( Direction.WEST, Direction.SOUTH, 27, 127 ) ),                 
203, "Wht Mrbl Angle", MaterialType.MARBLE, 0, new AngleFactory( Direction.SOUTH, Direction.EAST, 27, 127 ) ),                 
204, "Wht Mrbl Angle", MaterialType.MARBLE, 0, new AngleFactory( Direction.EAST, Direction.NORTH, 27, 127 ) ),
205, "Wht Marble Column", MaterialType.MARBLE, 0, new CylinderFactory(127,127,0.5f) ), 
206, "Wht Marble Post", MaterialType.MARBLE, 0, new CylinderFactory(127,127,0.25f) ),
207, "Thatch", MaterialType.LEAVES, 0, new ThatchFactory(29, 30, Direction.EAST) ),
208, "Thatch", MaterialType.LEAVES, 0, new ThatchFactory(29, 30, Direction.WEST) ),
209, "Thatch", MaterialType.LEAVES, 0, new ThatchFactory(29, 30, Direction.NORTH) ),
210, "Thatch", MaterialType.LEAVES, 0, new ThatchFactory(29, 30, Direction.SOUTH) ),
211, "Large Flame", MaterialType.EMPTY, -1,  
212, "Fire", MaterialType.EMPTY, -1,  
213, "Inferno", MaterialType.EMPTY, -1,  
214, "Small Flame", MaterialType.EMPTY, -1,  
215, "Thatch Corner", MaterialType.LEAVES, 0, new ThatchCornerFactory(29, 30, Direction.EAST) ), 
216, "Thatch Corner", MaterialType.LEAVES, 0, new ThatchCornerFactory(29, 30, Direction.NORTH) ),
217, "Thatch Corner", MaterialType.LEAVES, 0, new ThatchCornerFactory(29, 30, Direction.WEST) ), 
218, "Thatch Corner", MaterialType.LEAVES, 0, new ThatchCornerFactory(29, 30, Direction.SOUTH) ) 
219, "Wood Outer Crnr", MaterialType.WOOD, 0, new OuterCornerFactory( Direction.EAST, 111, 121, 11, 21 ) ),
220, "Wood Outer Crnr", MaterialType.WOOD, 0, new OuterCornerFactory( Direction.NORTH, 111, 121, 11, 21 ) ),
221, "Wood Outer Crnr", MaterialType.WOOD, 0, new OuterCornerFactory( Direction.WEST, 111, 121, 11, 21 ) ),
222, "Wood Outer Crnr", MaterialType.WOOD, 0, new OuterCornerFactory( Direction.SOUTH, 111, 121, 11, 21 ) ),
223, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, 0) ),
224, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, FastMath.HALF_PI) ),
225, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, FastMath.PI) ),
226, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, FastMath.HALF_PI * 3) ),                
227, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, FastMath.QUARTER_PI) ),
228, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, FastMath.HALF_PI + FastMath.QUARTER_PI) ),
229, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, FastMath.PI + FastMath.QUARTER_PI) ),
230, "Pine Branch", MaterialType.LEAVES, 4, new PineBranchFactory(32, FastMath.HALF_PI * 3 + FastMath.QUARTER_PI) ),
231, "Pine Sapling", MaterialType.LEAVES, 4, new PineTopFactory(108, 32) )
232, "Wild Flowers", MaterialType.FLORA, -1,  
233, "Flower 1", MaterialType.FLORA, -1,  
234, "Flower 2", MaterialType.FLORA, -1,  
235, "Flower 3", MaterialType.FLORA, -1,  
236, "Brush Short", MaterialType.FLORA, -1,  
237, "Brush Tall", MaterialType.FLORA, -1,  
238, "Spike", MaterialType.WOOD, 0, new ConeFactory(134,113,Direction.UP,1.2f,0.5f) ), 
239, "Spike", MaterialType.WOOD, 0, new ConeFactory(134,113,Direction.DOWN,1.2f,0.5f) ), 
240, "Spike", MaterialType.WOOD, 0, new ConeFactory(134,113,Direction.UP,1.1f,0.25f) ), 
241, "Spike", MaterialType.WOOD, 0, new ConeFactory(134,113,Direction.DOWN,1.1f,0.25f) ), 
242, "Stone Cone", MaterialType.STONE, 0, new ConeFactory(104,104,Direction.UP,1.2f,0.5f) ), 
243, "Stone Cone", MaterialType.STONE, 0, new ConeFactory(104,104,Direction.DOWN,1.2f,0.5f) ), 
244, "Stone Spike", MaterialType.STONE, 0, new ConeFactory(104,104,Direction.UP,1.1f,0.25f) ), 
245, "Stone Spike", MaterialType.STONE, 0, new ConeFactory(104,104,Direction.DOWN,1.1f,0.25f) ) 
246, "Blk Marble Cone", MaterialType.MARBLE, 0, new ConeFactory(120,120,Direction.UP,1.2f,0.5f) ), 
247, "Blk Marble Cone", MaterialType.MARBLE, 0, new ConeFactory(120,120,Direction.DOWN,1.2f,0.5f) ), 
248, "Blk Marble Spike", MaterialType.MARBLE, 0, new ConeFactory(120,120,Direction.UP,1.1f,0.25f) ), 
249, "Blk Marble Spike", MaterialType.MARBLE, 0, new ConeFactory(120,120,Direction.DOWN,1.1f,0.25f) ) 
250, "Wht Marble Cone", MaterialType.MARBLE, 0, new ConeFactory(127,127,Direction.UP,1.2f,0.5f) ), 
251, "Wht Marble Cone", MaterialType.MARBLE, 0, new ConeFactory(127,127,Direction.DOWN,1.2f,0.5f) ), 
252, "Wht Marble Spike", MaterialType.MARBLE, 0, new ConeFactory(127,127,Direction.UP,1.1f,0.25f) ), 
253, "Wht Marble Spike", MaterialType.MARBLE, 0, new ConeFactory(127,127,Direction.DOWN,1.1f,0.25f) ) 
254, "Shingles", MaterialType.WOOD, 0, Direction.NORTH, Direction.DOWN, 11, 135, 21 ),
255, "Shingles", MaterialType.WOOD, 0, Direction.SOUTH, Direction.DOWN, 11, 135, 21, true ),
256, "Shingles", MaterialType.WOOD, 0, Direction.EAST, Direction.DOWN, 11, 136, 11 ),
257, "Shingles", MaterialType.WOOD, 0, Direction.WEST, Direction.DOWN, 11, 136, 11, true ),
258, "Shingles Crnr", MaterialType.WOOD, 0, new OuterCornerFactory( Direction.EAST, 136, 135, 11, 21 ) ),
259, "Shingles Crnr", MaterialType.WOOD, 0, new OuterCornerFactory( Direction.NORTH, 136, 135, 11, 21 ) ),
260, "Shingles Crnr", MaterialType.WOOD, 0, new OuterCornerFactory( Direction.WEST, 136, 135, 11, 21 ) ),
261, "Shingles Crnr", MaterialType.WOOD, 0, new OuterCornerFactory( Direction.SOUTH, 136, 135, 11, 21 ) )
*/

