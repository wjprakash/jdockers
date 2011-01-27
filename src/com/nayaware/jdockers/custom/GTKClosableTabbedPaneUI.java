package com.nayaware.jdockers.custom;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

/**
 * This is a reformatted copy of
 * <code>javax.swing.plaf.basic.BasicTabbedPaneUI</code> from JDK 1.4.2 with the
 * following changes:
 * 
 * This class emulates the GTK look based on the <code>BasicTabbedPaneUI</code>
 * instead of the <code>SynthTabbedPaneUI</code> because that class isn't
 * accessible. It also fixes the wrong background color and adds correct button
 * hilighting to the scrollbuttons, something the original look doesn't do.
 * 
 * @author Winston Prakash
 * 		   Stefan Matthias Aust
 * @version 1.0
 */
public class GTKClosableTabbedPaneUI extends BasicClosableTabbedPaneUI {

	/**
	 * The color for unselected tabs and pressed buttons (90% of background
	 * color)
	 */
	protected Color unselectColor;

	/**
	 * The color for hovered components (110% of background color)
	 */
	protected Color selectHighlight;

	/**
	 * The tab the mouse is hovering right now.
	 */
	protected int tabHilightIndex = -1;

	/**
	 * Listener to hilight the tab the mouse is hovering, as required by the GTK
	 * look.
	 */
	protected MouseMotionListener tabHilightListener = new MouseMotionAdapter() {
		public void mouseMoved(MouseEvent e) {
			int index = tabPane.indexAtLocation(e.getX(), e.getY());
			if (index != tabHilightIndex) {
				repaintHighlightTab();
				tabHilightIndex = index;
				repaintHighlightTab();
			}
		}

		private void repaintHighlightTab() {
			if (tabHilightIndex >= 0 && tabHilightIndex < tabPane.getTabCount()) {
				tabPane.repaint(tabPane.getBoundsAt(tabHilightIndex));
			}
		}
	};

	/**
	 * Listener to hilight the scroll and close buttons the mouse is hovering,
	 * as GTK requires.
	 */
	protected MouseListener buttonHilightListener = new MouseAdapter() {
		private boolean inside, pressed;

		private void update(MouseEvent e) {
			e.getComponent()
					.setBackground(
							e.getComponent().isEnabled() && inside ? pressed ? unselectColor
									: selectHighlight
									: null);
		}

		public void mouseEntered(MouseEvent e) {
			inside = true;
			update(e);
		}

		public void mouseExited(MouseEvent e) {
			inside = false;
			update(e);
		}

		public void mousePressed(MouseEvent e) {
			pressed = true;
			update(e);
		}

		public void mouseReleased(MouseEvent e) {
			pressed = false;
			update(e);
		}
	};

	public static ComponentUI createUI(JComponent x) {
		return new GTKClosableTabbedPaneUI();
	}

	protected void installDefaults() {
		super.installDefaults();
		unselectColor = multiplyColor(
				UIManager.getColor("TabbedPane.background"), 0.912);
		selectHighlight = multiplyColor(
				UIManager.getColor("TabbedPane.background"), 1.09);
	}

	protected void installListeners() {
		super.installListeners();
		tabPane.addMouseMotionListener(tabHilightListener);
	}

	protected void uninstallListeners() {
		super.uninstallListeners();
		tabPane.removeMouseMotionListener(tabHilightListener);
	}

	/**
	 * Returns the background color of the specified tab. Takes selection state
	 * and the mouse hovering into account. New callback from
	 * BasicClosableTabbedPane.
	 */
	protected Color tabBackground(int tabIndex, boolean isSelected) {
		if (tabIndex == tabHilightIndex) {
			return selectHighlight;
		}
		return isSelected ? super.tabBackground(tabIndex, isSelected)
				: unselectColor;
	}

	/**
	 * Kind-of-hack callback from BasicClosableTabbedPane to add a hilight
	 * listener.
	 */
	protected void hookButtons(JButton b1, JButton b2, JButton b3) {
		b1.addMouseListener(buttonHilightListener);
		b2.addMouseListener(buttonHilightListener);
		b3.addMouseListener(buttonHilightListener);
	}

	/**
	 * Multiplies a color intensity by some factor. Similar to
	 * {@link Color#darker()} and {@link Color#brighter()} but more flexible.
	 */
	public static Color multiplyColor(Color color, double factor) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		if (factor > 1.0) {
			int i = (int) (1.0 / (factor - 1.0));
			if (r == 0 && g == 0 && b == 0) {
				return new Color(i, i, i, color.getAlpha());
			}
			r = Math.min((int) (Math.max(r, i) * factor), 255);
			g = Math.min((int) (Math.max(g, i) * factor), 255);
			b = Math.min((int) (Math.max(b, i) * factor), 255);
			return new Color(r, g, b, color.getAlpha());
		}
		return new Color((int) (r * factor), (int) (g * factor),
				(int) (b * factor), color.getAlpha());
	}
}
