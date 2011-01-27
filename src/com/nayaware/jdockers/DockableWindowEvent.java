package com.nayaware.jdockers;

import javax.swing.JComponent;

/**
 * This is a conveinent interface for providing specialized Layout window event.
 * Useful to create dockable style window event.
 * 
 * @author Winston Prakash
 * @version 1.0
 */

public interface DockableWindowEvent {
	/**
	 * Returns the source of this event (which is always a Layout Window). The
	 * method was added for compatibility with {@link java.util.EventObject}s.
	 * 
	 * @return the object that caused with event (never <code>null</code>).
	 */
	public Object getSource();

	/**
	 * Returns the Dockable Window that caused this event.
	 * 
	 * @return the object that caused with event (never <code>null</code>).
	 */
	public DockableWindow getDockableWindow();

	/**
	 * Returns the active component in the Layout Window. Useful in case of
	 * Multi View Layout Window.
	 * 
	 * @return JComponent (or <code>null</code> if there's no active component).
	 */
	public JComponent getActiveComponent();

}
