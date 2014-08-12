/**
 *  ${Id}
 *
 *  Copyright (c) 2012, Paul Speed
 *  All rights reserved.
 *
 *  Just makes sure the other local tools are initialized.
 *  This makes it easier to setup since we can easily add
 *  tool types here without having to change core code.
 *
 *  The later-to-be-defined plug-in architecture may negate
 *  this need a bit.
 */
 
scripts.initializeNext( "/scripts/tools/default-tool-type.groovy" );


import mythruna.item.*;


/// Register some base actions that are used by the object
// action system itself and are not part of any particular tool

entityAction( name:"setLeftTool" ) {

    def HeldEntities held = player[HeldEntities.class];
    if( held == null ) {
        held = new HeldEntities(player, it.entity, null);
    } else {
        held = held.addLeft(it.entity);
    }
    
    player << held;
        
    //console.echo( "Set left tool to:" + it.entity );    
}

entityAction( name:"setRightTool" ) {

    def HeldEntities held = player[HeldEntities.class];
    if( held == null ) {
        held = new HeldEntities(player, null, it.entity);
    } else {
        held = held.addRight(it.entity);
    }
    
    player << held;
        
    //console.echo( "Set right tool to:" + it.entity );
}


