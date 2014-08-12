package mythruna.script
/**
 *  ${Id}
 *
 *  Copyright (c) 2012, Paul Speed
 *  All rights reserved.
 */

import mythruna.db.*;
import mythruna.es.*;
import mythruna.phys.*
import mythruna.item.*;

void setReticleText( String text ) {

    def player = ExecutionContext.environment.player;
        
    if( text == null ) {
        entities.removeComponent( player, ReticleStyle.class );
        return;
    }
        
    player << new ReticleStyle( player, ReticleType.Default, text );
}

// Base stuff for model handling and importing (could be moved)

BlueprintData loadBlueprint( String name ) {

    File f = new File( localDirectory, name );
    return DefaultBlueprintDatabase.importBlueprint( f );
}

long createBlueprint( BlueprintData bp ) {

    def result = world.createBlueprint( bp.name, bp.xSize, bp.ySize, bp.zSize, bp.scale, bp.cells );
    return result.id;
}

EntityId createModel( String name, int version, Closure config ) {

    // See if the model already exists
    def result = entities.findEntity( nameFilter(name), Name.class, ModelTemplate.class );   
    if( result != null ) {
        // Check the version
        if( result[ModelTemplate.class].version >= version ) {
            println "Reusing existing model template for:" + name;        
            return result;
        }            
        
        println "Updating model template for:" + name;
    } else {
        println "Creating model template for:" + name;
    }
 
    // Otherwise, we need to create or update the model template
    // We will wait to create any resulting entity until the bluepring
    // is fully loaded.  That way we don't thrash records needlessly
    // for buggy blueprints
    config.setDelegate(ExecutionContext.environment);
    long id = config.call();
    println( "Created blueprint id:" + id );    

    if( result == null ) {        
        result = entities.createEntity();
        result << new Name(name);
    }
    
    result << new ModelTemplate( id, version );        
    
    return result;
}

EntityId createModel( String name, int version, String bpFile ) {
    return createModel( name, version ) {
        return createBlueprint(loadBlueprint(bpFile));
    }
}

EntityId createModel( String name, int version, String bpFile, Scale scale ) {
    return createModel( name, version ) {
        BlueprintData bp = loadBlueprint(bpFile);
        bp.scale = scale.getScale(); 
        return createBlueprint(bp);
    }
}

EntityId getModel( String name ) {
    return entities.findEntity( nameFilter(name), Name.class, ModelTemplate.class );   
}

void makePhysical( EntityId entity, long blueprintId ) { 
 
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
    entity << template[Volume.class];
    entity << template[Mass.class];    
    entity << template[MassProperties.class];        
}

/**
 *  An internal object used to represent an object template
 *  during creation.  This is the delegate to the createObjectTemplate()
 *  closure and allows accumulating the various parts.
 */
class Template implements ObjectTemplates.ObjectTemplate {

    private Object outer;
    private EntityId classEntity;
    private String name;
    private List actions = [];
    private List parents = [];
    private EntityId defaultModel;
    private List components = [];
    private List defaultActions = [];

    public Template( Object outer, String name ) {
        this.outer = outer;
        this.name = name;
        
        classEntity = outer.entities.findEntity( new FieldFilter( ObjectClass.class, "name", name ) );
        if( classEntity == null ) {
            println "Creating clas entity for:" + name;
            classEntity = outer.entities.createEntity();
            classEntity << new ObjectClass(name);
        } else {
            println "Reusing class entity:" + classEntity + " for name:" + name;
        }
    }
 
    Object getOuter() {
        return outer;
    }
    
    public void setModel( String name ) {
        defaultModel = outer.getModel(name);
        if( defaultModel == null ) {
            throw new RuntimeException( "No model found for name:" + name );
        }       
    }
 
    public EntityId getClassEntity() {
        return classEntity;
    }
 
    public void addComponents( EntityComponent... data ) {
        data.each { components += it }
    } 
 
    public String getName() {
        return name;
    }

    public boolean isInstance( EntityId entity ) {
        // Right now only direct lookups
        return entity.template == this;
    }
    
    /**
     *  Creates an instance of this object in the specified container.
     */
    public EntityId createInstanceOn( EntityId container ) {
 
        if( defaultModel == null ) {
            throw new RuntimeException( "No model defined for object template:" + name );            
        }       
        def model = defaultModel[ModelTemplate.class];
        
        EntityId result = outer.entities.createEntity();
        components.each {
            result << it;
        }
        result << new ClassInstance( classEntity );
        result << new ModelInfo(model.blueprintId);
        result << new Name(name);
        result << new InContainer(container, 1);
        result << new OwnedBy(container);
 
        outer.makePhysical( result, model.blueprintId );
        
        return result; 
    }
 
    public void addParents( String... args ) {
        for( String s : args ) {
            Template parent = outer.objectTemplates.getTemplate(s);
            if( parent == null ) { 
                throw new RuntimeException( "Parent template not found for:" + s );
            }                
                
            parents += parent;
        }
    }
    
    public void addDefaultActions( String... args ) {
        args.each{ defaultActions += it }
    }
 
    public List getDefaultActions() {
        List result = [];
        result.addAll( defaultActions );
        parents.each {
            result.addAll( it.defaultActions );
        }
        
        return result;
    }

    public List getEnabledActionRefs( Object env, EntityId object, ActionParameter arg ) {
        Map refs = new LinkedHashMap();
        addRefs( refs, env, object, arg );
//println "Refs so far:" + refs;        
        parents.each {
            it.addRefs( refs, env, object, arg );
        }
        
        return new ArrayList(refs.values());
    }
    
    protected void addRefs( Map refs, Object env, EntityId object, ActionParameter arg ) {
        actions.each {
            
            // Skip the "private" actions
            if( it.name.startsWith(":") )
                return;
        
            if( it.action.isEnabled( env, object, arg ) ) {
                refs.put( it.name, it.ref );
            }            
        }        
    } 
 
    public boolean hasAction( String a )
    {
        if( getAction(a) != null )
            return true;
            
        parents.each {
            if( it.hasAction(a) )
                ran = true; 
        }
        
        return false;
    }
 
    public boolean isEnabled( Object env, EntityId object, String name, ActionParameter arg ) {
    
        ScriptedAction a = getAction(name);
        if( a != null ) {        
            if( !a.isEnabled( env, object, arg ) )
                return false;
        }
        
        for( Template p : parents ) {        
            if( !p.isEnabled( env, object, name, arg ) )
                return false;
        }

        return true;
    } 
 
    public boolean execute( Object env, EntityId object, String name, ActionParameter arg ) {
 
        boolean ran = false;       
        ActionReference ref = getActionRef(name);
        if( ref != null ) {
            env.systems.actionManager.execute( ref.id, env, object, arg );
            ran = true; 
        }
 
        parents.each {
            if( it.execute(env, object, name, arg) )
                ran = true; 
        }
        
        return ran;
    }
 
    public ActionReference getActionRef( String name ) {
 
        // Just a straight look-up for the moment
        return outer.actions.getRef( this.name, name );        
    }

    public ScriptedAction getAction( String name ) {
 
        // Just a straight look-up for the moment
        return outer.actions.getAction( this.name, name );        
    }
 
    // The problem with a separate compile step is that
    // we may have object definitions interlaced with the 
    // things that use them.  We sort of have to do this on
    // the fly, I guess.  Or at least support both modes of
    // calling objects.   
    public void compile() {
        println( "Compile:" + name );
        println( "Actions:" + actions );
    }
    
    ActionTemplate action( String name, Closure code ) {
        def template = new ActionTemplate(outer, this, name, code);
        actions += template;
        return template;
    }
           
    ActionTemplate defaultAction( String name, Closure code ) {
        def template = action(name, code);
        defaultActions += name;
        return template;
    }       
}

class ActionTemplate {
    private String name;
    private ScriptedAction action;
    private ActionReference ref;
    
    public ActionTemplate( Object outer, Template parent, String name, Closure code ) {
        this.name = name;
        action = new ScriptedAction( parent.getName(), name, code );
        ref = outer.actions.addAction( action );
    }
    
    public ActionTemplate onlyIf( Closure condition ) {
        action.setCondition( condition );        
        return this;
    }
        
    public String toString() {
        return "ActionTemplate[" + name + ", " + action + "]";
    }
}

Template objectTemplate( String name, Closure configure ) {

    Template template = objectTemplates.getTemplate(name);
    if( template == null ) {
        template = new Template(this, name);
        objectTemplates.addTemplate(template);
    }

    configure.setDelegate(template);
    configure.call();
    return template;    
}

/**
 *  Returns all of the items in the specified container.
 */
Set<EntityId> getContainedItems( EntityId container ) {
    return entities.findEntities( new FieldFilter( InContainer.class, "parentId", container ),
                                  InContainer.class, ClassInstance.class );
}                                          

/**
 *  Looks up the template for the specified entity.  Will
 *  return the "Default" template if none is found. 
 */
Template getTemplate( EntityId entity ) {
    // Get the entity's class
    ClassInstance ci = entity != null ? entity[ClassInstance.class] : null;
    if( ci == null )
        return objectTemplates.getTemplate( "Default" );
    else
        return objectTemplates.getTemplate( ci.classEntity );
}

/**
 *  Adds some additional methods to EntityId that allow
 *  us to make calls against its template.
 */
EntityId.metaClass.cachedTemplate = null;
EntityId.metaClass.cachedLocals = null;
EntityId.metaClass {
    
    getTemplate() { ->
        if( delegate.cachedTemplate == null ) {
            delegate.cachedTemplate = getTemplate(delegate);
        }
        return delegate.cachedTemplate; 
    }
    
    hasAction() { String name ->
        return delegate.template.hasAction(name);
    }

    setModel() { String name ->    
        def EntityId model = delegate.template.outer.getModel( name );
        if( model == null )
            return;
        def mt = model[ModelTemplate.class];
        delegate << new ModelInfo(mt.blueprintId);    
    }

    getDefaultActions() { ->
        return delegate.template.getDefaultActions();
    }

    getLocals() { -> 
        if( delegate.cachedLocals == null )
            delegate.cachedLocals = new LocalVariables(delegate.template.outer.entities, delegate);
        return delegate.cachedLocals;
    }

    execute() { String name, ActionParameter parm ->
        boolean enabled = delegate.template.isEnabled( ExecutionContext.environment, 
                                                       delegate, name, parm );
        if( !enabled )
            return false;                                 
        return delegate.template.execute( ExecutionContext.environment, delegate, name, parm );                             
    }

    execute << { String name ->   
        return execute( name, null );
    }
 
    execute << { String name, EntityId source ->
        return execute( name, new EntityParameter(source) );
    }
 
    isEnabled() { String name, ActionParameter parm ->   
        return delegate.template.isEnabled( ExecutionContext.environment, delegate, name, parm );
    }  
    
    getEnabledActionRefs() { ActionParameter parm ->
        return delegate.template.getEnabledActionRefs( ExecutionContext.environment, delegate, parm ); 
    } 
}

// A default template that we can use when no other
// one is available.
objectTemplate( "Default" ) {    

    defaultAction( ":Miss" ) { self, hit ->  
        echo( "That doesn't seem to work." );
    }
}


// Some useful base classes
objectTemplate( "BaseItem" ) {

    addParents( "Default" );

    defaultAction( "Drop" ) { self, hit -> 
 
        // Need to place the object where the user clicked
        // Should just be a matter of placing it on the ground
        // and removing it from the container.
        def loc = hit.getContact();
        def heading = getHeading();

        self << new Position(loc, heading);
        
        entities.removeComponent( self, InContainer.class );
                
    }.onlyIf() { self, hit ->
    
        return hit.object == null && hit.normal.z == 1;
    } 
}

objectTemplate( "BaseTool" ) {

    addParents( "Default" );
}

