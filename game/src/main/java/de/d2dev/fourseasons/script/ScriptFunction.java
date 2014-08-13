package de.d2dev.fourseasons.script;

/**
 * An independent function made available by a script.
 * To be called by the application. Implementing classes overload
 * the call function to have matching parameters and return value.
 * 
 * @author Sebastian Bordt
 *
 */
public interface ScriptFunction {
	
	public Object call(Object... args);
}
