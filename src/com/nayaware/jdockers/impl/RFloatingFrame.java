package com.nayaware.jdockers.impl;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * This class implements a separate floating frame that can show one or more
 * Dockable Windows.
 * 
 * @author Winston Prakash
 * 		   Stefan Matthias Aust
 * @version 1.0
 */
public class RFloatingFrame extends JDialog {

	RLayoutPane layoutPane;

	RFloatingDockablePane pane;

	class RFloatingDockablePane extends RCombinedDockablePane {

		RFloatingDockablePane() {
			super(RFloatingFrame.this);
			floatAction.putValue(Action.NAME, "Dock");
		}

		void floatRequest() {
			// see autohide pane for same implemenation
			RDockableWindow[] windows = getWindows();
			RDockableWindow selectedWindow = getSelectedWindow();

			for (int i = 0; i < windows.length; i++) {
				RFloatingFrame.this.removeWindow(windows[i]);
			}
			RFloatingFrame.this.dispose();

			getLayoutContainer().redockWindows(windows, selectedWindow);
		}

		RLayoutPane getLayoutPane() {
			return RFloatingFrame.this.layoutPane;
		}

		void removeFromParent() {
			RFloatingFrame.this.dispose();
		}

		void markWindow(RDockableWindow window) {
			window.markAsFloated(RFloatingFrame.this);
		}

		/*
		 * protected void paintChildren(Graphics g) { super.paintChildren(g);
		 * g.setColor(Color.yellow); g.fillRect(getWidth() / 2 - 2, 0, 4,
		 * getHeight()); g.fillRect(0, getHeight() / 2 - 2, getWidth(), 4); }
		 */
	}

	// constructors
	// ------------------------------------------------------------------------------

	RFloatingFrame(RLayoutPane layoutPane) {
		super((Frame) SwingUtilities.getWindowAncestor(layoutPane));
		setUndecorated(true);
		this.layoutPane = layoutPane;

		this.pane = new RFloatingDockablePane();
		RTiledContainer container = new RTiledContainer(RLayoutPane.NONE) {
			RLayoutPane getLayoutPane() {
				return RFloatingFrame.this.layoutPane;
			}
		};
		container.dockPaneAt(RLayoutPane.TOP, pane, null, 1.0);
		((JPanel) getContentPane()).setBorder(BorderFactory
				.createRaisedBevelBorder());
		getContentPane().add(container);

		addWindowListener(new WindowAdapter() {
			/* closing the dialog hides all shown windows */
			public void windowClosing(WindowEvent e) {
				hideRequest();
			}

			/* deactivating the dialog deactivates the selected window */
			public void windowDeactivated(WindowEvent e) {
				RFloatingFrame.this.layoutPane.deactivateLastWindow(null);
			}
		});
	}

	// actions
	// -----------------------------------------------------------------------------------

	void hideRequest() {
		int count = pane.getWindowCount();
		while (count-- > 0) {
			pane.hideRequest();
		}
	}

	// events
	// ------------------------------------------------------------------------------------

	public void activateWindow(RDockableWindow window) {
		pane.activateWindow(window);
	}

	public void deactivateWindow(RDockableWindow window) {
		pane.deactivateWindow(window);
	}

	// methods
	// -----------------------------------------------------------------------------------

	public void addWindow(RDockableWindow window) {
		pane.addWindow(window);
		updateFloatingBounds(window);
	}

	public void addWindows(RDockableWindow[] windows) {
		pane.addWindows(windows);
		for (int i = 0; i < windows.length; i++) {
			updateFloatingBounds(windows[i]);
		}
	}

	public void removeWindow(RDockableWindow window) {
		pane.removeWindow(window);
		// XXXwindow.setBounds(pane.getAbsoluteBounds());
		layoutPane.floatingWindows.remove(window);
	}

	public void removeWindowOrPane(RDockableWindow window) {
		removeWindow(window);
		if (getWindowCount() == 0) {
			dispose();
		}
	}

	public RDockableWindow[] getWindows() {
		return pane.getWindows();
	}

	public int getWindowCount() {
		return pane.getWindowCount();
	}

	public RDockableWindow getWindow(int index) {
		return pane.getWindow(index);
	}

	public RDockableWindow getSelectedWindow() {
		return pane.getSelectedWindow();
	}

	// -------------------------------------------------------------------------------------------

	public void dispose() {
		pane.dispose();
		super.dispose();
	}

	// -------------------------------------------------------------------------------------------
	/**
	 * Saves enough information into the specified memento to restore the layout
	 * tree hierachy.
	 */
	public void saveLayout(RMemento memento) {
		Insets insets = getInsets();
		Rectangle bounds = getBounds();
		bounds.x += insets.left;
		bounds.y += insets.top;
		bounds.width -= insets.left + insets.right;
		bounds.height -= insets.top + insets.bottom;
		memento.putInteger("x", bounds.x);
		memento.putInteger("y", bounds.y);
		memento.putInteger("width", bounds.width);
		memento.putInteger("height", bounds.height);
		pane.saveLayout(memento.createMemento("pane"));
	}

	public void setAbsoluteBounds(Rectangle bounds) {
		// to get the correct insets, we'll have to call this...
		// addNotify();
		Insets insets = new Insets(2, 2, 2, 2);

		// let's look for layout hints... and overwrite bounds if found
		for (int i = 0; i < getWindowCount(); i++) {
			RMemento hint = layoutPane.getLayoutContainer().getLayoutHint(
					getWindow(i));
			if (hint == null) {
				continue;
			}
			hint = hint.getChild("floatingBounds");
			if (hint == null) {
				continue;
			}
			Integer x = hint.getInteger("x");
			Integer y = hint.getInteger("y");
			Integer w = hint.getInteger("width");
			Integer h = hint.getInteger("height");
			if (x == null || y == null || w == null || h == null
					|| w.intValue() < 1 || h.intValue() < 1) {
				continue;
			}
			bounds = new Rectangle(x.intValue(), y.intValue(), w.intValue(),
					h.intValue());
			break;
		}

		// no layout hint bounds found, no bound given, pack and return
		if (bounds == null) {
			pack();
			return;
		}

		// otherwise, make sure the bounds fit the screen
		bounds = new Rectangle(bounds.x - insets.left, bounds.y - insets.top,
				bounds.width + insets.left + insets.right, bounds.height
						+ insets.top + insets.bottom);
		Dimension ss = getToolkit().getScreenSize();
		bounds.width = Math.min(bounds.width, ss.width);
		bounds.height = Math.min(bounds.height, ss.height);
		bounds.x = Math.max(0, Math.min(bounds.x, ss.width - bounds.width));
		bounds.y = Math.max(0, Math.min(bounds.y, ss.height - bounds.height));
		setBounds(bounds);
	}

	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		if (pane != null) {
			// filter out the very first setBounds calls before complete
			// initialisation
			for (int i = 0; i < getWindowCount(); i++) {
				updateFloatingBounds(getWindow(i));
			}
		}
	}

	private void updateFloatingBounds(RDockableWindow window) {
		if (isShowing()) {
			layoutPane.getLayoutContainer().updateFloatingFrameDimensions(
					window);
		}
	}
}
