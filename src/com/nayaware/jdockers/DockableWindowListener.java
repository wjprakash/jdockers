package com.nayaware.jdockers;

import java.util.EventListener;

/**
 * This is a convenient listener if additional events need to be fired directly
 * from the dockable window. Otherwise use the Layout Manager listener that
 * fires most of the required layout window events
 * 
 * @author Winston Prakash
 * @version 1.0
 */
public interface DockableWindowListener extends EventListener {
	/**
	 * Fired when the Dockable Window is deactivated.
	 * 
	 * @param dockableWindowEvent
	 */
	public void dockableWindowDeactivated(
			DockableWindowEvent dockableWindowEvent);

	/**
	 * Fired when the Dockable Window is switched to auto-hide mode.
	 * 
	 * @param dockableWindowEvent
	 */
	public void dockableWindowAutoHidden(DockableWindowEvent dockableWindowEvent);

	/**
	 * Fired when the dockable window is switched to floating mode.
	 * 
	 * @param dockableWindowEvent
	 */
	public void dockableWindowFloated(DockableWindowEvent dockableWindowEvent);

	/**
	 * Fired when the dockable window is switched back to docking mode.
	 * 
	 * @param dockableWindowEvent
	 */
	public void dockableWindowDocked(DockableWindowEvent dockableWindowEvent);
}
