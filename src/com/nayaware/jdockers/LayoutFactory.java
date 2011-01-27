package com.nayaware.jdockers;

import com.nayaware.jdockers.util.Lookup;

/**
 * Factory for Layout manager
 * 
 * @author Winston Prakash
 * @version 1.0
 * 
 */
public class LayoutFactory {

	private static LayoutManager lmInstance;

	// Private because this class should not be instantiated
	private LayoutFactory() {
	}

	/**
	 * Gets a Layout Manager using Lookup. If no Layout Manager is provided
	 * through look up supply the default Layout Manager.
	 * 
	 * @see LayoutManager
	 * @see Lookup
	 * @return the Layout Manager (never <code>null</code>)
	 */
	public static final LayoutManager getLayoutManager() {
		synchronized (LayoutFactory.class) {
			if (lmInstance == null) {
				lmInstance = (LayoutManager) Lookup.getDefault().lookup(
						LayoutManager.class);
				if (lmInstance == null) {
					// lmInstance = new DefaultLayoutManager();
				}
			}
			return lmInstance;
		}
	}
}
