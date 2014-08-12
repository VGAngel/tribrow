package mythruna.script
/**
 *  ${Id}
 *
 *  Copyright (c) 2012, Paul Speed
 *  All rights reserved.
 */

import mythruna.Direction;
import mythruna.es.*;
import mythruna.script.*;

entityAction( name:"Create Stronghold" ) {   
    source, target ->   

    newOwner = player;
    if( target != null ) {
        newOwner = target.entity;
    }
    
    println "Create Stronghold on:" + newOwner; 
       
    nameComponent = newOwner[Name.class];
    name = "Player";
    if( nameComponent != null )
        name = nameComponent.name;
    
    println "Creating stronghold for:" + name;
    stronghold = entities.createEntity();
    stronghold << new OwnedBy(newOwner);
    stronghold << ClaimType.createStandard(ClaimType.TYPE_STRONGHOLD, null);
    stronghold << new Name(name + "'s Stronghold");
    stronghold << new InContainer(newOwner, (byte)0);
}

entityAction( name:"Create Town" ) {   
    source, target ->   

    newOwner = player;
    if( target != null ) {
        newOwner = target.entity;
    }
    
    println "Create Town on:" + newOwner;
    
    nameComponent = newOwner[Name.class];
    name = "Player";
    if( nameComponent != null )
        name = nameComponent.name;
    
    println "Creating town for:" + name;
    town = entities.createEntity();
    town << new OwnedBy(newOwner);
    town << ClaimType.createStandard(ClaimType.TYPE_TOWN, null);
    town << new Name(name + " Town");
    town << new InContainer(newOwner, (byte)0);
}    

entityAction( name:"Create City" ) {   
    source, target ->   

    newOwner = player;
    if( target != null ) {
        newOwner = target.entity;
    }
    
    println "Create City on:" + newOwner;
    
    nameComponent = newOwner[Name.class];
    name = "Player";
    if( nameComponent != null )
        name = nameComponent.name;
    
    println "Creating city for:" + name;
    city = entities.createEntity();
    city << new OwnedBy(newOwner);
    city << ClaimType.createStandard(ClaimType.TYPE_CITY, null);
    city << new Name(name + " City");
    city << new InContainer(newOwner, (byte)0);
}    



action( group:"Claims", name:"Info", type:ActionType.Object ) {
    
    println "Claims Info:" + it      
 
    //console.echo( "Hellow object:" + it );
     
    name = it[Name.class];
    println "name:" + name;
    ownedBy = it[OwnedBy.class];
    println "owner:" + ownedBy;
    ownerName = "";
    if( ownedBy.ownerId == player )
        ownerName = "You";
    else
        ownerName = ownedBy.ownerId[Name.class]?.name?:"Unknown";    
    
    console.echo( "Property:" + name.name + " owned by:" + ownerName );    
}

/*
-InContainer
    ...or...
-Position and ClaimArea( corners, etc. )

-Name
-MapStyle (scale, color, font)
-OwnedBy
-ClaimType( type, maxArea, parentClaim )

type: public, stronghold (player world claims), town/city, plot
*/

entityAction( name:"Place Claim", type:ActionType.Block ) {
    
    source, target ->

    println "Place claim:" + source + "  at:" + target;
    
    // We give it a position to be placed on the map...
    // but really we only care about the claim area for
    // claim permission processing.

    block = target.block;
    if( target.side != Direction.UP ) {
        console.echo( "Claims and plots must be placed on horizontal surfaces." );
        return;
    }
 
    block.z++;
 
    type = source[ClaimType.class];
    area = ClaimArea.createStandard( block, type );
 
    if( !perms.canPlaceClaim( block, area, source ) ) {
        console.echo( "You do not have permission to place a claim here." );
        return;
    } 
 
    // Is it a child plot or a world-level plot?
    if( type.isChild() ) {
        
        // This duplicates code from create plot... we could maybe
        // fix that by separating out a "place plot" that both
        // this and create could use

        // Make sure that the plot is within the bounds of the
        // town/city
        parentArea = type.parent[ClaimArea.class];
        if( !parentArea.contains(area) ) {
            console.echo( "Town/City plots must be placed within town/city borders." );
            return;
        }
    
        // Also need to make sure that it does not intersect with
        // siblings
        if( perms.intersectsSiblings( type.parent, area, null ) ) {
            console.echo( "Town/City plots cannot overlap." );
            return;
        }
    
    } else {   
        // See if the area fits where we are trying to place it
        if( perms.intersectsSiblings( null, area, null ) ) {
        
            // Try to trim it
            area = perms.trim( null, block, area, null );
            if( area == null ) {    
                console.echo( "This property is too close or inside another property." );
                return;
            }
            
            // Make sure the area still contains the block with appropriate
            // amount of buffer
            border = 5;
            if( type.isChild() )
                border = 1;
            
            if( !area.contains( block, border ) ) {
                console.echo( "This property is too close to another property." );
                return;
            }
        }
    }
     
    source << area;
    source << new Position(block.x + 0.75f, block.y + 0.75f, block.z);
    
    // And remove it from the player's inventory.
    entities.removeComponent( source, InContainer.class );
    
    console.echo( source[Name.class].name + " placed at:" + block.x + ", " + block.y + ", " + block.z );
}

entityAction( name:"Create Plot", type:ActionType.Block ) {
    
    source, target ->

    println "Create sub-plot for:" + source + "  at:" + target;

    block = target.block;
    if( target.side != Direction.UP ) {
        console.echo( "Claims and plots must be placed on horizontal surfaces." );
        return;
    }
    block.z++;

    parentType = source[ClaimType.class];
    
    type = ClaimType.createStandard(parentType.childType, source);
    childArea = ClaimArea.createStandard( block, type );

    // Make sure that the plot is within the bounds of the
    // town/city
    parentArea = source[ClaimArea.class];
    if( !parentArea.contains(childArea) ) {
        console.echo( "Town/City plots must be placed within town/city borders." );
        return;
    }
 
    // Also need to make sure that it does not intersect with
    // siblings
    if( perms.intersectsSiblings( source, childArea, null ) ) {
        console.echo( "Town/City plots cannot overlap." );
        return;
    }
 
    nameComponent = player[Name.class];
    name = "Player";
    if( nameComponent != null )
        name = nameComponent.name;
    
    println "Creating sub-plot for:" + name;
    plot = entities.createEntity();
    plot << new OwnedBy(player);
    plot << type;
    plot << new Name(name + "'s Lot");
    plot << childArea;
    plot << new Position(block.x + 0.75f, block.y + 0.75f, block.z);
    
    console.echo( plot[Name.class].name + " created at:" + block.x + ", " + block.y + ", " + block.z );
}

entityAction( name:"Give Plot", type:ActionType.Object ) {    
    source, target ->

    println "Give sub-plot for:" + source + "  to:" + target;

    targetPlayer = target.object;

    // See if it's a player we're giving it to... just to
    // double check
    uid = targetPlayer[UserId.class];
    if( uid == null ) {
        console.echo( "Properties can only be given to other players." );
        return;
    }

    // Create a plot just like regular except without location or area
    parentType = source[ClaimType.class];    
    type = ClaimType.createStandard(parentType.childType, source);

    name = targetPlayer[Name.class]?.name?:"Player";
    
    println "Creating sub-plot for:" + name;
    plot = entities.createEntity();
    plot << new OwnedBy(targetPlayer);
    plot << type;
    plot << new Name(name + "'s Lot");
    plot << new InContainer(targetPlayer,(byte)0);
    
    console.echo( "Gave sub-plot of:" + source[Name.class]?.name + " -> " 
                    + plot[Name.class].name + " to:" + name );
                    
    // Need to send a message to the target player also
    sendMessage( targetPlayer, "You have received a sub-plot for:" + source[Name.class]?.name );
}

entityAction( name:"Give Badge", type:ActionType.Object ) {    
    source, target ->

    println "Give badge for:" + source + "  to:" + target;

    targetPlayer = target.object;

    // See if it's a player we're giving it to... just to
    // double check
    uid = targetPlayer[UserId.class];
    if( uid == null ) {
        console.echo( "Badges can only be given to other players." );
        return;
    }

    type = source[ClaimType.class];
    if( type == null ) {
        console.echo( "Can only create badges for properties." );
        return;
    }
        
    name = targetPlayer[Name.class]?.name?:"Player";
    
    println "Creating badge for:" + name;
    
    badge = entities.createEntity();
    badge << new ClaimPermissions( source, 
                                   ClaimPermissions.BLOCK_REMOVE,
                                   ClaimPermissions.BLOCK_ADD,
                                   ClaimPermissions.OBJ_ADD,
                                   ClaimPermissions.OBJ_REMOVE,
                                   ClaimPermissions.OBJ_MOVE,
                                   ClaimPermissions.OBJ_CHANGE
                                 );
     
    badge << new InContainer(targetPlayer,(byte)0);
    
    console.echo( "Gave badge for:" + source[Name.class]?.name + " to:" + name );
                    
    // Need to send a message to the target player also
    sendMessage( targetPlayer, "You have received a badge for:" + source[Name.class]?.name );
}


entityAction( name:"Move Claim Marker", type:ActionType.Component ) {
    source, component ->
    
    pos = component.component;
    
    println "Update claim position:" + source + " to:" + pos;
 
    if( !perms.canMoveClaim(pos, source) )
        {
//println "Playerdata:" + playerData + "  grants:" + playerData.get( "grant" );         
        if( playerData == null || playerData.get( "grant.admin" ) == null )
            {
            // No message... just abort the move
            return;
            }
        println "Admin override.";            
        }
        
    // If the position is still within a buffered 
    // area of the claim, then allow it... otherwise don't.    
    area = source[ClaimArea.class];
    border = 5;
    if( source[ClaimType.class].parent != null )
        border = 1;
        
    if( !area.contains( pos.location, border ) )
        {
        println "Outside of bordered range, area:" + area + "  border:" + border;
        return;
        }
    
    source << pos;    
}                             

entityAction( name:"Update Claim Area", type:ActionType.Component ) {
    source, component ->
    
    area = component.component;
    
    println "Update claim area:" + source + "  to:" + area;
 
    // See if the area fits within the restrictions of the type
    type = source[ClaimType.class];
 
    println "area size:" + area.getAreaSize() + "  max area size:" + type.getMaxArea();  
    
    if( area.getAreaSize() > type.getMaxArea() )
        {
        println "Exceeds max area.";
        return;
        }
 
    if( area.deltaX < 2 || area.deltaY < 2 )
        {
        println "Trying to invert it or collapse it too small.";
        return;
        }
    
    // Check to see if we can make the change
    if( !perms.canMoveClaim( area, source ) )
        {
        if( playerData == null || playerData.get( "grant.admin" ) == null )
            {
            // No message... just abort the move
            return;
            }
        println "Admin override.";            
        }   
 
    // Make sure the claim marker is still in a valid location
    pos = source[Position.class];
    border = 5;
    if( source[ClaimType.class].parent != null )
        border = 1;
        
    if( !area.contains( pos.location, border ) ) {
        loc = pos.location;
        loc.x = Math.max( area.min.x + border, loc.x );
        loc.x = Math.min( area.min.x - border, loc.x );
        loc.y = Math.max( area.min.y + border, loc.y );
        loc.y = Math.min( area.min.y - border, loc.y );
        
        if( !area.contains( loc, border ) ) {
            // Just center it
            loc.x = area.min.x + area.deltaX * 0.5;
            loc.y = area.min.y + area.deltaY * 0.5;
        }
        
        // Make sure that z is still valid.
println "z before:" + loc.z;        
        loc.z = world.findEmptySpace( loc.x, loc.y, loc.z, 2, null );
println "z after:" + loc.z;        
        
        source << pos;           
    }   
 
    source << area;   
}


action( group:"Claims", name:"Rename", type:ActionType.NameComponent ) {

    source, component ->
    
    println "Name:" + source + "   to:" + component.component;
 
    if( !perms.canChangeClaim(source) )
        {
        console.echo( "You do not have permission to rename this property." );
        return;
        }
 
    source << component.component;
}

action( group:"Claims", name:"Retrieve", type:ActionType.Object ) {

    println "Retrieve:" + it;
 
    // See if it is even a property
    if( it[ClaimType.class] == null ) {
        println "Object is not a property.";
        return;
    }
 
    // Are they allowed to remove it?
    if( !perms.canRemoveClaim( it ) ) {
        console.echo( "You do not have permission to remove this property." );
        return;
    }
 
    // See if it is a town and if it has sub-plots
    if( perms.hasSubPlots( it ) ) {
        console.echo( "Towns/Cities cannot be removed when they contain placed plots." );
        return;
    }
 
    // Pull it back into the player's inventory
    println "player:" + player;
    
    it << new InContainer( player, 0 );
    entities.removeComponent( it, ClaimArea.class );
    entities.removeComponent( it, Position.class );
    
    console.echo( "Retrieved:" + it[Name.class].name );
}

action( group:"Claims", name:"Remove", type:ActionType.Object ) {

    println "Remove:" + it;
 
    // See if it is even a property
    if( it[ClaimType.class] == null ) {
        println "Object is not a property.";
        return;
    }
 
    // Are they allowed to remove it?
    if( !perms.canRemoveClaim( it ) ) {
        console.echo( "You do not have permission to remove this property." );
        return;
    }
 
    // See if it is a town and if it has sub-plots
    if( perms.hasSubPlots( it ) ) {
        console.echo( "Towns/Cities cannot be removed when they contain placed plots." );
        return;
    }
 
    name = it[Name.class];
    
    entities.removeComponent( it, ClaimArea.class );
    entities.removeComponent( it, Position.class );
    entities.removeComponent( it, InContainer.class );
    entities.removeComponent( it, Name.class );
    entities.removeComponent( it, ClaimType.class );
    entities.removeComponent( it, OwnedBy.class );
    
    console.echo( "Removed:" + name.name );
}

action( group:"Badges", name:"Revoke", type:ActionType.Object ) {

    println "Remove badge:" + it;
 
    // See if it is even a property
    badge = it[ClaimPermissions.class]; 
    if( it[ClaimPermissions.class] == null ) {
        println "Object is not a badge.";
        return;
    }
 
    // Double check that this player is the owner of the property
    // referenced by the badge.
    if( !perms.isOwner( badge.claimId ) ) {
        println "Wrong owner.";
        console.echo( "You are not the owner of this badge's property." );
    }     
 
    entities.removeComponent( it, ClaimPermissions.class );
    entities.removeComponent( it, InContainer.class );
    entities.removeComponent( it, OwnedBy.class );
    
    console.echo( "Removed badge:" + badge );
}

entityAction( name:"Close Radial" ) {

    //println "Closing radial for:" + player;
    
    entities.removeComponent( player, RadialActions.class );

}

entityAction( name:"Close Context Menu" ) {

    //println "Closing context for:" + player;
    
    entities.removeComponent( player, ContextActions.class );
}

entityAction( name:"Alternate Action" ) {
 
    println "Property alternate action:" + it;
 
    type = it[ClaimType.class];
    if( type != null ) {
    
        name = it.name;
        
        refs = []
    
        // Add some claim actions
        refs += actions.getRef( "Claims", "Info" );
        if( perms.isOwner(it) ) {
            refs += actions.getRef( "Claims", "Rename" );
 
            removeAction = "Retrieve";           
            if( type.isChild() ) {
                
                // If it's a child and the owner is different than
                // the owner of the parent then it is still a retrieve.
                // Otherwise, we won't gum up the owner's inventory and
                // we'll do a remove
                if( it[OwnedBy.class].ownerId == type.parent[OwnedBy.class].ownerId ) {            
                    refs += actions.getRef( "Claims", "Remove" );
                }
            } 
            
            refs += actions.getRef( "Claims", removeAction );
        }
        
        player << new RadialActions( player, it, "Property", name, refs );
                            
    } else {

        println "Executing original alternate action";    
        original(it);
    }         
     
    println "Created component:" + player[RadialActions.class];           
}

entityAction( name:"Row Click", type:ActionType.Object ) {
    source, obj ->
    
    println "Row click action:" + source;
    
    nameComponent = source[Name.class];
    title = "Object";
    if( nameComponent != null )
        title = nameComponent.name;
 
    refs = []
    
    type = source[ClaimType.class];
    badge = source[ClaimPermissions.class];
    
    name = "Unknown Type";    
    if( type != null ) {
        name = "Plot Actions";
        
        // Add some claim actions
        //refs += actions.getRef( "Claims", "Info" );
        if( perms.isOwner(source) ) {
            refs += actions.getRef( "Claims", "Rename" ); 
            refs += actions.getRef( "Claims", "Retrieve" );
            if( type.isChild() ) {
                
                // If it's a child and the owner is different than
                // the owner of the parent then it is still a retrieve.
                // Otherwise, we won't gum up the owner's inventory and
                // we'll do a remove
                if( source[OwnedBy.class].ownerId == type.parent[OwnedBy.class].ownerId ) {            
                    refs += actions.getRef( "Claims", "Remove" );
                }
            } 
            
        }            
    } else if( badge != null ) {
        name = "Badge Actions";
        
        // Add some claim actions
        refs += actions.getRef( "Badges", "Revoke" );
    }         
 
    player << new ContextActions( player, source, obj.location, name, title, refs );
    
    println "Created component:" + player[ContextActions.class];           
}

on( [playerJoined] ) {
    type, event ->
    
    println "Adding property tools to player:" + player;
 
    refs = []

/*
    ToolActions existing = player[ToolActions.class];

    refs += actions.getRefs( "Claims" );
    
    println "Refs:" + refs;
    
    player << new ToolActions(refs, existing)
    */
 
    nameComponent = player[Name.class];
    name = "Player";
    if( nameComponent != null )
        name = nameComponent.name;
 
    println "Player name:" + name + "   id:" + player;
       
    // And for now, see if the player already has some claims and if
    // not give them.
    claims = entities.getEntities( new FieldFilter( OwnedBy.class, "ownerId",
                                                    player ),
                                   ClaimType.class, OwnedBy.class );
    println "Existing claims:" + claims;
    
    claims.each {
        println "Name:" + it.id[Name.class];
        println "Location:" + it.id[ClaimArea.class];
        println "Type:" + it.get(ClaimType.class);
        println "Owner:" + it.get(OwnedBy.class);
    }
    
    noClaims = claims.isEmpty();
    claims.release();

    //runEntityAction( "Create Stronghold" );    
    //runEntityAction( "Create Town" );
            
    if( noClaims ) {
        
        // Create some
        runEntityAction( "Create Stronghold" );
        /*println "Creating stronghold for:" + name;
        stronghold = entities.createEntity();
        stronghold << new OwnedBy(player);
        stronghold << ClaimType.createStandard(ClaimType.TYPE_STRONGHOLD, null);
        stronghold << new Name(name + "'s Stronghold");
        stronghold << new InContainer(player, (byte)0);*/

        //runEntityAction( "Create Town" );        
        /*println "Creating town for:" + name;
        town = entities.createEntity();
        town << new OwnedBy(player);
        town << ClaimType.createStandard(ClaimType.TYPE_TOWN, null);
        town << new Name(name + " Town");
        town << new InContainer(player, (byte)0);*/
        
    }
    
    if( playerData != null ) {          
        if( playerData.get( "grant.admin" ) != null ) {
                    
            addShellCommand( shell, "giveprop", "Gives a stronghold, town, or city to another player.", 
                             null ) {
    
                String[] parms = it.split( " " );
                if( it.length() == 0 || parms.length < 2 ) {
                    console.echo( "Specify a client ID and a type: stronghold, town, or city" );
                    return;
                }
        
                String to = parms[0];
                toConn = null;
                toPlayer = null;
        
                if( to.isNumber() ) {
                    toConn = findConnection( to.toInteger() );
                    if( toConn == null ) {
                        console.echo( "Connection ID not found:" + to );
                        return;
                    }
                    toPlayer = toConn.getAttribute( "entityId" );              
                } else {
                    console.echo( "Operation not yet supported." );
                    return;
                }
 
                String propertyType = parms[1];
                if( "stronghold".equalsIgnoreCase(propertyType) ) {
                    runEntityAction( "Create Stronghold", new EntityParameter(toPlayer) );
                    console.echo( "Creating stronghold on:" + toPlayer );
                } else if( "town".equalsIgnoreCase(propertyType) ) {
                    console.echo( "Creating town on:" + toPlayer );
                    runEntityAction( "Create Town", new EntityParameter(toPlayer) );
                } else if( "city".equalsIgnoreCase(propertyType) ) {
                    console.echo( "Creating city on:" + toPlayer );
                    runEntityAction( "Create City", new EntityParameter(toPlayer) );
                }
       
            }
        }
    }
                                       
}    

