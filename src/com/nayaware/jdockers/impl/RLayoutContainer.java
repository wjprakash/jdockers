package com.nayaware.jdockers.impl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.nayaware.jdockers.DockableWindow;
import com.nayaware.jdockers.LayoutManager;

/**
 * This is the Layout Pane's central Layout Container which contains Dockable
 * Windows (directly or indirectly) and always at least one Tabbed Document
 * Container. The components are deeply nested by Split Containers which
 * implement the docking and horizontal and vertical splitting.
 * 
 * @author Winston Prakash
 *  	   Stefan Matthias Aust
 * @version 1.0
 */
class RLayoutContainer extends JPanel {

	private RTiledContainer documentContainer;

	private RTiledContainer[] containers = new RTiledContainer[4];

	private Map layoutHints = new HashMap();

	// Constructors
	// ------------------------------------------------------------------------------

	/**
	 * Constructs a new Layout Container. Each Layout Pane has one instance as a
	 * child.
	 */
	RLayoutContainer() {
		super(new BorderLayout());
		setName("layoutContainer");
		createInitialContainers();
	}

	/**
	 * Each Layout Container comes not only with batteries included but with a
	 * Tiled Document Container and up to four optional Tiled Dockable
	 * Contianers ready to use for your convenience.
	 */
	private void createInitialContainers() {
		documentContainer = new RTiledContainer(RLayoutPane.NONE) {
			protected RWindowPane createPrimaryPane() {
				return new RTabbedDocumentPane();
			}

			void saveLayout(RMemento memento) {
				memento.putString("type", "tiledContainer");
				memento.putInteger("position", getPosition());
			}
		};
		add(documentContainer, BorderLayout.CENTER);
	}

	// adding and removings of windows
	// -----------------------------------------------------------

	/**
	 * Adds a Document Window to one of the receiver's Document Container. The
	 * window is added to the last active container.
	 */
	void addDocumentWindow(RDockableWindow window) {
		if (applyLayoutHint(window)) {
			return;
		}
		documentContainer.addWindow(window);
	}

	/**
	 * Removes a Document Window from one of the receiver's Document Containers.
	 */
	void removeDocumentWindow(RDockableWindow window) {
		removeWindow(window);
	}

	/**
	 * Adds a Dockable Window to the receiver. The window is added for the first
	 * time, so its inital docking side will be respected.
	 */
	void addDockableWindow(RDockableWindow window) {
		if (applyLayoutHint(window)) {
			return;
		}
		int dockSide = dockSide(window.getDockSide());
		if (containers[dockSide] == null) {
			containers[dockSide] = new RTiledContainer(dockSide);
			dockPaneAt(dockSide, containers[dockSide], getBasePane(), 0.3);
		}
		RTiledContainer tc = containers[dockSide(window.getDockSide())];
		tc.addWindow(window);
		tc.updateLayoutHints();
	}

	/**
	 * Removes a Dockable Window from the receiver.
	 */
	void removeDockableWindow(RDockableWindow window) {
		removeWindow(window);
	}

	private void removeWindow(RDockableWindow window) {
		RWindowPane pane = (RWindowPane) findWindowPane(window);
		if (pane != null) {
			pane.removeWindowOrPane(window);
		}
	}

	// docking and undocking of panes
	// --------------------------------------------------------

	/**
	 * Adds the specified window on top of the also specified pane. If that pane
	 * is already a container, add the window as last window (regardless the
	 * method name "on top"). Otherwise, replace an existing pane with a new
	 * container that contains both windows.
	 */
	void dockOnTop(RDockableWindow window, RPane oldPane) {
		if (oldPane instanceof RWindowPane) {
			((RWindowPane) oldPane).addWindow(window);
			if (oldPane instanceof RCombinedDockablePane) {
				if (oldPane == containers[RLayoutPane.TOP]) {
					window.setDockSide(DockableWindow.DOCK_SIDE_TOP);
				} else if (oldPane == containers[RLayoutPane.LEFT]) {
					window.setDockSide(DockableWindow.DOCK_SIDE_LEFT);
				} else if (oldPane == containers[RLayoutPane.BOTTOM]) {
					window.setDockSide(DockableWindow.DOCK_SIDE_BOTTOM);
				} else if (oldPane == containers[RLayoutPane.RIGHT]) {
					window.setDockSide(DockableWindow.DOCK_SIDE_RIGHT);
				}
			}
		} else {
			throw new Error(); // should never happen
		}
	}

	/**
	 * Adds one pane besides another pane.
	 */
	void dockPaneAt(int dockSide, RPane newPane, RPane oldPane, double weight) {

		if (newPane instanceof RCombinedDockablePane
				&& (oldPane instanceof RTabbedDocumentPane || oldPane == documentContainer)) {
			if (containers[dockSide] == null) {
				containers[dockSide] = new RTiledContainer(dockSide);
			}
			containers[dockSide].dockPaneAt(RLayoutPane.RIGHT, newPane,
					containers[dockSide].getParentPane(), 0.5);
			dockPaneAt(dockSide, containers[dockSide],
					oldPane.getParentTiledContainer(), weight);
			containers[dockSide].updateLayoutHints();
			return;
		}

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
			throw new Error(); // should never happen
		}
	}

	/**
	 * Adds one pane either to the top, left, right, or bottom of another pane.
	 * The newPane must not be added to the pane hierachy while the oldPane must
	 * be part of that hierarchy.
	 */
	void dockPane(RPane newPane, RPane oldPane, int direction, boolean ltor,
			double weight) {
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

	void emptyNotify(RTiledContainer container) {
		for (int i = 0; i < containers.length; i++) {
			if (containers[i] == container) {
				containers[i] = null;
				container.removeFromParent();
			}
		}
	}

	/**
	 * Returns the root of the layout tree - or <code>null</code> if the
	 * container is empty.
	 */
	RPane getBasePane() { // name rootPane was taken...
		return getComponentCount() == 0 ? null : (RPane) getComponent(0);
	}

	/**
	 * Returns the numerical dockSide constant for the given string constant.
	 */
	int dockSide(String dockSide) {
		if (DockableWindow.DOCK_SIDE_TOP.equals(dockSide)) {
			return RLayoutPane.TOP;
		} else if (DockableWindow.DOCK_SIDE_LEFT.equals(dockSide)) {
			return RLayoutPane.LEFT;
		} else if (DockableWindow.DOCK_SIDE_BOTTOM.equals(dockSide)) {
			return RLayoutPane.BOTTOM;
		} else if (DockableWindow.DOCK_SIDE_RIGHT.equals(dockSide)) {
			return RLayoutPane.RIGHT;
		}
		throw new Error("illegal dockSide: " + dockSide);
	}

	/* See RPane */
	RWindowPane findWindowPane(RDockableWindow window) {
		for (int i = 0; i < getComponentCount(); i++) {
			RWindowPane pane = ((RPane) getComponent(i)).findWindowPane(window);
			if (pane != null) {
				return pane;
			}
		}
		RWindowPane pane = documentContainer.findWindowPane(window);
		if (pane != null) {
			return pane;
		}
		return null;
	}

	// ---------------------------------------------------------------------------------------

	// for unit testing only
	int componentCountForTest() {
		int count = 0;
		for (int i = 0; i < getComponentCount(); i++) {
			count += ((RPane) getComponent(i)).componentCountForTest();
		}
		return count;
	}

	// -------------------------------------------------------------------------------------------

	/**
	 * Saves enough information into the specified memento to restore the layout
	 * tree hierachy.
	 */
	void saveLayout(RMemento memento) {
		if (getComponentCount() != 1)
			throw new Error();

		memento = memento.createMemento("layoutContainer");
		getBasePane().saveLayout(memento.createMemento("pane"));
	}

	// ---------------------------------------------------------------------------------------

	static class Bounds {
		int top, left, bottom, right;

		Bounds(Component c) {
			synchronized (c.getTreeLock()) {
				for (Component cc = c; !(cc instanceof RLayoutContainer); cc = cc
						.getParent()) {
					top += cc.getY();
					left += cc.getX();
				}
				bottom = top + c.getHeight() - 1;
				right = left + c.getWidth() - 1;
			}
		}
	}

	/**
	 * Returns the dominance code of the given container. 0 = fills completely,
	 * 1 = top/left dominant, 2 = bottom/right dominant, 3 = not dominant, 4 =
	 * not even attaching
	 */
	public int getDominance(RTiledContainer container) {
		Bounds thisBounds = new Bounds(this);
		Bounds contBounds = new Bounds(container);
		int attach = 0;
		if (thisBounds.top == contBounds.top) {
			attach |= 1;
		}
		if (thisBounds.left == contBounds.left) {
			attach |= 2;
		}
		if (thisBounds.bottom == contBounds.bottom) {
			attach |= 4;
		}
		if (thisBounds.right == contBounds.right) {
			attach |= 8;
		}
		if (attach == 15) {
			return 0;
		}
		if (container.getPosition() == RLayoutPane.TOP) {
			if (attach == 11)
				return 0;
			if (attach == 3)
				return 1;
			if (attach == 9)
				return 2;
			if (attach == 1)
				return 3;
		} else if (container.getPosition() == RLayoutPane.LEFT) {
			if (attach == 7)
				return 0;
			if (attach == 3)
				return 1;
			if (attach == 6)
				return 2;
			if (attach == 2)
				return 3;
		} else if (container.getPosition() == RLayoutPane.BOTTOM) {
			if (attach == 14)
				return 0;
			if (attach == 6)
				return 1;
			if (attach == 12)
				return 2;
			if (attach == 4)
				return 3;
		} else if (container.getPosition() == RLayoutPane.RIGHT) {
			if (attach == 13)
				return 0;
			if (attach == 9)
				return 1;
			if (attach == 12)
				return 2;
			if (attach == 8)
				return 3;
		}
		return 4;
	}

	RMemento getLayoutHint(RDockableWindow window) {
		SoftReference ref = (SoftReference) layoutHints.get(window.getName());
		if (ref != null && ref.get() == null) {
			// System.out.println("###Lost LayoutHint for " + window.getName());
			layoutHints.remove(window.getName());
		}
		if (ref != null && window.layoutHint == null) {
			window.layoutHint = (RMemento) ref.get();
		}
		return window.layoutHint;
	}

	void setLayoutHint(RDockableWindow window, RMemento layoutHint) {
		layoutHints.put(window.getName(), new SoftReference(layoutHint));
		window.layoutHint = layoutHint;
	}

	/**
	 * Stores relevant layout hints for the given window. We store its siblings,
	 * enough information to recreate its tiledcontainer (and so its dock side)
	 * if needed, the anchor point in the tiledcontainer and the previous and
	 * next pane's windows. Furthermore, we store the dock state and the bounds
	 * of floating frames.
	 */
	void updateLayoutHint(RDockableWindow window) {
		// System.out.println("UPDATE: " + window.getName());
		if (!window.isVisible()) {
			return;
		}

		// find the window's pane
		RWindowPane pane = findWindowPane(window);
		if (pane == null) {
			return;
		}

		// find the window's pane's tiledcontainer
		RTiledContainer container = pane.getParentTiledContainer();
		if (container == null) {
			return;
		}

		RMemento layoutHint = new RMemento("layoutHint");
		layoutHint.putString("window", window.getName());

		layoutHint.putString("dockState", window.getDockState());

		if (RDockableWindow.DOCK_STATE_FLOATED.equals(window.getDockState())) {
			updateFloatingFrameDimensions(window);
		} else {
			RMemento hint = getLayoutHint(window);
			if (hint != null) {
				hint = hint.getChild("floatingBounds");
				if (hint != null) {
					layoutHint.putMemento(hint);
				}
			}
		}
		if (window.autohidePaneSize != null) {
			layoutHint.putInteger("autohidePaneWidth",
					window.autohidePaneSize.width);
			layoutHint.putInteger("autohidePaneHeight",
					window.autohidePaneSize.height);
		}

		// store the window's siblings
		for (int i = 0; i < pane.getWindowCount(); i++) {
			RDockableWindow w = pane.getWindow(i);
			if (w != window) {
				layoutHint.createMemento("sibling").putString("name",
						w.getName());
			}
		}

		// store enough information to recreate the tiled container if needed
		layoutHint.putInteger("containerDockSide", container.getPosition());
		layoutHint.putInteger("containerDominance", container.getDominance());
		layoutHint.putInteger("containerWidth", container.getWidth());
		layoutHint.putInteger("containerHeight", container.getHeight());

		layoutHint.putInteger("width", pane.getWidth());
		layoutHint.putInteger("height", pane.getHeight());

		positionOfPane(layoutHint, container, pane);

		setLayoutHint(window, layoutHint);

		// System.out.println("Snapshot: " + layoutHint);
	}

	private void positionOfPane(RMemento layoutHint, RTiledContainer container,
			RWindowPane pane) {
		Bounds contBounds = new Bounds(container);
		Bounds paneBounds = new Bounds(pane);
		int anchor = -1;
		if (container.isHorizontal()) {
			if (paneBounds.left == contBounds.left) {
				anchor = RLayoutPane.LEFT;
			}
			if (paneBounds.right == contBounds.right) {
				anchor = RLayoutPane.RIGHT;
			}
		}
		if (container.isVertical()) {
			if (paneBounds.top == contBounds.top) {
				anchor = RLayoutPane.TOP;
			}
			if (paneBounds.bottom == contBounds.bottom) {
				anchor = RLayoutPane.BOTTOM;
			}
		}
		if (anchor != -1) {
			layoutHint.putInteger("anchor", anchor);
		}
		List panes = container.getPanes();
		for (int i = 0; i < panes.size(); i++) {
			if (panes.get(i) == pane) {
				if (i > 0) {
					RWindowPane p = (RWindowPane) panes.get(i - 1);
					for (int j = 0; j < p.getWindowCount(); j++) {
						layoutHint.createMemento("prev").putString("name",
								p.getWindow(j).getName());
					}
				}
				if (i < panes.size() - 1) {
					RWindowPane p = (RWindowPane) panes.get(i + 1);
					for (int j = 0; j < p.getWindowCount(); j++) {
						layoutHint.createMemento("next").putString("name",
								p.getWindow(j).getName());
					}
				}
				break;
			}
		}
	}

	/**
	 * Apply the stored layout hints (if available) to the given window. If we
	 * find sibling windows, we'll put the window on top of those. We prefer a
	 * pane which is still at the same dock side but if there's none, we'll use
	 * one (random) of those panes with the most remaining sibling windows. If
	 * there's no tiledContainer that the specified dock side, that container is
	 * recreated. If the old container was dominant (that is, adjacent to all
	 * three edges) we'll create a dominant container. If the container was
	 * adjacent only to two edges, we'll try to create it the same way again (if
	 * the non-dominant edge isn't filled with another container anymore, we'll
	 * get a dominant container anyhow). If the old container was not even
	 * adacent to two edges, we'll create a container that is adjacent to the
	 * document container. This way, if there are other containers, we'll get
	 * the same non-adjacent container again. If the window was anchored to
	 * either the left or right side for horizontal containers and/or top or
	 * bottom for vertical containers, we'll anchor it again, bottom/right will
	 * take precedence over top/left. Last but not least, if we find the
	 * previous and next panes, we'll squeeze the window between those panes. If
	 * we find both panes but they're not adjacent, we'll again prefer to dock
	 * bottom/right, that is the previous pane takes precedence over the next
	 * pane.
	 */
	boolean applyLayoutHint(RDockableWindow window) {
		RMemento layoutHint = getLayoutHint(window);
		if (layoutHint == null) {
			return false;
		}

		// System.out.println("apply: " + layoutHint);

		window.apply(layoutHint);

		List siblings = layoutHint.getChildren("sibling");
		Integer dockSide = layoutHint.getInteger("containerDockSide");
		RTiledContainer container = null;
		if (dockSide != null) {
			if (dockSide.intValue() == RLayoutPane.NONE) {
				container = documentContainer;
			} else {
				container = containers[dockSide.intValue()];
			}
		}

		List panes = new ArrayList(siblings.size());
		Bag bag = new Bag();
		for (int i = 0; i < siblings.size(); i++) {
			String name = ((RMemento) siblings.get(i)).getString("name");
			RDockableWindow w = window.getLayoutManager().findRWindow(name);
			if (w != null) {
				RWindowPane pane = findWindowPane(w);
				if (pane != null) {
					if (pane.getParentTiledContainer() == container) {
						pane.addWindow(window);
						return true;
					}
					bag.add(pane);
				}
			}
		}
		RWindowPane pane = (RWindowPane) bag.getMax();
		if (pane != null) {
			pane.addWindow(window);
			return true;
		}

		if (LayoutManager.TYPE_DOCUMENT.equals(window.getType())) {
			return false;
		}

		Integer dominance = layoutHint.getInteger("containerDominance");
		if (container == null && dockSide != null && dominance != null) {
			int ds = dockSide.intValue();
			int dm = dominance.intValue();

			container = containers[ds] = new RTiledContainer(ds);

			double weight = 0.3;
			if (ds == RLayoutPane.TOP || ds == RLayoutPane.BOTTOM) {
				Integer value = layoutHint.getInteger("containerHeight");
				if (value != null) {
					weight = value.doubleValue() / getHeight();
				}
			}
			if (ds == RLayoutPane.LEFT || ds == RLayoutPane.RIGHT) {
				Integer value = layoutHint.getInteger("containerWidth");
				if (value != null) {
					weight = value.doubleValue() / getWidth();
				}
			}

			if (ds == RLayoutPane.TOP) {
				RPane oldPane;
				if (dm == 0
						|| (containers[RLayoutPane.LEFT] == null && containers[RLayoutPane.RIGHT] == null)) {
					oldPane = getBasePane();
				} else if (dm == 1 && containers[RLayoutPane.LEFT] != null) {
					oldPane = containers[RLayoutPane.LEFT].getParentSplitPane();
				} else if (dm == 2 && containers[RLayoutPane.RIGHT] != null) {
					oldPane = containers[RLayoutPane.RIGHT]
							.getParentSplitPane();
				} else {
					oldPane = documentContainer;
				}
				dockPaneAt(ds, containers[ds], oldPane, weight);
			} else if (ds == RLayoutPane.LEFT) {
				RPane oldPane;
				if (dm == 0
						|| (containers[RLayoutPane.TOP] == null && containers[RLayoutPane.BOTTOM] == null)) {
					oldPane = getBasePane();
				} else if (dm == 1 && containers[RLayoutPane.TOP] != null) {
					oldPane = containers[RLayoutPane.TOP].getParentSplitPane();
				} else if (dm == 2 && containers[RLayoutPane.BOTTOM] != null) {
					oldPane = containers[RLayoutPane.BOTTOM]
							.getParentSplitPane();
				} else {
					oldPane = documentContainer;
				}
				dockPaneAt(ds, containers[ds], oldPane, weight);
			} else if (ds == RLayoutPane.BOTTOM) {
				RPane oldPane;
				if (dm == 0
						|| (containers[RLayoutPane.LEFT] == null && containers[RLayoutPane.RIGHT] == null)) {
					oldPane = getBasePane();
				} else if (dm == 1 && containers[RLayoutPane.LEFT] != null) {
					oldPane = containers[RLayoutPane.LEFT].getParentSplitPane();
				} else if (dm == 2 && containers[RLayoutPane.RIGHT] != null) {
					oldPane = containers[RLayoutPane.RIGHT]
							.getParentSplitPane();
				} else {
					oldPane = documentContainer;
				}
				dockPaneAt(ds, containers[ds], oldPane, weight);
			} else if (ds == RLayoutPane.RIGHT) {
				RPane oldPane;
				if (dm == 0
						|| (containers[RLayoutPane.TOP] == null && containers[RLayoutPane.BOTTOM] == null)) {
					oldPane = getBasePane();
				} else if (dm == 1 && containers[RLayoutPane.TOP] != null) {
					oldPane = containers[RLayoutPane.TOP].getParentSplitPane();
				} else if (dm == 2 && containers[RLayoutPane.BOTTOM] != null) {
					oldPane = containers[RLayoutPane.BOTTOM]
							.getParentSplitPane();
				} else {
					oldPane = documentContainer;
				}
				dockPaneAt(ds, containers[ds], oldPane, weight);
			}
		}

		Integer anchor = layoutHint.getInteger("anchor");
		if (anchor != null) {
			double weight = getDim(layoutHint, container);
			RCombinedDockablePane newPane = new RCombinedDockablePane(window);
			container.dockPaneAt(anchor.intValue(), newPane,
					container.getBasePane(), weight);
			return true;
		}

		List previous = layoutHint.getChildren("prev");
		Bag previousBag = new Bag();
		for (int i = 0; i < previous.size(); i++) {
			String name = ((RMemento) previous.get(i)).getString("name");
			RDockableWindow w = window.getLayoutManager().findRWindow(name);
			if (w != null) {
				pane = container.findWindowPane(w);
				if (pane != null) {
					previousBag.add(pane);
				}
			}
		}

		List next = layoutHint.getChildren("next");
		Bag nextBag = new Bag();
		for (int i = 0; i < next.size(); i++) {
			String name = ((RMemento) next.get(i)).getString("name");
			RDockableWindow w = window.getLayoutManager().findRWindow(name);
			if (w != null) {
				pane = container.findWindowPane(w);
				if (pane != null) {
					nextBag.add(pane);
				}
			}
		}

		panes = container.getPanes();
		if (previousBag.isEmpty()) {
			if (nextBag.isEmpty()) {
				return false;
			}
			Set c = nextBag.getCandidates();
			for (int i = 0; i < panes.size(); i++) {
				if (c.contains(panes.get(i))) {
					RWindowPane newPane = new RCombinedDockablePane(window);
					int ds;
					if (container.isHorizontal()) {
						ds = RLayoutPane.LEFT;
					} else {
						ds = RLayoutPane.TOP;
					}
					double weight = getDim(layoutHint, container);
					container.dockPaneAt(ds, newPane, (RPane) panes.get(i),
							weight);
					return true;
				}
			}
		} else {
			Set c = previousBag.getCandidates();
			for (int i = 0; i < panes.size(); i++) {
				if (c.contains(panes.get(i))) {
					RWindowPane newPane = new RCombinedDockablePane(window);
					int ds;
					if (container.isHorizontal()) {
						ds = RLayoutPane.RIGHT;
					} else {
						ds = RLayoutPane.BOTTOM;
					}
					double weight = getDim(layoutHint, container);
					container.dockPaneAt(ds, newPane, (RPane) panes.get(i),
							weight);
					return true;
				}
			}
		}

		return false;
	}

	private double getDim(RMemento layoutHint, RTiledContainer container) {
		double weight = 0.5;
		if (container.isHorizontal()) {
			Integer value = layoutHint.getInteger("width");
			if (value != null) {
				weight = value.doubleValue() / container.getWidth();
			}
		} else {
			Integer value = layoutHint.getInteger("height");
			if (value != null) {
				weight = value.doubleValue() / container.getHeight();
			}
		}
		return Math.min(Math.max(weight, 0.1), 1.0 / (container.getPanes()
				.size() + 1.0));

	}

	static class Bag {
		private Map data = new HashMap();

		void add(Object object) {
			data.put(object, new Integer(occurencesOf(object) + 1));
		}

		int occurencesOf(Object object) {
			Integer count = (Integer) data.get(object);
			return count == null ? 0 : count.intValue();
		}

		boolean isEmpty() {
			return data.isEmpty();
		}

		Object getMax() {
			Object maxObject = null;
			int maxCount = 0;
			for (Iterator i = data.entrySet().iterator(); i.hasNext();) {
				Entry e = (Entry) i.next();
				int count = ((Integer) e.getValue()).intValue();
				if (count > maxCount) {
					maxCount = count;
					maxObject = e.getKey();
				}
			}
			return maxObject;
		}

		Set getCandidates() {
			int maxCount = 0;
			for (Iterator i = data.values().iterator(); i.hasNext();) {
				int count = ((Integer) i.next()).intValue();
				if (count > maxCount) {
					maxCount = count;
				}
			}
			Set result = new HashSet(data.size());
			for (Iterator i = data.entrySet().iterator(); i.hasNext();) {
				Entry e = (Entry) i.next();
				int count = ((Integer) e.getValue()).intValue();
				if (count == maxCount) {
					result.add(e.getKey());
				}
			}
			return result;
		}
	}

	RTiledContainer getContainer(int position) {
		if (position == RLayoutPane.NONE) {
			documentContainer.removeFromParent();
			return documentContainer;
		}
		if (containers[position] == null) {
			containers[position] = new RTiledContainer(position);
		}
		return containers[position];
	}

	void updateLayoutHints() {
		if (getBasePane() != null) {
			getBasePane().updateLayoutHints();
		}
	}

	void redockWindows(RDockableWindow[] windows, RDockableWindow selectedWindow) {
		int position = RLayoutPane.RIGHT;
		RMemento layoutHint = getLayoutHint(selectedWindow);
		if (layoutHint != null) {
			Integer value = layoutHint.getInteger("containerDockSide");
			if (value != null) {
				position = value.intValue();
			}
		}
		redockWindows(position, windows, selectedWindow);
	}

	void redockWindows(int position, RDockableWindow[] windows,
			RDockableWindow selectedWindow) {
		if (windows.length > 0 && applyLayoutHint(windows[0])) {
			RCombinedDockablePane pane = (RCombinedDockablePane) findWindowPane(windows[0]);
			int j = 0;
			for (int i = 1; i < windows.length; i++) {
				dockOnTop(windows[i], pane);
				if (windows[i] == selectedWindow) {
					j = i;
				}
			}
			pane.setSelectedIndex(j);
		} else {
			String dockSide;
			if (position == RLayoutPane.TOP) {
				dockSide = RDockableWindow.DOCK_SIDE_TOP;
			} else if (position == RLayoutPane.LEFT) {
				dockSide = RDockableWindow.DOCK_SIDE_LEFT;
			} else if (position == RLayoutPane.BOTTOM) {
				dockSide = RDockableWindow.DOCK_SIDE_BOTTOM;
			} else {
				dockSide = RDockableWindow.DOCK_SIDE_RIGHT;
			}
			for (int i = 0; i < windows.length; i++) {
				windows[i].setDockSide(dockSide);
				addDockableWindow(windows[i]);
			}
		}
	}

	/**
	 * Removes all layout hints regarding the specified window names from the
	 * layout hints of the given windows.
	 */
	void removeHintsFor(String[] names, RDockableWindow[] windows) {
		for (int i = 0; i < windows.length; i++) {
			RMemento layoutHint = getLayoutHint(windows[0]);
			if (layoutHint != null) {
				removeHintsTo(layoutHint, "sibling", names);
				removeHintsTo(layoutHint, "prev", names);
				removeHintsTo(layoutHint, "next", names);
			}
		}
	}

	/**
	 * Removes all references to the given window names from the specified
	 * layout hint's children of the specified type.
	 */
	void removeHintsTo(RMemento memento, String type, String[] names) {
		List children = memento.getChildren(type);
		for (Iterator i = children.iterator(); i.hasNext();) {
			RMemento m = (RMemento) i.next();
			String name = m.getString("name");
			if (name != null) {
				for (int j = 0; j < names.length; j++) {
					if (names[j].equals(name)) {
						memento.removeChild(m);
						break;
					}
				}
			}
		}
	}

	/**
	 * Updates all width and height settings of existing windows to reflect the
	 * tiling.
	 */
	void updateLayoutHintDimensions() {
		// System.out.println("update dimensions");

		for (Iterator i = getBasePane().getPanes().iterator(); i.hasNext();) {
			RWindowPane pane = (RWindowPane) i.next();
			RTiledContainer container = pane.getParentTiledContainer();

			RDockableWindow[] windows = pane.getWindows();
			for (int j = 0; j < windows.length; j++) {
				RMemento layoutHint = getLayoutHint(windows[j]);
				if (layoutHint != null) {
					layoutHint.putInteger("containerWidth",
							container.getWidth());
					layoutHint.putInteger("containerHeight",
							container.getHeight());
					layoutHint.putInteger("width", pane.getWidth());
					layoutHint.putInteger("height", pane.getHeight());
				}
			}
		}
	}

	void setupLayoutHint(RDockableWindow window) {
		if (getLayoutHint(window) == null) {
			updateLayoutHint(window);
		}
	}

	void updateFloatingFrameDimensions(RDockableWindow window) {
		setupLayoutHint(window);
		RMemento layoutHint = getLayoutHint(window);
		if (layoutHint != null) {
			RMemento m = layoutHint.getChild("floatingBounds");
			if (m == null) {
				m = layoutHint.createMemento("floatingBounds");
			}
			Container c = window.getWindowComponent().getParent();
			while (c != null && !(c instanceof RPane)) {
				c = c.getParent();
			}
			Rectangle bounds = c == null ? new Rectangle(window
					.getWindowComponent().getBounds()) : ((RPane) c)
					.getAbsoluteBounds();
			/*
			 * Dimension size = window.getWindowComponent().getSize(); Rectangle
			 * bounds = new Rectangle(
			 * window.getWindowComponent().getLocationOnScreen(), new
			 * Dimension(size.width, size.height + 17));
			 */
			m.putInteger("x", bounds.x);
			m.putInteger("y", bounds.y);
			m.putInteger("width", bounds.width);
			m.putInteger("height", bounds.height);
		}
	}
}