package com.nayaware.jdockers;

import javax.swing.JComponent;

/**
 * Object of this event will be fired when events occurs in the Layout Window.
 * 
 * @author Winston Prakash
 * @version 1.0
 */
public interface LayoutWindowEvent {
	/**
	 * Returns the source of this event (which is always the Layout Manager).
	 * The method was added for compatibility with {@link java.util.EventObject}
	 * s.
	 * 
	 * @return the object that caused with event (never <code>null</code>).
	 */
	public Object getSource();

	/**
	 * Returns the Layout Manager that caused this event.
	 * 
	 * @return the object that caused with event (never <code>null</code>).
	 */
	public LayoutManager getLayoutManager();

	/**
	 * Returns the Layout Window that caused this event to be fired.
	 * 
	 * @return Layout Window (never <code>null</code>).
	 */
	public LayoutWindow getLayoutWindow();

	/**
	 * Returns the active component in the Layout Window. Useful in case of
	 * Multi View Layout Window.
	 * 
	 * @return JComponent (or <code>null</code> if there's no active component).
	 */
	public JComponent getActiveComponent();

	/**
	 * Returns the previously active component that was superceded by the
	 * activate component of this Layout Window.
	 * 
	 * @return JComponent (or <code>null</code> if there's no previous
	 *         component).
	 */
	public JComponent getPreviousComponent();
}
