package com.nayaware.jdockers.impl;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

/**
 * This is the abstract super class of all Layout Window Container children.
 * Concrete subclasses are Tabbed Document Window Containers, Dockable Windows,
 * Tabbed Dockable Window Containers and the Auto Hide Pane (for no real reason
 * other than it shares the TitledPane superclass).
 * 
 * @author Winston Prakash
 *  	   Stefan Matthias Aust
 * @version 1.0
 */
abstract class RPane extends DnDPanel {

	/**
	 * Constructs a new pane. By default, it will use a BorderLayout instead of
	 * a FlowLayout.
	 */
	RPane() {
		super(new BorderLayout());
	}

	/**
	 * Do not forget to dispose() panes. This is required to remove listeners
	 * and/or to dispose any other allocated resources.
	 */
	void dispose() {
	}

	/**
	 * Removes this pane from its parent. If the parent is a RSplitPane, that
	 * pane gets replaced with this pane sibling and the RSplitPane is disposed.
	 */
	void removeFromParent() {
		getParentTiledContainer().undockPane(this);
	}

	// accessing
	// ---------------------------------------------------------------------------------

	/**
	 * Returns the pane's central Layout Pane - or <code>null</code> is this
	 * pane isn't part of the layout tree spanned by the Layout Pane and its
	 * Layout Container.
	 */
	RLayoutPane getLayoutPane() {
		return (RLayoutPane) SwingUtilities.getAncestorOfClass(
				RLayoutPane.class, this);
	}

	RLayoutContainer getLayoutContainer() {
		return getLayoutPane().getLayoutContainer();
	}

	RPane getParentPane() {
		if (getParent() instanceof JSplitPane) {
			return (RSplitPane) getParent().getParent();
		}
		return (RPane) getParent();
	}

	RTiledContainer getParentTiledContainer() {
		RPane parentPane = getParentPane();
		return parentPane == null ? null : parentPane.getParentTiledContainer();
	}

	/**
	 * Returns the pane that manages the specified window.
	 */
	abstract RWindowPane findWindowPane(RDockableWindow window);

	// drag'n'drop
	// -------------------------------------------------------------------------------

	/*
	 * Double dispatches drop request over the RPane hierachy.
	 */
	protected void dockRequest(DnDPanel panel, int index) {
		RWindowPane source = (RWindowPane) panel;
		switch (getDropZone()) {
		case 1:
		case 2:
		case 3:
		case 4:
			if (index == -1) {
				dropRequestDock(source);
			} else {
				dropRequestDock(source, index);
			}
			break;
		case 5:
			if (index == -1) {
				dropRequestOnTop(source);
			} else {
				dropRequestOnTop(source, index);
			}
			break;
		default:
			throw illegalDnDOperation();
		}
	}

	/*
	 * A pane was docked top/left/bottom/right to this pane.
	 */
	void dropRequestDock(RWindowPane source) {
		source.getParentTiledContainer().removeHintsFor(source.getWindows());
		RLayoutContainer lwc = getLayoutContainer();
		RTiledContainer tc = getParentTiledContainer();

		double weight;
		if (tc.isHorizontal()) {
			weight = (double) source.getWidth() / tc.getWidth();
		} else {
			weight = (double) source.getHeight() / tc.getHeight();
		}
		weight = Math.min(Math.max(weight, 0.1), 0.5);

		RWindowPane nsource = source;
		if (source instanceof RFloatingFrame.RFloatingDockablePane) {
			RDockableWindow[] windows = source.getWindows();
			nsource = new RCombinedDockablePane(windows,
					source.getSelectedWindow());
		}
		source.removeFromParent();
		lwc.dockPaneAt(getDropZone() - 1, nsource, this, weight);
		tc.updateLayoutHints();
	}

	/*
	 * A tab of a pane was docked top/left/bottom/right to this pane.
	 */
	void dropRequestDock(RWindowPane source, int index) {
		RDockableWindow window = source.getWindow(index);
		source.getParentTiledContainer().removeHintsFor(
				new RDockableWindow[] { window });
		RTiledContainer tc = getParentTiledContainer();

		double weight;
		if (tc.isHorizontal()) {
			weight = (double) source.getWidth() / tc.getWidth();
		} else {
			weight = (double) source.getHeight() / tc.getHeight();
		}
		weight = Math.min(Math.max(weight, 0.1), 0.5);

		getLayoutPane().removeDockableOrFloatingWindow(window);
		RWindowPane newPane;
		if (isDockableContainer(source)) {
			newPane = new RCombinedDockablePane();
		} else {
			newPane = new RTabbedDocumentPane();
		}
		newPane.addWindow(window);
		getLayoutContainer().dockPaneAt(getDropZone() - 1, newPane, this,
				weight);
		tc.updateLayoutHints();
	}

	/*
	 * A pane was docked on top of this pane.
	 */
	void dropRequestOnTop(RWindowPane source) {
		if (source == this || !(source instanceof RCombinedDockablePane)) {
			throw illegalDnDOperation();
		}
		RDockableWindow[] windows = source.getWindows();
		source.getParentTiledContainer().removeHintsFor(windows);
		RLayoutContainer lwc = getLayoutContainer();
		source.removeFromParent();
		for (int i = 0; i < windows.length; i++) {
			lwc.dockOnTop(windows[i], this);
		}
		getParentTiledContainer().updateLayoutHints();
	}

	/*
	 * A tab of a pane was docked on top of this pane.
	 */
	void dropRequestOnTop(RWindowPane source, int index) {
		if (source == this || isDockableContainer(this)
				^ isDockableContainer(source)) {
			throw illegalDnDOperation();
		}
		RDockableWindow window = source.getWindow(index);
		source.getParentTiledContainer().removeHintsFor(
				new RDockableWindow[] { window });
		getLayoutPane().removeDockableOrFloatingWindow(window);
		getLayoutContainer().dockOnTop(window, this);
		getParentTiledContainer().updateLayoutHints();
	}

	boolean isDocumentContainer(RPane p) {
		return p instanceof RTabbedDocumentPane;
	}

	boolean isDockableContainer(RPane p) {
		return p instanceof RCombinedDockablePane;
	}

	Error illegalDnDOperation() {
		return new Error("Illegal D&D Operation");
	}

	/**
	 * Saves enough information into the specified memento to restore the layout
	 * tree hierachy.
	 */
	abstract void saveLayout(RMemento memento);

	protected RLayoutWindow getActiveWindow() {
		RLayoutPane pane = getLayoutPane();
		return pane == null ? null : pane.getActiveWindow();
	}

	abstract boolean isDocumentSplitting();

	// -------------------------------------------------------------------------------------------

	// needed for unit tests only
	abstract int componentCountForTest();

	RSplitPane getParentSplitPane() {
		if (getParent() instanceof JSplitPane) {
			return (RSplitPane) getParent().getParent();
		}
		return null;
	}

	List getPanes() {
		List result = new ArrayList();
		result.add(this);
		return result;
	}

	abstract void updateLayoutHints();

	Rectangle getAbsoluteBounds() {
		if (isShowing()) {
			return new Rectangle(getLocationOnScreen(), getSize());
		} else {
			return new Rectangle(new Point(), getPreferredSize());
		}
	}

	abstract String debugName();
}