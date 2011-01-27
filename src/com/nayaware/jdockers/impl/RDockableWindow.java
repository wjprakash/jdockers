package com.nayaware.jdockers.impl;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Vector;

import com.nayaware.jdockers.DockableWindow;
import com.nayaware.jdockers.DockableWindowListener;

/**
 * This class implements the requirements for Dockable Windows and otherwise
 * plays the role of a model in my Pane-DnD-Window (kind of MVC) triad.
 * 
 * @author Winston Prakash
 * @version 1.0
 */
public class RDockableWindow extends RLayoutWindow implements DockableWindow {

	private Vector windowListeners = new Vector(1);

	private String dockState = DOCK_STATE_DOCKED;

	private String dockSide = DOCK_SIDE_RIGHT;

	RMemento layoutHint;

	Dimension autohidePaneSize;

	// constructors
	// ------------------------------------------------------------------------------

	/**
	 * Constructs a new dockable window.
	 */
	public RDockableWindow(RLayoutManager manager, String name) {
		super(manager, name);
	}

	// listeners
	// ---------------------------------------------------------------------------------

	/**
	 * Adds a new listener to the receiver.
	 */
	public void addDockableWindowListener(DockableWindowListener listener) {
		if (listener == null) {
			throw new NullPointerException();
		}
		windowListeners.add(listener);
	}

	/**
	 * Removes a listener from the receiver.
	 */
	public void removeDockableWindowListener(DockableWindowListener listener) {
		windowListeners.remove(listener);
	}

	// accessors
	// ---------------------------------------------------------------------------------

	public void setInitialDockSide(String dockSide) {
		setDockSide(dockSide);
	}

	public String getDockSide() {
		return dockSide;
	}

	public void setDockSide(String dockSide) {
		if (!DOCK_SIDE_LEFT.equals(dockSide) && !DOCK_SIDE_TOP.equals(dockSide)
				&& !DOCK_SIDE_RIGHT.equals(dockSide)
				&& !DOCK_SIDE_BOTTOM.equals(dockSide)) {
			throw new IllegalArgumentException(dockSide);
		}
		this.dockSide = dockSide;
	}

	public void setInitialDockState(String dockState) {
		setDockState(dockState);
	}

	public String getDockState() {
		return dockState;
	}

	public void setDockState(String dockState) {
		if (!DOCK_STATE_DOCKED.equals(dockState)
				&& !DOCK_STATE_FLOATED.equals(dockState)
				&& !DOCK_STATE_AUTOHIDDEN.equals(dockState)) {
			throw new IllegalArgumentException(dockState);
		}
		this.dockState = dockState;
	}

	// private event triggers
	// --------------------------------------------------------------------

	void fireWindowActivated() {
		getLayoutManager().fireWindowActivated(this);
	}

	void fireWindowDeactivated() {
		Vector l = (Vector) windowListeners.clone();
		int size = l.size();
		if (size > 0) {
			RDockableWindowEvent e = new RDockableWindowEvent(this);
			for (int i = 0; i < size; i++) {
				((DockableWindowListener) l.get(i))
						.dockableWindowDeactivated(e);
			}
		}
	}

	void fireWindowAutoHidden() {
		Vector l = (Vector) windowListeners.clone();
		int size = l.size();
		if (size > 0) {
			RDockableWindowEvent e = new RDockableWindowEvent(this);
			for (int i = 0; i < size; i++) {
				((DockableWindowListener) l.get(i)).dockableWindowAutoHidden(e);
			}
		}
	}

	void fireWindowFloated() {
		Vector l = (Vector) windowListeners.clone();
		int size = l.size();
		if (size > 0) {
			RDockableWindowEvent e = new RDockableWindowEvent(this);
			for (int i = 0; i < size; i++) {
				((DockableWindowListener) l.get(i)).dockableWindowFloated(e);
			}
		}
	}

	void fireWindowDocked() {
		Vector l = (Vector) windowListeners.clone();
		int size = l.size();
		if (size > 0) {
			RDockableWindowEvent e = new RDockableWindowEvent(this);
			for (int i = 0; i < size; i++) {
				((DockableWindowListener) l.get(i)).dockableWindowDocked(e);
			}
		}
	}

	void markAsAutohidden() {
		if (!DOCK_STATE_AUTOHIDDEN.equals(dockState)) {
			setDockState(DOCK_STATE_AUTOHIDDEN);
			((RLayoutPane) getLayoutManager().getLayoutPane()).floatingWindows
					.remove(this);
			fireWindowAutoHidden();
		}
	}

	void markAsFloated(RFloatingFrame ff) {
		if (!DOCK_STATE_FLOATED.equals(dockState)) {
			setDockState(DOCK_STATE_FLOATED);
			((RLayoutPane) getLayoutManager().getLayoutPane()).floatingWindows
					.put(this, ff);
			fireWindowFloated();
		}
	}

	void markAsDocked() {
		if (!DOCK_STATE_DOCKED.equals(dockState)) {
			setDockState(DOCK_STATE_DOCKED);
			((RLayoutPane) getLayoutManager().getLayoutPane()).floatingWindows
					.remove(this);
			fireWindowDocked();
		}
	}

	void update(RMemento memento) {
		memento.putString("window", getName());
		memento.putString("dockState", getDockState());
		if (autohidePaneSize != null) {
			memento.putInteger("autohidePaneWidth", autohidePaneSize.width);
			memento.putInteger("autohidePaneHeight", autohidePaneSize.height);
		}
		if (getBounds() != null) {
			RMemento m = memento.createMemento("floatingBounds");
			m.putInteger("x", getBounds().x);
			m.putInteger("y", getBounds().y);
			m.putInteger("width", getBounds().width);
			m.putInteger("height", getBounds().height);
		}
	}

	void apply(RMemento memento) {
		Integer w = memento.getInteger("autohidePaneWidth");
		Integer h = memento.getInteger("autohidePaneHeight");
		if (w != null && h != null) {
			autohidePaneSize = new Dimension(w.intValue(), h.intValue());
		}
		RMemento m = memento.getChild("floatingBounds");
		if (m != null) {
			Integer x = m.getInteger("x");
			Integer y = m.getInteger("y");
			w = m.getInteger("width");
			h = m.getInteger("height");
			if (x != null && y != null && w != null && h != null
					&& w.intValue() > 0 && h.intValue() > 0) {
				setBounds(new Rectangle(x.intValue(), y.intValue(),
						w.intValue(), h.intValue()));
			}
		}
	}
}
