package mythruna.script
/**
 *  ${Id}
 *
 *  Copyright (c) 2012, Paul Speed
 *  All rights reserved.
 */

import com.jme3.math.*;

import mythruna.Coordinates;
import mythruna.Direction;
import mythruna.es.*;
import mythruna.phys.*;
import mythruna.script.*;



action( group:"Objects", name:"Clone", type:ActionType.Object ) {

    // Need to figure out the blueprint for the object
    model = it[ModelInfo.class];
    if( model == null ) {
        warn( "Cloned object has no blueprint:" + target );
        console.echo( "Error cloning object." );
        return; 
    }

    blueprintId = model.getBlueprintId();

    blueprint = entities.createEntity();       
    blueprint << new BlueprintReference(blueprintId);
    blueprint << new InContainer(player, (byte)0);  

    console.echo( "Object cloned to blueprint palette." );    
}

action( group:"Objects", name:"Snap\nPosition", type:ActionType.Object ) {

    Position pos = it[Position];
    if( pos == null ) {
        warn( "Snapped entity has no position:" + target );
        console.echo( "Error snapping object." );
        return;
    }
    
    float x = pos.getLocation().x;
    float y = pos.getLocation().y;
    float z = pos.getLocation().z;
    x = Coordinates.worldToCell(x) + 0.5f;
    y = Coordinates.worldToCell(y) + 0.5f;
 
    it << new Position( new Vector3f(x,y,z), pos.getRotation() );
}

action( group:"Objects", name:"Snap\nRotation", type:ActionType.Object ) {

    Position pos = it[Position];
    if( pos == null ) {
        warn( "Snapped entity has no position:" + target );
        console.echo( "Error snapping object." );
        return;
    }
     
    // Convert the orientation into angles
    float[] angles = pos.getRotation().toAngles(null);
 
    float yAxis = angles[1];
    if( yAxis < 0 )
        yAxis += FastMath.TWO_PI;
    float part = yAxis % FastMath.HALF_PI;
    if( part > FastMath.QUARTER_PI )
        yAxis += FastMath.HALF_PI - part;
    else 
        yAxis -= part;
 
    Quaternion rot = new Quaternion();
    rot.fromAngles( 0, yAxis, 0 );
 
    it << new Position( pos.getLocation(), rot );           
}

action( group:"Objects", name:"Snap\nVertical", type:ActionType.Object ) {

    Position pos = it[Position];
    if( pos == null ) {
        warn( "Snapped entity has no position:" + target );
        console.echo( "Error snapping object." );
        return;
    }
    
    float x = pos.getLocation().x;
    float y = pos.getLocation().y;
    float z = pos.getLocation().z;
    z = Coordinates.worldToCell(z);
 
    it << new Position( new Vector3f(x,y,z), pos.getRotation() );
}

action( group:"Objects", name:"Snap\nAll", type:ActionType.Object ) {

    Position pos = it[Position];
    if( pos == null ) {
        warn( "Snapped entity has no position:" + target );
        console.echo( "Error snapping object." );
        return;
    }
     
    // Convert the orientation into angles
    float[] angles = pos.getRotation().toAngles(null);
 
    float yAxis = angles[1];
    if( yAxis < 0 )
        yAxis += FastMath.TWO_PI;
    float part = yAxis % FastMath.HALF_PI;
    if( part > FastMath.QUARTER_PI )
        yAxis += FastMath.HALF_PI - part;
    else 
        yAxis -= part;
 
    Quaternion rot = new Quaternion();
    rot.fromAngles( 0, yAxis, 0 );

    float x = pos.getLocation().x;
    float y = pos.getLocation().y;
    float z = pos.getLocation().z;
    x = Coordinates.worldToCell(x) + 0.5f;
    y = Coordinates.worldToCell(y) + 0.5f;
    z = Coordinates.worldToCell(z);
 
    it << new Position( new Vector3f(x,y,z), rot );           
}

action( group:"Objects", name:"Delete", type:ActionType.Object ) {

    // Should check for permissions first...
    
    removePlaceable( it );
    console.echo( "Object deleted." );    
}

action( group:"Objects", name:"Snap", type:ActionType.Object ) {

    println "Snap action:" + it;
 
    refs = []
 
    refs += actions.getRef( "Objects", "Snap\nPosition" );
    refs += actions.getRef( "Objects", "Snap\nRotation" );
    refs += actions.getRef( "Objects", "Snap\nVertical" );
    refs += actions.getRef( "Objects", "Snap\nAll" );

    player << new RadialActions( player, it, "Object", it.name, refs );
    
    println "Created component:" + player[RadialActions.class];           
}

action( group:"Objects", name:"Make\nPhysical", type:ActionType.Object ) {

    // Need to figure out the blueprint for the object
    model = it[ModelInfo.class];
    if( model == null ) {
        warn( "Object has no blueprint:" + target );
        console.echo( "Error modifying object." );
        return; 
    }

    blueprintId = model.getBlueprintId();
 
    filter = new FieldFilter( BodyTemplate.class, "blueprintId", blueprintId );
    template = entities.findEntity( filter );

    println "Loaded body template:" + template;
    
    if( template == null ) {
        println "Creating body template for:" + blueprintId;
    
        // Then create one
        template = entities.createEntity();
        template << new BodyTemplate(blueprintId);
        
        blueprint = world.getBlueprint( blueprintId );
        println "Loading blueprint:" + blueprint;
        
        BodyUtils.addMassProperties( entities, template, blueprint );    
    }

    // Now copy the body properties over
    it << template[Volume.class];
    it << new Mass(0);

}

action( group:"Objects", name:"Make\nMobile", type:ActionType.Object ) {

    // Need to figure out the blueprint for the object
    model = it[ModelInfo.class];
    if( model == null ) {
        warn( "Object has no blueprint:" + target );
        console.echo( "Error modifying object." );
        return; 
    }

    blueprintId = model.getBlueprintId();
 
    filter = new FieldFilter( BodyTemplate.class, "blueprintId", blueprintId );
    template = entities.findEntity( filter );

    println "Loaded body template:" + template;
    
    if( template == null ) {
        println "Creating body template for:" + blueprintId;
    
        // Then create one
        template = entities.createEntity();
        template << new BodyTemplate(blueprintId);
        
        blueprint = world.getBlueprint( blueprintId );
        println "Loading blueprint:" + blueprint;
        
        BodyUtils.addMassProperties( entities, template, blueprint );    
    }

    // Now copy the body properties over
    it << template[Volume.class];
    it << template[Mass.class];    
    it << template[MassProperties.class];    

}

action( group:"Objects", name:"Make\nHologram", type:ActionType.Object ) {

    entities.removeComponent( it, Volume.class );
    entities.removeComponent( it, Mass.class );
    entities.removeComponent( it, MassProperties.class );
}

action( group:"Objects", name:"Make\nStatic", type:ActionType.Object ) {

    entities.removeComponent( it, MassProperties.class );
    it << new Mass(0);
}

action( group:"Objects", name:"Physics", type:ActionType.Object ) {

    println "Physics action:" + it;
 
    refs = [] 
    refs += actions.getRef( "Objects", "Make\nHologram" );
    
    if( it[MassProperties.class] == null ) {
        refs += actions.getRef( "Objects", "Make\nMobile" );
    } else {
        refs += actions.getRef( "Objects", "Make\nStatic" );
    }        
    
    player << new RadialActions( player, it, "Object", it.name, refs );
    
    println "Created component:" + player[RadialActions.class];           
}

entityAction( name:"Alternate Action" ) {
    
    println "Default alternate action:" + it;
 
    refs = []
 
    refs += actions.getRef( "Objects", "Clone" );

    Position pos = it[Position.class];
    
    if( perms.canMoveObject( pos.location, it ) ) {    
        refs += actions.getRef( "Objects", "Snap" );
    }
    
    if( perms.canChangeObject( pos.location, it ) ) {    
        if( it[Mass.class] == null ) {
            refs += actions.getRef( "Objects", "Make\nPhysical" );        
        } else {
            refs += actions.getRef( "Objects", "Physics" );
        }
    }        
    
    if( perms.canRemoveObject( pos.location, it ) ) {    
        refs += actions.getRef( "Objects", "Delete" );
    }

    player << new RadialActions( player, it, "Object", it.name, refs );
    
    println "Created component:" + player[RadialActions.class];           
}

