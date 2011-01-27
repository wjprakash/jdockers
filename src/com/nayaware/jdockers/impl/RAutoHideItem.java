package com.nayaware.jdockers.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * An Auto Hide Item represents number of hidden Dockable Window inside an Auto
 * Hide Item Container. It displays icons for all windows and the title of the
 * selected window. You can mouse-over to change the selection. If you
 * mouse-over an icon or title, the window's content is displayed as an Auto
 * Hide Pane by the global Layout Pane.
 * 
 * @author Winston Prakash
 * @version 1.0
 */
public class RAutoHideItem extends JComponent implements ChangeListener,
		PropertyChangeListener {

	/**
	 * 90 degrees in radians
	 */
	static final double PI2 = Math.PI / 2d;

	/**
	 * Time delay in milliseconds before Autohide Pane opens
	 */
	static final int SHOW_DELAY = 333;

	/**
	 * Time delay in milliseconds before Autohide Pane closes
	 */
	static final int HIDE_DELAY = 333;

	/**
	 * Margin around the item. This is for the horizontal item, it will be
	 * automatically rotated for the vertical oriented item.
	 */
	static final Insets margin = new Insets(3, 3, 3, 3);

	/**
	 * Gap between icon and text.
	 */
	static final int gap = 4;

	/**
	 * Reference to the combined pane holding the windows.
	 */
	RCombinedAutoHidePane autoHidePane;

	/**
	 * <code>true</code> if the <code>RGlobalEventListener</code> that thinks
	 * the mouse is currently inside the Autohide Pane.
	 */
	boolean mouseEnteredAutohidePane;

	/**
	 * Timer to delay showing the Auto Hide Pane for 333 milliseconds to improve
	 * usability. This way, we don't mind if the mouse just touches the item for
	 * a few milliseconds.
	 */
	Timer showTimer = new Timer(SHOW_DELAY, new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (isValid()) {
				getLayoutPane().setActiveAutoHideItem(RAutoHideItem.this);
			}
		}
	});

	/**
	 * Timer to delay hiding the Auto Hide Pane for 333 milliseconds to improve
	 * usability. This way, we don't mind if the mouse leaves the item for a few
	 * milliseconds.
	 */
	Timer hideTimer = new Timer(HIDE_DELAY, new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (isValid()) {
				getLayoutPane().resetActiveAutoHideItem(RAutoHideItem.this);
			}
		}
	});

	// constructors
	// ------------------------------------------------------------------------------

	/**
	 * Constructs a new empty Auto Hide Item. You need to call addWindow() or
	 * addWindows().
	 */
	RAutoHideItem() {
		autoHidePane = new RCombinedAutoHidePane(this);

		showTimer.setRepeats(false);
		hideTimer.setRepeats(false);

		addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				if ((e.getModifiers() & (MouseEvent.BUTTON1_MASK
						| MouseEvent.BUTTON2_MASK | MouseEvent.BUTTON3_MASK)) != 0) {
					return;
				}
				paneEntered();
			}

			public void mouseExited(MouseEvent e) {
				if (mouseEnteredAutohidePane) {
					return;
				}
				paneExited();
			}

			public void mousePressed(MouseEvent e) {
				showTimer.stop();
				hideTimer.stop();
				getLayoutPane().setActiveAutoHideItem(RAutoHideItem.this);
				autoHidePane.getSelectedWindow().activate();
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				int pos = getOrientation() == RLayoutPane.HORIZONTAL ? e.getX()
						: e.getY();
				for (int i = 0; i < items.length; i++) {
					if (pos < items[i].dimension) {
						setIndex(i);
						return;
					}
				}
			}
		});
		setOpaque(true);
	}

	/**
	 * Constructs a new Auto Hide Item for the specified window.
	 */
	RAutoHideItem(RDockableWindow window) {
		this();
		window.fireHidden();
		addWindow(window);
	}

	/**
	 * Constructs a new Auto Hide Item for the specified set of windows.
	 */
	RAutoHideItem(RDockableWindow[] windows, RDockableWindow selectedWindow) {
		this();
		if (selectedWindow != null) {
			selectedWindow.fireHidden();
		}
		addWindows(windows);
		for (int i = 0; i < windows.length; i++) {
			if (windows[i] == selectedWindow) {
				setIndex(i);
				break;
			}
		}
	}

	// methods
	// -----------------------------------------------------------------------------------

	void addWindow(RDockableWindow window) {
		autoHidePane.addWindow(window);
		rebuildItems();
	}

	void addWindows(RDockableWindow[] windows) {
		autoHidePane.addWindows(windows);
		rebuildItems();
	}

	void removeWindow(RDockableWindow window) {
		autoHidePane.removeWindow(window);
		rebuildItems();
	}

	int getWindowCount() {
		return autoHidePane.getWindowCount();
	}

	/**
	 * Returns the item's layout pane.
	 */
	private RLayoutPane getLayoutPane() {
		return (RLayoutPane) getParent().getParent();
	}

	/**
	 * Returns the item's logical position.
	 */
	int getPosition() {
		return ((RAutoHideItemContainer) getParent()).getPosition();
	}

	void dispose() {
		removeListeners();
		autoHidePane.dispose();
		autoHidePane = null;
	}

	private static class Item {
		String title;
		Icon icon;
		int dimension;
		RDockableWindow window;

		Item(RDockableWindow window) {
			this.window = window;
			title = window.getTabName() != null ? window.getTabName() : window
					.getTitle() != null ? window.getTitle() : "";
			icon = window.getIcon() != null ? window.getIcon() : RSwing.pane;
		}
	}

	private Item[] items;

	private int selectedIndex;

	private JTabbedPane listeningTo;

	/**
	 * Returns the component's preferred size (including the specified margin).
	 */
	public Dimension getPreferredSize() {
		FontMetrics fm = getFontMetrics(getFont());
		int w, h;
		switch (getOrientation()) {
		case RLayoutPane.HORIZONTAL:
			w = 0;
			h = fm.getHeight();
			for (int i = 0; i < items.length; i++) {
				Item item = items[i];
				w += margin.left;
				w += item.icon.getIconWidth();
				if (i == selectedIndex) {
					w += gap;
					w += fm.stringWidth(item.title);
				}
				h = Math.max(h, item.icon.getIconHeight());
				w += margin.right;
			}
			h += margin.top;
			h += margin.bottom;
			break;

		case RLayoutPane.VERTICAL:
			w = fm.getHeight();
			h = 0;
			for (int i = 0; i < items.length; i++) {
				Item item = items[i];
				w = Math.max(w, item.icon.getIconWidth());
				h += margin.left;
				h += item.icon.getIconHeight();
				if (i == selectedIndex) {
					h += gap;
					h += fm.stringWidth(item.title);
				}
				h += margin.right;
			}
			w += margin.top;
			w += margin.bottom;
			break;

		default:
			throw new Error(); // should never happen
		}
		return new Dimension(w, h);
	}

	/**
	 * Paints the component. Notice that this sets the "dimension" Item property
	 * as a side effect.
	 */
	protected void paintComponent(Graphics g) {
		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		Color highlight = getBackground().brighter();
		Color shadow = getBackground().darker();
		switch (getOrientation()) {
		case RLayoutPane.HORIZONTAL:
			int x = margin.left,
			x0 = 0;
			for (int i = 0; i < items.length; i++) {
				Item item = items[i];
				items[i].icon.paintIcon(this, g, x,
						(getHeight() - item.icon.getIconHeight()) / 2);
				x += item.icon.getIconWidth();
				if (i == selectedIndex) {
					g.setColor(getForeground());
					g.setFont(getFont());
					FontMetrics fm = g.getFontMetrics();
					x += gap;
					g.drawString(item.title, x, (getHeight() - fm.getHeight())
							/ 2 + fm.getAscent());
					x += fm.stringWidth(item.title);
				}
				x += margin.right;
				items[i].dimension = x;
				g.setColor(highlight);
				g.fillRect(x0, 0, x - x0 - 1, 1);
				g.fillRect(x0, 1, 1, getHeight() - 1);
				g.setColor(shadow);
				g.fillRect(x - 1, 0, 1, getHeight() - 1);
				g.fillRect(x0 + 1, getHeight() - 1, x - x0 - 1, 1);
				x0 = x;
				x += margin.left;
			}
			break;

		case RLayoutPane.VERTICAL:
			int y = margin.left,
			y0 = 0;
			for (int i = 0; i < items.length; i++) {
				Item item = items[i];
				items[i].icon.paintIcon(this, g,
						(getWidth() - item.icon.getIconWidth()) / 2, y);
				y += item.icon.getIconHeight();
				if (i == selectedIndex) {
					g.setColor(getForeground());
					g.setFont(getFont());
					FontMetrics fm = g.getFontMetrics();
					y += gap;
					Graphics2D g2 = (Graphics2D) g;
					g2.rotate(PI2);
					g2.drawString(item.title, y, -fm.getDescent()
							- (getWidth() - fm.getHeight()) / 2);
					g2.rotate(-PI2);
					y += fm.stringWidth(item.title);
				}
				y += margin.right;
				items[i].dimension = y;
				g.setColor(highlight);
				g.fillRect(0, y0, getWidth() - 1, 1);
				g.fillRect(0, y0 + 1, 1, y - y0 - 1);
				g.setColor(shadow);
				g.fillRect(1, y - 1, getWidth() - 1, 1);
				g.fillRect(getWidth() - 1, y0, 1, y - y0 - 1);
				y0 = y;
				y += margin.left;
			}
			break;

		default:
			throw new Error(); // should never happen
		}
	}

	/**
	 * Sets the index of the selected window.
	 */
	void setIndex(int index) {
		if (this.selectedIndex != index) {
			this.selectedIndex = index;
			autoHidePane.setSelectedIndex(index);
			refresh();
		}
	}

	/*
	 * Some window property has changed so rebuild the internal item struct and
	 * refresh the item. We could probably be more clever here but why?
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		rebuildItems();
	}

	/*
	 * The Auto Hide Pane's TabbedPane has changed so synchronize the item's
	 * index. This, btw. triggers this event again and again sets the index but
	 * the recursion stopps because we're clever enough not to trigger new
	 * events to identical indices.
	 */
	public void stateChanged(ChangeEvent e) {
		int i = listeningTo.getSelectedIndex();
		if (i != -1) {
			setIndex(i);
		}
	}

	/*
	 * Rebuilds the internal item struct that caches the information used to
	 * paint and layout the item. We'll remove all listeners, rebuild the items
	 * and rewire all listeners. Then we'll refresh the display.
	 */
	private void rebuildItems() {
		removeListeners();
		RDockableWindow[] windows = autoHidePane.getWindows();
		items = new Item[windows.length];
		for (int i = 0; i < windows.length; i++) {
			items[i] = new Item(windows[i]);
			items[i].window.addPropertyChangeListener(this);
		}
		selectedIndex = Math.min(selectedIndex, items.length - 1);
		listeningTo = autoHidePane.getTabbedPane();
		if (listeningTo != null) {
			listeningTo.addChangeListener(this);
		}
		refresh();
	}

	/*
	 * We listen for property changes and selection changes so we need to remove
	 * these listeners to clean up. If we think, something might have changed,
	 * we simply call this method and rewire all listeners thereafter.
	 */
	private void removeListeners() {
		if (listeningTo != null) {
			listeningTo.removeChangeListener(this);
			listeningTo = null;
		}
		if (items != null) {
			for (int i = 0; i < items.length; i++) {
				items[i].window.removePropertyChangeListener(this);
			}
		}
	}

	/*
	 * The selected index has change so the component needs to be redrawn. We
	 * use invalidate() instead of repaint() as probably the the component's
	 * preferred size has also changed and so the item's container needs to
	 * revalidate its layout.
	 */
	private void refresh() {
		invalidate();
		if (getParent() != null) {
			getParent().validate();
			getParent().repaint();
		}
	}

	/*
	 * Returns HORIZONTAL or VERTICAL to determine the item's orientation. It
	 * would probably be better if the components would know its orientation
	 * itself...
	 */
	private int getOrientation() {
		if (getParent() != null) {
			int position = getPosition();
			if (position == RLayoutPane.LEFT || position == RLayoutPane.RIGHT) {
				return RLayoutPane.VERTICAL;
			}
		}
		return RLayoutPane.HORIZONTAL;
	}

	void paneEntered() {
		hideTimer.stop();
		if (getLayoutPane().activeItem != null) {
			getLayoutPane().setActiveAutoHideItem(this);
		} else {
			showTimer.start();
		}
	}

	public void paneExited() {
		showTimer.stop();
		if (!autoHidePane.isActive()) {
			hideTimer.start();
		}
	}

	/**
	 * Saves enough information into the specified memento to restore the layout
	 * tree hierachy.
	 */
	void saveLayout(RMemento memento) {
		RDockableWindow[] windows = autoHidePane.getWindows();
		for (int i = 0; i < windows.length; i++) {
			memento.createMemento("window").putString("name",
					windows[i].getName());
		}
		if (selectedIndex != -1) {
			memento.putString("selectedWindow",
					windows[selectedIndex].getName());
		}
	}

	void setAutohideViewWidth(int width) {
		autoHidePane.getSelectedWindow().autohidePaneSize = new Dimension(
				width, width);
		RLayoutPane p = getLayoutPane();
		if (p != null) {
			RMemento hint = p.getLayoutContainer().getLayoutHint(
					autoHidePane.getSelectedWindow());
			if (hint != null) {
				hint.putInteger("autohidePaneWidth", width);
				hint.putInteger("autohidePaneHeight", width);
			}
		}
	}

	// unit testing
	// ------------------------------------------------------------------------------

	public Object[] getTitlesAndIcons() {
		Object[] o = new Object[items.length * 2];
		for (int i = 0; i < items.length; i++) {
			o[i * 2] = items[i].icon;
			if (i == selectedIndex) {
				o[i * 2 + 1] = items[i].title;
			}
		}
		return o;
	}
}
