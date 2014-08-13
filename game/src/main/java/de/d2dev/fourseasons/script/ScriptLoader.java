package de.d2dev.fourseasons.script;

import de.schlichtherle.truezip.file.TFile;

/**
 * Interface for script loading. To be implemented by each scripting language.
 * @author Sebastian Bordt
 *
 */
public interface ScriptLoader {
	
	/**
	 * Attempt to load a script form a {@link de.schlichtherle.truezip.file.TFile}.
	 * @param file
	 * @return {@code null} if the loader can't load this script.
	 */
	public Script load(TFile file);
}
