package com.nayaware.jdockers.util;

import java.util.HashMap;
import java.util.Map;

/**
 * A central registry to lookup certain global objects by Interface.class.
 * Placeholder for the real implementation.
 * 
 * @author Stefan Matthias Aust 
 */
public class Lookup {

	private static final Lookup instance = new Lookup();

	private Map registry = new HashMap();

	/**
	 * Returns the default registry.
	 * 
	 * @return default lookup registry (never <code>null</code>)
	 */
	public static Lookup getDefault() {
		return instance;
	}

	/**
	 * Returns the object registered for the specified key or <code>null</code>
	 * if no the key is unknown to the registry.
	 * 
	 * @see #bind(Class, Object)
	 * @param key
	 *            interface class object used as key
	 * @return the associated object (or <code>null</code>)
	 * @throws NullPointerException
	 *             if key is <code>null</code>
	 * 
	 */
	public Object lookup(Class key) {
		return registry.get(key);
	}

	/**
	 * Registers the specified object with the specified key. Silently
	 * overwrites old bindings.
	 * 
	 * @see #lookup(Class)
	 * @param key
	 *            interface class object used as key
	 * @param object
	 *            the global object associated with the key
	 * @throws NullPointerException
	 *             if key is <code>null</code>
	 */
	public void bind(Class key, Object object) {
		registry.put(key, object);
	}

}
