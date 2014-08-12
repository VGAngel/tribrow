package mythruna.script
/**
 *  ${Id}
 *
 *  Copyright (c) 2012, Paul Speed
 *  All rights reserved.
 */

import mythruna.*;

on( [cellChanged] ) {
    type, event ->
 
//println "Cell hook:" + event.cell + " = " + event.newType; 
    Vector3i loc = event.cell;
    
    if( event.newType == 0 ) {
        
        // Block is being removed
        
        /*if( event.oldType == 7 || event.oldType == 8 ) {
            event.revert();
            return;
        }*/ 
    
        def above = getCellType( loc.x, loc.y, loc.z + 1 );
        def aboveType = toMaterial(above);
    
        if( aboveType == MaterialType.FLORA ) {
            //println "Flora";
        
            // Remove it
            setCellType( loc.x, loc.y, loc.z + 1, 0 );
        } else if( aboveType == MaterialType.WATER ) {
            // Set the new empty block's type to water
            event.newType = 7;
        } else {
            // check the cardinal directions
            for( int d = 0; d < 4; d++ ) {
                def beside = getCellType( loc.x + Direction.DIRS[d][0],
                                          loc.y + Direction.DIRS[d][1],
                                          loc.z + Direction.DIRS[d][2] );
                def besideType = toMaterial(beside);
                if( besideType == MaterialType.WATER ) {
                    event.newType = beside;
                    break;
                }     
            }
        }
    } else {
    
        def below = getCellType( loc.x, loc.y, loc.z - 1 );
        def belowType = toMaterial(below);    
 
        def blockType = toBlockType(event.newType);
        if( blockType.isSolid( Direction.DOWN ) && blockType.getTransparency(Direction.Z_AXIS) < 0.1 ) {
            //println "Need to check to see if grass is below us";
            if( belowType == MaterialType.GRASS ) {
                // Set it to dirt
                setCellType( loc.x, loc.y, loc.z - 1, 1 );
            }
        }   
    }   
}    

