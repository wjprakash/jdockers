package com.nayaware.jdockers;

/**
 * A Dockable Window is a special form of a Layout Window. The window can be
 * docked, floated or autohided.
 * 
 * @author Winston Prakash
 * @version 1.0
 */
public interface DockableWindow extends LayoutWindow {

	public static final String DOCK_STATE_DOCKED = "docked";
	public static final String DOCK_STATE_FLOATED = "floated";
	public static final String DOCK_STATE_AUTOHIDDEN = "autohidden";
	// public static final String DOCK_STATE_NONE = "none";

	public static final String DOCK_SIDE_LEFT = "left";
	public static final String DOCK_SIDE_TOP = "top";
	public static final String DOCK_SIDE_RIGHT = "right";
	public static final String DOCK_SIDE_BOTTOM = "bottom";
	public static final String DOCK_SIDE_NONE = "none";

	/**
	 * Adds a Dockable Window Listener.
	 * 
	 * @param listener
	 *            the listener
	 * @throws NullPointerException
	 *             if listener is <code>null</code>
	 */
	public void addDockableWindowListener(DockableWindowListener listener);

	/**
	 * Removes a Dockable Window Listener. Fails silently if listener is unkown
	 * or <code>null</code>.
	 * 
	 * @param listener
	 *            the listener
	 */
	public void removeDockableWindowListener(DockableWindowListener listener);

	/**
	 * Sets the initial docking side of the Dockable Window. Useful as hint when
	 * the window is opened first time. Ignored if used after opening the
	 * window.
	 * 
	 * @param String
	 *            with value left/right/top/bottom/none
	 * @throws IllegalArgumentException
	 *             if not one of DOCK_SIDE_xxx constants.
	 */
	public void setInitialDockSide(String dockSide);

	/**
	 * Returns the current docking side of the Dockable Window.
	 * 
	 * @return String (value left/right/top/bottom/none) (never
	 *         <code>null</code>).
	 */
	public String getDockSide();

	/**
	 * Sets the initial state of the Dockable Window. Useful has hint when the
	 * window is opened first time.
	 * 
	 * @param String
	 *            with value docked/floated/none
	 * @throws IllegalArgumentException
	 *             if not one of DOCK_STATE_xxx constants.
	 */
	public void setInitialDockState(String dockState);

	/**
	 * Returns the current state of the Dockable Window.
	 * 
	 * @return String (value docked/floated/none) (never <code>null</code>).
	 */
	public String getDockState();

}
