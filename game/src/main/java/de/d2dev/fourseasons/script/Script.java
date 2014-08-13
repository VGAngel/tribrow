package de.d2dev.fourseasons.script;

import java.util.List;

/**
 * Interface for any instance of loaded and executable script.
 * May expose different functions. To be implemented by each
 * scripting language.
 * @author Sebastian Bordt
 *
 */
public interface Script {
	
	/**
	 * The functions made available by the script.
	 * @return
	 */
	public List<ScriptFunction> getFunctions();
}
