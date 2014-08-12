package mythruna.script
/**
 *  ${Id}
 *
 *  Copyright (c) 2012, Paul Speed
 *  All rights reserved.
 */

import mythruna.script.*;

/**
 *  Defines an action that can later be run by
 *  group and name.  The "group" and "name" arguments
 *  are required.  Other arguments will be passed directly
 *  through to the setters on the created ScriptedAction.
 */
void action( Map args, Closure doIt ) {
    action = new ScriptedAction( args.group, args.name, doIt );
    
    for( Map.Entry e : args ) {
        if( e.key == "group" || e.key == "name" )
            continue;            
        action[e.key] = e.value;
    }
    
    actions.addAction( action );
}

println "Running action method."

action( group:"Foo", name:"Bar" ) {
    println "Inside action"
    
    println "It:" + it 
    println "Player:" + player 
    println "Console:" + console
    println "Actions:" + actions 
    //println "Test:" + test
    
    console.echo( "Testing" ) 
}

action( group:"Foo", name:"Speed Test", access:"admin" ) {
    // Do nothing.
} 
