package com.nayaware.jdockers;

import java.util.EventListener;

/**
 * Any listener that listens to the Layout Manager should implement this.
 * 
 * @author Winston Prakash
 * @version 1.0
 */
public interface LayoutWindowListener extends EventListener {
	/**
	 * Fired after a Layout Window was opened (that is registered with the
	 * Layout Manager). The event object contains the reference to the Layout
	 * Window object. Triggered by
	 * {@link LayoutManager#createLayoutWindow(String, String, ImageIcon, String)}
	 * and {@link LayoutManager#openLayoutWindow(LayoutWindow)}.
	 * 
	 * @param LayoutWindowEvent
	 */
	public void layoutWindowOpened(LayoutWindowEvent layoutWindowEvent);

	/**
	 * Fired when a Layout Window has be shown. The event object contains the
	 * reference to the Layout Window object. Triggered by
	 * {@link LayoutManager#showLayoutWindow(LayoutWindow)}.
	 * 
	 * @param LayoutWindowEvent
	 */
	public void layoutWindowShown(LayoutWindowEvent layoutWindowEvent);

	/**
	 * Fired when a Document Window should be closed. The listener can veto the
	 * close operation by calling {@link LayoutWindow#setClosable(boolean)} and
	 * passing <code>false</code>. The event object contains the reference to
	 * the Layout Window object. Triggered by
	 * {@link LayoutManager#hideLayoutWindow(LayoutWindow)}.
	 * 
	 * @param LayoutWindowEvent
	 */
	public void layoutWindowHiding(LayoutWindowEvent layoutWindowEvent);

	/**
	 * Fired when a Layout Window was hidden. The event object contains the
	 * reference to the Layout Window object. Triggered by
	 * {@link LayoutManager#hideLayoutWindow(LayoutWindow)}.
	 * 
	 * @param LayoutWindowEvent
	 */
	public void layoutWindowHidden(LayoutWindowEvent layoutWindowEvent);

	/**
	 * Fired after a Layout Window was unregistred from the Layout Manager. The
	 * event object contains the reference to the Layout Window object.
	 * Triggered by {@link LayoutManager#closeLayoutWindow(LayoutWindow)}.
	 * 
	 * @param layoutWindowEvent
	 */
	public void layoutWindowClosed(LayoutWindowEvent layoutWindowEvent);

	/**
	 * Fired when a Layout Window was activated. This happens if the Layout
	 * Manager's parent window gets focus and that Layout Window was the last
	 * active window before the parent window lost focus or if the user clicks
	 * on the Layout Window. The event object contains the reference to the
	 * Layout Window object. Triggered by
	 * {@link LayoutManager#activateLayoutWindow(LayoutWindow)}.
	 * 
	 * @param LayoutWindowEvent
	 */
	public void layoutWindowActivated(LayoutWindowEvent layoutWindowEvent);

	/**
	 * Fired when a Layout Window's component is added, removed or if the active
	 * component changes. Triggered by
	 * {@link LayoutManager#updateLayoutWindow(LayoutWindow)}.
	 * 
	 * @param LayoutWindowEvent
	 */
	public void layoutWindowChanged(LayoutWindowEvent layoutWindowEvent);

	public void layoutWindowComponentAdded(LayoutWindowEvent layoutWindowEvent);

	public void layoutWindowComponentRemoved(LayoutWindowEvent layoutWindowEvent);

	/**
	 * Fired when a the user did some action to request a save of a Layout
	 * Window. The event object contains the reference to the Layout Window
	 * object.
	 * 
	 * @param LayoutWindowEvent
	 */
	public void layoutWindowSaveNeeded(LayoutWindowEvent layoutWindowEvent);
}
