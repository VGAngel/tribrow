

defOption( id:"web", text:"Is there a Mythruna web site or forum?" ) {

    showPrompt( """\
                There is a forum at mythruna.com/forum<br>
                Also, development can be followed at:<br>
                facebook.com/mythruna or on twitter @MythrunaGame
                """ 
                ) {
        option( "controls" )
        option( "func-keys" )
        option( "build" )
        option( "objects" )
        option( "blueprints" )
        option( "map" )
        option( text:"Done" );                
    } 
}

defOption( id:"controls", text:"What are the basic controls?" ) {
    
    showPrompt( """\
                Mouse - looks<br>
                WASD - to move in game or rotate the builder platform<br>                
                <space> - jumps<br>
                <shift> - runs<br>
                <enter> - opens the chat/command bar<br>
                <esc> - opens the in-game menu                
                """ 
                ) {
        option( "more-controls" )
        option( text:"Done" );                
    } 
}

defOption( id:"more-controls", text:"More controls..." ) {
    
    showPrompt( """\
                <tab> - opens the tab menu<br>
                M, B, P - shortcuts to the Map, Blueprint, and Property tabs.<br>
                Scroll Wheel - changes major block/tool type<br>
                Ctrl Scroll Wheel - changes sub type (ex: shape, object, or tool)<br>
                C - selects the block type under the cursor
                """ 
                ) {
        option( "func-keys" )
        option( "web" )
        option( "build" )
        option( "objects" )
        option( "blueprints" )
        option( "map" )
        option( text:"Done" );                
    } 
}

defOption( id:"func-keys", text:"What are the function keys?" ) {
    
    showPrompt( """\
                F1 - opens help<br>
                F2 - takes a screen shot<br>
                F3 - toggles the HUD off and on<br>
                F4 - with mouse wheel changes time of day<br>
                F5 - toggles debug info off and on<br>
                F9 - toggles post-processing<br>
                F10 - early exits the loading screen.<br>
                F11 - toggles the stats display<br>
                F12 - changes the clip range
                """ 
                ) {
        option( "web" )
        option( "build" )
        option( "objects" )
        option( "blueprints" )
        option( "map" )
        option( text:"Done" );                
    } 
}

defOption( id:"build", text:"How do I build?" ) {

    showPrompt( """\
                When a block material is shown in the bottom of the
                screen, the right mouse button will place that block
                type and the left mouse button will remove any type
                of block.
                """ 
                ) {
        option( "build2" )
        option( text:"Done" );                
    } 
}

defOption( id:"build2", text:"How do I change block type?" ) {

    showPrompt( """\
                The mouse wheel will scroll through the major block
                groups and other tools.  The ctrl key + mouse wheel
                will change the sub-type or shape of a material.  
                """ 
                ) {
        option( "web" )
        option( "controls" )
        option( "func-keys" )
        option( "objects" )
        option( "blueprints" )
        option( "map" )
        option( text:"Done" );                
    } 
}

defOption( id:"objects", text:"How do I place objects?" ) {

    showPrompt( """\
                Use the mouse wheel to find the 'Objects' tool.
                Ctrl + mouse wheel changes object to be placed.
                Right mouse button places or opens the menu of
                an existing object.  Left mouse button drags.
                """ 
                ) {
        option( "web" )
        option( "controls" )
        option( "func-keys" )
        option( "build" )
        option( "blueprints" )
        option( "map" )
        option( text:"Done" );                
    } 
}

defOption( id:"blueprints", text:"How do I make my own objects?" ) {

    showPrompt( """\
                The in game object builder can be accessed using one of the
                following methods:<br>
                - press TAB and click 'Blueprints'<br>
                - press 'b' 
                """ 
                ) {
        option( "blueprints2" )
        option( text:"Done" );                
    } 
}

defOption( id:"blueprints2", text:"How does the blueprint editor work?" ) {

    showPrompt( """\
                The controls work similar to in game. Use the move keys to
                rotate the platform.  Mouse wheel, ctrl + mouse wheel, left
                click, and right click all work the same as in game.
                Select an existing object to edit or click 'New' to start a
                new one.<br>
                Click 'Save' to save your work and have it
                available to be placed in game.
                """ 
                ) {
        option( "web" )
        option( "controls" )
        option( "func-keys" )
        option( "build" )
        option( "objects" )
        option( "map" )
        option( text:"Done" );                
    } 
}

defOption( id:"map", text:"How do I see the map?" ) {

    showPrompt( """\
                The in game overland map can be accessed using one of the
                following methods:<br>
                - press TAB and click 'Map'<br>
                - press 'm' 
                """ 
                ) {
        option( "web" )
        option( "controls" )
        option( "build" )
        option( "objects" )
        option( "blueprints" )
        option( text:"Done" );                
    } 
}

showPrompt( """\
Welcome to Mythruna's in game help.
Please select a help option from below.
"""
) {
    option( "web" )
    option( "controls" )
    option( "func-keys" )
    option( "build" )
    option( "objects" )
    option( "blueprints" )
    option( "map" )
    option( text:"Never mind" )
}


