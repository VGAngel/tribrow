package mythruna.script
/**
 *  ${Id}
 *
 *  Copyright (c) 2012, Paul Speed
 *  All rights reserved.
 *
 *
 *  Scripts to setup the player entity.  This hooks
 *  into the player join event but we maybe should have
 *  an event that gets fired earlier.
 */

import mythruna.es.*;

// Standard blueprints we try to give to every player
DEFAULT_BLUEPRINTS = [
                1299456980967l,
                1299457293967l,
                1299457813326l,
                1299458315576l,
                1308905675317l,
                1308034318670l,
                1308034600717l,
                1308127658326l,
                1308574507379l,
                1308128399810l,
                1308291932560l,
                1308292196982l,
                1308905881848l            
                ]

EntityId createBlueprintItem( EntityId player, long blueprintId ) {

    def item = entities.createEntity();
    
    item << new BlueprintReference(blueprintId);
    item << new InContainer(player, (byte)0);

    return item;
}    

on( [playerJoined] ) {
    type, event ->
    
    println "Setting up player entity:" + player;

    // Make sure the entity name matches the player name
    if( player.name != playerData.get( "characterInfo.name" ) )
        {
        //echo( "Setting player entity name to:" + playerData.get( "characterInfo.name" ) ); 
        player << new Name(playerData.get( "characterInfo.name" ));
        }

    def items = entities.findEntities( new FieldFilter( InContainer.class, "parentId", player ),
                                       InContainer.class, BlueprintReference.class );        
    def blueprints = items.collect { 
        it[BlueprintReference.class].blueprintId 
    }

    //blueprints.each { println "player has blueprint:" + it }

    for( long id : DEFAULT_BLUEPRINTS ) {
    
        if( blueprints.contains(id) ) {
            println "Player already has:" + id;
            continue;
        }
        
        println "Adding ID:" + id;
        createBlueprintItem( player, id );
    }
        

}    

/*
    public static void setupPlayerEntity( EntityId entity, World world, EntityActionEnvironment env )
    {   
        // Really need a cheaper way to grab one-offs
        EntityData ed = world.getEntityData();
        EntitySet items = ed.getEntities( new FieldFilter( InContainer.class, "parentId", entity ),
                                          InContainer.class );
        
        try
            {               
System.out.println( "Items in inventory:" + items );               
            if( items.size() == 0 )
                {
                // Give the player all of the current blueprints... if it's
                // more than a certain amount then we'll cap them in the editor
                //for( Long bpId : world.getBlueprintIds() )
                // No, now we just use preset list... maybe we'll fix this
                // later and have it copy everything attacheed to a template
                // user.
                for( long bpId : DEFAULT_BLUEPRINTS )
                    {
                    EntityAction action = new CreateBlueprintItemAction( bpId );
                    env.execute( action, null );
                    }
                }
            }
        finally
            {
            items.release();
            }                            
    }
}
*/
