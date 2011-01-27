package com.nayaware.jdockers.impl;

/**
 * A common superclass for panes like <code>RCombinedDockablePane</code> and
 * <code>RTabbedDocumentPane</code> which contain a set of windows.
 * 
 * @author Winston Prakash
 *  	   Stefan Matthias Aust
 * @version 1.0
 */
public abstract class RWindowPane extends RPane {

	// window operations
	// -------------------------------------------------------------------------

	/**
	 * Adds the specified window to the this pane. The window must not be part
	 * of this or any other pane.
	 */
	abstract void addWindow(RDockableWindow window);

	/**
	 * Removes the specified window from this pane. Fails silently if the window
	 * isn't part of this pane.
	 */
	abstract void removeWindow(RDockableWindow window);

	/**
	 * Removes the specified window from this pane and removes the pane from its
	 * parent if no windows are left after calling
	 * {@link #removeWindow(RDockableWindow)}.
	 */
	void removeWindowOrPane(RDockableWindow window) {
		removeWindow(window);
		if (getWindowCount() == 0) {
			removeFromParent();
		}
	}

	/**
	 * Returns the number of windows of this pane.
	 */
	abstract int getWindowCount();

	/**
	 * Returns an array (never <code>null</code>) with all windows of this pane.
	 */
	abstract RDockableWindow[] getWindows();

	/**
	 * Returns the index'th window of this pane.
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 */
	abstract RDockableWindow getWindow(int index);

	/**
	 * Returns the selected window or <code>null</code> if there's no window
	 * selected.
	 */
	abstract RDockableWindow getSelectedWindow();

	// window events
	// -----------------------------------------------------------------------------

	/**
	 * Called to activate the specified window of this pane. Use
	 * {@link #findWindowInHierachy(RDockableWindow)} to find the right pane.
	 */
	abstract void activateWindow(RDockableWindow window);

	/**
	 * Called to deactivate the specified window of this pane. Use
	 * {@link #findWindowInHierachy(RDockableWindow)} to find the right pane.
	 */
	abstract void deactivateWindow(RDockableWindow window);

	// -------------------------------------------------------------------------------------------

	int componentCountForTest() {
		return getWindowCount();
	}

	void updateLayoutHints() {
		RLayoutContainer lc = getLayoutContainer();
		if (lc != null) {
			for (int i = 0; i < getWindowCount(); i++) {
				lc.updateLayoutHint(getWindow(i));
			}
		}
	}

	String debugName() {
		String n = "Pane";
		for (int i = 0; i < getWindowCount(); i++) {
			n += (i == 0 ? "{" : ",") + getWindow(i).getName();
		}
		return n + "}";
	}
}
