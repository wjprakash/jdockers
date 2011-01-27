package com.nayaware.jdockers.impl;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

/**
 * This listener implements collapsing of Auto Hide Panes if the mouse is
 * clicked somewhere else or if the mouse leaves the pane of an inactive Auto
 * Hide Pane. Furthermore, this listener deals with activating windows if their
 * pane is clicked and used to deal with window focus lost/gained events.
 * 
 * @author Winston Prakash
 * @version 1.0
 */
class RGlobalEventListener implements AWTEventListener {

	private final int NONE = 0;
	private final int NORTH = 1;
	private final int SOUTH = 2;
	private final int EAST = 3;
	private final int WESTT = 4;
	private final int NORTH_EAST = 5;
	private final int NORTH_WEST = 6;
	private final int SOUTH_EAST = 7;
	private final int SOUT_WEST = 8;

	Cursor normalCursor = null;
	Cursor northCursor = new Cursor(Cursor.N_RESIZE_CURSOR);
	Cursor eastCursor = new Cursor(Cursor.E_RESIZE_CURSOR);
	Cursor northEastCursor = new Cursor(Cursor.NE_RESIZE_CURSOR);
	Cursor northWestCursor = new Cursor(Cursor.NW_RESIZE_CURSOR);
	Cursor southWestCursor = new Cursor(Cursor.SW_RESIZE_CURSOR);
	Cursor southEastCursor = new Cursor(Cursor.SE_RESIZE_CURSOR);
	boolean startAutohideViewResize = false;

	boolean floatingFrameResize = false;
	int floatingFrameResizeEdge = NONE;

	private final RLayoutPane pane;

	Component cursorChangedComponent, cursorChangedParent;
	Point prevResizePoint = null;

	RGlobalEventListener(RLayoutPane pane) {
		this.pane = pane;
		normalCursor = pane.getCursor();
		this.pane.getToolkit().addAWTEventListener(this,
				AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
	}

	public void eventDispatched(AWTEvent event) {
		switch (event.getID()) {
		case MouseEvent.MOUSE_PRESSED:
			mousePressed((MouseEvent) event);
			break;
		case MouseEvent.MOUSE_RELEASED:
			mouseReleased((MouseEvent) event);
			break;
		case MouseEvent.MOUSE_MOVED:
			mouseMoved((MouseEvent) event);
			break;
		case MouseEvent.MOUSE_DRAGGED:
			mouseDragged((MouseEvent) event);
			break;
		case MouseEvent.MOUSE_ENTERED:
		case MouseEvent.MOUSE_EXITED:
			mouseEnteredOrExited((MouseEvent) event);
			break;
		case WindowEvent.WINDOW_GAINED_FOCUS:
			windowFocusGained((WindowEvent) event);
			break;
		case WindowEvent.WINDOW_LOST_FOCUS:
			windowFocusLost((WindowEvent) event);
			break;
		case FocusEvent.FOCUS_GAINED:
			focusGained((FocusEvent) event);
			break;
		}
	}

	private void focusGained(FocusEvent e) {
		activate(e, (RPane) getPane2(e));
	}

	private void mousePressed(MouseEvent e) {
		RFloatingFrame fPane = getFloatingFrame(e);
		if (fPane != null) {
			Point p = SwingUtilities.convertPoint(e.getComponent(),
					e.getPoint(), fPane);
			setFloatingFrameResizeEdge(p, fPane);
		}
		// auto hide items and context menus
		if (e.getComponent() instanceof RAutoHideItem
				|| e.getComponent() instanceof JMenuItem) {
			return;
		}
		// now determine the base that contains the component that was clicked
		activate(e, getPane(e));

		Component comp = getPane2(e);
		if ((comp instanceof RCombinedAutoHidePane)
				&& (pane.activeItem != null)) {
			RCombinedAutoHidePane ahPane = (RCombinedAutoHidePane) comp;
			Point p = SwingUtilities.convertPoint(e.getComponent(),
					e.getPoint(), ahPane);
			switch (pane.activeItem.getPosition()) {
			case RLayoutPane.TOP:
				if ((p.getY() > (ahPane.getHeight() - 10))
						&& (p.getY() < ahPane.getHeight()))
					startAutohideViewResize = true;
				break;
			case RLayoutPane.BOTTOM:
				if ((p.getY() < 10) && (p.getY() > 0))
					startAutohideViewResize = true;
				break;
			case RLayoutPane.LEFT:
				if ((p.getX() > (ahPane.getWidth() - 10))
						&& (p.getX() < ahPane.getWidth()))
					startAutohideViewResize = true;
				break;
			case RLayoutPane.RIGHT:
				if ((p.getX() < 10) && (p.getX() > 0))
					startAutohideViewResize = true;
				break;
			}
		}
	}

	private void activate(ComponentEvent e, RPane pane) {
		// if there's an active auto hide pane and we clicked somewhere else,
		// hide it
		if (this.pane.activeItem != null
				&& !(pane instanceof RCombinedAutoHidePane)) {
			this.pane.resetActiveAutoHideItem(this.pane.activeItem);
		}

		// if we found a pane, activate it
		if (pane != null) {
			// if the user clicked the pane's tabbed pane, he's about to change
			// the selected
			// window and that will generate an activate event anyhow so we'll
			// abort here not
			// to generate a superfluous wrong event.
			if (e.getComponent() == pane.getTabbedPane()) {
				return;
			}
			if (pane instanceof RWindowPane) {
				RDockableWindow w = ((RWindowPane) pane).getSelectedWindow();
				if (w != null) {
					w.activate();
				}
			}
		}
	}

	RPane getPane(MouseEvent e) {
		/*
		 * Component c = e.getComponent(); synchronized (c.getTreeLock()) { if
		 * (c instanceof JSplitPane) { c = ((JSplitPane)
		 * c).getComponentAt(e.getX(), e.getY()); return c instanceof RPane ?
		 * (RPane) c : null; } return (RPane) getPane2(e); }
		 */
		return (RPane) getPane2(e);
	}

	Component getPane2(ComponentEvent e) {
		Component c = e.getComponent();
		synchronized (c.getTreeLock()) {
			while (c != null) {
				if (c instanceof RPane) {
					if (c.getParent() instanceof RCombinedAutoHidePane) {
						return c.getParent();
					}
					return (RPane) c;
				}
				c = c.getParent();
			}
		}
		return null;
	}

	RFloatingFrame getFloatingFrame(ComponentEvent e) {
		Component c = e.getComponent();
		synchronized (c.getTreeLock()) {
			while (c != null) {
				if (c instanceof RFloatingFrame) {
					return (RFloatingFrame) c;
				}
				c = c.getParent();
			}
		}
		return null;
	}

	void mouseEnteredOrExited(MouseEvent e) {
		if (pane.activeItem != null && !pane.activeItem.autoHidePane.isActive()) {
			Point pt = SwingUtilities.convertPoint(e.getComponent(),
					e.getPoint(), pane.activeItem.autoHidePane);
			if (e.getID() == MouseEvent.MOUSE_ENTERED) {
				if (!this.pane.activeItem.mouseEnteredAutohidePane
						&& pane.activeItem.autoHidePane.contains(pt)) {
					this.pane.activeItem.mouseEnteredAutohidePane = true;
					this.pane.activeItem.paneEntered();
				}
			} else {
				if (this.pane.activeItem.mouseEnteredAutohidePane
						&& !pane.activeItem.autoHidePane.contains(pt)) {
					this.pane.activeItem.mouseEnteredAutohidePane = false;
					this.pane.activeItem.paneExited();
				}
			}
		}
	}

	public void mouseReleased(final java.awt.event.MouseEvent me) {
		Component comp = getPane2(me);
		startAutohideViewResize = false;
		floatingFrameResize = false;
		if (cursorChangedComponent != null) {
			cursorChangedComponent.setCursor(normalCursor);
			cursorChangedComponent = null;
		}
		if (cursorChangedParent != null) {
			cursorChangedParent.setCursor(normalCursor);
			cursorChangedParent = null;
		}
	}

	public Cursor findCursor(Point p, RFloatingFrame fPane) {
		Rectangle r = fPane.getBounds();
		// If the pointer is not in the outer edge return normal cursor
		Rectangle rect = new Rectangle(10, 10, (int) r.getWidth() - 20,
				(int) r.getHeight() - 20);
		if (rect.contains(p))
			return normalCursor;

		// Pointer is in the NorthWest edge
		// rect = new Rectangle(0,0,10, 10);
		// if (rect.contains(p)) return northWestCursor;

		// Pointer is in the NorthEast edge
		// rect = new Rectangle((int)r.getWidth()-10,0,10, 10);
		// if (rect.contains(p)) return northEastCursor;

		// Pointer is in the SouthWest edge
		// rect = new Rectangle(0,(int)r.getHeight()-10,10, 10);
		// if (rect.contains(p)) return southWestCursor;

		// Pointer is in the SouthEast edge
		rect = new Rectangle((int) r.getWidth() - 10, (int) r.getHeight() - 10,
				10, 10);
		if (rect.contains(p))
			return southEastCursor;

		// Pointer is in the North edge
		// rect = new Rectangle(10,0,(int)r.getWidth()-20, 10);
		// if (rect.contains(p)) return northCursor;

		// Pointer is in the South edge
		rect = new Rectangle(10, (int) r.getHeight() - 10,
				(int) r.getWidth() - 20, 10);
		if (rect.contains(p))
			return northCursor;

		// Pointer is in the West edge
		// rect = new Rectangle(0,10,10, (int)r.getHeight()-20);
		// if (rect.contains(p)) return eastCursor;

		// Pointer is in the East edge
		rect = new Rectangle((int) r.getWidth() - 10, 10, 10,
				(int) r.getHeight() - 20);
		if (rect.contains(p))
			return eastCursor;
		return normalCursor;
	}

	private void setFloatingFrameResizeEdge(Point p, RFloatingFrame fPane) {
		Rectangle r = fPane.getBounds();
		// If the pointer is not in the outer edge return normal cursor
		Rectangle rect = new Rectangle(10, 10, (int) r.getWidth() - 20,
				(int) r.getHeight() - 20);
		if (rect.contains(p)) {
			floatingFrameResize = false;
			floatingFrameResizeEdge = NONE;
			return;
		}
		// Pointer is in the NorthWest edge
		// rect = new Rectangle(0,0,10, 10);

		// Pointer is in the NorthEast edge
		// rect = new Rectangle((int)r.getWidth()-10,0,10, 10);

		// Pointer is in the SouthWest edge
		// rect = new Rectangle(0,(int)r.getHeight()-10,10, 10);

		// Pointer is in the SouthEast edge
		rect = new Rectangle((int) r.getWidth() - 10, (int) r.getHeight() - 10,
				10, 10);
		if (rect.contains(p)) {
			floatingFrameResize = true;
			floatingFrameResizeEdge = SOUTH_EAST;
			return;
		}

		// Pointer is in the North edge
		// rect = new Rectangle(10,0,(int)r.getWidth()-20, 10);

		// Pointer is in the South edge
		rect = new Rectangle(10, (int) r.getHeight() - 10,
				(int) r.getWidth() - 20, 10);
		if (rect.contains(p)) {
			floatingFrameResize = true;
			floatingFrameResizeEdge = SOUTH;
			return;
		}

		// Pointer is in the West edge
		// rect = new Rectangle(0,10,10, (int)r.getHeight()-20);

		// Pointer is in the East edge
		rect = new Rectangle((int) r.getWidth() - 10, 10, 10,
				(int) r.getHeight() - 20);
		if (rect.contains(p)) {
			floatingFrameResize = true;
			floatingFrameResizeEdge = EAST;
			return;
		}

		floatingFrameResize = false;
		floatingFrameResizeEdge = NONE;
	}

	private void resizeFloatingFrame(Point p, RFloatingFrame fPane) {
		if (prevResizePoint != null) {
			if (prevResizePoint.distance(p) < 5) {
				return;
			} else {
				prevResizePoint = p;
			}
		} else {
			prevResizePoint = p;
		}
		switch (floatingFrameResizeEdge) {
		case SOUTH:
			fPane.setSize(fPane.getWidth(), (int) p.getY());
			break;
		case EAST:
			fPane.setSize((int) p.getX(), fPane.getHeight());
			break;
		case SOUTH_EAST:
			fPane.setSize((int) p.getX(), (int) p.getY());
			break;
		}

	}

	public void mouseMoved(final java.awt.event.MouseEvent me) {
		RFloatingFrame fPane = getFloatingFrame(me);
		if (getFloatingFrame(me) != null) {
			if (me.getComponent() instanceof JButton) {
				fPane.setCursor(normalCursor);
				return;
			}
			Point p = SwingUtilities.convertPoint(me.getComponent(),
					me.getPoint(), fPane);
			fPane.setCursor(findCursor(p, fPane));
		}
		Component comp = getPane2(me);
		if ((comp instanceof RCombinedAutoHidePane)
				&& (pane.activeItem != null)) {
			RCombinedAutoHidePane ahPane = (RCombinedAutoHidePane) comp;
			Point p = SwingUtilities.convertPoint(me.getComponent(),
					me.getPoint(), ahPane);
			Cursor cursor = normalCursor;
			switch (pane.activeItem.getPosition()) {
			case RLayoutPane.TOP:
				if ((p.getY() > (ahPane.getHeight() - 10))
						&& (p.getY() < ahPane.getHeight())) {
					cursor = northCursor;
				}
				break;
			case RLayoutPane.BOTTOM:
				if ((p.getY() < 10) && (p.getY() > 0)) {
					cursor = northCursor;
				}
				break;
			case RLayoutPane.LEFT:
				if ((p.getX() > (ahPane.getWidth() - 10))
						&& (p.getX() < ahPane.getWidth())) {
					cursor = eastCursor;
				}
				break;
			case RLayoutPane.RIGHT:
				if ((p.getX() < 10) && (p.getX() > 0)) {
					cursor = eastCursor;
				}
				break;
			}
			ahPane.setCursor(cursor);
		}

	}

	public void mouseDragged(final java.awt.event.MouseEvent me) {
		RFloatingFrame fPane = getFloatingFrame(me);
		if (getFloatingFrame(me) != null) {
			Point p = SwingUtilities.convertPoint(me.getComponent(),
					me.getPoint(), fPane);
			fPane.setCursor(findCursor(p, fPane));
			if (floatingFrameResize)
				resizeFloatingFrame(p, fPane);
			fPane.invalidate();
			fPane.validate();
		}

		Component comp = getPane2(me);
		if ((comp instanceof RCombinedAutoHidePane)
				&& (pane.activeItem != null)) {
			if (startAutohideViewResize) {
				RCombinedAutoHidePane ahPane = (RCombinedAutoHidePane) comp;
				Point p = SwingUtilities.convertPoint(me.getComponent(),
						me.getPoint(), ahPane);
				int w = 200, h = 200, aw = 200;
				Cursor cursor = normalCursor;
				switch (pane.activeItem.getPosition()) {
				case RLayoutPane.TOP:
					ahPane.setSize(ahPane.getWidth(), (int) p.getY());
					pane.activeItem.setAutohideViewWidth(ahPane.getHeight());
					cursor = northCursor;
					break;
				case RLayoutPane.BOTTOM:
					ahPane.setBounds(ahPane.getX(),
							ahPane.getY() + ((int) p.getY()),
							ahPane.getWidth(),
							ahPane.getHeight() - ((int) p.getY()));
					pane.activeItem.setAutohideViewWidth(ahPane.getHeight());
					cursor = northCursor;
					break;
				case RLayoutPane.LEFT:
					ahPane.setSize((int) p.getX(), ahPane.getHeight());
					pane.activeItem.setAutohideViewWidth(ahPane.getWidth());
					cursor = eastCursor;
					break;
				case RLayoutPane.RIGHT:
					ahPane.setBounds(ahPane.getX() + ((int) p.getX()),
							ahPane.getY(),
							ahPane.getWidth() - ((int) p.getX()),
							ahPane.getHeight());
					pane.activeItem.setAutohideViewWidth(ahPane.getWidth());
					cursor = eastCursor;
					break;
				}
				if (cursorChangedComponent == null) {
					cursorChangedComponent = ahPane;
					cursorChangedComponent.setCursor(cursor);
				}
				if (cursorChangedParent == null) {
					cursorChangedParent = ahPane.getParent();
					cursorChangedParent.setCursor(cursor);
				}
				ahPane.validate();
			}
		}
	}

	WeakHashMap map = new WeakHashMap(); // TODO: big gun for a small bird

	void windowFocusGained(WindowEvent e) {
		Reference ref = (Reference) map.get(e.getWindow());
		RLayoutWindow lastFocussedWindow = ref == null ? null
				: (RLayoutWindow) ref.get();
		if (lastFocussedWindow != null) {
			// System.out.println("Refocus " + lastFocussedWindow.getName());
			lastFocussedWindow.activate();
			map.remove(e.getWindow());
		}
	}

	private void windowFocusLost(WindowEvent e) {
		if (this.pane.activeItem != null) {
			this.pane.resetActiveAutoHideItem(this.pane.activeItem);
		}
		RLayoutWindow lastFocussedWindow = this.pane.getActiveWindow();
		if (lastFocussedWindow != null) {
			this.pane.deactivateLastWindow(null);
			map.put(e.getWindow(), new WeakReference(lastFocussedWindow));
		}
	}
}