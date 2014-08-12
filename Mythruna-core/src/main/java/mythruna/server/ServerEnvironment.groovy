package mythruna.server

import java.lang.reflect.*;
import com.jme3.math.*;
import com.jme3.network.*;
import mythruna.event.*;
import mythruna.server.event.*;

ServerEvents.fields.each {
    if( Modifier.isStatic( it.getModifiers() ) ) {
        //println "Binding event type:" + it.name
        bindings.put( it.name, it.get(null) );
    }
}

/*
void addShellCommand( HostedConnection conn, String name, String description, String help, Closure exec ) {
    
    String[] helpArray = null;
    if( help != null )
        helpArray = help.split( "\\r?\\n" );

    cmd = new ShellScript( description, helpArray, exec );
    conn.getAttribute("shell").registerCommand( name, cmd );
}
*/


// Play shell script stuff for admin commands, etc..
import mythruna.PlayerData;
import mythruna.es.*;
import mythruna.msg.*;
import mythruna.script.*;
import mythruna.shell.Console;
import mythruna.sim.*;

Vector3f getLocation() {
    //println "Environment:" + ShellScript.environment;    
    return getLocation(ExecutionContext.environment.connection);
}

Quaternion getFacing() {
    return getFacing(ExecutionContext.environment.connection);
}

float getHeading() {
    float[] array = getFacing().toAngles(new float[3]);
    return array[1];
}

void setLocation( float x, float y, float z ) {
    warp( ExecutionContext.environment.connection, x, y, z );
}

HostedConnection findConnection( String playerName ) {
    for( HostedConnection conn : server.server.getConnections() ) {
        PlayerData p = conn.getAttribute( "player" );
        if( playerName == p.get( "userInfo.userId" ) )
            return conn;     
    }
    return null; 
}

HostedConnection findCharacter( String name ) {
    for( HostedConnection conn : server.server.getConnections() ) {
        PlayerData p = conn.getAttribute( "player" );
        if( playerName == p.get( "characterInfo.name" ) )
            return conn;     
    }
    return null; 
}

HostedConnection findConnection( int clientId ) {
    return server.server.getConnection( clientId );
}

void sendMessage( EntityId target, String message ) {
    client = server.getClientId( target );
    if( client < 0 ) {
        System.out.println( "Invalid client ID[" + client + "] in sendMessage(" + message + ")" );
        return;
    }
 
    conn = server.server.getConnection(client);
    if( conn == null ) {
        System.out.println( "Invalid client ID[" + client + "] in sendMessage(" + message + ")" );
        return;
    }
    
    targetShell = conn.getAttribute("shell");
    if( targetShell == null ) {
        System.out.println( "Invalid shell for client ID[" + client + "] in sendMessage(" + message + ")" );
        return;
    }
    
    targetShell.echo( message );   
}

Vector3f getLocation( HostedConnection conn ) {

    Mob e = conn.getAttribute("entity");
    if( e == null ) {
        return null;
    }
    
    // Grab the current location 
    long t = GameSimulation.getTime();                     
    FrameTransition ft = e.getFrame( t );
    Vector3f pos = ft.getPosition(t);
    
    return pos;
}

Vector3f getFacing( HostedConnection conn ) {

    Mob e = conn.getAttribute("entity");
    if( e == null ) {
        return null;
    }
    
    // Grab the current location 
    long t = GameSimulation.getTime();                     
    FrameTransition ft = e.getFrame( t );
    return ft.getRotation(t);
}

void warp( String player, Vector3f location, Quaternion direction, Console out ) {
    
    // Find the player that matches
    conn = findConnection( player );
    if( conn == null ) {
        out.echo( "Player not found:" + player );
        return;
    }        

    warp( conn, location, direction );
}

void warp( HostedConnection conn, Vector3f location, Quaternion direction ) {

    long time = System.currentTimeMillis();
    Message m = new WarpPlayerMessage( time, location, direction );
    conn.send(m);     
} 

void warp( HostedConnection conn, float x, float y, float z ) {

    long time = System.currentTimeMillis();
    Message m = new WarpPlayerMessage( time, new Vector3f( x, y, z), 
                                       Quaternion.DIRECTION_Z );
    conn.send(m);     
} 

void respawn( HostedConnection conn, Console out ) {

    warp( conn, 
          server.getWorld().getDefaultSpawnLocation(), 
          server.getWorld().getDefaultSpawnDirection() );
} 

/*
            mythruna.db.user.Player p = conn.getAttribute( "player" );
            
            long time = System.currentTimeMillis();
            Message m = new WarpPlayerMessage( time, server.getWorld().getDefaultSpawnLocation(), 
                                                     server.getWorld().getDefaultSpawnDirection() );
            
            System.out.println( "Warping:" + p.get( "userInfo.userId" ) + " to:" + server.getWorld().getDefaultSpawnLocation() );
            conn.send(m); 
            */

