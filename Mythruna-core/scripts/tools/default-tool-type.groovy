/**
 *  ${Id}
 *
 *  Copyright (c) 2012, Paul Speed
 *  All rights reserved.
 *
 *  These are the scripts associated with the default tool
 *  type.  This is the tool that is used by items that do not
 *  specify a more specific tool type.
 *
 *  A tool type is a "flavor" of input.  It translates user
 *  clicks and gestures into script calls in a specific way.
 *  The default tool basically passes through the basic clicks
 *  and hover but provides no special dragging or rolling operations.
 */

import com.jme3.math.Vector3f;
import mythruna.es.*;
import mythruna.script.*;

action( group:"DefaultTool", name:"mainClick", type:ActionType.Hit ) {
    tool, hit ->

    println "Main click:" + tool + " on:" + hit 
    //console.echo( "Main click:" + tool + " on:" + hit );
 
    List todo = tool.getDefaultActions();
    for( String s : todo ) {   
//        println "Trying to execute:" + s;
        if( tool.execute( s, hit ) ) {
            break;
        }
    }
}

action( group:"DefaultTool", name:"alternateClick", type:ActionType.Hit ) {
    tool, hit ->

    //console.echo( "Alternate click:" + tool + " on:" + hit );
 
    String name = tool.name;
    if( tool == player )
        name = "Hand";
        
    Vector3f location = hit.contact;
    if( hit.object != null ) {
        String objectName = hit.object.name;
        if( objectName != null )
            name = objectName;
            
        Position pos = hit.object[Position.class];
        if( pos != null ) {
            location = pos.getLocation();
        }
    }
    
    List refs = tool.getEnabledActionRefs(hit);
    
    println "Todo:" + refs;   

    if( refs.isEmpty() )
        return;
 
    //player << new ContextActions( player, tool, location, "A name", "A title", refs );
    player << new ContextActions( player, tool, null, name, null, refs, hit );
   
}

action( group:"DefaultTool", name:"hover", type:ActionType.Hit ) {
    tool, hit ->

    //console.echo( "Hover:" + tool + " on:" + hit );
    println "Hover:" + tool + " on:" + hit

    setReticleText( null );
     
    List todo = tool.getDefaultActions();
    for( String s : todo ) {   
        //println "Trying to execute:" + s;
        if( tool.isEnabled( s, hit ) ) {        
            //echo "Default action:" + s;
            //println "Default action:" + s;
            
            if( !s.startsWith(":") ) {
                setReticleText( s );
            } 
            break;
        }
    }
}


// Create the DefaultTool if it doesn't exist or just retrieve it
// if it does.
namedEntity( "DefaultTool" ) {

    // Whether it existed already or not, we will
    // still reset the tools
    def refs = []
    
    refs += actions.getRefs( "DefaultTool" );
    
    it << new ToolActions(refs);   
}


