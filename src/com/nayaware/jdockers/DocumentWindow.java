package com.nayaware.jdockers;

/**
 * This is a conveinent interface for providing specialized dockable window.
 * Useful to create document window that get tabbed at the center
 * 
 * @author Winston Prakash
 * @version 1.0
 */
public interface DocumentWindow extends DockableWindow {
	/**
	 * Adds a Document Window Listener.
	 * 
	 * @param listener
	 *            the listener
	 * @throws NullPointerException
	 *             if listener is <code>null</code>
	 */
	public void addDocumentWindowListener(DocumentWindowListener listener);

	/**
	 * Removes a Document Window Listener. Fails silently if listener is unkown
	 * or <code>null</code>.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void removeDocumentWindowListener(DocumentWindowListener listener);
}