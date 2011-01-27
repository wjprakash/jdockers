package com.nayaware.jdockers.impl;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

/**
 * Top level Autohide pane container
 * 
 * @author Winston Prakash
 * @version 1.0
 */
public class RCombinedAutoHidePane extends RCombinedDockablePane {

	RAutoHideItem item;

	// constructors
	// ------------------------------------------------------------------------------

	RCombinedAutoHidePane(RAutoHideItem item) {
		this.item = item;

		pin.setIcon(RSwing.pin2);
		pin.setPressedIcon(RSwing.pin1);
		autoHideAction.putValue(Action.NAME, "Dock");

		setOpaque(true); // otherwise GTK look doesn't look right
		setBorder(PaneBorder.border);
	}

	// methods
	// -----------------------------------------------------------------------------------

	void addWindow(RDockableWindow window) {
		super.addWindow(window);
		if (firstWindow == null) {
			tabbedPane.setUI(new TablessTabbedPaneUI());
		}
	}

	// actions
	// -----------------------------------------------------------------------------------

	/**
	 * Redocks the window(s) of this pane.
	 */
	void pinRequest() {
		int position = item.getPosition();
		RLayoutPane layoutPane = getLayoutPane();
		layoutPane.removeAutoHideItem(item);
		RDockableWindow[] windows = getWindows();
		RDockableWindow selectedWindow = getSelectedWindow();
		layoutPane.getLayoutContainer().redockWindows(position, windows,
				selectedWindow);
	}

	// events
	// -----------------------------------------------------------------------------------

	/*
	 * If a window of the pane gets (programmatically) deactived, we hide the
	 * pane.
	 */
	void deactivateWindow(RDockableWindow window) {
		super.deactivateWindow(window);
		RLayoutPane layoutPane = getLayoutPane();
		if (layoutPane != null) {
			layoutPane.resetActiveAutoHideItem(item);
		}
	}

	// -------------------------------------------------------------------------------------------

	/*
	 * Overwritten to not call activateRequest. Changing the selected window
	 * shall not activate the pane.
	 */
	public void stateChanged(ChangeEvent e) {
		Component comp = tabbedPane.getSelectedComponent();
		if (comp != null) {
			setTitle(modelOf(comp).getTitle());
		}
	}

	void markWindow(RDockableWindow window) {
		window.markAsAutohidden();
	}

	void fireChangedNoActivate() {
	}

	void setSelectedIndex(int index) {
		super.setSelectedIndex(index);
		super.fireChangedNoActivate();
	}

	public void addNotify() {
		super.addNotify();
		lastSelectedWindow = getSelectedWindow();
		if (lastSelectedWindow != null) {
			lastSelectedWindow.fireShown();
		}
	}

	public void removeNotify() {
		super.removeNotify();
		if (lastSelectedWindow != null) {
			lastSelectedWindow.fireHidden();
			lastSelectedWindow = null;
		}
	}

	/*
	 * Called from floatRequest(). We remove the item which in turn will remove
	 * this pane.
	 */
	void removeFromParent() {
		getLayoutPane().removeAutoHideItem(item);
	}

	// -------------------------------------------------------------------------------------------

	/**
	 * This class removes border and tab display from a tabbedPane.
	 */
	static class TablessTabbedPaneUI extends BasicTabbedPaneUI {
		protected void installDefaults() {
			super.installDefaults();
			tabAreaInsets = contentBorderInsets = new Insets(0, 0, 0, 0);
		}

		public void paint(Graphics g, JComponent c) {
		}

		protected int calculateMaxTabHeight(int tabPlacement) {
			return 0;
		}

		protected int calculateTabHeight(int tabPlacement, int tabIndex,
				int fontHeight) {
			return 0;
		}
	}

	static class PaneBorder implements Border {
		static PaneBorder border = new PaneBorder();

		static Insets borderInsets = new Insets(1, 1, 1, 1);

		public boolean isBorderOpaque() {
			return true;
		}

		public void paintBorder(Component c, Graphics g, int x, int y,
				int width, int height) {
			g.setColor(c.getBackground().brighter());
			g.drawLine(x, y, x + width - 1, y);
			g.drawLine(x, y, x, y + height - 2);
			x += width - 1;
			y += height - 1;
			g.setColor(c.getBackground().darker());
			g.drawLine(x, y, x, y - height - 2);
			g.drawLine(x, y, x - width - 1, y);
		}

		public Insets getBorderInsets(Component c) {
			return borderInsets;
		}
	}
}
