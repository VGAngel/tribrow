package mythruna.script
/**
 *  ${Id}
 *
 *  Copyright (c) 2012, Paul Speed
 *  All rights reserved.
 */

import mythruna.es.*;
import mythruna.script.*;

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

action( group:"Admin", name:"Object Info", type:ActionType.Object ) {
    
    println "Object info for:" + it
       
    cb = it.object[CreatedBy.class];
    userId = it.object[UserId.class];
 
    if( userId != null ) {
        console.echo( "Player:" + userId );
        console.echo( "Name:" + it.object[Name.class] );        
    } else {          
        id = cb.creatorId;
 
        console.echo( "Model:" + it.object[ModelInfo.class] );
        console.echo( "Created by entity:" + id );
        console.echo( "Name:" + id[Name.class] );
    
    //if( id == player ) {
    //    console.echo( "That's you." );
    //} else {
        // Try to figure it out
        if( id[UserId.class] != null ) { 
            console.echo( "User ID:" + id[UserId.class] );
        } else if( playerData != null ) {
            // We're on the server so we should be able
            // to do additional look-ups           
            p = server.userDatabase.findUser( "characterInfo.entityId", id.getId() );
            console.echo( "Player:" + p );
        }            
    //}
    }        
}

entityAction( name:"Open Help" ) {

    println "Launching help for:" + player; 
    dialogs.startDialog( player, "help" );    
}

// The action that will call dialog options
entityAction( name:"DialogOption", type:ActionType.Number ) {
    source, obj ->
 
    println "Select dialog option:" + obj;
 
    prompt = player[DialogPrompt.class];
    
    println "From prompt:" + prompt;
    
    if( prompt == null ) {
        println "Error: no prompt associated with player:" + player;
        return;
    }
    
    option = prompt.options[obj.number - 1];
    
    println "Option:" + option;
    
    dialogs.selectOption( player, prompt, obj.number - 1, option );    
}       
 

on( [playerJoined] ) {
    type, event ->
    
    println "Adding standard tools to player:" + player;

println "Delegate:" + delegate;
 
    refs = []

    ToolActions existing = player[ToolActions.class];
 
    if( playerData != null ) {          
        if( playerData.get( "grant.admin" ) != null ) {
            refs += actions.getRef( "Admin", "Object Info" ); 
        }
        if( playerData.get( "grant.test" ) != null ) {
            refs += actions.getRef( "Foo", "Bar" );  
            refs += actions.getRef( "Foo", "Speed Test" );
        }
    }            
    
    println "Refs:" + refs;
    
    player << new ToolActions(refs, existing)
        
/*    
    addShellCommand( shell, "test", "Test stuff.", null ) {
        console.echo( "Hello, world." );
    }*/
    
}    

