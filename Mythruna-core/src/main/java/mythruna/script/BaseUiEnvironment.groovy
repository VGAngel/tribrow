package mythruna.script
/**
 *  ${Id}
 *
 *  Copyright (c) 2012, Paul Speed
 *  All rights reserved.
 */

import org.progeeks.util.log.Log;

log = Log.getLog( "ui-init" );

void warn( String msg ) {
    log.warn( msg );
}

import mythruna.*;
import mythruna.script.ShellScript;
import org.progeeks.tool.console.Shell;

void addShellCommand( Shell shell, String name, String description, String help, Closure exec ) {
    
    String[] helpArray = null;
    if( help != null )
        helpArray = help.split( "\\r?\\n" );

    cmd = new ShellScript( description, helpArray, exec );
    shell.registerCommand( name, cmd );
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


