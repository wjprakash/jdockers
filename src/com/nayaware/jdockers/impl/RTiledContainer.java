package com.nayaware.jdockers.impl;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JSplitPane;

/**
 * A container that tiles (splitable) panes horizontally or vertically.
 * 
 * @author Winston Prakash
 *  	   Stefan Matthias Aust
 * @version 1.0
 */
public class RTiledContainer extends RPane {

	/**
	 * The container's position in the global layout container
	 * (top/left/bottom/right/none)
	 */
	private int position;

	// constructors
	// ------------------------------------------------------------------------------
	/**
	 * Constructs a new container with the specified position constraint.
	 */
	public RTiledContainer(int position) {
		this.position = position;
	}

	// accessing state
	// ---------------------------------------------------------------------------
	/**
	 * Returns the container's position in the parent container
	 * (top/left/bottom/right or none).
	 */
	int getPosition() {
		return position;
	}

	/**
	 * Returns true if this container layouts its panes horizontally. Please
	 * notice, the Document Container returns always false.
	 */
	boolean isHorizontal() {
		return position == RLayoutPane.TOP || position == RLayoutPane.BOTTOM;
	}

	/**
	 * Returns true if this container layouts its panes vertically. Please
	 * notice, the Document Container returns always false.
	 */
	boolean isVertical() {
		return position == RLayoutPane.LEFT || position == RLayoutPane.RIGHT;
	}

	/**
	 * Returns the RTiledContainer that contains this pane.
	 */
	RTiledContainer getParentTiledContainer() {
		return this;
	}

	/**
	 * Returns the container's base pane for docking. This is either a leaf
	 * (RCombinedDockablePane, RTabbedDocumentPane) or a node (RSplitPane) of
	 * the layout tree.
	 */
	RPane getBasePane() {
		return getComponentCount() == 0 ? null : (RPane) getComponent(0);
	}

	// adding initial windows
	// --------------------------------------------------------------------
	/**
	 * Adds a window to the receiver's primary pane.
	 */
	public void addWindow(RDockableWindow window) {
		getPrimaryPane().addWindow(window);
	}

	/**
	 * Returns the receiver's primary pane. If no such pane exists, it will be
	 * created.
	 */
	public RWindowPane getPrimaryPane() {
		List panes = getPanes();
		if (panes.isEmpty()) {
			RWindowPane pane = createPrimaryPane();
			dockPaneAt(RLayoutPane.LEFT, pane, getBasePane(), 0.5);
			return pane;
		}
		return (RWindowPane) panes.get(0);
	}

	/**
	 * Returns a pane instance that can be used as primary pane. Subclasses
	 * should overwrite this method to use the right class.
	 */
	protected RWindowPane createPrimaryPane() {
		return new RCombinedDockablePane();
	}

	// docking
	// -----------------------------------------------------------------------------------
	/**
	 * Adds a new pane (directly or indirectly) to the receiver. The pane is
	 * docked at the specified side relative to the speficied pane (which must
	 * be a member of this container).
	 */
	public void dockPaneAt(int dockSide, RPane newPane, RPane oldPane,
			double weight) {
		switch (dockSide) {
		case RLayoutPane.TOP:
			dockPane(newPane, oldPane, RLayoutPane.HORIZONTAL, true, weight);
			break;
		case RLayoutPane.LEFT:
			dockPane(newPane, oldPane, RLayoutPane.VERTICAL, true, weight);
			break;
		case RLayoutPane.BOTTOM:
			dockPane(newPane, oldPane, RLayoutPane.HORIZONTAL, false, weight);
			break;
		case RLayoutPane.RIGHT:
			dockPane(newPane, oldPane, RLayoutPane.VERTICAL, false, weight);
			break;
		default:
			throw new Error("illegal dockSide:" + dockSide);
		}
	}

	private void dockPane(RPane newPane, RPane oldPane, int direction,
			boolean ltor, double weight) {
		// no base pane -> ignore docking constraints
		if (getBasePane() == null) {
			add(newPane, BorderLayout.CENTER);
			validate();
			return;
		}

		RSplitPane splitPane = new RSplitPane(direction, ltor, weight);

		Container parentPane = oldPane.getParent();
		if (parentPane instanceof JSplitPane) {
			((RSplitPane) parentPane.getParent()).replaceChild(oldPane,
					splitPane);
		} else {
			parentPane.remove(oldPane);
			parentPane.add(splitPane);
		}
		splitPane.setTopLeftPane(ltor ? newPane : oldPane);
		splitPane.setBottomRightPane(ltor ? oldPane : newPane);

		parentPane.validate();
	}

	/**
	 * Removes the specified pane from this container. Notifies the container's
	 * global layout container if it lost its last pane.
	 */
	public void undockPane(RPane pane) {
		Container parent = pane.getParent();
		if (parent instanceof JSplitPane) {
			RSplitPane splitPane = (RSplitPane) parent.getParent();
			parent = splitPane.getParent();
			RPane other = splitPane.getOther(pane);
			if (parent instanceof JSplitPane) {
				((RSplitPane) parent.getParent())
						.replaceChild(splitPane, other);
			} else {
				parent.remove(splitPane);
				parent.add(other);
				parent.validate();
			}
			pane.getParent().remove(pane);
			splitPane.dispose();
		} else {
			parent.remove(pane);
			parent.repaint();
		}
		if (getBasePane() == null && getParent() != null) {
			getLayoutContainer().emptyNotify(this);
		}
	}

	/**
	 * Returns the pane that contains the specified window or <code>null</code>
	 * if no such pane exists in this branch of the layout tree hierachy.
	 */
	RWindowPane findWindowPane(RDockableWindow window) {
		return getBasePane() == null ? null : getBasePane().findWindowPane(
				window);
	}

	// -------------------------------------------------------------------------------------------
	/**
	 * Saves enough information into the specified memento to restore the layout
	 * tree hierachy.
	 */
	void saveLayout(RMemento memento) {
		memento.putString("type", "tiledContainer");
		memento.putInteger("position", getPosition());
		if (getBasePane() != null) {
			getBasePane().saveLayout(memento.createMemento("pane"));
		}
	}

	int componentCountForTest() {
		return getBasePane() == null ? 0 : getBasePane()
				.componentCountForTest();
	}

	boolean isDocumentSplitting() {
		return false;
	}

	int getDominance() {
		return getLayoutContainer().getDominance(this);
	}

	List getPanes() {
		List result = new ArrayList();
		if (getBasePane() != null) {
			result.addAll(getBasePane().getPanes());
		}
		return result;
	}

	void updateLayoutHints() {
		if (getBasePane() != null) {
			getBasePane().updateLayoutHints();
		}
	}

	/**
	 * Removes all layout hints regarding the specified windows from the layout
	 * hints of this container's panes's windows.
	 */
	void removeHintsFor(RDockableWindow[] windows) {
		String[] names = new String[windows.length];
		for (int i = 0; i < windows.length; i++) {
			names[i] = windows[i].getName();
		}
		for (Iterator i = getPanes().iterator(); i.hasNext();) {
			getLayoutContainer().removeHintsFor(names,
					((RWindowPane) i.next()).getWindows());
		}
	}

	String debugName() {
		return (isHorizontal() ? "horizontal" : "vertical") + " container("
				+ (getBasePane() == null ? "NULL" : getBasePane().debugName())
				+ ")";
	}

	/*
	 * protected void paintChildren(Graphics g) { super.paintChildren(g);
	 * g.setColor(Color.orange); g.drawLine(0, 0, getWidth(), getHeight());
	 * g.drawLine(0, getHeight(), getWidth(), 0);
	 * g.setFont(getFont().deriveFont(1)); for (int i = 5; i < 14; i++) {
	 * g.setColor(i == 13 ? Color.orange : Color.black);
	 * g.drawString("Position: " + position, 20 + i % 3, 20 + (i / 3) % 3); } }
	 */
}