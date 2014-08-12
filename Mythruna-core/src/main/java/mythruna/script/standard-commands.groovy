package mythruna.script
/**
 *  ${Id}
 *
 *  Copyright (c) 2012, Paul Speed
 *  All rights reserved.
 */


// This is run by the uiInit script manager... not
// the regular one.  Technically, this is client only and
// could probably go live there.

import mythruna.*;
import mythruna.client.*;


def climbHelp = """\
        Usage: climb <mode> 
                   
        Where:
            <mode> is one of: none, all, simian, or reptilian
                        
            This sets the climb abilities for the player to one
            of the specified races.  none removes all climbing of
            shear surfaces.  'all' allows climbing any shear surface.
            'simian' and 'reptilian' limit climbing to only certain
            types of materials.
            
            This is a temporary command used for testing and will
            not exist in the final version of the game since then
            climbing will be an ability.
        """

addShellCommand( shell, "climb", "Sets the climb mode or displays the current climb mode.", climbHelp ) {
 
    if( it == null || it == "" ) {   
        console.echo( "No mode specified" );
        return;
    }
        
    if( it.toLowerCase() == "none" ) {
        gameClient.setCollisionMaskStrategy( null );
        console.echo( "Set climb mode to NONE." );
    } else if( it.toLowerCase() == "all" ) {
        gameClient.setCollisionMaskStrategy( MaskStrategies.ALL );
        console.echo( "Set climb mode to ALL." );
    } else if( it.toLowerCase() == "simian" ) {
        gameClient.setCollisionMaskStrategy( MaskStrategies.SIMIAN );
        console.echo( "Set climb mode to SIMIAN." );
    } else if( it.toLowerCase() == "reptilian" ) {
        gameClient.setCollisionMaskStrategy( MaskStrategies.REPTILIAN );
        console.echo( "Set climb mode to REPTILIAN." );
    }
}        
 
console.echo( "Climb mode set to ALL." );
    
def treeHelp = """\
        Usage: trees <mode> 
                   
        Where:
            <mode> is one of: low or normal
                        
            This sets the tree quality to low or normal.  Normal
            is the default.
                        
            Low quality mode renders all leaf blocks as the old
            green sponge variety... even for pine trees. 
        """
addShellCommand( shell, "trees", "Sets the tree quality.", treeHelp ) {
 
    if( it == null || it == "" ) {   
        console.echo( "No mode specified" );
        return;
    }
        
    if( it.toLowerCase() == "low" ) {
        console.echo( "Set tree mode to low quality." );
        BlockTypeIndex.setTreeQualityLow( true );
    } else if( it.toLowerCase() == "normal" ) {
        console.echo( "Set tree mode to normal quality." );
        BlockTypeIndex.setTreeQualityLow( false );
    }
        
    MainStart.globalStateManager.getState( GameAppState.class ).getLocalArea().rebuildGeometry();
}        

def floraHelp = """\
        Usage: flora <mode> 
                    
        Where:
            <mode> is one of: low or normal
                        
            This sets the flora quality to low or normal.  
            Normal is the default.
                        
            Low quality mode renders all flora blocks as the dirt for
            testing.
        """
addShellCommand( shell, "flora", "Sets the flora quality.", floraHelp ) {
 
    if( it == null || it == "" ) {   
        console.echo( "No mode specified" );
        return;
    }
        
    if( it.toLowerCase() == "low" ) {
        console.echo( "Set flora mode to low quality." );
        BlockTypeIndex.setFloraQualityLow( true );
        BlockTypeIndex.setGrassQuality( 0 );
    } else if( it.toLowerCase() == "normal" ) {
        console.echo( "Set flora mode to normal quality." );
        BlockTypeIndex.setFloraQualityLow( false );
    }
        
    MainStart.globalStateManager.getState( GameAppState.class ).getLocalArea().rebuildGeometry();
}        

def grassHelp = """\
        Usage: grass <mode> 
                    
        Where:
            <mode> is one of: low, medium, medium2, or normal
                        
            This sets the tall grass quality to low, medium, or normal.  
            Normal is the default.
                        
            Low quality mode renders all grass blocks as the dirt for
            testing.  Medium renders the grass panels with a solid material.
        """
addShellCommand( shell, "grass", "Sets the grass quality.", floraHelp ) {
 
    if( it == null || it == "" ) {   
        console.echo( "No mode specified" );
        return;
    }
        
    if( it.toLowerCase() == "low" ) {
        console.echo( "Set grass mode to low quality." );
        BlockTypeIndex.setGrassQuality( 0 );
    } else if( it.toLowerCase() == "medium" ) {
        console.echo( "Set grass mode to medium quality." );
        BlockTypeIndex.setGrassQuality( 1 );
    } else if( it.toLowerCase() == "medium2" ) {
        console.echo( "Set grass mode to medium2 quality." );
        BlockTypeIndex.setGrassQuality( 2 );
    } else if( it.toLowerCase() == "normal" ) {
        console.echo( "Set grass mode to normal quality." );
        BlockTypeIndex.setGrassQuality( 3 );
    }
        
    MainStart.globalStateManager.getState( GameAppState.class ).getLocalArea().rebuildGeometry();
}        
       

def noiseHelp = """\
        Usage: noise <mode> 
                    
        Where:
            <mode> is one of: on or off
                        
            This sets the noise factor to on or off.  When noise is
            on, textures get an extra variation layer.
        """
addShellCommand( shell, "noise", "Sets the texture noise mode.", noiseHelp ) {
 
    if( it == null || it == "" ) {   
        console.echo( "No mode specified" );
        return;
    }
        
    if( it.toLowerCase() == "on" ) {
        console.echo( "Set noise mode on." );
        MaterialIndex.setGritty( true );
    } else if( it.toLowerCase() == "off" ) {
        console.echo( "Set noise mode off." );
        MaterialIndex.setGritty( false );
    }
}        
       


