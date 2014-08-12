/*
 * $Id: default.groovy 2171 2012-04-14 22:12:08Z pspeed $
 *
 *  This is the root init script for the default block libraries
 *  and the only one called by the block script system by default.
 *  It should setup any separate scripts that will construct the
 *  actual default block libraries. 
 *
 * Copyright (c) 2012, Paul Speed
 * All rights reserved.
 */


import com.jme3.math.FastMath;

import mythruna.BlockTypeIndex;
import mythruna.Direction;
import mythruna.MaterialType;
import mythruna.ShapeIndex;
import mythruna.geom.*;
import mythruna.phys.collision.*;

/*BlockTypeIndex.addGroup( "Minerals 2",
    BlockTypeIndex.addBlockType( 400, "Mineral Vein 1", MaterialType.STONE, 0, 19 ),
    BlockTypeIndex.addBlockType( 401, "Mineral Vein 2", MaterialType.STONE, 0, 19 )
);*/

scripts.initializeNext( "/scripts/blocks/base-templates.groovy" );
scripts.initializeNext( "/scripts/blocks/base-blocks.groovy" );


