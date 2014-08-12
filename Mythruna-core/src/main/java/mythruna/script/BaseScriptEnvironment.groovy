package mythruna.script
/**
 *  ${Id}
 *
 *  Copyright (c) 2012, Paul Speed
 *  All rights reserved.
 */
 
import java.lang.reflect.*;
import com.jme3.math.*;
import com.jme3.network.*;

import org.progeeks.util.ObjectUtils;
import org.progeeks.util.log.Log;

import mythruna.*;
import mythruna.db.WorldDatabaseEvents;
import mythruna.es.*;
import mythruna.event.*;
import mythruna.phys.*;
import mythruna.script.*;

log = Log.getLog( "scripts" );

PlayerEvents.fields.each {
    if( Modifier.isStatic( it.getModifiers() ) ) {
        //println "Binding event type:" + it.name
        bindings.put( it.name, it.get(null) );
    }
}

WorldEvents.fields.each {
    if( Modifier.isStatic( it.getModifiers() ) ) {
        //println "Binding event type:" + it.name
        bindings.put( it.name, it.get(null) );
    }
}

WorldDatabaseEvents.fields.each {
    if( Modifier.isStatic( it.getModifiers() ) ) {
        //println "Binding event type:" + it.name
        bindings.put( it.name, it.get(null) );
    }
}

EntityId.metaClass {    
    leftShift { 
        EntityComponent c ->
        Object old = entities.getComponent( delegate, c.getType() );
        if( ObjectUtils.areEqual(old, c) )
            return; 
        entities.setComponent( delegate, c )  
    }
    getAt { Class c -> entities.getComponent( delegate, c ) }
    
    getName = { //propertyName ->
        //if (propertyName == 'name') { 
            Name name = entities.getComponent( delegate, Name.class );
            if( name == null )
                return null;
            return name.name;
        //} else { 
        //    delegate 
        //}
    }    
}

void warn( String msg ) {
    log.warn( msg );
}

EventListener on( EventType type, Closure doIt ) {

    // So... an issue here is that doIt is the same
    // instance every time.  So by resetting the delegate
    // we are sort of resetting the environment of every
    // other sub-closure that was created before us.
    // I think the only reason we avoid this in the action
    // code is because we set the delegate right before calling
    // the doIt and we get lucky.
    //
    // Cloning doIt here I don't think fixes the problem, either.
    // It's the closures that are created inside the event processing
    // that need an isolated environment.  We need a per-context
    // doIt in this case.  So maybe context can provide that.
    // Except that context stuff is not scripting specific.
    // 
    // For now, inside the event we can just clone it every time,
    // I guess.
    //
    // This should be moved into a special scipted event class so
    // that we don't have to do it all in the anonymous class here.
    // That code could be a little smarter about caching per-context
    // too. 

    def EventListener result = new EventListener() {
        public void newEvent( EventType eventType, Object o ) {            
            Closure toRun = doIt.clone();        
            toRun.setDelegate( o.getContext() );
            int parmCount = toRun.getMaximumNumberOfParameters();
            if( parmCount < 2 )
                toRun(o);
            else          
                toRun(eventType, o);
        }
    };
     
    eventDispatcher.addListener( type, result );
    
    return result;
}

EventListener on( List types, Closure doIt ) {
    EventListener l = new EventListener() {
        public void newEvent( EventType eventType, Object o ) {
            Closure toRun = doIt.clone();        
            toRun.setDelegate( o.getContext() );
            int parmCount = toRun.getMaximumNumberOfParameters();
            if( parmCount < 2 )
                toRun(o);
            else          
                toRun(eventType, o);
        }
    };
     
    types.each { type ->
        eventDispatcher.addListener( type, l ); 
    }
    
    return l;        
}

void removeHook( EventType type, EventListener hook ) {
    eventDispatcher.removeListener(type, hook);
}

void removeHook( List types, EventListener hook ) {
    types.each { type ->
        eventDispatcher.removeListener(type, hook);
    }
}

import mythruna.script.*;

/**
 *  Defines an action that can later be run by
 *  group and name.  The "group" and "name" arguments
 *  are required.  Other arguments will be passed directly
 *  through to the setters on the created ScriptedAction.
 */
ScriptedAction action( Map args, Closure doIt ) {
    action = new ScriptedAction( args.group, args.name, doIt );
    
    for( Map.Entry e : args ) {
        if( e.key == "group" || e.key == "name" )
            continue;            
        action[e.key] = e.value; 
    }
    
    actions.addAction( action );
    
    return action;
}

/**
 *  Defines an action that can later be run on behalf of
 *  an entity using just the name.  The "name" argument
 *  is required.  Other arguments will be passed directly
 *  through to the setters on the created ScriptedAction.
 */
void entityAction( Map args, Closure doIt ) {

println "entityAction() Delegate:" + doIt.delegate + "  parent:" + doIt.owner;

//println "closure metaclass:" + doIt.metaClass;

//    doIt.class.metaClass.original = null;

    // Store the action name into the strings table
    symbolGroups.addString( ActionManager.ENTITY_ACTIONS_GROUP, args.name ); 

    // The closure may still take one or two arguments
    action = new ScriptedAction( ActionManager.ENTITY_ACTIONS_GROUP, args.name, doIt );
    
    for( Map.Entry e : args ) {
        if( e.key == "group" || e.key == "name" )
            continue;            
        action[e.key] = e.value;
    }
    
    actions.addAction( action );
}

FieldFilter nameFilter( String name ) {
    return new FieldFilter( Name.class, "name", name );
    
}

EntityId namedEntity( String name, Closure exec ) {

    def entity = entities.findEntity( nameFilter(name) );
    if( entity == null ) {
        entity = entities.createEntity();
        entity << new Name(name);
    }   
 
    // Now run the closure
    exec.call( entity );   
}


import mythruna.script.ShellScript;

import org.progeeks.tool.console.Shell;

void addShellCommand( Shell shell, String name, String description, String help, Closure exec ) {
    
    String[] helpArray = null;
    if( help != null )
        helpArray = help.split( "\\r?\\n" );

    cmd = new ShellScript( description, helpArray, exec );
    shell.registerCommand( name, cmd );
}


void removePlaceable( Entity e ) { 
    removePlaceable( e.id );   
}

void removePlaceable( EntityId e ) {
    
    // Remove the components we know about
    entities.removeComponent( e, Position.class );
    entities.removeComponent( e, ModelInfo.class );
    entities.removeComponent( e, InContainer.class );
    entities.removeComponent( e, Name.class );
    entities.removeComponent( e, CreatedBy.class );
    entities.removeComponent( e, OwnedBy.class );
 
    entities.removeComponent( e, Volume.class );
    entities.removeComponent( e, Mass.class );
    entities.removeComponent( e, MassProperties.class );
       
    // And finally remove whatever we know by default that might
    // be transient
    entities.removeEntity( e );    
}

int getCellType( double x, double y, double z ) {
    return world.getType( Coordinates.worldToCell(x), Coordinates.worldToCell(y), Coordinates.worldToCell(z), null );  
}

int getCellType( int x, int y, int z ) {
    return world.getType( x, y, z, null );  
}

int getCellType( Vector3i loc ) {
    return world.getType( loc.x, loc.y, loc.z, null );
}

void setCellType( int x, int y, int z, int type ) {
    world.setCellType( x, y, z, type );
}

void setCellType( Vector3i loc, int type ) {
    world.setCellType( loc.x, loc.y, loc.z, type );
}

BlockType toBlockType( int blockType ) {
    if( blockType < 0 || blockType >= BlockTypeIndex.types.length )
        return null; 
    return BlockTypeIndex.types[blockType];
}

MaterialType toMaterial( int blockType ) {
    BlockType type = toBlockType(blockType);
    if( type == null )
        return null;
            
    return type.getMaterial();
}


