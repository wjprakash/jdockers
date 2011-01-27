package com.nayaware.jdockers.impl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

/**
 * A support class to implement dragging and dropping <code>DnDPane</code>s.
 * Needs to work on a DnDPanel to display drop zones. This class combines simple
 * pane dragging and tab dragging.
 * 
 * @author Winston Prakash
 *  	   Stefan Matthias Aust
 * @version 1.0
 */
class DnDSupport extends MouseInputAdapter {

	/**
	 * This class can move four <code>Window</code> over the screen as if they
	 * were a yellow drag frame. Used to implement dragging of floating windows.
	 */
	private static class DnDDragIndicator {

		static final int BORDER_SIZE = 3;

		static final Color INDICATOR_COLOR = Color.yellow;

		Window w1, w2, w3, w4;

		boolean shown;

		DnDDragIndicator(Component owner) {
			Window w = SwingUtilities.getWindowAncestor(owner);
			w1 = new Window(w);
			w2 = new Window(w);
			w3 = new Window(w);
			w4 = new Window(w);
			w1.setBackground(INDICATOR_COLOR);
			w2.setBackground(INDICATOR_COLOR);
			w3.setBackground(INDICATOR_COLOR);
			w4.setBackground(INDICATOR_COLOR);
		}

		void show(int x, int y, int width, int height) {
			setBounds(x, y, width, height);
			if (!shown) {
				shown = true;
				w1.show();
				w2.show();
				w3.show();
				w4.show();
			}
		}

		void hide() {
			if (shown) {
				shown = false;
				w1.hide();
				w2.hide();
				w3.hide();
				w4.hide();
			}
		}

		void setBounds(int x, int y, int width, int height) {
			w1.setBounds(x, y, width, BORDER_SIZE);
			w2.setBounds(x, y + BORDER_SIZE, BORDER_SIZE, height - BORDER_SIZE
					* 2);
			w3.setBounds(x + width - BORDER_SIZE, y + BORDER_SIZE, BORDER_SIZE,
					height - BORDER_SIZE * 2);
			w4.setBounds(x, y + height - BORDER_SIZE, width, BORDER_SIZE);
		}
	}

	/**
	 * List of drag cursors used to illustrate the different DnD operations.
	 */
	private static final Cursor[] cursors = new Cursor[] {
			makeCursor("dz0", new Point(6, 6)),
			makeCursor("dz1", new Point(6, 1)),
			makeCursor("dz2", new Point(1, 6)),
			makeCursor("dz3", new Point(6, 11)),
			makeCursor("dz4", new Point(11, 6)),
			makeCursor("dz5", new Point(6, 6)),
			Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR) };

	/**
	 * Helper method to create a custom cursor from a PNG image.
	 */
	private static Cursor makeCursor(String name, Point hotSpot) {
		return Toolkit.getDefaultToolkit().createCustomCursor(
				Toolkit.getDefaultToolkit().getImage(
						DnDPanel.class.getResource(name + ".png")), hotSpot,
				name);
	}

	/**
	 * Reference to the currently used drag cursor. Set by the DropTagetListener
	 * and used by the DragSoruceListener.
	 */
	private static Cursor dragCursor;

	/**
	 * This is a reference to the drag source. Because <code>DnDSupport</code>
	 * implements both the source and the target of drag, this is also a
	 * <code>DnDSupport</code> object. Use <code>lastSource.panel</code> to get
	 * source pane.
	 */
	private static DnDSupport lastSource;

	/**
	 * This is the index of the dragged tab or <code>-1</code> if a whole pane
	 * is dragged.
	 */
	private static int lastIndex;

	/**
	 * This is the panel this object is supporting.
	 */
	private DnDPanel panel;

	private DnDDragIndicator indicator;

	private RootPaneContainer rootPaneContainer;

	// constructors
	// ------------------------------------------------------------------------------

	/**
	 * Constructs a new support object for the specified pane.
	 */
	public DnDSupport(DnDPanel panel) {
		this.panel = panel;
	}

	/**
	 * Adds the specified component as a source for DnD operations. Normally a
	 * pane plus its title and tabbedpane will be registred.
	 */
	public void addSource(Component component) {
		component.addMouseListener(this);
		component.addMouseMotionListener(this);
	}

	// -------------------------------------------------------------------------------------------

	private Point origin;

	private int dndState;

	private DnDSupport target;

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
			origin = e.getPoint();
			dndState = 1;
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (dndState == 2) {
			if (target != null) {
				target.doDrop(e);
				target.dropExit();
				target = null;
			} else {
				doDrop(e);
			}

			dragDropEnd();

			rootPaneContainer.getGlassPane().setVisible(false);
			rootPaneContainer = null;
		}
		dndState = 0;
	}

	public void mouseDragged(MouseEvent e) {
		switch (dndState) {
		case 0:
			break;
		case 1:
			startDnD(e);
			break;
		case 2:
			doDnD(e);
			break;
		}
	}

	/*
	 * After pressing the mouse, if the mouse is moved a bit while still
	 * pressing the button a D&D operation will start. Reuses the old code as
	 * much as possible,
	 */
	void startDnD(MouseEvent e) {
		// mouse needs to move a little bit before D&D will start
		if (e.getPoint().distance(origin) < 3) {
			return;
		}

		// we're pessimistic: no D&D
		dndState = 0;

		// disable D&D for auto hide panes
		if (panel instanceof RCombinedAutoHidePane) {
			return;
		}

		// as default, assume floating mode
		setDropZone(0);
		lastSource = this;
		lastIndex = -1;

		// disable D&D for tabbedpanes if no tab was clicked
		if (e.getComponent() instanceof JTabbedPane) {
			int index = indexAtLocation((JTabbedPane) e.getComponent(),
					origin.x, origin.y);
			if (index == -1) {
				return;
			}
			// if a tab was clicked, default to tab rearranging
			lastIndex = index;
			setDropZone(6);
		}

		indicator = new DnDDragIndicator(panel);

		// drag gesture was recognized
		dndState = 2;

		rootPaneContainer = (RootPaneContainer) SwingUtilities
				.getWindowAncestor(panel);
		rootPaneContainer.getGlassPane().setVisible(true);

		doDnD(e);
	}

	/*
	 * A JTabbedPane with SCROLL_LAYOUT never gets mouse events so we use a
	 * JClosedTabPane which has this bug fixed. However, that component still
	 * has problems with indexAtLocation() placed somewhere else that at TOP. I
	 * can't really fix this inside the TabbedPaneUI because other methods relay
	 * on that IMHO broken implementation as the already pass wrong parameters.
	 * Therefore, I'll fix it here:
	 */
	static int indexAtLocation(JTabbedPane tp, int x, int y) {
		if (tp.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
			for (int i = 0; i < tp.getTabCount(); i++) {
				if (tp.getBoundsAt(i).contains(x, y)) {
					return i;
				}
			}
			return -1;
		}
		return tp.indexAtLocation(x, y);
	}

	// -------------------------------------------------------------------------------------------

	/*
	 * Once a D&D gesture was recognized, this method is called. Determine drop
	 * target and inform it if the mouse enters or exits it. Also call dragOver
	 * to perform the old D&D code. Furthermore, immitate a DragSourceListener
	 * to set the cursor and/or move the DnDDragIndicator.
	 */
	void doDnD(MouseEvent e) {
		// emulate DropTargetListener
		DnDSupport dt = getDropTarget(e);
		if (dt != target) {
			if (target != null) {
				target.dropExit();
			}
			target = dt;
			if (target != null) {
				target.dropEnter();
			}
		}
		if (dt != null) {
			dt.dropOver(SwingUtilities.convertPoint(e.getComponent(),
					e.getPoint(), dt.panel));
		}
		// emulate DragSourceListener
		Point pt = e.getPoint();
		SwingUtilities.convertPointToScreen(pt, e.getComponent());
		dragOver(pt);
	}

	/*
	 * Returns the DnDSupport of the topmost DnDPanel the mouse is over.
	 */
	private DnDSupport getDropTarget(MouseEvent e) {
		// check floating frames for drop target
		Set w = new HashSet(
				((RPane) panel).getLayoutPane().floatingWindows.values());
		for (Iterator i = w.iterator(); i.hasNext();) {
			RFloatingFrame ff = (RFloatingFrame) i.next();
			Component c = ff.findComponentAt(SwingUtilities.convertPoint(
					e.getComponent(), e.getPoint(), ff));
			while (c != null) {
				if (c instanceof DnDPanel) {
					return ((DnDPanel) c).dnd;
				}
				c = c.getParent();
			}
		}

		Container container = rootPaneContainer.getContentPane();

		Window win = SwingUtilities.getWindowAncestor(((RPane) panel)
				.getLayoutPane());
		if (win instanceof JFrame) {
			container = ((JFrame) win).getContentPane();
		}

		synchronized (container.getTreeLock()) {
			Component c = container.findComponentAt(SwingUtilities
					.convertPoint(e.getComponent(), e.getPoint(), container));
			while (c != null) {
				if (c instanceof DnDPanel) {
					return ((DnDPanel) c).dnd;
				}
				c = c.getParent();
			}
			return null;
		}
	}

	/*
	 * Mouse entered the object's panel.
	 */
	public void dropEnter() {
	}

	/*
	 * A DropTargetListener will determine the drop zone. Because we never DnD
	 * across VMs we will cheat and use information about the source we actually
	 * don't have. There are 7 drop zones: 0=none aka float, 1..4=dock
	 * top/left/bottom/right, 5=dock as tab and 6=rearrange tabs.
	 * 
	 * @see DropTargetListener
	 */
	public void dropOver(Point pt) {
		// first test for tabs...
		if (lastSource == this) {
			JTabbedPane tabbedPane = panel.getTabbedPane();
			if (lastIndex != -1) {
				pt.y -= tabbedPane.getY();
			}

			int index = tabbedPane == null ? -1 : indexAtLocation(tabbedPane,
					pt.x, pt.y);

			// did we hit a tab?
			if (index != -1) {
				// yes, but can we rearrange tabs?
				if (lastSource == this && lastIndex != -1 && lastIndex != index) {
					setDropZone(6);

					// omit flickering tabs if a small tab is dragged over a
					// larger tab
					// which is left to the smaller one in the same JTabbedPane.
					if (index < lastIndex) {
						Rectangle newBounds = tabbedPane.getBoundsAt(index);
						Rectangle oldBounds = tabbedPane.getBoundsAt(lastIndex);
						if (pt.x >= newBounds.x + oldBounds.width - 2) {
							return;
						}
					}
					Component oldcomp = tabbedPane.getComponentAt(lastIndex);
					String oldtitle = tabbedPane.getTitleAt(lastIndex).trim();
					String oldtooltip = tabbedPane.getToolTipTextAt(lastIndex);
					Icon oldicon = tabbedPane.getIconAt(lastIndex);

					tabbedPane.removeTabAt(lastIndex);
					tabbedPane.insertTab(oldtitle, oldicon, oldcomp,
							oldtooltip, index);

					// insertTab is too stupid to notice that inserting a tab at
					// the
					// current selection index should trigger a state change.
					// Therefore
					// we explictly change the selection to some dummy value to
					// trigger
					// the change.
					if (index == tabbedPane.getSelectedIndex()) {
						tabbedPane.setSelectedIndex(index == 0 ? 1 : 0);
					}
					tabbedPane.setSelectedIndex(index);
					lastIndex = index;
				}
				return;
			}

			// the empty space around the tabs should be considered as drop zone
			// 6.
			if (tabbedPane != null && tabbedPane.getTabCount() > 0) {
				if (!tabbedPane.getComponentAt(0).getBounds().contains(pt)) {
					setDropZone(6);
					// return;
				}
			}
		}

		// then determine drop zone...
		int titleHeight = panel.getTitleHeight();
		if ((lastSource.isDocument() || lastIndex == -1 || lastSource.panel
				.getTabbedPane().getTabCount() > 1) && pt.y < titleHeight) {
			// We forbid drop if we try to drop over the same dockable pane, a
			// dockable
			// window or pane on document container, a document window on a
			// dockable
			// window or pane, or a document window of a container with just one
			// window.
			if (lastSource == this || (lastSource.isDocument() ^ isDocument())) {
				setDropZone(0);
				// dtde.rejectDrag();
				return;
			}
			setDropZone(5);
		} else {
			if (isFloating()) {
				if (lastSource.isDocument()) {
					setDropZone(0);
					// dtde.rejectDrag();
					return;
				}
				setDropZone(5);
				return;
			}

			int width = panel.getWidth();
			int height = panel.getHeight() - titleHeight;

			// if drag originates from the pane and not the tab, fix positions
			if (lastSource.isDocument() || lastIndex == -1) {
				pt.y -= titleHeight;
			}

			// take tab strip of dockable panes into account
			if (panel instanceof RCombinedDockablePane
					&& panel.getTabbedPane() != null
					&& panel.getTabbedPane().getTabCount() > 0) {
				height -= panel.getTabbedPane().getBoundsAt(0).height;
			}

			int marginx = Math.min(width / 3, 15); // Math.max(width / 4, 10);
			int marginy = Math.min(height / 3, 15); // Math.max(height / 4, 10);
			Rectangle center = new Rectangle(marginx, marginy, width - marginx
					* 2, height - marginy * 2);

			if (center.contains(pt)) {
				setDropZone(0);
			} else {
				// We forbid drop if we try to dock a single dockable pane to
				// itself, a tabbed
				// dockable to itself (docking one tab of a tabbed dockable pane
				// is okay), or
				// a document tab of a document container that contains only one
				// document.
				if (lastSource == this
						&& ((panel instanceof RCombinedDockablePane && ((RCombinedDockablePane) panel)
								.getWindowCount() == 1)
								|| (panel instanceof RCombinedDockablePane && ((RCombinedDockablePane) panel)
										.getWindowCount() > 1)
								&& lastIndex == -1 || panel instanceof RTabbedDocumentPane
								&& ((RTabbedDocumentPane) panel)
										.getWindowCount() < 2)) {
					setDropZone(0);
					return;
				}

				// do not drag document panes on dockable panes (and vice versa)
				if (lastSource.isDocument() && !isDocument()) {
					setDropZone(0);
					return;
				}

				pt.x -= width / 2;
				pt.y -= height / 2;
				double degree = Math.toDegrees(Math.atan2(pt.y * width, pt.x
						* height));
				if (degree < -45 && degree >= -135) {
					setDropZone(1);
				} else if (degree < -135 || degree > 135) {
					setDropZone(2);
				} else if (degree < 135 && degree >= 45) {
					setDropZone(3);
				} else {
					setDropZone(4);
				}

				// implement the "vice versa"
				if (!lastSource.isDocument() && isDocument()) {
					RLayoutContainer.Bounds lcBounds = new RLayoutContainer.Bounds(
							((RPane) panel).getLayoutContainer());
					RLayoutContainer.Bounds pBounds = new RLayoutContainer.Bounds(
							(RPane) panel);
					boolean forbid = false;
					switch (panel.getDropZone()) {
					case 1:
						forbid = lcBounds.top != pBounds.top;
						break;
					case 2:
						forbid = lcBounds.left != pBounds.left;
						break;
					case 3:
						forbid = lcBounds.bottom != pBounds.bottom;
						break;
					case 4:
						forbid = lcBounds.right != pBounds.right;
						break;
					}
					if (forbid) {
						setDropZone(0);
					}
				}
			}
		}
	}

	/*
	 * Tells the panel that the drop zone has changed and also changes the
	 * dragCursor. This cursor will be set in the DragSourceListener.
	 */
	private void setDropZone(int zone) {
		panel.setDropZone(zone);
		dragCursor = cursors[zone];
	}

	private boolean isDocument() {
		return panel instanceof RTabbedDocumentPane
				|| panel instanceof RTiledContainer;
	}

	void dropExit() {
		setDropZone(0);
	}

	/*
	 * The user decided to drop the panel here. Extracts information about the
	 * source panel and the source index and call the panel's
	 * <code>drop()</code> method.
	 * 
	 * @see DropTargetListener
	 */
	void doDrop(MouseEvent e) {
		if (panel.getDropZone() == 0) {
			if (lastSource.isDocument()) {
				return;
			}
			Point pt = e.getPoint();
			SwingUtilities.convertPointToScreen(pt, lastSource.panel);
			RPane p = (RPane) lastSource.panel;
			RLayoutPane layoutPane = p.getLayoutPane();
			RDockableWindow[] windows;
			if (lastIndex == -1) {
				windows = ((RCombinedDockablePane) p).getWindows();

				if (p instanceof RFloatingFrame.RFloatingDockablePane) {
					SwingUtilities.getWindowAncestor(p).setLocation(pt);
					return;
				}
				for (int i = 0; i < windows.length; i++) {
					p.getLayoutContainer().setupLayoutHint(windows[i]);
					windows[i].setBounds(p.getAbsoluteBounds());
				}
				p.removeFromParent();
			} else {
				RDockableWindow w = ((RCombinedDockablePane) p)
						.getWindow(lastIndex);
				w.setBounds(p.getAbsoluteBounds());
				((RCombinedDockablePane) p).removeWindow(w);
				windows = new RDockableWindow[] { w };
			}
			if (windows.length > 0) {
				layoutPane.floatWindows(windows, new Rectangle(pt, windows[0]
						.getBounds().getSize()));
			}
		} else {
			panel.drop(lastSource.panel, lastIndex);
		}
	}

	// -------------------------------------------------------------------------------------------

	/*
	 * @see DragSourceListener
	 * 
	 * public void dragEnter() { setCursor(dragCursor); }
	 */

	/*
	 * The cursor was moved over a drag target, set a nice cursor.
	 * 
	 * @see DragSourceListener
	 */
	public void dragOver(Point pt) {
		setCursor(dragCursor);
		if (dragCursor == cursors[0]) {
			indicator.show(pt.x - 30, pt.y - 20, 60, 40);
		} else {
			indicator.hide();
		}
	}

	/*
	 * @see DragSourceListener
	 */
	public void dragDropEnd() {
		setCursor(null);
		indicator.hide();
	}

	/*
	 * The cursor has left the drag target, reset the cursor.
	 * 
	 * @see DragSourceListener
	 * 
	 * public void dragExit() { setCursor(null); }
	 */

	private void setCursor(Cursor cursor) {
		rootPaneContainer.getGlassPane().setCursor(cursor);
	}

	public boolean isFloating() {
		return panel instanceof RFloatingFrame.RFloatingDockablePane;
	}

}

// -------------------------------------------------------------------------------------------

