package com.nayaware.jdockers.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import com.nayaware.jdockers.*;

/**
 * This class implements the central Layout Manager that manages all Dockable
 * and Document Windows.
 * 
 * @author Winston Prakash
 *  	   Stefan Matthias Aust
 * @version 1.0
 */
public class RLayoutManager implements LayoutManager {

	/**
	 * List of listeners for <code>LayoutWindowEvent</code>s.
	 */
	private Vector listeners = new Vector(1);

	/**
	 * Reference to the Layout Pane used to display all registered windows.
	 */
	private RLayoutPane layoutPane = new RLayoutPane();

	/**
	 * Registry of all opened and created Layout Windows.
	 */
	private Map windowRegistry = new HashMap();

	/**
	 * Registry of all known Window Set definitions.
	 */
	private Map windowSetRegistry = new HashMap();

	// listeners
	// ---------------------------------------------------------------------------------

	public void addLayoutWindowListener(LayoutWindowListener listener) {
		if (listener == null) {
			log("addLayoutWindowListener: called with listener == null");
			throw new NullPointerException();
		}
		listeners.add(listener);
	}

	public void removeLayoutWindowListener(LayoutWindowListener listener) {
		if (listener == null) {
			log("removeLayoutWindowListener: called with listener == null");
		}
		listeners.remove(listener);
	}

	/*
	 * Returns a copy of the Layout Manager listeners.
	 */
	Vector getListeners() {
		return (Vector) listeners.clone();
	}

	// accessors
	// ---------------------------------------------------------------------------------

	public JComponent getLayoutPane() {
		return layoutPane;
	}

	// layout window factory
	// ---------------------------------------------------------------------

	public LayoutWindow createLayoutWindow(String name, String title,
			ImageIcon icon, String type) {

		if (windowRegistry.containsKey(name)) {
			log("createLayoutWindow: name already in use: ", name);
			throw new LayoutManagerException("name already in use: " + name);
		}

		RLayoutWindow w;
		if (TYPE_DOCKABLE.equals(type)) {
			w = new RDockableWindow(this, name);
		} else if (TYPE_DOCUMENT.equals(type)) {
			w = new RDocumentWindow(this, name);
		} else {
			log("createLayoutWindow: unknown type: ", type);
			throw new IllegalArgumentException("unknown type: " + type);
		}
		w.setTitle(title);
		w.setIcon(icon);
		w.setType(type);

		openLayoutWindowImpl(w);

		return w;
	}

	// layout window operations
	// ------------------------------------------------------------------

	public void openLayoutWindow(LayoutWindow layoutWindow) {
		checkLayoutManager(layoutWindow);

		String name = layoutWindow.getName();
		if (windowRegistry.containsKey(name)) {
			log("openLayoutWindow: tried to open window twice: ", name);
			throw new LayoutManagerException("tried to open window twice: "
					+ name);
		}

		openLayoutWindowImpl(layoutWindow);
	}

	private void openLayoutWindowImpl(LayoutWindow layoutWindow) {
		windowRegistry.put(layoutWindow.getName(), layoutWindow);
		fireWindowOpened((RLayoutWindow) layoutWindow);
	}

	public void closeLayoutWindow(LayoutWindow layoutWindow) {
		String name = layoutWindow.getName();
		if (isVisible(layoutWindow)) {
			log("closeLayoutWindow: try to close shown window: ", name);
			return;
		} else if (windowRegistry.remove(name) == null) {
			log("closeLayoutWindow: try to close unopened window: ", name);
			return;
		}
		fireWindowClosed((RLayoutWindow) layoutWindow);
	}

	public void showLayoutWindow(LayoutWindow layoutWindow) {
		if (isVisible(layoutWindow)) {
			return;
		}

		RLayoutWindow window = (RLayoutWindow) layoutWindow;
		layoutPane.addWindow(window);
		// fireWindowShown(window);
	}

	public void autohideLayoutWindow(LayoutWindow layoutWindow) {
		((DockableWindow) layoutWindow)
				.setInitialDockState(DockableWindow.DOCK_STATE_AUTOHIDDEN);
		showLayoutWindow(layoutWindow);
	}

	public void hideLayoutWindow(LayoutWindow layoutWindow) {
		if (!isVisible(layoutWindow)) {
			return;
		}

		RLayoutWindow window = (RLayoutWindow) layoutWindow;
		boolean isDocumentWindow = TYPE_DOCUMENT.equals(window.getType());

		if (isDocumentWindow) {
			Vector l = getListeners();
			int size = l.size();
			if (size > 0) {
				RLayoutWindowEvent e = new RLayoutWindowEvent(this, window);
				for (Iterator i = l.iterator(); i.hasNext();) {
					((LayoutWindowListener) i.next()).layoutWindowHiding(e);
					if (!layoutWindow.isClosable()) {
						return;
					}
				}
			}
		}
		layoutPane.removeWindow(window);
		// fireWindowHidden(window);
	}

	public boolean isVisible(LayoutWindow layoutWindow) {
		checkLayoutManager(layoutWindow);
		return ((RLayoutWindow) layoutWindow).isVisible();
	}

	public void activateLayoutWindow(LayoutWindow layoutWindow) {
		if (!isVisible(layoutWindow)) {
			return;
		}

		if (layoutPane.activateLayoutWindow((RDockableWindow) layoutWindow)) {
			fireWindowActivated((RLayoutWindow) layoutWindow);
		}
	}

	public void updateLayoutWindow(LayoutWindow layoutWindow) {
		checkLayoutManager(layoutWindow);

		((RLayoutWindow) layoutWindow).fireComponentChanged(null);
	}

	public LayoutWindow findLayoutWindow(String layoutWindowName) {
		return findWindow(layoutWindowName, null);
	}

	public LayoutWindow[] getAllLayoutWindows() {
		return (LayoutWindow[]) windowRegistry.values().toArray(
				new LayoutWindow[windowRegistry.size()]);
	}

	public void hideAllLayoutWindows() {
		hideAll(null);
	}

	public void closeAllLayoutWindows() {
		closeAll(null);
	}

	// firing events
	// -----------------------------------------------------------------------------

	void fireWindowOpened(RLayoutWindow window) {
		Vector l = getListeners();
		int size = l.size();
		if (size > 0) {
			RLayoutWindowEvent e = new RLayoutWindowEvent(this, window);
			for (int i = 0; i < size; i++) {
				((LayoutWindowListener) l.get(i)).layoutWindowOpened(e);
			}
		}
	}

	void fireWindowClosed(RLayoutWindow window) {
		Vector l = getListeners();
		int size = l.size();
		if (size > 0) {
			RLayoutWindowEvent e = new RLayoutWindowEvent(this, window);
			for (int i = 0; i < size; i++) {
				((LayoutWindowListener) l.get(i)).layoutWindowClosed(e);
			}
		}
	}

	void fireWindowShown(RLayoutWindow window) {
		Vector l = getListeners();
		int size = l.size();
		if (size > 0) {
			RLayoutWindowEvent e = new RLayoutWindowEvent(this, window);
			for (int i = 0; i < size; i++) {
				((LayoutWindowListener) l.get(i)).layoutWindowShown(e);
			}
		}
	}

	void fireWindowHidden(RLayoutWindow window) {
		Vector l = getListeners();
		int size = l.size();
		if (size > 0) {
			RLayoutWindowEvent e = new RLayoutWindowEvent(this, window);
			for (int i = 0; i < size; i++) {
				((LayoutWindowListener) l.get(i)).layoutWindowHidden(e);
			}
		}
	}

	void fireWindowActivated(RLayoutWindow window) {
		Vector l = getListeners();
		int size = l.size();
		if (size > 0) {
			RLayoutWindowEvent e = new RLayoutWindowEvent(this, window);
			for (int i = 0; i < size; i++) {
				((LayoutWindowListener) l.get(i)).layoutWindowActivated(e);
			}
		}
	}

	void fireWindowDeactivated(LayoutWindow window) {
		((RDockableWindow) window).fireWindowDeactivated();
	}

	void fireSaveNeeded(RLayoutWindow window) {
		Vector l = (Vector) listeners.clone();
		int size = l.size();
		if (size > 0) {
			RLayoutWindowEvent e = new RLayoutWindowEvent(this, window);
			for (Iterator i = l.iterator(); i.hasNext();) {
				((LayoutWindowListener) i.next()).layoutWindowSaveNeeded(e);
			}
		}
	}

	// dockable window factory & operations
	// ------------------------------------------------------

	public DockableWindow createDockableWindow(String name, String title,
			ImageIcon icon) {
		return (DockableWindow) createLayoutWindow(name, title, icon,
				TYPE_DOCKABLE);
	}

	public DockableWindow[] getAllDockableWindows() {
		List result = new ArrayList(windowRegistry.size());
		for (Iterator i = windowRegistry.values().iterator(); i.hasNext();) {
			RLayoutWindow w = (RLayoutWindow) i.next();
			if (TYPE_DOCKABLE.equals(w.getType())) {
				result.add(w);
			}
		}
		return (DockableWindow[]) result.toArray(new DockableWindow[result
				.size()]);
	}

	public DockableWindow findDockableWindow(String dockableWindowName) {
		return (DockableWindow) findWindow(dockableWindowName, TYPE_DOCKABLE);
	}

	public void hideAllDockableWindows() {
		hideAll(TYPE_DOCKABLE);
	}

	public void closeAllDockableWindows() {
		closeAll(TYPE_DOCKABLE);
	}

	public void autohideDockableWindow(DockableWindow dockableWindow) {
		checkLayoutManager(dockableWindow);
		layoutPane.requestAutoHide((RDockableWindow) dockableWindow);
	}

	public void floatDockableWindow(DockableWindow dockableWindow) {
		checkLayoutManager(dockableWindow);
		layoutPane.requestFloat((RDockableWindow) dockableWindow);
	}

	public void redockDockableWindow(DockableWindow dockableWindow) {
		checkLayoutManager(dockableWindow);
		layoutPane.requestDock((RDockableWindow) dockableWindow);
	}

	// document window factory & operations
	// ------------------------------------------------------

	public DocumentWindow createDocumentWindow(String name, String title,
			ImageIcon icon) {
		return (DocumentWindow) createLayoutWindow(name, title, icon,
				TYPE_DOCUMENT);
	}

	public DocumentWindow findDocumentWindow(String documentWindowName) {
		return (DocumentWindow) findWindow(documentWindowName, TYPE_DOCUMENT);
	}

	public DocumentWindow[] getAllDocumentWindows() {
		List result = new ArrayList(windowRegistry.size());
		for (Iterator i = windowRegistry.values().iterator(); i.hasNext();) {
			RLayoutWindow w = (RLayoutWindow) i.next();
			if (TYPE_DOCUMENT.equals(w.getType())) {
				result.add(w);
			}
		}
		return (DocumentWindow[]) result.toArray(new DocumentWindow[result
				.size()]);
	}

	public void hideAllDocumentWindows() {
		hideAll(TYPE_DOCUMENT);
	}

	public void closeAllDocumentWindows() {
		closeAll(TYPE_DOCUMENT);
	}

	// window sets
	// -------------------------------------------------------------------------------

	public void addWindowSet(String windowSetName, Set windowNames) {
		if (windowSetName == null) {
			throw new NullPointerException();
		}
		for (Iterator i = windowNames.iterator(); i.hasNext();) {
			if (!(i.next() instanceof String)) {
				throw new IllegalArgumentException();
			}
		}
		if (windowSetRegistry.containsKey(windowSetName)) {
			log("addWindowSet: overwriting window set name: ", windowSetName);
		}
		windowSetRegistry.put(windowSetName, new HashSet(windowNames));
	}

	public void removeWindowSet(String windowSetName) {
		if (windowSetRegistry.remove(windowSetName) == null) {
			log("removeWindowSet: unkown window set name: ", windowSetName);
		}
	}

	public void showWindowSet(String windowSetName) {
		Set windowNames = (Set) windowSetRegistry.get(windowSetName);
		if (windowNames != null) {
			for (Iterator i = windowNames.iterator(); i.hasNext();) {
				Object name = i.next();
				LayoutWindow w = (LayoutWindow) windowRegistry.get(name);
				if (w != null) {
					showLayoutWindow(w);
				} else {
					log("showWindowSet: unknown window name: ", name);
				}
			}
		} else {
			log("showWindowSet: can't show unkown window set named: ",
					windowSetName);
		}
	}

	public void hideWindowSet(String windowSetName) {
		Set windowNames = (Set) windowSetRegistry.get(windowSetName);
		if (windowNames != null) {
			for (Iterator i = windowNames.iterator(); i.hasNext();) {
				LayoutWindow window = (LayoutWindow) windowRegistry.get(i
						.next());
				if (window != null) {
					hideLayoutWindow(window);
				}
			}
		} else {
			log("can't hide unkown window set named: ", windowSetName);
		}
	}

	public void showWindowSet(Set windowNames) {
		addWindowSet("default", windowNames);
		showWindowSet("default");
	}

	// layouts
	// -----------------------------------------------------------------------------------

	public void saveLayout(File layoutFile) throws IOException {
		if (layoutFile == null) {
			throw new NullPointerException();
		}
		String layout = getLayout();
		FileWriter f = new FileWriter(layoutFile);
		try {
			f.write(layout);
		} finally {
			f.close();
		}
	}

	public void loadLayout(File layoutFile) throws IOException {
		applyLayoutFrom(new BufferedReader(new FileReader(layoutFile)));
	}

	public String getLayout() {
		StringWriter w = new StringWriter(4096);
		try {
			saveLayoutTo(w);
			w.close();
		} catch (IOException e) {
			throw new Error(e); // should never happen
		}
		return w.toString();
	}

	public void setLayout(String layoutData) throws IOException {
		applyLayoutFrom(new StringReader(layoutData));
	}

	// logging
	// -----------------------------------------------------------------------------------

	private boolean debug;

	public void enableDebug(boolean enable) {
		debug = enable;
	}

	public void log(String message) {
		log(message, "");
	}

	public void log(String message, Object arg) {
		if (debug) {
			System.err.print("winsys: ");
			System.err.print(message);
			System.err.print(arg);
			System.err.println();
		}
	}

	// internal methods
	// --------------------------------------------------------------------------

	/**
	 * Checks whether the specified Layout Window belongs to this Layout
	 * Manager.
	 * 
	 * @throws NullPointerException
	 *             if layoutWindow is <code>null</code>
	 * @throws LayoutManagerException
	 *             if wrong Layout Manager
	 */
	void checkLayoutManager(LayoutWindow layoutWindow) {
		if (((RLayoutWindow) layoutWindow).getLayoutManager() != this) {
			log("a layout window was created by a different layout manager");
			throw new LayoutManagerException("layout manager mismatch");
		}
	}

	/**
	 * Finds a window by name and optionally by type.
	 */
	LayoutWindow findWindow(String layoutWindowName, String type) {
		RLayoutWindow w = (RLayoutWindow) windowRegistry.get(layoutWindowName);
		if (w != null && type != null && !type.equals(w.getType())) {
			w = null;
		}
		return w;
	}

	/**
	 * Hides all windows or all windows of a given type. Please note, this might
	 * fire a cascade of hiding events and listeners may veto the hide.
	 */
	void hideAll(String type) {
		for (Iterator i = windowRegistry.values().iterator(); i.hasNext();) {
			RLayoutWindow w = (RLayoutWindow) i.next();
			if (type == null || type.equals(w.getType())) {
				hideLayoutWindow(w);
			}
		}
	}

	/**
	 * Closes all windows or all windows of a given type.
	 */
	void closeAll(String type) {
		for (Iterator i = new ArrayList(windowRegistry.values()).iterator(); i
				.hasNext();) {
			RLayoutWindow w = (RLayoutWindow) i.next();
			if (type == null || type.equals(w.getType())) {
				closeLayoutWindow(w);
			}
		}
	}

	/**
	 * Applies the layout to the Layout Pane after completely reading it. This
	 * is important so that an IOException will either occur before or after but
	 * never while the layout is modified.
	 */
	void applyLayoutFrom(Reader r) throws IOException {
		try {
			layoutPane
					.restoreLayout(RMemento.read(new BufferedReader(r)), this);
		} finally {
			r.close();
		}
	}

	/**
	 * Saves the current layout to the specified writer.
	 */
	void saveLayoutTo(Writer w) throws IOException {
		RMemento memento = new RMemento("layout");
		layoutPane.saveLayout(memento, this);
		PrintWriter pw = new PrintWriter(w);
		pw.println("<?xml version=\"1.0\"?>");
		memento.write(pw);
		pw.flush();
	}

	/* Callback from LayoutPane#readLayout(Pile, RLayoutManager) */
	RDockableWindow findRWindow(String name) {
		return (RDockableWindow) windowRegistry.get(name);
	}

	/* Another callback from LayoutPane#readLayout(Pile, RLayoutManager) */
	List getAllShownDockedWindows() {
		List windows = new ArrayList(windowRegistry.size());
		for (Iterator i = windowRegistry.values().iterator(); i.hasNext();) {
			RDockableWindow w = (RDockableWindow) i.next();
			if (w.isVisible()
					&& LayoutManager.TYPE_DOCKABLE.equals(w.getType())
					&& DockableWindow.DOCK_STATE_DOCKED
							.equals(w.getDockState())) {
				windows.add(w);
			}
		}
		return windows;
	}

	/* Callback from RLayoutWindow#setName(String) */
	void changeName(RLayoutWindow window, String name) {
		if (windowRegistry.containsKey(name)) {
			log("setName: name already in use: ", name);
			throw new LayoutManagerException("name already in use: " + name);
		}
		windowRegistry.put(name, windowRegistry.remove(window.getName()));
	}
}
