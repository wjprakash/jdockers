package com.nayaware.jdockers.impl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * This class closely works together with <code>DnDSupport</code> and can
 * display a yellow drop zone indicator.
 * 
 * DnDSupport expects the following methods: setDropZone(), drop(),
 * getTitleHeight(), getTabbedPane();
 * 
 * @author Winston Prakash
 *  	   Stefan Matthias Aust
 * @version 1.0
 */
class DnDPanel extends JPanel {

	private int dropZone;

	protected DnDSupport dnd = new DnDSupport(this);

	public DnDPanel() {
		super();
	}

	public DnDPanel(LayoutManager layout) {
		super(layout);
	}

	/**
	 * Returns the drop zone. Center (0), Top (1), Left(2), Bottom (3), Right
	 * (4), Docking (5), or Tab Area (6).
	 */
	public int getDropZone() {
		return dropZone;
	}

	/**
	 * Sets the drop zone. Certain drop zones are highlighted.
	 * 
	 * @see #getDropZone()
	 */
	public void setDropZone(int zone) {
		if (dropZone != zone) {
			repaintDropZone();
			dropZone = zone;
			repaintDropZone();
		}
	}

	private void repaintDropZone() {
		if (dropZone != 0 && dropZone != 6) {
			int x = 2, y = 2;
			int w = Math.max(getWidth() / 3, 6);
			int h = Math.max(getHeight() / 3, 6);
			switch (dropZone) {
			case 3:
				y = getHeight() - h - y;
			case 1:
				w = getWidth() - 4;
				break;
			case 4:
				x = getWidth() - w - x;
			case 2:
				h = getHeight() - 4;
				break;
			case 5:
				w = getWidth() - 4;
				h = getHeight() - 4;
				break;
			default:
				throw new Error(); // should never happen
			}
			repaint(x, y, w, h);
		}
	}

	/**
	 * Paints a drop zone marker above all children.
	 */
	protected void paintChildren(Graphics g) {
		super.paintChildren(g);
		paintDropZone(g);
	}

	protected void paintDropZone(Graphics g) {
		if (dropZone != 0 && dropZone != 6) {
			int x = 2, y = 2;
			int w = Math.max(getWidth() / 3, 6);
			int h = Math.max(getHeight() / 3, 6);
			int ht = 0, wt = 0;
			switch (dropZone) {
			case 3:
				y = getHeight() - h - y;
			case 1:
				w = getWidth() - 4;
				break;
			case 4:
				x = getWidth() - w - x;
			case 2:
				h = getHeight() - 4;
				break;
			case 5:
				w = getWidth() - 4;
				h = getHeight() - 4;
				wt = Math.min(getWidth() / 4, 80);
				ht = getHeight() < 20 ? 0 : 20;
				break;
			default:
				throw new Error(); // should never happen
			}
			g.setColor(Color.yellow);
			w -= 1;
			h -= 1;
			for (int i = 0; i < 3; i++) {
				if (dropZone == 5) {
					int x2 = x + w;
					int y2 = y + h;
					int xt = x + wt;
					int yt = y2 - ht;
					g.drawLine(x, y, x2, y);
					g.drawLine(x, y, x, y2);
					g.drawLine(x, y2, xt, y2);
					g.drawLine(xt, y2, xt, yt);
					g.drawLine(xt, yt, x2, yt);
					g.drawLine(x2, yt, x2, y);
				} else {
					g.drawRect(x, y, w, h);
				}
				x += 1;
				y += 1;
				w -= 2;
				h -= 2;
				wt -= 2;
			}
		}
	}

	/**
	 * A Pane has been drop on this object. In case it was something with tabs
	 * and the drag operation started on a tab, "index" is that tab index.
	 * Otherwise, "index" is -1.
	 */
	public void drop(DnDPanel pane, int index) {
		if (dropZone != 6) {
			dockRequest(pane, index);
		}
	}

	/**
	 * Subclasses should overwrite this method to implement docking.
	 */
	protected void dockRequest(DnDPanel pane, int index) {
	}

	/**
	 * Subclasses should overwrite this if the panel has a title bar. This
	 * method must return something != 0 if dropZone == 5 should be possible.
	 */
	protected int getTitleHeight() {
		return 0;
	}

	/**
	 * Subclasses should overwrite this if the panel has a tabbed pane. This
	 * method must return something != null if dropZone == 6 should be possible.
	 * A reference to the tabbed pane is also required to drag single tabs.
	 */
	protected JTabbedPane getTabbedPane() {
		return null;
	}
}