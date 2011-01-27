package com.nayaware.jdockers.impl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JLayeredPane;

import com.nayaware.jdockers.DockableWindow;
import com.nayaware.jdockers.LayoutManager;
import com.nayaware.jdockers.LayoutWindow;

/**
 * This is the global Layout Pane. It supports Auto Hide Containers on all edges
 * which contains Auto Hide Items, a single Auto Hide Pane that can be displayed
 * on top of all other windows and a central Layout Window Container that
 * contains Dockable Windows (both directly and indirectly - then grouped by a
 * Tabbed Dockable Window Container), Document Windows (indirectly - grouped by
 * Tabbed Document Window Containers). The Layout Window Container arranges
 * Dockable Windows and Tabbed Containers with the help of Split Containers.
 * 
 * You can add and remove Dockable Windows, Document Windows and Auto Hide
 * Items. The Layout Pane also knows how to make a Window or Tabbed Container a
 * floating window.
 * 
 * It deals with Auto Hide Items, showing its pane as the current Auto Hide Pane
 * if the mouse is over an auto hide item.
 * 
 * Last but not least, it knows about the active window, maintaining a list of
 * all previous active windows to activate an old window if the currently active
 * window gets removed.
 * 
 * Finally, it has a list of associated FloatFrames.
 * 
 * @author Winston Prakash
 *  	   Stefan Matthias Aust
 * @version 1.0
 */
public class RLayoutPane extends JLayeredPane {
	// some constants
	static final int TOP = 0, LEFT = 1, BOTTOM = 2, RIGHT = 3, NONE = 4;
	static final int HORIZONTAL = 0, VERTICAL = 1;

	// the four auto hide containers - one per edge
	RAutoHideItemContainer[] autoHideItemContainer = new RAutoHideItemContainer[4];

	// the central layout window container
	private RLayoutContainer layoutContainer = new RLayoutContainer();

	// the active Auto Hide Item or null
	RAutoHideItem activeItem;

	// constructor
	// -------------------------------------------------------------------------------

	RLayoutPane() {
		super();
		setName("layoutPane");
		setLayout(new AutoHideBorderLayout());

		new RGlobalEventListener(this);

		autoHideItemContainer[TOP] = new RAutoHideItemContainer(TOP);
		autoHideItemContainer[LEFT] = new RAutoHideItemContainer(LEFT);
		autoHideItemContainer[BOTTOM] = new RAutoHideItemContainer(BOTTOM);
		autoHideItemContainer[RIGHT] = new RAutoHideItemContainer(RIGHT);

		add(autoHideItemContainer[TOP], AutoHideBorderLayout.TOP);
		add(autoHideItemContainer[LEFT], AutoHideBorderLayout.LEFT);
		add(autoHideItemContainer[BOTTOM], AutoHideBorderLayout.BOTTOM);
		add(autoHideItemContainer[RIGHT], AutoHideBorderLayout.RIGHT);

		add(layoutContainer, AutoHideBorderLayout.CENTER);

		setOpaque(true);
		// setBackground(UIManager.getColor("controlShadow"));

		// we need non-zero dimension, now
		setSize(1000, 1000);
		doLayout();
	}

	// API
	// ---------------------------------------------------------------------------------------

	/**
	 * Adds a window to the receiver's Layout Container. The window is added for
	 * the first time, so its initial docking state and side will be respected.
	 * Called from the Layout Manager.
	 */
	public void addWindow(RLayoutWindow window) {
		if (window instanceof RDocumentWindow) {
			addDocumentWindow((RDocumentWindow) window);
		} else if (window instanceof RDockableWindow) {
			addDockableWindow((RDockableWindow) window);
		} else {
			throw new Error("neither document nor dockable window: " + window);
		}
	}

	/**
	 * Removes a window from the receiver (either from its Layout Container, any
	 * of its Autohide Item Containers or any Floating Frame). Called from the
	 * Layout Manager and internally when a window changes its state.
	 */
	public void removeWindow(RLayoutWindow window) {
		if (window instanceof RDocumentWindow) {
			removeDocumentWindow((RDocumentWindow) window);
		} else if (window instanceof RDockableWindow) {
			removeDockableWindow((RDockableWindow) window);
		} else {
			throw new Error("neither document nor dockable window: " + window);
		}
	}

	/**
	 * Adds a Document Window to the receiver's Layout Container's first
	 * Document Container. The window is added for the first time, so its
	 * initial docking state and side will be respected.
	 */
	public void addDocumentWindow(RDocumentWindow window) {
		if (window.isVisible()) {
			return;
		}
		layoutContainer.addDocumentWindow(window);
		window.setDockState(RDockableWindow.DOCK_STATE_DOCKED);
		window.setVisible(true);
	}

	/**
	 * Removes a Document Window from the receiver's Layout Container's Document
	 * Container.
	 */
	public void removeDocumentWindow(RDocumentWindow window) {
		if (!window.isVisible()) {
			return;
		}
		RWindowPane pane = layoutContainer.findWindowPane(window);
		RDockableWindow nextActiveWindow = null;
		if (pane != null) {
			pane.removeWindowOrPane(window);
			nextActiveWindow = pane.getSelectedWindow();
		}
		removeLayoutWindowHook(window, nextActiveWindow);
		window.setVisible(false);
	}

	/**
	 * Adds a Dockable Window in "docking" mode to the receiver's Layout
	 * Container. The window is added for the first time, so its initial docking
	 * state and side will be respected.
	 */
	public void addDockableWindow(RDockableWindow window) {
		if (window.isVisible()) {
			return;
		}
		if (DockableWindow.DOCK_STATE_FLOATED.equals(window.getDockState())) {
			RFloatingFrame ff = new RFloatingFrame(this);
			ff.addWindow(window);
			ff.setAbsoluteBounds(window.getBounds());
			ff.show();
		} else if (DockableWindow.DOCK_STATE_AUTOHIDDEN.equals(window
				.getDockState())) {
			layoutContainer.addDockableWindow(window);
			requestAutoHide(window);
		} else {
			layoutContainer.addDockableWindow(window);
		}
		window.setVisible(true);
	}

	/**
	 * Removes a Dockable Window from the receiver's Layout Container, or any of
	 * the receiver's Autohide Item Containers or Floating Frames.
	 */
	public void removeDockableWindow(RDockableWindow window) {
		RDockableWindow nextActiveWindow = null;
		if (!window.isVisible()) {
			return;
		}
		RFloatingFrame ff = (RFloatingFrame) floatingWindows.get(window);
		if (ff != null) {
			ff.removeWindowOrPane(window);
			nextActiveWindow = ff.getSelectedWindow();
		} else {
			boolean found = false;

			for (int i = 0; i < autoHideItemContainer.length; i++) {
				for (int j = 0; j < autoHideItemContainer[i].getItemCount(); j++) {
					RAutoHideItem item = autoHideItemContainer[i].getItem(j);
					RDockableWindow[] win = item.autoHidePane.getWindows();
					for (int k = 0; k < win.length; k++) {
						if (win[k] == window) {
							item.removeWindow(window);
							if (win.length == 1) {
								removeAutoHideItem(item);
							} else {
								nextActiveWindow = item.autoHidePane
										.getSelectedWindow();
							}
							found = true;
							break;
						}
					}
				}
				if (found) {
					break;
				}
			}
			if (!found) {
				RWindowPane pane = layoutContainer.findWindowPane(window);
				if (pane != null) {
					pane.removeWindowOrPane(window);
					nextActiveWindow = pane.getSelectedWindow();
				}
			}
		}
		removeLayoutWindowHook(window, nextActiveWindow);
		window.setVisible(false);
	}

	/**
	 * Called from dropRequest to make sure that floating frames are considered.
	 */
	void removeDockableOrFloatingWindow(RDockableWindow window) {
		RFloatingFrame ff = (RFloatingFrame) floatingWindows.get(window);
		if (ff != null) {
			ff.removeWindowOrPane(window);
		} else {
			layoutContainer.removeDockableWindow(window);
		}
	}

	// ---------------------------------------------------------------------------------------

	RLayoutContainer getLayoutContainer() {
		return layoutContainer;
	}

	void removeAutoHideItem(RAutoHideItem item) {
		item.getParent().remove(item);
		resetActiveAutoHideItem(item);
	}

	/**
	 * Autohides an existing docked Dockable Window. Silently ignores windows
	 * already auto hidden or windows of Floating Frames.
	 */
	void requestAutoHide(RDockableWindow window) {
		RWindowPane pane = getLayoutContainer().findWindowPane(window);
		if (pane != null) {
			requestAutoHide(pane, window);
		}
	}

	/**
	 * Autohides all windows of the specified dockable pane. Do not call with a
	 * pane of a Floating Frame!
	 */
	void requestAutoHide(RWindowPane pane) {
		requestAutoHide(pane, null);
	}

	/**
	 * Autohides the specified window of the specified pane. If window is
	 * <code>null</code>, autohide the whole pane. Do not call with a pane of a
	 * Floating Frame!
	 */
	void requestAutoHide(RWindowPane pane, RDockableWindow window) {
		// deactive window before autohiding it
		RLayoutWindow activeWindow = getActiveWindow();
		if (activeWindow == window
				|| pane.findWindowPane((RDockableWindow) activeWindow) != null) {
			deactivateLastWindow(null);
		}
		// determine position of autohide item
		int position = pane.getParentTiledContainer().getPosition();
		// create item: default dimension is current size, then remove window
		// and/or pane
		RAutoHideItem item;
		if (window != null && pane.getWindowCount() > 1) {
			if (window.autohidePaneSize == null) {
				window.autohidePaneSize = pane.getSize();
			}
			getLayoutContainer().setupLayoutHint(window);
			pane.removeWindow(window);
			item = new RAutoHideItem(window);
		} else {
			RDockableWindow[] windows = pane.getWindows();
			for (int i = 0; i < windows.length; i++) {
				if (windows[i].autohidePaneSize == null) {
					Dimension size = pane.getSize();
					if (size.width > 0 && size.height > 0) {
						windows[i].autohidePaneSize = size;
					}
				}
				getLayoutContainer().setupLayoutHint(windows[i]);
			}
			item = new RAutoHideItem(windows, pane.getSelectedWindow());
			pane.removeFromParent();
		}
		// add initialized item and make it visible
		autoHideItemContainer[position].add(item);
		validate();
	}

	// ---------------------------------------------------------------------------------------

	/**
	 * Floats the windows if not already floating. Otherwise raise the window's
	 * Floating Frame to front. If the window is already visible, remove it from
	 * the Layout Pane first.
	 */
	public void requestFloat(RDockableWindow window) {
		RFloatingFrame ff = (RFloatingFrame) floatingWindows.get(window);
		if (ff != null) {
			ff.toFront();
		} else {
			getLayoutContainer().setupLayoutHint(window);
			if (window.isVisible()) {
				// if ahi, remove item
				// if dp, remove it
				RWindowPane pane = findWindowPane(window);
				if (pane instanceof RCombinedAutoHidePane) {
					removeAutoHideItem(((RCombinedAutoHidePane) pane).item);
				} else if (pane instanceof RCombinedDockablePane) {
					((RCombinedDockablePane) pane).removeWindowOrPane(window);
				} else {
					throw new Error(); // should not happen
				}
			} else {
				window.setVisible(true);
			}
			ff = new RFloatingFrame(this);
			ff.addWindow(window);
			ff.setAbsoluteBounds(window.getBounds());
			ff.show();
		}
	}

	/*
	 * void floatWindow(RDockableWindow window, Rectangle bounds) {
	 * floatWindows(new RDockableWindow[] { window }, bounds); }
	 */

	void floatWindows(RDockableWindow[] windows, Rectangle bounds) {
		RFloatingFrame ff = new RFloatingFrame(this);
		ff.addWindows(windows);
		if (bounds != null) {
			ff.setBounds(bounds);
			ff.show();
			ff.setBounds(bounds); // update layout hint
		} else {
			ff.setAbsoluteBounds(windows[0].getBounds());
			ff.show();
		}
	}

	// ---------------------------------------------------------------------------------------

	/**
	 * Redocks the window if not already docked.
	 */
	void requestDock(RDockableWindow window) {
		RFloatingFrame ff = (RFloatingFrame) floatingWindows.get(window);
		if (ff != null) {
			window.setBounds(ff.pane.getAbsoluteBounds());
			ff.removeWindow(window);
			if (ff.getWindowCount() == 0) {
				ff.dispose();
			}
			getLayoutContainer().redockWindows(
					new RDockableWindow[] { window }, window);
			return;
		}
		RWindowPane pane = findWindowPane(window);
		if (pane instanceof RCombinedAutoHidePane) {
			int position = ((RCombinedAutoHidePane) pane).item.getPosition();
			removeAutoHideItem(((RCombinedAutoHidePane) pane).item);
			getLayoutContainer().redockWindows(position, pane.getWindows(),
					pane.getSelectedWindow());
			return;
		}
	}

	// ---------------------------------------------------------------------------------------

	/*
	 * Restore layout from specified pile.
	 */
	void restoreLayout(RMemento memento, RLayoutManager manager) {
		// Clean up layout window container...
		List windows = manager.getAllShownDockedWindows();
		for (Iterator i = windows.iterator(); i.hasNext();) {
			removeWindow((RDockableWindow) i.next());
		}

		List shownWindows = new ArrayList();
		restoreLayoutHints(memento, manager);
		restoreLayoutFloatingWindows(memento, manager, windows, shownWindows);
		restoreLayoutAutohiddenWindows(memento, manager, windows, shownWindows);
		restoreLayoutDockedWindows(memento, manager, windows, shownWindows);

		// set active window
		RDockableWindow window = manager.findRWindow(memento
				.getString("activeWindow"));
		if (window != null) {
			manager.activateLayoutWindow(window);
		}
		validate();
		getLayoutContainer().updateLayoutHints();

		for (Iterator i = shownWindows.iterator(); i.hasNext();) {
			manager.fireWindowShown((RLayoutWindow) i.next());
		}
	}

	void restoreLayoutHints(RMemento memento, RLayoutManager manager) {
		List hints = memento.getChildren("layoutHint");
		for (int i = 0; i < hints.size(); i++) {
			RMemento hint = (RMemento) hints.get(i);
			RDockableWindow w = manager.findRWindow(hint.getString("window"));
			if (w != null) {
				getLayoutContainer().setLayoutHint(w, hint);
				w.apply(hint);
			}
		}
	}

	/*
	 * Recreates all floating frames. Existing windows (either docked, already
	 * floated or autohidden) are rearranged. A hidden window is reshown and a
	 * shown event is fired.
	 */
	void restoreLayoutFloatingWindows(RMemento memento, RLayoutManager manager,
			List openWindows, List shownWindows) {

		List floatingFrames = memento.getChildren("floatingFrame");
		for (Iterator i = floatingFrames.iterator(); i.hasNext();) {
			RMemento frame = (RMemento) i.next();
			RMemento pane = frame.getChild("pane");
			if (pane != null) {
				List windows = getWindows(pane, manager, openWindows,
						shownWindows);
				if (windows.size() > 0) {
					RFloatingFrame ff = new RFloatingFrame(this);
					for (Iterator j = windows.iterator(); j.hasNext();) {
						RDockableWindow w = (RDockableWindow) j.next();
						ff.addWindow(w);
						floatingWindows.put(w, ff);
					}
					Dimension size = ff.getPreferredSize();
					ff.setAbsoluteBounds(new Rectangle(or(
							frame.getInteger("x"), 0), or(
							frame.getInteger("y"), 0), or(
							frame.getInteger("width"), size.width), or(
							frame.getInteger("height"), size.height)));
					ff.show();
				}
			}
		}
	}

	private List getWindows(RMemento memento, RLayoutManager manager,
			List openWindows, List shownWindows) {
		List names = memento.getChildren("window");
		List windows = new ArrayList(names.size());
		for (Iterator j = names.iterator(); j.hasNext();) {
			String name = ((RMemento) j.next()).getString("name");
			RDockableWindow window = manager.findRWindow(name);
			if (window != null) {
				if (window.isVisible()) {
					removeWindow(window);
				} else if (!openWindows.remove(window)) {
					shownWindows.add(window);
				}
				windows.add(window);
				window.setVisible(true);
			}
		}
		return windows;
	}

	/*
	 * Recreate all auto hide items. Existing windows (either docked, already
	 * autohidden or floated) are rearranged. A hidden window is reshown and a
	 * shown event is fired.
	 */
	void restoreLayoutAutohiddenWindows(RMemento memento,
			RLayoutManager manager, List openWindows, List shownWindows) {
		List autoHideContainers = memento.getChildren("autoHideItemContainer");
		for (Iterator i = autoHideContainers.iterator(); i.hasNext();) {
			RMemento container = (RMemento) i.next();
			int position = or(container.getInteger("position"),
					RLayoutPane.RIGHT);
			List items = container.getChildren("autoHideItem");
			for (Iterator j = items.iterator(); j.hasNext();) {
				RMemento item = (RMemento) j.next();
				List windows = getWindows(item, manager, openWindows,
						shownWindows);
				if (windows.size() > 0) {
					RDockableWindow window = manager.findRWindow(item
							.getString("selectedWindow"));
					autoHideItemContainer[position]
							.add(new RAutoHideItem(
									(RDockableWindow[]) windows
											.toArray(new RDockableWindow[windows
													.size()]), window));
				}
			}
		}
	}

	/*
	 * Recreate all docked windows. Existing windows (already docked or
	 * autohidden or floated) are rearranged. A hidden window is reshown and a
	 * shown event is fired.
	 */
	void restoreLayoutDockedWindows(RMemento memento, RLayoutManager manager,
			List openWindows, List shownWindows) {

		memento = memento.getChild("layoutContainer");
		if (memento != null) {
			memento = memento.getChild("pane");
			RPane pane = readLayoutOfPane(memento, manager, openWindows,
					shownWindows);
			if (pane != null) {
				getLayoutContainer().add(pane, BorderLayout.CENTER);
			}
		}
	}

	private RPane readLayoutOfPane(RMemento memento, RLayoutManager manager,
			List openWindows, List shownWindows) {
		return readLayoutOfPaneImpl(memento, manager, openWindows, shownWindows);
	}

	private RPane readLayoutOfPaneImpl(RMemento memento,
			RLayoutManager manager, List openWindows, List shownWindows) {
		if (memento == null) {
			return null;
		}
		String type = memento.getString("type");
		if (type == null) {
			return null;
		} else if ("splitPane".equals(type)) {
			List panes = memento.getChildren("pane");
			RPane topLeftPane = readLayoutOfPane((RMemento) panes.get(0),
					manager, openWindows, shownWindows);
			RPane bottomRightPane = readLayoutOfPane((RMemento) panes.get(1),
					manager, openWindows, shownWindows);
			if (topLeftPane == null) {
				return bottomRightPane;
			}
			if (bottomRightPane == null) {
				return topLeftPane;
			}
			RSplitPane pane = new RSplitPane(or(
					memento.getInteger("orientation"), 0), or(
					memento.getDouble("ltor"), 0) == 0, or(
					memento.getDouble("weight"), 0.5));
			pane.setTopLeftPane(topLeftPane);
			pane.setBottomRightPane(bottomRightPane);
			return pane;
		} else if ("documentContainer".equals(type)) {
			List windows = getWindows(memento, manager, openWindows,
					shownWindows);
			for (Iterator i = windows.iterator(); i.hasNext();) {
				RDockableWindow w = (RDockableWindow) i.next();
				if (!LayoutManager.TYPE_DOCUMENT.equals(w.getType())) {
					i.remove();
				}
			}
			if (windows.isEmpty()) {
				return null;
			}
			RTabbedDocumentPane pane = new RTabbedDocumentPane();
			for (Iterator i = windows.iterator(); i.hasNext();) {
				pane.addWindow((RDockableWindow) i.next());
			}
			RDockableWindow sw = manager.findRWindow(memento
					.getString("selectedWindow"));
			if (sw != null && LayoutManager.TYPE_DOCUMENT.equals(sw.getType())) {
				pane.getTabbedPane().setSelectedIndex(windows.indexOf(sw));
			}
			return pane;
		} else if ("combinedDockableContainer".equals(type)) {
			List windows = getWindows(memento, manager, openWindows,
					shownWindows);
			for (Iterator i = windows.iterator(); i.hasNext();) {
				RDockableWindow w = (RDockableWindow) i.next();
				if (!LayoutManager.TYPE_DOCKABLE.equals(w.getType())) {
					i.remove();
				}
			}
			if (windows.isEmpty()) {
				return null;
			}
			RCombinedDockablePane pane = new RCombinedDockablePane();
			for (Iterator i = windows.iterator(); i.hasNext();) {
				pane.addWindow((RDockableWindow) i.next());
			}
			RDockableWindow sw = manager.findRWindow(memento
					.getString("selectedWindow"));
			if (sw != null && LayoutManager.TYPE_DOCKABLE.equals(sw.getType())) {
				pane.setSelectedIndex(windows.indexOf(sw));
			}
			return pane;
		} else if ("tiledContainer".equals(type)) {
			int position = memento.getInteger("position").intValue();
			RPane pane = readLayoutOfPane(memento.getChild("pane"), manager,
					openWindows, shownWindows);
			if (pane != null) {
				RTiledContainer tc = getLayoutContainer()
						.getContainer(position);
				tc.dockPaneAt(RLayoutPane.RIGHT, pane, tc.getBasePane(), 0.5);
				return tc;
			} else if (position == RLayoutPane.NONE) {
				return getLayoutContainer().getContainer(position);
			}
			return null;
		} else {
			throw new Error("unknown pane type: " + type); // should not happen
		}
	}

	private static int or(Integer value, int defaultValue) {
		return value == null ? defaultValue : value.intValue();
	}

	private static double or(Double value, double defaultValue) {
		return value == null ? defaultValue : value.doubleValue();
	}

	/**
	 * Saves the current layout. For each window shown, we store whether it is
	 * floating, autohidden or docked along with its layout hints. Then, we
	 * recursively store the layout tree hierachy of all panes.
	 */
	void saveLayout(RMemento memento, RLayoutManager manager) {
		saveLayoutHints(memento, manager);
		saveLayoutFloatingWindows(memento);
		saveLayoutAutohiddenWindows(memento);
		saveLayoutDockedWindows(memento);
		saveLayoutActiveWindow(memento);
	}

	void saveLayoutHints(RMemento memento, RLayoutManager manager) {
		LayoutWindow[] windows = manager.getAllLayoutWindows();
		for (int i = 0; i < windows.length; i++) {
			RDockableWindow window = (RDockableWindow) windows[i];
			if (window.isVisible()) {
				getLayoutContainer().updateLayoutHint(window);
			}
			RMemento m = getLayoutContainer().getLayoutHint(window);
			if (m != null) {
				memento.putMemento(m);
			}
		}
	}

	void saveLayoutFloatingWindows(RMemento memento) {
		Set floatingFrames = new HashSet(floatingWindows.values());
		for (Iterator i = floatingFrames.iterator(); i.hasNext();) {
			((RFloatingFrame) i.next()).saveLayout(memento
					.createMemento("floatingFrame"));
		}
	}

	void saveLayoutAutohiddenWindows(RMemento memento) {
		for (int i = 0; i < autoHideItemContainer.length; i++) {
			autoHideItemContainer[i].saveLayout(memento);
		}
	}

	void saveLayoutDockedWindows(RMemento memento) {
		layoutContainer.saveLayout(memento);
	}

	void saveLayoutActiveWindow(RMemento memento) {
		RLayoutWindow activeWindow = getActiveWindow();
		if (activeWindow != null) {
			memento.putString("activeWindow", activeWindow.getName());
		}
	}

	RLayoutWindow getActiveWindow() {
		if (lastActiveWindowList.isEmpty()) {
			return null;
		}
		return (RLayoutWindow) lastActiveWindowList.getLast();
	}

	// ---------------------------------------------------------------------------------------

	/**
	 * Activates an Auto Hide Item displaying the associated Auto Hide Pane.
	 */
	void setActiveAutoHideItem(RAutoHideItem item) {
		if (activeItem != item) {
			if (activeItem != null) {
				remove(activeItem.autoHidePane);
				repaint();
			}
			activeItem = item;
			if (activeItem != null) {
				setLayer(activeItem.autoHidePane, 100);
				add(activeItem.autoHidePane, AutoHideBorderLayout.AUTOHIDE, 0);
				activeItem.autoHidePane.repaint();
			}
			validate();
		}
	}

	/**
	 * Deactivates an Auto Hide Item, hiding the associated Auto Hide Pane if
	 * there is no newer Auto Hide Item active.
	 */
	void resetActiveAutoHideItem(RAutoHideItem item) {
		if (activeItem == item) {
			remove(activeItem.autoHidePane);
			repaint();
			activeItem = null;
		}
		validate();
	}

	// ---------------------------------------------------------------------------------------

	LinkedList lastActiveWindowList = new LinkedList();

	Map floatingWindows = new HashMap();

	/**
	 * Activates a Dockable Window or Document Window. Does nothing if the
	 * window is already active. Otherwise, set window as new active window and
	 * deactivate the last active window - if any. If the new window is
	 * autohidden, do nothing. Otherwise select title bar and/or select tab. If
	 * contained in a floating frame, activate that window and pull it to front.
	 */
	boolean activateLayoutWindow(RDockableWindow window) {
		// window is already active
		if (window == getActiveWindow()) {
			RFloatingFrame ff = (RFloatingFrame) floatingWindows.get(window);
			if (ff != null) {
				ff.toFront();
			} else {
				RWindowPane pane = findWindowPane(window);
				if (pane instanceof RCombinedAutoHidePane) {
					setActiveAutoHideItem(((RCombinedAutoHidePane) pane).item);
				}
			}
			return false;
		}
		// if there's an auto hide pane an another window gets activated, hide
		// that pane
		if (activeItem != null
				&& activeItem.autoHidePane.getSelectedWindow() != window) {
			resetActiveAutoHideItem(activeItem);
		}
		// search floating frames...
		RFloatingFrame ff = (RFloatingFrame) floatingWindows.get(window);
		if (ff != null) {
			deactivateLastWindow(window);
			if (!ff.isActive()) {
				ff.toFront();
			}
			ff.requestFocus();
			ff.activateWindow(window);
			return true;
		}

		// search document windows...
		// search auto hide items...
		// search dockable windows...
		RWindowPane pane = findWindowPane(window);
		if (pane != null) {
			deactivateLastWindow(window);
			pane.requestFocus();
			pane.activateWindow(window);
			if (pane instanceof RCombinedAutoHidePane) {
				setActiveAutoHideItem(((RCombinedAutoHidePane) pane).item);
			}
		}
		return true;
	}

	RWindowPane findWindowPane(RDockableWindow window) {
		RFloatingFrame ff = (RFloatingFrame) floatingWindows.get(window);
		if (ff != null) {
			return ff.pane.findWindowPane(window);
		}
		for (int i = 0; i < autoHideItemContainer.length; i++) {
			RWindowPane pane = autoHideItemContainer[i].findWindowPane(window);
			if (pane != null) {
				return pane;
			}
		}
		return getLayoutContainer().findWindowPane(window);
	}

	/**
	 * A window is removed. If it was the active window, transfer focus to
	 * "nextActiveWindow" - if available, or pick the next window in the list -
	 * if existing.
	 */
	void removeLayoutWindowHook(RDockableWindow window,
			RDockableWindow nextActiveWindow) {
		if (window == getActiveWindow()) {
			if (nextActiveWindow != null) {
				nextActiveWindow.activate();
				/*
				 * #1 } else if (lastActiveWindowList.size() > 1) {
				 * ((RDockableWindow)
				 * lastActiveWindowList.get(lastActiveWindowList.size() -
				 * 2)).activate();
				 */
				/*
				 * #2 } else { lastActiveWindowList.clear();
				 */
				/* #3 */
			} else {
				for (int i = lastActiveWindowList.size() - 2; i >= 0; i--) {
					RDockableWindow w = (RDockableWindow) lastActiveWindowList
							.get(i);
					RWindowPane p = findWindowPane(w);
					if (p != null && !(p instanceof RCombinedAutoHidePane)) {
						w.activate();
						break;
					}
				}
			}
		}
		lastActiveWindowList.remove(window);
	}

	/**
	 * Deactivates the currently active window. The window gets replaced with
	 * the specified window.
	 */
	void deactivateLastWindow(RDockableWindow newWindow) {
		RDockableWindow oldWindow = (RDockableWindow) getActiveWindow();
		if (oldWindow != null) {
			RFloatingFrame ff = (RFloatingFrame) floatingWindows.get(oldWindow);
			if (ff == null) {
				RWindowPane oldPane = findWindowPane(oldWindow);
				if (oldPane != null) {
					if (activeItem == null
							|| activeItem.autoHidePane != oldPane) {
						oldPane.deactivateWindow(oldWindow);
					}
				}
			} else {
				ff.deactivateWindow(oldWindow);
			}
			oldWindow.fireWindowDeactivated();
		}
		if (newWindow != null) {
			lastActiveWindowList.remove(newWindow);
			lastActiveWindowList.addLast(newWindow);
		} else {
			lastActiveWindowList.clear();
		}
	}

	// ---------------------------------------------------------------------------------------

	// for unit testing only
	public int componentCountForTest() {
		return layoutContainer.componentCountForTest();
	}

	// -------------------------------------------------------------------------------------------

	/**
	 * This specialized BorderLayout-like LayoutManager knows how to layout an
	 * additional sixth component on top of the center component to implement
	 * Auto Hide Panes.
	 */
	static class AutoHideBorderLayout implements java.awt.LayoutManager {

		static final String TOP = "Top";
		static final String LEFT = "Left";
		static final String BOTTOM = "Bottom";
		static final String RIGHT = "Right";
		static final String CENTER = "Center";
		static final String AUTOHIDE = "AutoHide";

		static final int HGAP = 2;
		static final int VGAP = 2;

		private Component top, left, bottom, right, center, autohide;

		public void addLayoutComponent(Component comp, Object constraints) {
			addLayoutComponent((String) constraints, comp);
		}

		public void addLayoutComponent(String name, Component comp) {
			if (TOP.equals(name)) {
				top = comp;
			} else if (LEFT.equals(name)) {
				left = comp;
			} else if (BOTTOM.equals(name)) {
				bottom = comp;
			} else if (RIGHT.equals(name)) {
				right = comp;
			} else if (CENTER.equals(name) || name == null) {
				center = comp;
			} else if (AUTOHIDE.equals(name)) {
				autohide = comp;
			} else {
				throw new IllegalArgumentException("Unkown constraint: " + name);
			}
		}

		public void removeLayoutComponent(Component comp) {
			if (top == comp) {
				top = null;
			} else if (left == comp) {
				left = null;
			} else if (bottom == comp) {
				bottom = null;
			} else if (right == comp) {
				right = null;
			} else if (center == comp) {
				center = null;
			} else if (autohide == comp) {
				autohide = null;
			}
		}

		public void layoutContainer(Container target) {
			Insets insets = target.getInsets();
			int wleft = left != null ? left.getPreferredSize().width : 0;
			int gleft = wleft > 0 ? HGAP : 0;
			int wright = right != null ? right.getPreferredSize().width : 0;
			int gright = wright > 0 ? HGAP : 0;
			int x = insets.left + wleft + gleft;
			int y = insets.top;
			int w = target.getWidth() - x - insets.right - wright - gright;
			int h = target.getHeight() - y - insets.bottom;

			if (top != null) {
				int htop = top.getPreferredSize().height;
				int gtop = htop > 0 ? VGAP : 0;
				top.setBounds(x, y, w, htop);
				y += htop + gtop;
				h -= htop + gtop;
			}
			if (bottom != null) {
				int hbottom = bottom.getPreferredSize().height;
				int gbottom = hbottom > 0 ? VGAP : 0;
				h -= hbottom;
				bottom.setBounds(x, y + h, w, hbottom);
				h -= gbottom;
			}
			if (left != null) {
				left.setBounds(x - wleft - gleft, y, wleft, h);
			}
			if (right != null) {
				right.setBounds(x + w + gright, y, wright, h);
			}
			if (center != null) {
				// System.out.println("set bounds " + x + "," + y + "," + w +
				// "," + h);
				center.setBounds(x, y, w, h);
			}

			if (autohide != null) {
				int w3 = w / 3;
				int h3 = h / 3;
				if (autohide instanceof RCombinedAutoHidePane) {
					RDockableWindow window = ((RCombinedAutoHidePane) autohide)
							.getSelectedWindow();
					Dimension size = window == null ? null
							: window.autohidePaneSize;
					if (size != null) {
						w3 = Math.min(size.width, w);
						h3 = Math.min(size.height, h);
					}
				}
				switch (((RCombinedAutoHidePane) autohide).item.getPosition()) {
				case RLayoutPane.TOP:
					autohide.setBounds(x, y - VGAP, w, h3);
					break;
				case RLayoutPane.LEFT:
					autohide.setBounds(x - gleft, y, w3, h);
					break;
				case RLayoutPane.BOTTOM:
					autohide.setBounds(x, y + h - h3 + VGAP, w, h3);
					break;
				case RLayoutPane.RIGHT:
					autohide.setBounds(x + w - w3 + gright, y, w3, h);
					break;
				default:
					throw new Error(); // should never happen
				}
			}
		}

		public Dimension preferredLayoutSize(Container parent) {
			return layoutSize(parent, true);
		}

		public Dimension minimumLayoutSize(Container parent) {
			return layoutSize(parent, false);
		}

		private Dimension layoutSize(Container parent, boolean preferred) {
			Dimension dim = center != null ? (preferred ? center
					.getPreferredSize() : center.getMinimumSize())
					: new Dimension();
			Insets insets = parent.getInsets();
			int w = insets.left + insets.right, h = insets.top + insets.bottom;
			if (top != null) {
				Dimension d = preferred ? top.getPreferredSize() : top
						.getMinimumSize();
				dim.width = Math.max(dim.width, d.width);
				h += d.height + (d.height > 0 ? VGAP : 0);
			}
			if (bottom != null) {
				Dimension d = preferred ? bottom.getPreferredSize() : bottom
						.getMinimumSize();
				dim.width = Math.max(dim.width, d.width);
				h += d.height + (d.height > 0 ? VGAP : 0);
			}
			if (left != null) {
				Dimension d = preferred ? left.getPreferredSize() : left
						.getMinimumSize();
				dim.height = Math.max(dim.height, d.height);
				w += d.width + (d.width > 0 ? HGAP : 0);
			}
			if (right != null) {
				Dimension d = preferred ? right.getPreferredSize() : right
						.getMinimumSize();
				dim.height = Math.max(dim.height, d.height);
				w += d.width + (d.width > 0 ? HGAP : 0);
			}
			dim.width += w;
			dim.height += h;
			return dim;
		}
	}
}