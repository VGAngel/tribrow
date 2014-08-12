package mythruna.script
/**
 *  ${Id}
 *
 *  Copyright (c) 2012, Paul Speed
 *  All rights reserved.
 */


import org.progeeks.util.ObjectUtils;
import mythruna.es.*

//println "Bindings:" + bindings;

// Really should roll this into its own script.
EntityId.metaClass {
    leftShift { 
        EntityComponent c ->
        Object old = entities.getComponent( delegate, c.getType() );
        if( ObjectUtils.areEqual(old, c) )
            return;
        entities.setComponent( delegate, c )         
    }
    getAt { Class c -> entities.getComponent( delegate, c ) }
}


/**
 *  Internal class used to build up a DialogOption.  By keeping
 *  a separate builder class it might let us do better things with
 *  text setup, etc..
 */
class OptionBuilder {
    Object id;
    String text;
    Closure action;
 
    public DialogOption build() {
        return new DialogOption( text.trim(), action );
    }
    
    public String toString() {
        return text + "->" + action;
    }    
}

/**
 *  Internal class used to build up a DialogPrompt.
 */
class PromptBuilder {
    String prompt;
    List opts = new ArrayList();
 
    Map optionsLookup;
    Object outer;
    
    public PromptBuilder( Object outer, Map optionsLookup ) {
        this.outer = outer;
        this.optionsLookup = optionsLookup;
    }
    
    public DialogPrompt build( EntityId player ) {
        
        DialogOption[] array = new DialogOption[opts.size()];
        for( int i = 0; i < opts.size(); i++  ) {
            OptionBuilder o = opts.get(i);
            array[i] = o.build();
        }
        
        return new DialogPrompt( player, prompt.trim(), array );
    }
    
    public void option( Object id ) {
        //println "PromptBuilder.option(" + id + ")";
        opts += optionsLookup.get(id);
    }
    
    public OptionBuilder option( Map args ) {
        //println "PromptBuilder.option(" + args + ")";
        if( args.id != null ) {
            option( args.id );
            return null;
        } else if( args.text != null ) {            
            return option( text:args.text ) {
                    outer.clearPrompt();
                }
        }                
 
        throw new IllegalArgumentException( "No text or ID specified." );               
    }
    
    public OptionBuilder option( Map args, Closure doIt ) {
        //println "PromptBuilder.option(" + args + ", " + doIt + ")";
        OptionBuilder result = new OptionBuilder();
        result.text = args.text;
        result.action = doIt;
        opts += result;
        return result;
    }
    
    public String toString() {
        return prompt + ":" + opts;
    }
}

//println "Defining defOption(map, closure)"

/**
 *  Defines an option that can be looked up by ID if an id
 *  has been specified.
 */
OptionBuilder defOption( Map args, Closure doIt ) {

    //println "option(" + args + ", " + doIt + ")";
 
    if( args.id == null )
        throw new IllegalArgumentException( "No id: specified in defOption() call." );
           
    result = new OptionBuilder();
    result.id = args.id;
    result.text = args.text;
    result.action = doIt;
    
    options.put( result.id, result );      
    
    return result;
}

//println "Defining defOption(map)"

/**
 *  Convenience version that registers a closure that just
 *  clears the current prompt, ie: terminates the dialog.
 */
OptionBuilder defOption( Map args ) {
    return defOption( args ) {
        clearPrompt();
    }
}

/**
 *  Terminates the current dialog.
 */
void clearPrompt() {
    //println "clearPrompt()";
    entities.removeComponent( player, DialogPrompt.class );
}

/**
 *  Creates a DialogPrompt from options setup in the showPrompt closure.
 */
void showPrompt( String prompt, Closure setup ) {
    builder = new PromptBuilder(this, options);
    //builder.prompt = prompt.replaceAll( "\\n[ \\t]+", "\n" );
    builder.prompt = prompt.replaceAll( "\\s+", " " );
    setup.setDelegate(builder);
    setup.call();
    
    //println "Prompt:" + builder;
    
    component = builder.build(player);
    //println "Real component:" + component;
    
    player << component;
}



